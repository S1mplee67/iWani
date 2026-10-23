"""
Prepare FSL Reference Images and Full Alphabet Chart.

Extracts representative reference images for A-Z from 'FSL datasets'
and generates:
1. assets/images/alfabet/fsl/<letter>.webp (26 letter reference images)
2. assets/images/alfabet/alfabet_fsl_full.jpg (comprehensive visual chart)
"""

import os
import glob
from PIL import Image, ImageDraw, ImageFont

DATASET_DIR = "FSL datasets"
OUTPUT_DIR = "assets/images/alfabet/fsl"
FULL_CHART_PATH = "assets/images/alfabet/alfabet_fsl_full.jpg"

os.makedirs(OUTPUT_DIR, exist_ok=True)

LETTERS = [chr(c) for c in range(ord('A'), ord('Z') + 1)]

print(f"Generating FSL reference images into {OUTPUT_DIR}...")

letter_images = {}

for letter in LETTERS:
    folder = os.path.join(DATASET_DIR, letter)
    if not os.path.exists(folder):
        print(f"Warning: Folder {folder} not found")
        continue

    # Find images
    candidates = sorted(glob.glob(os.path.join(folder, "*.jpg")) + glob.glob(os.path.join(folder, "*.png")))
    if not candidates:
        print(f"Warning: No images found for {letter}")
        continue

    # Pick a solid image (prefer simple non-augmented name if possible, else 1st)
    chosen_path = candidates[0]
    for c in candidates:
        basename = os.path.basename(c)
        if "_" not in basename and "-" not in basename:
            chosen_path = c
            break

    im = Image.open(chosen_path).convert("RGB")
    letter_images[letter] = im

    # Save webp
    out_webp = os.path.join(OUTPUT_DIR, f"{letter.lower()}.webp")
    im.save(out_webp, "WEBP", quality=90)

    # Also save jpg as fallback
    out_jpg = os.path.join(OUTPUT_DIR, f"{letter.lower()}.jpg")
    im.save(out_jpg, "JPEG", quality=90)

print(f"Saved {len(letter_images)} reference images to {OUTPUT_DIR}")

# Generate Full FSL Alphabet Chart
print("Creating full FSL alphabet chart...")
cols = 6
rows = 5 # 30 slots for 26 letters
tile_w = 160
tile_h = 190
padding = 16
header_h = 100

chart_w = cols * tile_w + (cols + 1) * padding
chart_h = header_h + rows * tile_h + (rows + 1) * padding

# Create chart canvas with deep navy background
chart = Image.new("RGB", (chart_w, chart_h), color="#0F172A")
draw = ImageDraw.Draw(chart)

# Draw Title Header
# Try basic font or default
try:
    font_title = ImageFont.truetype("arial.ttf", 32)
    font_sub = ImageFont.truetype("arial.ttf", 16)
    font_letter = ImageFont.truetype("arialbd.ttf", 22)
except Exception:
    font_title = ImageFont.load_default()
    font_sub = ImageFont.load_default()
    font_letter = ImageFont.load_default()

draw.text((padding + 10, 24), "FILIPINO SIGN LANGUAGE (FSL)", fill="#FDB813", font=font_title)
draw.text((padding + 10, 64), "Project Nexus • Official 26-Letter One-Hand Fingerspelling Chart", fill="#94A3B8", font=font_sub)

# Draw decorative Philippine flag accent line
draw.rectangle([padding + 10, 88, chart_w - padding - 10, 92], fill="#0038A8")

# Place letter tiles
for idx, letter in enumerate(LETTERS):
    r = idx // cols
    c = idx % cols

    x = padding + c * (tile_w + padding)
    y = header_h + padding + r * (tile_h + padding)

    # Tile background card
    draw.rounded_rectangle([x, y, x + tile_w, y + tile_h], radius=10, fill="#1E293B", outline="#334155", width=1)

    if letter in letter_images:
        src_im = letter_images[letter]
        # Resize to fit tile
        thumb = src_im.resize((tile_w - 16, tile_h - 48), Image.Resampling.LANCZOS)
        chart.paste(thumb, (x + 8, y + 8))

    # Letter label bar
    label_y = y + tile_h - 34
    draw.rounded_rectangle([x + 8, label_y, x + tile_w - 8, y + tile_h - 6], radius=6, fill="#0F172A")
    draw.text((x + tile_w // 2 - 8, label_y + 4), letter, fill="#FDB813", font=font_letter)

# Save chart
chart.save(FULL_CHART_PATH, "JPEG", quality=95)
print(f"FSL Alphabet full chart created successfully at {FULL_CHART_PATH} ({chart_w}x{chart_h})")
