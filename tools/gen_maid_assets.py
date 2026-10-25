"""Generate the LittleMaid-based Blockbench projects and placeholder monster girl textures.

Geometry is transcribed from LittleMaidModelLoader's ModelLittleMaidBase (the default
"Orign" maid), flattened into the part layout HumanoidModel needs. It must stay in sync
with MonsterGirlModel.createBodyLayer().

LittleMaid assets are under the LittleMaid Licence (non-commercial only); see
blockbench/littlemaid_textures/LICENCE.md.

Run: python3 tools/gen_maid_assets.py
"""
import base64
import io
import json
import uuid
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
MAID_TEXTURES = ROOT / "blockbench/littlemaid_textures"
TEX_DIR = ROOT / "src/main/resources/assets/monstergirlcompanions/textures/entity/monster_girl"
BB_DIR = ROOT / "blockbench"
TEX_W, TEX_H = 64, 32

# Parts in Java model space (y down, feet at y=24): name -> (parent, pivot relative to parent).
PARTS = {
    "head":      (None,   (0, 8, 0)),
    "hat":       (None,   (0, 8, 0)),
    "body":      (None,   (0, 8, 0)),
    "skirt":     ("body", (0, 7, 0)),
    "right_arm": (None,   (-3, 9.6, 0)),
    "left_arm":  (None,   (3, 9.6, 0)),
    "right_leg": (None,   (-1, 15, 0)),
    "left_leg":  (None,   (1, 15, 0)),
}
# part -> [(box origin relative to pivot, size, uv, mirror)]
CUBES = {
    "head": [
        ((-4, -8, -4),      (8, 8, 8), (0, 0),   False),  # head
        ((-4, 0, 1),        (8, 4, 3), (24, 0),  False),  # back hair
        ((-5, -7, 0.2),     (1, 3, 3), (24, 18), False),  # right side hair
        ((4, -7, 0.2),      (1, 3, 3), (24, 18), False),  # left side hair
        ((-2, -7.2, 4),     (4, 4, 2), (52, 10), False),  # ponytail knot
        ((-1.5, -6.8, 4),   (3, 9, 3), (46, 20), False),  # ponytail
        ((-5.5, -6.8, 0.9), (1, 8, 2), (58, 21), False),  # right sidelock
        ((4.5, -6.8, 0.9),  (1, 8, 2), (58, 21), True),   # left sidelock
    ],
    "body":      [((-3, 0, -2), (6, 7, 4), (32, 8), False)],
    "skirt":     [((-4, -2, -4), (8, 8, 8), (0, 16), False)],
    "right_arm": [((-2, -1, -1), (2, 8, 2), (48, 0), False)],
    "left_arm":  [((0, -1, -1), (2, 8, 2), (56, 0), False)],
    "right_leg": [((-2, 0, -2), (3, 9, 4), (32, 19), False)],
    "left_leg":  [((-1, 0, -2), (3, 9, 4), (32, 19), True)],
}


def absolute_pivot(part):
    parent, (x, y, z) = PARTS[part]
    if parent is None:
        return (x, y, z)
    px, py, pz = absolute_pivot(parent)
    return (px + x, py + y, pz + z)


def box_faces(u, v, w, h, d):
    return {
        "north": [u + d, v + d, u + d + w, v + d + h],
        "east":  [u, v + d, u + d, v + d + h],
        "south": [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h],
        "west":  [u + d + w, v + d, u + 2 * d + w, v + d + h],
        "up":    [u + d + w, v + d, u + d, v],
        "down":  [u + d + 2 * w, v, u + d + w, v + d],
    }


def to_bb(x, y, z):
    """Java entity space -> Blockbench space (y up from the feet, x mirrored)."""
    return [-x, 24 - y, z]


def bbmodel(name, texture):
    elements, groups = [], {}
    for part in PARTS:
        groups[part] = {
            "name": part, "origin": to_bb(*absolute_pivot(part)), "rotation": [0, 0, 0],
            "color": 0, "uuid": str(uuid.uuid4()), "export": True, "mirror_uv": False,
            "isOpen": True, "locked": False, "visibility": True, "autouv": 0, "children": [],
        }
    for part, cubes in CUBES.items():
        px, py, pz = absolute_pivot(part)
        for i, ((ox, oy, oz), (w, h, d), (u, v), mirror) in enumerate(cubes):
            ax, ay, az = px + ox, py + oy, pz + oz
            el = {
                "name": part if i == 0 else f"{part}_{i}", "box_uv": True, "rescale": False,
                "locked": False, "render_order": "default", "allow_mirror_modeling": True,
                "from": [-(ax + w), 24 - (ay + h), az], "to": [-ax, 24 - ay, az + d],
                "autouv": 0, "color": 0, "mirror_uv": mirror,
                "origin": to_bb(px, py, pz), "uv_offset": [u, v],
                "faces": {f: {"uv": r, "texture": 0} for f, r in box_faces(u, v, w, h, d).items()},
                "type": "cube", "uuid": str(uuid.uuid4()),
            }
            elements.append(el)
            groups[part]["children"].append(el["uuid"])
    outliner = []
    for part, (parent, _) in PARTS.items():
        (groups[parent]["children"] if parent else outliner).append(groups[part])

    buf = io.BytesIO()
    texture.save(buf, "PNG")
    return {
        "meta": {"format_version": "4.10", "model_format": "modded_entity", "box_uv": True},
        "name": name, "model_identifier": "", "modded_entity_entity_class": "",
        "modded_entity_version": "Mojmaps", "modded_entity_flip_y": True,
        "visible_box": [1, 2, 0], "variable_placeholders": "", "variable_placeholder_buttons": [],
        "timeline_setups": [], "unhandled_root_fields": {},
        "resolution": {"width": TEX_W, "height": TEX_H},
        "elements": elements, "outliner": outliner,
        "textures": [{
            "path": "", "name": f"{name}.png", "folder": "", "namespace": "", "id": "0",
            "width": TEX_W, "height": TEX_H, "uv_width": TEX_W, "uv_height": TEX_H,
            "particle": False, "layers_enabled": False, "render_mode": "default",
            "render_sides": "auto", "frame_time": 1, "frame_order_type": "loop",
            "frame_order": "", "frame_interpolate": False, "visible": True, "internal": True,
            "saved": False, "uuid": str(uuid.uuid4()), "relative_path": "",
            "source": "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode(),
        }],
    }


def zombify(texture):
    """Placeholder zombie girl: the maid texture with its skin recolored zombie green."""
    img = texture.copy()
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if a and r > 200 and r > g > b and 150 < g < 225 and 110 < b < 205:  # skin tones
                shade = (r + g + b) / (3 * 230)
                px[x, y] = (int(110 * shade), int(165 * shade), int(85 * shade), a)
    return img


def main():
    maid = Image.open(MAID_TEXTURES / "mob_littlemaid_0c.png").convert("RGBA")
    (BB_DIR / "littlemaid.bbmodel").write_text(json.dumps(bbmodel("littlemaid", maid), indent=1))

    zombie = zombify(maid)
    TEX_DIR.mkdir(parents=True, exist_ok=True)
    zombie.save(TEX_DIR / "zombie_girl.png")
    (BB_DIR / "zombie_girl.bbmodel").write_text(json.dumps(bbmodel("zombie_girl", zombie), indent=1))


if __name__ == "__main__":
    main()
