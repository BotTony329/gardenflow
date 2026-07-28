from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "store-assets" / "google-play"
DRAWABLE = ROOT / "app" / "src" / "main" / "res" / "drawable"

BG = "#F7F2EA"
CARD = "#FFFFFF"
CREAM = "#EFE7D7"
GREEN = "#559261"
GREEN_DARK = "#243A2E"
MUTED = "#766F68"
PALE_GREEN = "#EAF3EA"
BROWN = "#A77554"
PURPLE = "#E9DAFF"
SHADOW = (0, 0, 0, 38)


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    candidates = [
        "/System/Library/Fonts/SFNS.ttf",
        "/System/Library/Fonts/HelveticaNeue.ttc",
        "/System/Library/Fonts/Helvetica.ttc",
    ]
    if bold:
        candidates.insert(0, "/System/Library/Fonts/SFNS.ttf")
        candidates.insert(0, "/System/Library/Fonts/Supplemental/Arial Bold.ttf")
    for candidate in candidates:
        try:
            return ImageFont.truetype(candidate, size=size)
        except OSError:
            continue
    return ImageFont.load_default()


def text_size(draw: ImageDraw.ImageDraw, text: str, fnt: ImageFont.ImageFont) -> tuple[int, int]:
    box = draw.textbbox((0, 0), text, font=fnt)
    return box[2] - box[0], box[3] - box[1]


def rounded_card(img: Image.Image, xy, radius: int, fill: str = CARD, shadow: bool = True):
    if shadow:
        layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
        sd = ImageDraw.Draw(layer)
        sx1, sy1, sx2, sy2 = xy
        sd.rounded_rectangle((sx1, sy1 + 10, sx2, sy2 + 10), radius, fill=SHADOW)
        layer = layer.filter(ImageFilter.GaussianBlur(12))
        img.alpha_composite(layer)
    ImageDraw.Draw(img).rounded_rectangle(xy, radius, fill=fill)


def icon(name: str, size: int, color=GREEN) -> Image.Image:
    path = DRAWABLE / f"gf_icon_{name}.png"
    if not path.exists():
        path = DRAWABLE / "gf_icon_plant_other.png"
    src = Image.open(path).convert("RGBA").resize((size, size), Image.Resampling.LANCZOS)
    alpha = src.getchannel("A")
    if isinstance(color, str):
        color = Image.new("RGBA", (1, 1), color).getpixel((0, 0))
    tinted = Image.new("RGBA", src.size, color)
    tinted.putalpha(alpha)
    return tinted


def paste_center(base: Image.Image, overlay: Image.Image, box):
    x1, y1, x2, y2 = box
    x = x1 + ((x2 - x1) - overlay.width) // 2
    y = y1 + ((y2 - y1) - overlay.height) // 2
    base.alpha_composite(overlay, (x, y))


def draw_text(draw, xy, text, size, fill=GREEN_DARK, bold=False, anchor=None):
    draw.text(xy, text, font=font(size, bold), fill=fill, anchor=anchor)


def wrap_text(draw, text, fnt, max_width):
    words, lines, current = text.split(), [], ""
    for word in words:
        trial = f"{current} {word}".strip()
        if text_size(draw, trial, fnt)[0] <= max_width:
            current = trial
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)
    return lines


def draw_wrapped(draw, xy, text, size, max_width, fill=MUTED, bold=False, gap=8):
    fnt = font(size, bold)
    y = xy[1]
    for line in wrap_text(draw, text, fnt, max_width):
        draw.text((xy[0], y), line, font=fnt, fill=fill)
        y += text_size(draw, line, fnt)[1] + gap
    return y


def make_app_icon():
    img = Image.new("RGBA", (512, 512), BG)
    d = ImageDraw.Draw(img)
    rounded_card(img, (34, 34, 478, 478), 116, fill=GREEN, shadow=True)
    d.rounded_rectangle((82, 82, 430, 430), 92, fill="#FBF8F1")
    paste_center(img, icon("plant_other", 244, GREEN), (82, 82, 430, 430))
    img.save(OUT / "app-icon" / "gardenflow-app-icon-512.png", quality=95)


