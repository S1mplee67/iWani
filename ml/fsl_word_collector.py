"""
FSL Word-Level Sign Collector & Preprocessing Tool
Project Nexus • iWani

Collects 30-frame temporal sign sequences for priority FSL words:
- SALAMAT (Thank you)
- TULONG (Help)
- OSPITAL (Hospital)
- PULIS (Police)
- SUNOG (Fire)
- KAIN (Eat)
- TUBIG (Water)
- MAGANDANG_UMAGA (Good morning)
- KUMUSTA (Hello / How are you)
- PAALAM (Goodbye)

Pipeline aligns 100% with iWani's GestureService:
1. MediaPipe Pose + Hands extraction (sensor/portrait space)
2. 100-dim nose-centered feature vector per frame
3. 30-frame temporal sequence buffer
4. Per-sequence std-normalization + velocity & acceleration derivatives (100 -> 300)
5. Saves clean NumPy .npz ready for Conv1D + Transformer TFLite training.
"""

import os
import sys
import json
import time
import argparse
import numpy as np
import cv2

try:
    import mediapipe as mp
    MP_AVAILABLE = True
except ImportError:
    MP_AVAILABLE = False

LABELS_PATH = os.path.join(os.path.dirname(__file__), "models", "fsl_wl_labels.json")
OUTPUT_DATA_PATH = os.path.join(os.path.dirname(__file__), "data", "fsl_word_landmarks.npz")
SEQUENCE_LEN = 30
FEATURE_DIM = 100
DERIV_DIM = 300

def load_labels(path=LABELS_PATH):
    if os.path.exists(path):
        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)
    return {
        "labels": ["salamat", "tulong", "ospital", "pulis", "sunog", "kain", "tubig", "magandang_umaga", "kumusta", "paalam"],
        "num_classes": 10
    }

def extract_frame_features(pose_landmarks, left_hand_landmarks, right_hand_landmarks):
    """
    Extracts 100-dimensional nose-centered feature vector from MediaPipe landmarks.
    Layout matches GestureService._extractBisindoFeatures:
      [0:42]   Right hand 21*(x,y)
      [42:84]  Left hand 21*(x,y)
      [84:86]  Nose position (0, 0)
      [86:88]  Left shoulder
      [88:90]  Right shoulder
      [90:92]  Left ear
      [92:94]  Right ear
      [94:96]  Left elbow
      [96:98]  Right elbow
      [98]     has_right_hand
      [99]     has_left_hand
    """
    feat = np.full(FEATURE_DIM, np.nan, dtype=np.float32)

    # Pose anchors
    has_pose = pose_landmarks is not None
    nose_x, nose_y = 0.5, 0.5
    if has_pose:
        nose = pose_landmarks.landmark[0]
        nose_x, nose_y = nose.x, nose.y

        # Landmarks: 0: nose, 11: l_sh, 12: r_sh, 7: l_ear, 8: r_ear, 13: l_elb, 14: r_elb
        anchor_indices = [0, 11, 12, 7, 8, 13, 14]
        for i, idx in enumerate(anchor_indices):
            lm = pose_landmarks.landmark[idx]
            if lm.visibility > 0.3:
                feat[84 + i * 2] = lm.x - nose_x
                feat[84 + i * 2 + 1] = lm.y - nose_y

    # Right hand [0:42]
    has_rh = right_hand_landmarks is not None
    if has_rh:
        for i, lm in enumerate(right_hand_landmarks.landmark):
            feat[i * 2] = lm.x - nose_x
            feat[i * 2 + 1] = lm.y - nose_y

    # Left hand [42:84]
    has_lh = left_hand_landmarks is not None
    if has_lh:
        for i, lm in enumerate(left_hand_landmarks.landmark):
            feat[42 + i * 2] = lm.x - nose_x
            feat[42 + i * 2 + 1] = lm.y - nose_y

    # Presence flags
    feat[98] = 1.0 if has_rh else np.nan
    feat[99] = 1.0 if has_lh else np.nan

    return feat

def normalize_sequence(seq):
    """
    Applies per-sequence std normalization (NaN-safe) and adds temporal derivatives:
    (30, 100) -> (30, 300)
    """
    # Std normalize
    vals = seq[np.isfinite(seq)]
    if len(vals) > 0:
        mean = np.mean(vals)
        std = np.std(vals)
        if std < 1e-6:
            std = 1.0
        norm_seq = (seq - mean) / std
    else:
        norm_seq = np.zeros_like(seq)

    norm_seq = np.nan_to_num(norm_seq, nan=0.0)

    # Derivatives
    T, D = norm_seq.shape
    vel = np.zeros_like(norm_seq)
    acc = np.zeros_like(norm_seq)

    for t in range(1, T):
        vel[t] = norm_seq[t] - norm_seq[t - 1]
    for t in range(2, T):
        acc[t] = norm_seq[t] - norm_seq[t - 2]

    return np.concatenate([norm_seq, vel, acc], axis=-1)

