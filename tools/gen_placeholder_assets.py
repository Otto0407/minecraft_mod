"""Generate placeholder monster girl textures and the matching Blockbench project.

Cube layout must stay in sync with MonsterGirlModel.createBodyLayer().
Run: python3 tools/gen_placeholder_assets.py
"""
import base64
import io
import json
import uuid
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
TEX_DIR = ROOT / "src/main/resources/assets/trialcompanions/textures/entity/monster_girl"
BB_DIR = ROOT / "blockbench"

# name, parent pivot (java, absolute), box origin (relative to pivot), size, uv, inflate
# Java model space: y down, head pivot at y=0, feet at y=24.
CUBES = [
    ("head",      (0, 0, 0),      (-4, -8, -4),     (8, 8, 8),  (0, 0),   0.0),
    ("hair_back", (0, 0, 0),      (-4, -1, 3.5),    (8, 10, 1), (28, 32), 0.0),
    ("hat",       (0, 0, 0),      (-4, -8, -4),     (8, 8, 8),  (32, 0),  0.5),
    ("body",      (0, 0, 0),      (-4, 0, -2),      (8, 12, 4), (16, 16), 0.0),
    ("skirt",     (0, 0, 0),      (-4.5, 10, -2.5), (9, 5, 5),  (0, 32),  0.0),
    ("right_arm", (-5, 2.5, 0),   (-2, -2, -2),     (3, 12, 4), (40, 16), 0.0),
    ("left_arm",  (5, 2.5, 0),    (-1, -2, -2),     (3, 12, 4), (32, 48), 0.0),
    ("right_leg", (-1.9, 12, 0),  (-2, 0, -2),      (4, 12, 4), (0, 16),  0.0),
    ("left_leg",  (1.9, 12, 0),   (-2, 0, -2),      (4, 12, 4), (16, 48), 0.0),
]
# Groups (bones) and which cubes they hold; children nest like the Java parts.
BONES = {
    "head": ["head", "hair_back"], "hat": ["hat"], "body": ["body", "skirt"],
    "right_arm": ["right_arm"], "left_arm": ["left_arm"],
    "right_leg": ["right_leg"], "left_leg": ["left_leg"],
}
BONE_PIVOT = {c[0]: c[1] for c in CUBES}


def box_faces(u, v, w, h, d):
    """Rectangles (x0, y0, x1, y1) of each face in Minecraft box UV."""
    return {
        "up":    (u + d, v, u + d + w, v + d),
        "down":  (u + d + w, v, u + d + 2 * w, v + d),
        "east":  (u, v + d, u + d, v + d + h),
        "north": (u + d, v + d, u + d + w, v + d + h),
        "west":  (u + d + w, v + d, u + 2 * d + w, v + d + h),
        "south": (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
    }


def paint(draw, cube, colors, face_colors=None):
    _, _, _, (w, h, d), (u, v), _ = cube
    for face, rect in box_faces(u, v, w, h, d).items():
        x0, y0, x1, y1 = rect
        color = (face_colors or {}).get(face, colors)
        draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=color)


