#!/usr/bin/env python3
"""
extract_landmarks.py — Extract MediaPipe Hand Landmarks from FSL Dataset
========================================================================
Processes static images in 'FSL datasets/A' ... 'FSL datasets/Z'.
Extracts 21 2D landmarks per image using MediaPipe Tasks HandLandmarker.
Applies Bohacek bounding-box normalization yielding 42 floats per sample.
Saves extracted features and labels to ml/data/fsl_alphabet_landmarks.npz
and ml/models/fsl_alphabet_labels.json.
"""

import os
import json
import string
import numpy as np
import mediapipe as mp
from mediapipe.tasks import python
from mediapipe.tasks.python import vision

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATASET_DIR = os.path.join(REPO_ROOT, "FSL datasets")
MODEL_TASK_PATH = os.path.join(REPO_ROOT, "ml", "hand_landmarker.task")
OUTPUT_NPZ = os.path.join(REPO_ROOT, "ml", "data", "fsl_alphabet_landmarks.npz")
OUTPUT_LABELS_JSON = os.path.join(REPO_ROOT, "ml", "models", "fsl_alphabet_labels.json")
ASSET_LABELS_JSON = os.path.join(REPO_ROOT, "assets", "models", "fsl_alphabet_labels.json")

def normalize_landmarks_21(landmarks_21) -> np.ndarray:
    coords = np.array([[lm.x, lm.y] for lm in landmarks_21], dtype=np.float32)
    min_x, min_y = coords[:, 0].min(), coords[:, 1].min()
    max_x, max_y = coords[:, 0].max(), coords[:, 1].max()
    w = max(max_x - min_x, 1e-6)
    h = max(max_y - min_y, 1e-6)
    norm = np.zeros_like(coords)
    norm[:, 0] = (coords[:, 0] - min_x) / w
    norm[:, 1] = (coords[:, 1] - min_y) / h
    return norm.flatten() # 42 floats

def extract(max_per_class: int = 150):
    print(f"Loading HandLandmarker from {MODEL_TASK_PATH}...")
    base_options = python.BaseOptions(model_asset_path=MODEL_TASK_PATH)
    options = vision.HandLandmarkerOptions(
        base_options=base_options,
        num_hands=1,
        min_hand_detection_confidence=0.1,
        min_hand_presence_confidence=0.1,
    )
    detector = vision.HandLandmarker.create_from_options(options)

    letters = list(string.ascii_uppercase)
    all_features = []
    all_labels = []

    print(f"Extracting landmarks from {DATASET_DIR} (max {max_per_class} per class)...")
    for class_idx, letter in enumerate(letters):
        folder = os.path.join(DATASET_DIR, letter)
        if not os.path.isdir(folder):
            continue
        
        files = [f for f in os.listdir(folder) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
        if max_per_class > 0:
            files = files[:max_per_class]

        detected_count = 0
        for f in files:
            img_path = os.path.join(folder, f)
            try:
                mp_img = mp.Image.create_from_file(img_path)
                result = detector.detect(mp_img)
                if result.hand_landmarks:
                    feat = normalize_landmarks_21(result.hand_landmarks[0])
                    all_features.append(feat)
                    all_labels.append(class_idx)
                    detected_count += 1
            except Exception as e:
                continue

        print(f"Class '{letter}' ({class_idx}): {detected_count}/{len(files)} landmarks extracted.")

    X = np.array(all_features, dtype=np.float32)
    y = np.array(all_labels, dtype=np.int32)
    print(f"\nTotal extracted: {len(X)} samples, feature dimension: {X.shape[1]}")

    os.makedirs(os.path.dirname(OUTPUT_NPZ), exist_ok=True)
    np.savez_compressed(OUTPUT_NPZ, features=X, labels=y)
    print(f"Saved dataset to {OUTPUT_NPZ}")

    os.makedirs(os.path.dirname(OUTPUT_LABELS_JSON), exist_ok=True)
    with open(OUTPUT_LABELS_JSON, "w", encoding="utf-8") as f:
        json.dump(letters, f, indent=2)

    os.makedirs(os.path.dirname(ASSET_LABELS_JSON), exist_ok=True)
    with open(ASSET_LABELS_JSON, "w", encoding="utf-8") as f:
        json.dump(letters, f, indent=2)
    print(f"Saved labels to {OUTPUT_LABELS_JSON} and {ASSET_LABELS_JSON}")

if __name__ == "__main__":
    extract(max_per_class=120)
