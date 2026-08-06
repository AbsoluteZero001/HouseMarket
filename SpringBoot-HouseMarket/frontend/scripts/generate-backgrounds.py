#!/usr/bin/env python3
"""Generate Beijing poster backgrounds for the HouseMarket frontend.

The generated PNGs are committed under frontend/public/backgrounds so a fresh
clone renders the exact same visual as the author's machine. This script is
optional and only needed when someone wants to regenerate the art.
"""

import math
import os

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont


ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
UPLOADS = os.path.join(ROOT, "uploads")
OUT = os.path.join(ROOT, "frontend", "public", "backgrounds")
W, H = 1920, 1080

FONT_CANDIDATES = [
    r"C:\Windows\Fonts\msyhbd.ttc",
    r"C:\Windows\Fonts\Dengb.ttf",
    r"C:\Windows\Fonts\simhei.ttf",
    "/System/Library/Fonts/PingFang.ttc",
    "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc",
]


def find_font():
    for path in FONT_CANDIDATES:
        if os.path.exists(path):
            return path
    return None


def cover_image(path):
    image = Image.open(path).convert("RGB")
    scale = max(W / image.width, H / image.height)
    image = image.resize(
        (max(1, round(image.width * scale)), max(1, round(image.height * scale))),
        Image.LANCZOS,
    )
    left = max(0, (image.width - W) // 2)
    top = max(0, (image.height - H) // 2)
    return image.crop((left, top, left + W, top + H))


def vertical_gradient(stops):
    overlay = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)
    step = 2
    for y in range(0, H, step):
        t = y / (H - 1)
        for i in range(len(stops) - 1):
            p0, c0 = stops[i]
            p1, c1 = stops[i + 1]
            if p0 <= t <= p1:
                k = (t - p0) / (p1 - p0)
                color = tuple(round(c0[j] + (c1[j] - c0[j]) * k) for j in range(4))
                draw.rectangle((0, y, W, y + step - 1), fill=color)
                break
    return overlay


def add_grain(base, intensity=7):
    noise = Image.effect_noise((W // 4, H // 4), 32).convert("L").resize((W, H), Image.BILINEAR)
    noise = noise.point(lambda p: p if p > 255 - intensity else 255)
    rgba = base.convert("RGBA")
    grain = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    grain.putalpha(noise.point(lambda p: max(0, p - 245)))
    return Image.alpha_composite(rgba, grain)


def draw_watermark(draw, text_cn, text_en, align="right"):
    font_path = find_font()
    cn_size = 118 if align == "right" else 88
    en_size = 30 if align == "right" else 26
    cn_font = ImageFont.truetype(font_path, cn_size) if font_path else ImageFont.load_default()
    en_font = ImageFont.truetype(font_path, en_size) if font_path else ImageFont.load_default()

    if align == "right":
        draw.text((W - 56, H - 150), text_cn, font=cn_font, fill=(255, 255, 255, 26), anchor="rs")
        draw.text((W - 62, H - 186), text_en, font=en_font, fill=(255, 255, 255, 44), anchor="rs")
        draw.rectangle((W - 560, H - 210, W - 62, H - 204), fill=(255, 255, 255, 28))
    else:
        draw.text((64, 72), text_cn, font=cn_font, fill=(255, 255, 255, 34), anchor="la")
        draw.text((68, 186), text_en, font=en_font, fill=(255, 255, 255, 50), anchor="la")


def draw_geometry(draw, colors):
    for x in range(0, W, 160):
        draw.line((x, 0, x, H), fill=(255, 255, 255, 10), width=1)
    for y in range(0, H, 160):
        draw.line((0, y, W, y), fill=(255, 255, 255, 8), width=1)

    draw.line((0, H - 3, W, H - 3), fill=colors["bottom"], width=3)
    draw.line((0, H - 72, W, H - 72), fill=(255, 255, 255, 16), width=1)

    # diagonal accent band, left side
    draw.polygon([(0, 0), (210, 0), (0, 210)], fill=colors["accent"])
    draw.polygon([(W, H), (W - 300, H), (W, H - 300)], fill=colors["accent2"])


def make_poster(name, source, stops, tint, text_cn, text_en, align="right", warm=False):
    base = cover_image(os.path.join(UPLOADS, source))
    base = ImageEnhance.Color(base).enhance(1.18 if warm else 1.08)
    base = ImageEnhance.Contrast(base).enhance(1.06)

    tint_layer = Image.new("RGBA", (W, H), tint)
    canvas = Image.alpha_composite(base.convert("RGBA"), tint_layer)
    canvas = Image.alpha_composite(canvas, vertical_gradient(stops))

    draw = ImageDraw.Draw(canvas, "RGBA")
    colors = {
        "accent": (103, 232, 249, 34),
        "accent2": (236, 72, 153, 26),
        "bottom": (255, 255, 255, 70),
    }
    draw_geometry(draw, colors)
    draw_watermark(draw, text_cn, text_en, align)
    canvas = add_grain(canvas, intensity=6)

    os.makedirs(OUT, exist_ok=True)
    target = os.path.join(OUT, name)
    canvas.convert("RGB").save(target, "PNG")
    print("generated", target)


def main():
    make_poster(
        "beijing-hero.png",
        "img_4.png",
        stops=[
            (0.0, (6, 182, 212, 58)),
            (0.42, (23, 37, 84, 120)),
            (1.0, (7, 16, 36, 205)),
        ],
        tint=(7, 22, 48, 70),
        text_cn="北京 · 理想家",
        text_en="BEIJING · HOUSEMARKET · 2026",
        align="right",
    )
    make_poster(
        "tenant-hero.png",
        "img_1.png",
        stops=[
            (0.0, (34, 211, 238, 66)),
            (0.45, (139, 92, 246, 120)),
            (1.0, (236, 72, 153, 170)),
        ],
        tint=(23, 37, 84, 84),
        text_cn="遇见属于你的北京",
        text_en="RENT SMART · LIVE BETTER",
        align="left",
        warm=True,
    )
    make_poster(
        "landlord-hero.png",
        "img_3.png",
        stops=[
            (0.0, (59, 130, 246, 60)),
            (0.45, (30, 27, 75, 150)),
            (1.0, (139, 92, 246, 205)),
        ],
        tint=(13, 20, 56, 90),
        text_cn="房东审批工作台",
        text_en="LANDLORD · APPROVAL FLOW",
        align="right",
    )
    make_poster(
        "auth-hero.png",
        "img_5.png",
        stops=[
            (0.0, (22, 119, 255, 52)),
            (0.42, (139, 92, 246, 120)),
            (1.0, (236, 72, 153, 170)),
        ],
        tint=(20, 28, 74, 80),
        text_cn="房源市场",
        text_en="HOUSEMARKET · SIGN IN",
        align="left",
        warm=True,
    )
    make_poster(
        "admin-hero.png",
        "img_2.png",
        stops=[
            (0.0, (6, 182, 212, 48)),
            (0.5, (37, 99, 235, 120)),
            (1.0, (30, 27, 75, 190)),
        ],
        tint=(24, 36, 74, 74),
        text_cn="房源市场 · 管理后台",
        text_en="HOUSEMARKET · ADMIN CONSOLE",
        align="right",
    )


if __name__ == "__main__":
    main()