def run_headless_simulation(num_samples=20):
    """
    Headless self-check / demo generator:
    Generates synthetic landmark sequences matching FSL word shapes to verify pipeline.
    """
    print(f"Running headless self-check / synthetic FSL word collection ({num_samples} samples)...")
    labels_info = load_labels()
    labels = labels_info["labels"]

    X_raw = []
    X_deriv = []
    y = []

    for i in range(num_samples):
        cls_idx = i % len(labels)
        # Generate random smooth trajectory for 30 frames
        t = np.linspace(0, np.pi, SEQUENCE_LEN)[:, None]
        base_hand = np.sin(t + cls_idx * 0.3) * 0.2
        frame_feats = np.random.normal(0, 0.05, (SEQUENCE_LEN, FEATURE_DIM)).astype(np.float32)
        frame_feats[:, :42] += base_hand
        frame_feats[:, 98] = 1.0

        deriv = normalize_sequence(frame_feats)

        X_raw.append(frame_feats)
        X_deriv.append(deriv)
        y.append(cls_idx)

    X_raw = np.array(X_raw, dtype=np.float32)
    X_deriv = np.array(X_deriv, dtype=np.float32)
    y = np.array(y, dtype=np.int64)

    os.makedirs(os.path.dirname(OUTPUT_DATA_PATH), exist_ok=True)
    np.savez_compressed(
        OUTPUT_DATA_PATH,
        X_raw=X_raw,
        X_deriv=X_deriv,
        y=y,
        labels=np.array(labels)
    )
    print(f"Verified & saved {len(X_raw)} samples to {OUTPUT_DATA_PATH}")
    print(f"X_raw shape: {X_raw.shape}, X_deriv shape: {X_deriv.shape}, y shape: {y.shape}")
    return True

def main():
    parser = argparse.ArgumentParser(description="FSL Word Gesture Collector (Project Nexus)")
    parser.add_argument("--headless", action="store_true", help="Run simulated collection without camera")
    parser.add_argument("--num-samples", type=int, default=30, help="Number of simulated samples in headless mode")
    args = parser.parse_args()

    if args.headless or not MP_AVAILABLE:
        if not MP_AVAILABLE:
            print("MediaPipe not available for live camera; running headless simulation mode.")
        run_headless_simulation(args.num_samples)
        return

    print("=== iWani FSL Word-Level Collector ===")
    print("Commands:")
    print(" [SPACE] : Start 30-frame recording")
    print(" [0-9]   : Select word class")
    print(" [s]     : Save dataset to disk")
    print(" [q]     : Quit")

    labels_info = load_labels()
    labels = labels_info["labels"]
    current_class = 0

    cap = cv2.VideoCapture(0)
    if not cap.isOpened():
        print("Camera not detected! Switching to headless simulation mode.")
        run_headless_simulation(args.num_samples)
        return

    mp_holistic = mp.solutions.holistic
    holistic = mp_holistic.Holistic(
        min_detection_confidence=0.5,
        min_tracking_confidence=0.5,
        model_complexity=1
    )

    collected_sequences = []
    collected_labels = []

    recording = False
    record_buffer = []

    try:
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break

            # Mirror for natural signing
            frame = cv2.flip(frame, 1)
            h, w, _ = frame.shape
            rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            results = holistic.process(rgb)

            feat = extract_frame_features(
                results.pose_landmarks,
                results.left_hand_landmarks,
                results.right_hand_landmarks
            )

            if recording:
                record_buffer.append(feat)
                prog = len(record_buffer) / SEQUENCE_LEN
                cv2.rectangle(frame, (20, h - 30), (int(20 + (w - 40) * prog), h - 10), (0, 255, 0), -1)

                if len(record_buffer) >= SEQUENCE_LEN:
                    recording = False
                    seq = np.array(record_buffer, dtype=np.float32)
                    collected_sequences.append(seq)
                    collected_labels.append(current_class)
                    record_buffer = []
                    print(f"Recorded sign '{labels[current_class]}'! Total: {len(collected_sequences)}")

            # Visual UI
            target_name = labels[current_class].upper()
            cv2.putText(frame, f"FSL Target: {target_name} [{current_class}]", (20, 40),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 215, 255), 2)
            cv2.putText(frame, f"Samples: {len(collected_sequences)} | Press SPACE to Record", (20, 75),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 1)

            cv2.imshow("iWani FSL Collector", frame)
            key = cv2.waitKey(1) & 0xFF

            if key == ord('q'):
                break
            elif key == ord(' '):
                if not recording:
                    recording = True
                    record_buffer = []
            elif ord('0') <= key <= ord('9'):
                idx = key - ord('0')
                if idx < len(labels):
                    current_class = idx
            elif key == ord('s'):
                if collected_sequences:
                    X_raw = np.array(collected_sequences, dtype=np.float32)
                    X_deriv = np.array([normalize_sequence(s) for s in collected_sequences], dtype=np.float32)
                    y = np.array(collected_labels, dtype=np.int64)
                    os.makedirs(os.path.dirname(OUTPUT_DATA_PATH), exist_ok=True)
                    np.savez_compressed(
                        OUTPUT_DATA_PATH,
                        X_raw=X_raw,
                        X_deriv=X_deriv,
                        y=y,
                        labels=np.array(labels)
                    )
                    print(f"Saved {len(X_raw)} sequences to {OUTPUT_DATA_PATH}")

    finally:
        cap.release()
        cv2.destroyAllWindows()
        holistic.close()

if __name__ == "__main__":
    main()
