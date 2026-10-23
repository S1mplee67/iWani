#!/usr/bin/env python3
"""
train_alphabet_model.py — Train FSL Alphabet Classifier and Export TFLite
========================================================================
1. Loads extracted landmarks from ml/data/fsl_alphabet_landmarks.npz
2. Trains a 5-layer MLP (42 -> 512 -> 256 -> 128 -> 64 -> 26) with ReLU and Softmax
3. Evaluates accuracy on 20% held-out test split (target >= 85%)
4. Exports compliant TFLite model to ml/models/fsl_alphabet.tflite
   and copies to iwani_app/assets/models/fsl_alphabet.tflite
"""

import os
import json
import struct
import shutil
import numpy as np
from sklearn.neural_network import MLPClassifier
from sklearn.model_selection import train_test_split

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA_PATH = os.path.join(REPO_ROOT, "ml", "data", "fsl_alphabet_landmarks.npz")
BASE_TEMPLATE_PATH = os.path.join(REPO_ROOT, "assets", "models", "sibi_alphabet_model_f32.tflite")
OUTPUT_TFLITE = os.path.join(REPO_ROOT, "ml", "models", "fsl_alphabet.tflite")
ASSET_TFLITE = os.path.join(REPO_ROOT, "assets", "models", "fsl_alphabet.tflite")
LABELS_JSON = os.path.join(REPO_ROOT, "ml", "models", "fsl_alphabet_labels.json")
ASSET_LABELS_JSON = os.path.join(REPO_ROOT, "assets", "models", "fsl_alphabet_labels.json")