def plant_thumb(img, x, y, size, name):
    d = ImageDraw.Draw(img)
    d.rounded_rectangle((x, y, x + size, y + size), size // 4, fill=PALE_GREEN)
    paste_center(img, icon(name, int(size * 0.55), GREEN), (x, y, x + size, y + size))


def button(draw, xy, label, fill, outline=None, txt="#FFFFFF", icon_name=None, size=28):
    x1, y1, x2, y2 = xy
    draw.rounded_rectangle(xy, (y2 - y1) // 2, fill=fill, outline=outline, width=3 if outline else 1)
    tx = (x1 + x2) // 2
    if icon_name:
        tx += 18
    draw.text((tx, (y1 + y2) // 2), label, font=font(size, True), fill=txt, anchor="mm")


def draw_weather(img, x, y, w, h, scale=1.0):
    d = ImageDraw.Draw(img)
    rounded_card(img, (x, y, x + w, y + h), int(34 * scale), fill=CREAM, shadow=False)
    paste_center(img, icon("partly_cloudy", int(82 * scale), BROWN), (x + int(34 * scale), y, x + int(130 * scale), y + h))
    draw_text(d, (x + int(150 * scale), y + int(36 * scale)), "Box Hill, Victoria", int(34 * scale), "#25231F", True)
    draw_text(d, (x + int(150 * scale), y + int(86 * scale)), "No significant rain expected", int(25 * scale), MUTED)
    draw_text(d, (x + w - int(48 * scale), y + int(40 * scale)), "13°C", int(50 * scale), "#25231F", False, "ra")
    draw_text(d, (x + w - int(48 * scale), y + int(102 * scale)), "Good for outdoor care", int(21 * scale), MUTED, False, "ra")


def draw_plant_card(img, x, y, w, h, title, variety, stage, next_text, plant_icon):
    d = ImageDraw.Draw(img)
    rounded_card(img, (x, y, x + w, y + h), 34, fill=CARD, shadow=True)
    plant_thumb(img, x + 34, y + 42, 130, plant_icon)
    draw_text(d, (x + 200, y + 40), title, 43, "#25231F", True)
    draw_text(d, (x + 200, y + 94), variety, 29, MUTED)
    d.rounded_rectangle((x + 200, y + 138, x + 570, y + 184), 23, fill=PALE_GREEN)
    draw_text(d, (x + 220, y + 146), stage, 27, GREEN)
    draw_text(d, (x + 210, y + 204), f"• {next_text}", 30, GREEN, True)
    button(d, (x + 34, y + h - 92, x + w // 2 - 14, y + h - 32), "Water", GREEN)
    paste_center(img, icon("water", 34, "#FFFFFF"), (x + 98, y + h - 84, x + 136, y + h - 44))
    button(d, (x + w // 2 + 14, y + h - 92, x + w - 34, y + h - 32), "Fertilise", "#FFFFFF", outline="#B5C9B9", txt=GREEN)
    paste_center(img, icon("fertilise", 34, GREEN), (x + w // 2 + 92, y + h - 84, x + w // 2 + 130, y + h - 44))


def draw_nav(img, w, h, scale=1.0):
    d = ImageDraw.Draw(img)
    nav_w, nav_h = int(w * 0.82), int(110 * scale)
    x, y = (w - nav_w) // 2, h - nav_h - int(54 * scale)
    rounded_card(img, (x, y, x + nav_w, y + nav_h), nav_h // 2, fill="#FFFFFF", shadow=True)
    d.rounded_rectangle((x + int(16 * scale), y + int(14 * scale), x + nav_w // 2 - int(8 * scale), y + nav_h - int(14 * scale)), nav_h // 2, fill=GREEN)
    paste_center(img, icon("plant_other", int(30 * scale), "#FFFFFF"), (x + int(58 * scale), y, x + int(112 * scale), y + nav_h))
    draw_text(d, (x + int(160 * scale), y + nav_h // 2), "Garden", int(30 * scale), "#FFFFFF", True, "lm")
    paste_center(img, icon("setting_other", int(32 * scale), "#595752"), (x + nav_w // 2 + int(66 * scale), y, x + nav_w // 2 + int(122 * scale), y + nav_h))
    draw_text(d, (x + nav_w // 2 + int(150 * scale), y + nav_h // 2), "Settings", int(30 * scale), "#595752", True, "lm")


def garden_screen(w, h, tablet="7") -> Image.Image:
    img = Image.new("RGBA", (w, h), BG)
    d = ImageDraw.Draw(img)
    margin = int(w * 0.075)
    scale = w / 1200
    title = "Your garden is all cared for today"
    draw_wrapped(d, (margin, int(120 * scale)), title, int(62 * scale), int(w * 0.72), "#25231F", True, 12)
    draw_weather(img, margin, int(360 * scale), w - margin * 2, int(210 * scale), scale)
    draw_text(d, (margin, int(650 * scale)), "Your Plants", int(42 * scale), "#25231F", True)
    card_w = w - margin * 2
    draw_plant_card(img, margin, int(735 * scale), card_w, int(330 * scale), "Dwarf Lemon", "Meyer dwarf lemon", "Summer development · 93 d", "Water in 4 days", "lemon")
    draw_plant_card(img, margin, int(1115 * scale), card_w, int(330 * scale), "Feijoa", "Dwarf", "Vegetative growth · 93 d", "Water in 6 days", "plant_other")
    draw_plant_card(img, margin, int(1495 * scale), card_w, int(330 * scale), "Potato", "General", "Sprouting · 3 d", "Water in 4 days", "potato")
    d.rounded_rectangle((w - margin - int(140 * scale), h - int(360 * scale), w - margin, h - int(220 * scale)), int(38 * scale), fill=PURPLE)
    draw_text(d, (w - margin - int(70 * scale), h - int(290 * scale)), "+", int(40 * scale), "#2F1E4A", True, "mm")
    draw_nav(img, w, h, scale)
    return img.convert("RGB")


def detail_screen(w, h) -> Image.Image:
    img = Image.new("RGBA", (w, h), BG)
    d = ImageDraw.Draw(img)
    margin = int(w * 0.075)
    scale = w / 1200
    draw_text(d, (margin, int(90 * scale)), "←  GardenFlow", int(34 * scale), "#25231F", True)
    rounded_card(img, (margin, int(170 * scale), w - margin, int(455 * scale)), int(38 * scale), fill=PALE_GREEN, shadow=False)
    paste_center(img, icon("lemon", int(150 * scale), GREEN), (margin, int(170 * scale), w - margin, int(455 * scale)))
    draw_text(d, (margin, int(540 * scale)), "Dwarf Lemon", int(70 * scale), "#25231F", True)
    draw_text(d, (margin, int(625 * scale)), "Meyer dwarf lemon", int(34 * scale), MUTED, True)
    d.rounded_rectangle((margin + int(330 * scale), int(620 * scale), margin + int(760 * scale), int(680 * scale)), int(30 * scale), fill=PALE_GREEN)
    draw_text(d, (margin + int(356 * scale), int(631 * scale)), "Summer development · 93 d", int(25 * scale), GREEN)
    draw_text(d, (margin, int(760 * scale)), "Growth timeline", int(46 * scale), "#25231F")
    rounded_card(img, (margin, int(840 * scale), w - margin, int(1195 * scale)), int(34 * scale), fill=CARD, shadow=True)
    stages = [("Growth", "0-90 d", False), ("Summer development", "90-180 d", True), ("Harvest", "180-270 d", False)]
    col_w = (w - margin * 2) // 3
    for i, (label, days, current) in enumerate(stages):
        cx = margin + col_w * i + col_w // 2
        color = GREEN if current else "#D9D0C7"
        d.ellipse((cx - 18, int(900 * scale), cx + 18, int(936 * scale)), fill=color)
        draw_text(d, (cx, int(970 * scale)), label, int(30 * scale), GREEN if current else "#25231F", True, "mm")
        draw_text(d, (cx, int(1030 * scale)), days, int(25 * scale), MUTED, False, "mm")
        if current:
            draw_text(d, (cx, int(1090 * scale)), "Day 93", int(27 * scale), GREEN, True, "mm")
    draw_text(d, (margin, int(1290 * scale)), "Care plan", int(46 * scale), "#25231F")
    rounded_card(img, (margin, int(1370 * scale), w - margin, int(1810 * scale)), int(34 * scale), fill=CARD, shadow=True)
    rows = [("water", "Water every 7 days", "About 25 mm · skip after 10 mm rain"), ("fertilise", "Fertilise every 60 days", "Use a balanced citrus fertiliser"), ("high_temp_rule", "Preferred temperature", "10-35°C")]
    y = int(1430 * scale)
    for ic, title, sub in rows:
        paste_center(img, icon(ic, int(58 * scale), GREEN), (margin + int(40 * scale), y - int(8 * scale), margin + int(120 * scale), y + int(72 * scale)))
        draw_text(d, (margin + int(150 * scale), y), title, int(32 * scale), "#25231F", True)
        draw_text(d, (margin + int(150 * scale), y + int(48 * scale)), sub, int(26 * scale), MUTED)
        y += int(125 * scale)
    return img.convert("RGB")


def add_screen(w, h) -> Image.Image:
    img = Image.new("RGBA", (w, h), BG)
    d = ImageDraw.Draw(img)
    margin = int(w * 0.075)
    scale = w / 1200
    draw_text(d, (margin, int(90 * scale)), "←  GardenFlow", int(34 * scale), "#25231F", True)
    draw_text(d, (margin, int(195 * scale)), "Step 1 / 4", int(34 * scale), GREEN, True)
    d.rounded_rectangle((margin, int(255 * scale), w - margin, int(268 * scale)), int(6 * scale), fill="#E9E1D8")
    d.rounded_rectangle((margin, int(255 * scale), margin + int((w - margin * 2) * 0.25), int(268 * scale)), int(6 * scale), fill=GREEN)
    draw_wrapped(d, (margin, int(340 * scale)), "Add a new plant", int(72 * scale), int(w * 0.55), "#25231F", True)
    draw_text(d, (margin, int(540 * scale)), "Choose how to add it. AI will help with the care plan.", int(32 * scale), MUTED)
    options = [("camera", "Take packet photo", "Open camera and run OCR", True), ("custom", "Choose packet image", "Pick existing photos and run OCR", False), ("custom", "Enter plant name", "tomato", False)]
    y = int(650 * scale)
    for ic, title, sub, selected in options:
        rounded_card(img, (margin, y, w - margin, y + int(180 * scale)), int(34 * scale), fill=CARD, shadow=True)
        if selected:
            d.rounded_rectangle((margin, y, w - margin, y + int(180 * scale)), int(34 * scale), outline=GREEN, width=int(4 * scale))
        paste_center(img, icon("custom" if ic == "camera" else "plant_other", int(62 * scale), GREEN), (margin + int(50 * scale), y + int(40 * scale), margin + int(150 * scale), y + int(140 * scale)))
        draw_text(d, (margin + int(190 * scale), y + int(42 * scale)), title, int(36 * scale), "#25231F", True)
        draw_text(d, (margin + int(190 * scale), y + int(96 * scale)), sub, int(28 * scale), MUTED)
        draw_text(d, (w - margin - int(55 * scale), y + int(90 * scale)), "›", int(58 * scale), "#9B958F", False, "mm")
        y += int(220 * scale)
    button(d, (margin, int(1435 * scale), w - margin, int(1535 * scale)), "Continue", GREEN, size=int(30 * scale))
    return img.convert("RGB")


def settings_screen(w, h) -> Image.Image:
    img = Image.new("RGBA", (w, h), BG)
    d = ImageDraw.Draw(img)
    margin = int(w * 0.075)
    scale = w / 1200
    draw_text(d, (margin, int(90 * scale)), "←", int(48 * scale), "#25231F", True)
    draw_text(d, (margin, int(230 * scale)), "Settings", int(86 * scale), "#25231F")
    sections = [
        ("Garden", [("garden_location", "Garden location", "Box Hill, Victoria, Australia"), ("daily_reminder", "Task reminders", "Only notify when tasks are due")]),
        ("Weather", [("high_temp_rule", "High temperature rule", "30°C shortens watering interval")]),
        ("AI", [("deepseek_api", "AI status", "Working")]),
        ("App", [("about", "Language", "System · 中文 · English"), ("export_data", "Export data", "Coming soon")]),
    ]
    y = int(420 * scale)
    for section, rows in sections:
        draw_text(d, (margin, y), section, int(42 * scale), GREEN, True)
        y += int(72 * scale)
        rounded_card(img, (margin, y, w - margin, y + int((118 * len(rows) + 42) * scale)), int(34 * scale), fill=CARD, shadow=True)
        yy = y + int(32 * scale)
        for ic, title, sub in rows:
            paste_center(img, icon(ic, int(52 * scale), GREEN), (margin + int(38 * scale), yy, margin + int(118 * scale), yy + int(80 * scale)))
            draw_text(d, (margin + int(145 * scale), yy + int(3 * scale)), title, int(30 * scale), "#25231F", True)
            draw_text(d, (margin + int(145 * scale), yy + int(45 * scale)), sub, int(25 * scale), MUTED)
            yy += int(118 * scale)
        y = yy + int(70 * scale)
    draw_nav(img, w, h, scale)
    return img.convert("RGB")


def make_feature():
    img = Image.new("RGBA", (1024, 500), BG)
    d = ImageDraw.Draw(img)
    draw_text(d, (64, 72), "GardenFlow", 58, "#25231F", True)
    draw_wrapped(d, (64, 148), "Weather-aware care plans for every plant in your garden.", 34, 370, MUTED, False, 8)
    button(d, (64, 350, 318, 426), "Plan, water, grow", GREEN, size=28)
    rounded_card(img, (512, 54, 960, 446), 44, fill=CARD, shadow=True)
    draw_text(d, (552, 92), "2 plants need care today", 34, "#25231F", True)
    draw_weather(img, 552, 150, 360, 104, 0.56)
    rounded_card(img, (552, 286, 912, 410), 26, fill="#FFFFFF", shadow=True)
    plant_thumb(img, 576, 312, 76, "lemon")
    draw_text(d, (674, 312), "Dwarf Lemon", 28, "#25231F", True)
    draw_text(d, (674, 350), "Water today", 22, GREEN, True)
    d.rounded_rectangle((674, 378, 804, 405), 14, fill=PALE_GREEN)
    draw_text(d, (688, 381), "Growth · 93 d", 16, GREEN)
    d.ellipse((830, 328, 882, 380), fill=GREEN)
    paste_center(img, icon("water", 28, "#FFFFFF"), (830, 328, 882, 380))
    plant_thumb(img, 388, 206, 108, "tomato")
    plant_thumb(img, 338, 308, 88, "water")
    plant_thumb(img, 422, 334, 88, "fertilise")
    img.convert("RGB").save(OUT / "feature-graphic" / "gardenflow-feature-graphic-1024x500.png", quality=95)


def save_screens():
    sizes = {
        "tablet-7-inch": (1200, 1920),
        "tablet-10-inch": (1600, 2560),
    }
    makers = [
        ("01-garden.png", garden_screen),
        ("02-plant-detail.png", detail_screen),
        ("03-add-plant.png", add_screen),
        ("04-settings.png", settings_screen),
    ]
    for folder, (w, h) in sizes.items():
        for filename, maker in makers:
            maker(w, h).save(OUT / folder / filename, quality=95)


def write_readme():
    text = """# GardenFlow Google Play Assets

Generated store listing assets for Google Play Console.

## Files

- `app-icon/gardenflow-app-icon-512.png` — 512 x 512 32-bit PNG app icon.
- `feature-graphic/gardenflow-feature-graphic-1024x500.png` — 1024 x 500 feature graphic.
- `tablet-7-inch/*.png` — 1200 x 1920 7-inch tablet screenshots.
- `tablet-10-inch/*.png` — 1600 x 2560 10-inch tablet screenshots.

All screenshots are RGB PNG files and use GardenFlow's current calm cream/green visual system.
"""
    (OUT / "README.md").write_text(text, encoding="utf-8")


def main():
    for folder in ["app-icon", "feature-graphic", "tablet-7-inch", "tablet-10-inch"]:
        (OUT / folder).mkdir(parents=True, exist_ok=True)
    make_app_icon()
    make_feature()
    save_screens()
    write_readme()
    print(f"Generated Google Play assets in {OUT}")


if __name__ == "__main__":
    main()