def zombie_girl_texture():
    skin, hair, shirt, skirt, legs = (112, 170, 90), (46, 82, 40), (40, 150, 160), (70, 60, 120), (60, 70, 130)
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    cube = {c[0]: c for c in CUBES}

    # Head: hair everywhere except the face.
    paint(draw, cube["head"], hair, {"north": skin})
    # Face (north face of head is at x 8..16, y 8..16): bangs, eyes, mouth.
    draw.rectangle((8, 8, 15, 9), fill=hair)
    draw.point([(8, 10), (15, 10)], fill=hair)
    draw.rectangle((9, 12, 10, 13), fill=(20, 20, 20))
    draw.rectangle((13, 12, 14, 13), fill=(20, 20, 20))
    draw.point([(9, 12), (13, 12)], fill=(200, 40, 40))
    draw.rectangle((11, 15, 12, 15), fill=(70, 110, 60))
    paint(draw, cube["hair_back"], hair)
    # Hat layer stays transparent.

    paint(draw, cube["body"], shirt)
    draw.rectangle((20, 20, 27, 20), fill=(30, 110, 120))  # collar line on the front
    paint(draw, cube["skirt"], skirt)
    for name in ("right_arm", "left_arm"):
        paint(draw, cube[name], skin)
        _, _, _, (w, h, d), (u, v), _ = cube[name]
        draw.rectangle((u, v + d, u + 2 * (w + d) - 1, v + d + 3), fill=shirt)  # short sleeves
    for name in ("right_leg", "left_leg"):
        paint(draw, cube[name], skin)
        _, _, _, (w, h, d), (u, v), _ = cube[name]
        draw.rectangle((u, v + d + 8, u + 2 * (w + d) - 1, v + d + h - 1), fill=legs)  # shoes
    return img


def bbmodel(name, texture_png):
    """Blockbench 'Modded Entity' project. Blockbench uses y-up with feet at y=0 and mirrored x."""
    def to_bb(px, py, pz):
        return [-px, 24 - py, pz]

    elements, by_name = [], {}
    for cname, pivot, (ox, oy, oz), (w, h, d), (u, v), inflate in CUBES:
        ax, ay, az = pivot[0] + ox, pivot[1] + oy, pivot[2] + oz
        el = {
            "name": cname, "box_uv": True, "rescale": False, "locked": False,
            "render_order": "default", "allow_mirror_modeling": True,
            "from": [-(ax + w), 24 - (ay + h), az], "to": [-ax, 24 - ay, az + d],
            "autouv": 0, "color": 0, "inflate": inflate,
            "origin": to_bb(*pivot), "uv_offset": [u, v],
            "faces": {f: {"uv": list(r), "texture": 0} for f, r in box_faces(u, v, w, h, d).items()},
            "type": "cube", "uuid": str(uuid.uuid4()),
        }
        elements.append(el)
        by_name[cname] = el["uuid"]

    outliner = [{
        "name": bone, "origin": to_bb(*BONE_PIVOT[bone]), "color": 0, "uuid": str(uuid.uuid4()),
        "export": True, "mirror_uv": False, "isOpen": True, "locked": False, "visibility": True,
        "autouv": 0, "children": [by_name[c] for c in cubes],
    } for bone, cubes in BONES.items()]

    buf = io.BytesIO()
    texture_png.save(buf, "PNG")
    return {
        "meta": {"format_version": "4.10", "model_format": "modded_entity", "box_uv": True},
        "name": name, "model_identifier": "", "modded_entity_entity_class": "",
        "modded_entity_version": "Mojmaps", "modded_entity_flip_y": True,
        "visible_box": [1, 2, 0], "variable_placeholders": "", "variable_placeholder_buttons": [],
        "timeline_setups": [], "unhandled_root_fields": {},
        "resolution": {"width": 64, "height": 64},
        "elements": elements, "outliner": outliner,
        "textures": [{
            "path": "", "name": f"{name}.png", "folder": "", "namespace": "", "id": "0",
            "width": 64, "height": 64, "uv_width": 64, "uv_height": 64, "particle": False,
            "layers_enabled": False, "render_mode": "default", "render_sides": "auto",
            "frame_time": 1, "frame_order_type": "loop", "frame_order": "", "frame_interpolate": False,
            "visible": True, "internal": True, "saved": False, "uuid": str(uuid.uuid4()),
            "relative_path": "", "source": "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode(),
        }],
    }


def main():
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    BB_DIR.mkdir(parents=True, exist_ok=True)
    tex = zombie_girl_texture()
    tex.save(TEX_DIR / "zombie_girl.png")
    (BB_DIR / "zombie_girl.bbmodel").write_text(json.dumps(bbmodel("zombie_girl", tex), indent=1))


if __name__ == "__main__":
    main()