def train_and_export():
    print(f"Loading dataset from {DATA_PATH}...")
    data = np.load(DATA_PATH)
    X = data['features']
    y = data['labels']
    num_classes = len(np.unique(y))
    print(f"Dataset: {len(X)} samples, feature_dim: {X.shape[1]}, classes: {num_classes}")

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    print("Training 5-layer MLP classifier on FSL landmarks...")
    clf = MLPClassifier(
        hidden_layer_sizes=(512, 256, 128, 64),
        activation='relu',
        max_iter=250,
        random_state=42,
        early_stopping=True,
        validation_fraction=0.1
    )
    clf.fit(X_train, y_train)

    val_acc = clf.score(X_test, y_test)
    print(f"Validation accuracy on held-out test split: {val_acc * 100:.2f}%")
    assert val_acc >= 0.85, f"Validation accuracy {val_acc:.2f} is below 85% requirement!"

    # Weights:
    # W0: (42, 512) -> TFLite FC weight: (512, 42)
    # B0: (512,)
    # W1: (512, 256) -> TFLite FC weight: (256, 512)
    # B1: (256,)
    # W2: (256, 128) -> TFLite FC weight: (128, 256)
    # B2: (128,)
    # W3: (128, 64) -> TFLite FC weight: (64, 128)
    # B3: (64,)
    # W4: (64, 26) -> TFLite FC weight: (26, 64)
    # B4: (26,)

    w0 = clf.coefs_[0].T.astype(np.float32)
    b0 = clf.intercepts_[0].astype(np.float32)
    w1 = clf.coefs_[1].T.astype(np.float32)
    b1 = clf.intercepts_[1].astype(np.float32)
    w2 = clf.coefs_[2].T.astype(np.float32)
    b2 = clf.intercepts_[2].astype(np.float32)
    w3 = clf.coefs_[3].T.astype(np.float32)
    b3 = clf.intercepts_[3].astype(np.float32)
    w4 = clf.coefs_[4].T.astype(np.float32)
    b4 = clf.intercepts_[4].astype(np.float32)

    print("Building compliant TFLite FlatBuffer with FSL weights...")
    # Use flatbuffers to build the clean model
    import flatbuffers

    builder = flatbuffers.Builder(1024 * 1024)

    # 1. Buffers (Buffer 0 is empty)
    # In TFLite buffer table:
    # buffer 0: empty
    # buffer 1: empty (input tensor 0)
    # buffer 2: w3 (64, 128)
    # buffer 3: b3 (64,)
    # buffer 4: w2 (128, 256)
    # buffer 5: b2 (128,)
    # buffer 6: w1 (256, 512)
    # buffer 7: b1 (256,)
    # buffer 8: w4 (26, 64)
    # buffer 9: b4 (26,)
    # buffer 10: b0 (512,)
    # buffer 11: w0 (512, 42)
    # buffer 12..17: intermediate activation tensors (empty)
    # buffer 18, 19: metadata
    raw_buffers = [
        b"",
        b"",
        w3.tobytes(),
        b3.tobytes(),
        w2.tobytes(),
        b2.tobytes(),
        w1.tobytes(),
        b1.tobytes(),
        w4.tobytes(),
        b4.tobytes(),
        b0.tobytes(),
        w0.tobytes(),
        b"",
        b"",
        b"",
        b"",
        b"",
        b"",
    ]

    buffer_offsets = []
    for raw in raw_buffers:
        if len(raw) == 0:
            builder.StartObject(1)
            buf_obj = builder.EndObject()
        else:
            builder.StartVector(1, len(raw), 1)
            for byte in reversed(raw):
                builder.PrependByte(byte)
            data_vec = builder.EndVector()
            builder.StartObject(1)
            builder.PrependUOffsetTRelativeSlot(0, data_vec, 0)
            buf_obj = builder.EndObject()
        buffer_offsets.append(buf_obj)

    builder.StartVector(4, len(buffer_offsets), 4)
    for bo in reversed(buffer_offsets):
        builder.PrependUOffsetTRelative(bo)
    buffers_vec = builder.EndVector()

    # 2. Operator codes: 0 = FULLY_CONNECTED (9), 1 = SOFTMAX (25)
    op_code_offsets = []
    for code in [9, 25]:
        builder.StartObject(4)
        builder.PrependInt8Slot(0, code, 0)
        builder.PrependInt32Slot(2, 1, 1) # version
        builder.PrependInt32Slot(3, code, 0)
        op_code_offsets.append(builder.EndObject())

    builder.StartVector(4, len(op_code_offsets), 4)
    for opc in reversed(op_code_offsets):
        builder.PrependUOffsetTRelative(opc)
    op_codes_vec = builder.EndVector()

    # 3. Builtin Options
    # FC ReLU options
    builder.StartObject(4)
    builder.PrependInt8Slot(0, 1, 0) # fused_activation: 1 = RELU
    fc_relu_opt = builder.EndObject()

    # FC None options
    builder.StartObject(4)
    builder.PrependInt8Slot(0, 0, 0) # fused_activation: 0 = NONE
    fc_none_opt = builder.EndObject()

    # Softmax options
    builder.StartObject(1)
    builder.PrependFloat32Slot(0, 1.0, 1.0) # beta = 1.0
    softmax_opt = builder.EndObject()

    # 4. Operators
    # Op 0: FC ReLU (in: [0, 10, 9], out: [11]) -> wait: weight is tensor 10, bias is tensor 9
    # Op 1: FC ReLU (in: [11, 5, 6], out: [12])
    # Op 2: FC ReLU (in: [12, 3, 4], out: [13])
    # Op 3: FC ReLU (in: [13, 1, 2], out: [14])
    # Op 4: FC None (in: [14, 7, 8], out: [15])
    # Op 5: Softmax (in: [15], out: [16])
    operators_def = [
        (0, [0, 10, 9], [11], 8, fc_relu_opt),
        (0, [11, 5, 6], [12], 8, fc_relu_opt),
        (0, [12, 3, 4], [13], 8, fc_relu_opt),
        (0, [13, 1, 2], [14], 8, fc_relu_opt),
        (0, [14, 7, 8], [15], 8, fc_none_opt),
        (1, [15], [16], 9, softmax_opt),
    ]

    operator_offsets = []
    for opcode_idx, in_indices, out_indices, opt_type, opt_table in operators_def:
        # inputs vector
        builder.StartVector(4, len(in_indices), 4)
        for idx in reversed(in_indices):
            builder.PrependInt32(idx)
        in_vec = builder.EndVector()

        # outputs vector
        builder.StartVector(4, len(out_indices), 4)
        for idx in reversed(out_indices):
            builder.PrependInt32(idx)
        out_vec = builder.EndVector()

        builder.StartObject(5)
        builder.PrependUint32Slot(0, opcode_idx, 0)
        builder.PrependUOffsetTRelativeSlot(1, in_vec, 0)
        builder.PrependUOffsetTRelativeSlot(2, out_vec, 0)
        builder.PrependUint8Slot(3, opt_type, 0)
        builder.PrependUOffsetTRelativeSlot(4, opt_table, 0)
        operator_offsets.append(builder.EndObject())

    builder.StartVector(4, len(operator_offsets), 4)
    for op in reversed(operator_offsets):
        builder.PrependUOffsetTRelative(op)
    operators_vec = builder.EndVector()

    # 5. Tensors (17 tensors)
    # T0: shape=[1, 42], buffer=1, name="serving_default_input:0"
    # T1: shape=[64, 128], buffer=2, name="fc3_weight"
    # T2: shape=[64], buffer=3, name="fc3_bias"
    # T3: shape=[128, 256], buffer=4, name="fc2_weight"
    # T4: shape=[128], buffer=5, name="fc2_bias"
    # T5: shape=[256, 512], buffer=6, name="fc1_weight"
    # T6: shape=[256], buffer=7, name="fc1_bias"
    # T7: shape=[26, 64], buffer=8, name="fc4_weight"
    # T8: shape=[26], buffer=9, name="fc4_bias"
    # T9: shape=[512], buffer=10, name="fc0_bias"
    # T10: shape=[512, 42], buffer=11, name="fc0_weight"
    # T11: shape=[1, 512], buffer=12, name="act0"
    # T12: shape=[1, 256], buffer=13, name="act1"
    # T13: shape=[1, 128], buffer=14, name="act2"
    # T14: shape=[1, 64], buffer=15, name="act3"
    # T15: shape=[1, 26], buffer=16, name="logits"
    # T16: shape=[1, 26], buffer=17, name="StatefulPartitionedCall:0"

    tensors_def = [
        ([1, 42], 1, "input"),
        ([64, 128], 2, "fc3_w"),
        ([64], 3, "fc3_b"),
        ([128, 256], 4, "fc2_w"),
        ([128], 5, "fc2_b"),
        ([256, 512], 6, "fc1_w"),
        ([256], 7, "fc1_b"),
        ([26, 64], 8, "fc4_w"),
        ([26], 9, "fc4_b"),
        ([512], 10, "fc0_b"),
        ([512, 42], 11, "fc0_w"),
        ([1, 512], 12, "act0"),
        ([1, 256], 13, "act1"),
        ([1, 128], 14, "act2"),
        ([1, 64], 15, "act3"),
        ([1, 26], 16, "logits"),
        ([1, 26], 17, "output"),
    ]

    tensor_offsets = []
    for sh, b_idx, name in tensors_def:
        builder.StartVector(4, len(sh), 4)
        for s in reversed(sh):
            builder.PrependInt32(s)
        shape_vec = builder.EndVector()
        name_str = builder.CreateString(name)

        builder.StartObject(6)
        builder.PrependUOffsetTRelativeSlot(0, shape_vec, 0)
        builder.PrependInt8Slot(1, 0, 0) # TensorType: 0 = FLOAT32
        builder.PrependUint32Slot(2, b_idx, 0)
        builder.PrependUOffsetTRelativeSlot(3, name_str, 0)
        tensor_offsets.append(builder.EndObject())

    builder.StartVector(4, len(tensor_offsets), 4)
    for to in reversed(tensor_offsets):
        builder.PrependUOffsetTRelative(to)
    tensors_vec = builder.EndVector()

    # SubGraph inputs and outputs
    builder.StartVector(4, 1, 4)
    builder.PrependInt32(0) # tensor 0 is input
    sg_inputs = builder.EndVector()

    builder.StartVector(4, 1, 4)
    builder.PrependInt32(16) # tensor 16 is output
    sg_outputs = builder.EndVector()

    sg_name = builder.CreateString("main")

    builder.StartObject(5)
    builder.PrependUOffsetTRelativeSlot(0, tensors_vec, 0)
    builder.PrependUOffsetTRelativeSlot(1, sg_inputs, 0)
    builder.PrependUOffsetTRelativeSlot(2, sg_outputs, 0)
    builder.PrependUOffsetTRelativeSlot(3, operators_vec, 0)
    builder.PrependUOffsetTRelativeSlot(4, sg_name, 0)
    subgraph_obj = builder.EndObject()

    builder.StartVector(4, 1, 4)
    builder.PrependUOffsetTRelative(subgraph_obj)
    subgraphs_vec = builder.EndVector()

    desc_str = builder.CreateString("iWani FSL Alphabet Classifier (Project Nexus)")

    # Root Model table
    builder.StartObject(5)
    builder.PrependUint32Slot(0, 3, 0) # version = 3
    builder.PrependUOffsetTRelativeSlot(1, op_codes_vec, 0)
    builder.PrependUOffsetTRelativeSlot(2, subgraphs_vec, 0)
    builder.PrependUOffsetTRelativeSlot(3, desc_str, 0)
    builder.PrependUOffsetTRelativeSlot(4, buffers_vec, 0)
    model_obj = builder.EndObject()

    builder.Finish(model_obj, b"TFL3")
    tflite_bytes = builder.Output()

    os.makedirs(os.path.dirname(OUTPUT_TFLITE), exist_ok=True)
    with open(OUTPUT_TFLITE, "wb") as f:
        f.write(tflite_bytes)
    print(f"Exported FSL TFLite model to {OUTPUT_TFLITE} ({len(tflite_bytes) / 1024:.1f} KB)")

    os.makedirs(os.path.dirname(ASSET_TFLITE), exist_ok=True)
    with open(ASSET_TFLITE, "wb") as f:
        f.write(tflite_bytes)
    print(f"Copied FSL TFLite model to {ASSET_TFLITE}")

    # Copy labels
    letters = [chr(ord('A') + i) for i in range(26)]
    with open(LABELS_JSON, "w", encoding="utf-8") as f:
        json.dump(letters, f, indent=2)
    with open(ASSET_LABELS_JSON, "w", encoding="utf-8") as f:
        json.dump(letters, f, indent=2)
    print(f"Exported FSL 26-class labels to {LABELS_JSON} and {ASSET_LABELS_JSON}")

if __name__ == "__main__":
    train_and_export()
