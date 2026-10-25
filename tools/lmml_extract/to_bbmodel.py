"""Convert LittleMaid model dumps (from Dump.java) into Blockbench 'Modded Entity' projects.

Usage: python3 to_bbmodel.py <dump.json> <out.bbmodel> [texture.png]

Coordinates: LittleMaid/Java models are y-down with x pointing to the model's left;
Blockbench is y-up with x mirrored, so (x, y, z) -> (-x, ground - y, z) and rotations
(rx, ry, rz) -> (-rx, -ry, rz), the same mapping Blockbench uses for Java entity export.
"""
import base64
import json
import math
import sys
import uuid

from PIL import Image, ImageDraw


def walk(part, parent_abs=(0.0, 0.0, 0.0)):
    """Yield (part, absolute pivot) depth-first; absolute = sum of pivots and offsets."""
    px, py, pz = part["pivot"]
    ox, oy, oz = (o * 16 for o in part["offset"])
    ab = (parent_abs[0] + px + ox, parent_abs[1] + py + oy, parent_abs[2] + pz + oz)
    yield part, ab
    for child in part["children"]:
        yield from walk(child, ab)


def cube_bounds(cube, scale):
    """Local (from, to) of a cube or plate, scaled the way ModelRenderer scales its display list."""
    x, y, z = cube["from"]
    w, h, d = cube["size"]
    if cube["kind"] == "plate":
        plane = cube["plane"] & 3
        w, h, d = {0: (w, h, 0), 1: (0, h, w), 2: (w, 0, h)}[plane]
    inf = cube["inflate"]
    lo = [x, y, z]
    hi = [x + w, y + h, z + d]
    sx, sy, sz = scale
    return [lo[0] * sx, lo[1] * sy, lo[2] * sz], [hi[0] * sx, hi[1] * sy, hi[2] * sz], inf


def box_faces(u, v, w, h, d):
    return {
        "north": [u + d, v + d, u + d + w, v + d + h],
        "east":  [u, v + d, u + d, v + d + h],
        "south": [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h],
        "west":  [u + d + w, v + d, u + 2 * d + w, v + d + h],
        "up":    [u + d + w, v + d, u + d, v],
        "down":  [u + d + 2 * w, v, u + d + w, v + d],
    }


def plate_faces(cube):
    u, v = cube["uv"]
    w, h = cube["size"][0], cube["size"][1]
    rect = [u + w, v, u, v + h] if cube["mirror"] else [u, v, u + w, v + h]
    sides = {0: ("north", "south"), 1: ("east", "west"), 2: ("up", "down")}[cube["plane"] & 3]
    return {f: ({"uv": rect, "texture": 0} if f in sides else {"uv": [0, 0, 0, 0], "texture": None})
            for f in ("north", "east", "south", "west", "up", "down")}


def uv_template(dump):
    """Colour each cube's UV island so models without a bundled texture can still be painted."""
    img = Image.new("RGBA", (dump["texW"], dump["texH"]), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    palette = [(230, 120, 120), (120, 200, 120), (120, 150, 230), (230, 200, 110), (190, 130, 220), (110, 210, 210)]
    for part, _ in walk(dump["root"]):
        for cube in part["cubes"]:
            if cube["kind"] == "plate":
                u, v = cube["uv"]
                draw.rectangle((u, v, u + cube["size"][0] - 1, v + cube["size"][1] - 1), fill=(200, 200, 200))
                continue
            w, h, d = cube["size"]
            for i, r in enumerate(box_faces(*cube["uv"], w, h, d).values()):
                x0, x1 = sorted((r[0], r[2]))
                y0, y1 = sorted((r[1], r[3]))
                if x1 > x0 and y1 > y0:
                    draw.rectangle((x0, y0, x1 - 1, y1 - 1), fill=palette[i])
    return img


def convert(dump, texture):
    parts = list(walk(dump["root"]))
    # Stand the model on the ground: the lowest point of any cube becomes y = 0.
    ground = max(ab[1] + cube_bounds(c, p["scale"])[1][1] for p, ab in parts for c in p["cubes"])

    def to_bb(x, y, z):
        return [round(-x, 4), round(ground - y, 4), round(z, 4)]

    used = {}

    def unique(name):
        used[name] = used.get(name, 0) + 1
        return name if used[name] == 1 else f"{name}{used[name]}"

    elements = []

    def build(part, ab):
        group = {
            "name": unique(part["name"]), "origin": to_bb(*ab),
            "rotation": [round(-math.degrees(part["rot"][0]), 4), round(-math.degrees(part["rot"][1]), 4),
                         round(math.degrees(part["rot"][2]), 4)],
            "color": 0, "uuid": str(uuid.uuid4()), "export": True, "mirror_uv": False,
            "isOpen": True, "locked": False, "visibility": part["visible"], "autouv": 0, "children": [],
        }
        for i, cube in enumerate(part["cubes"]):
            lo, hi, inf = cube_bounds(cube, part["scale"])
            a_lo = [ab[k] + lo[k] for k in range(3)]
            a_hi = [ab[k] + hi[k] for k in range(3)]
            el = {
                "name": f"{part['name']}_{i}", "rescale": False, "locked": False,
                "render_order": "default", "allow_mirror_modeling": True,
                "from": [round(-a_hi[0], 4), round(ground - a_hi[1], 4), round(a_lo[2], 4)],
                "to": [round(-a_lo[0], 4), round(ground - a_lo[1], 4), round(a_hi[2], 4)],
                "autouv": 0, "color": i % 8, "inflate": inf, "origin": to_bb(*ab),
                "type": "cube", "uuid": str(uuid.uuid4()),
            }
            if cube["kind"] == "plate":
                el.update(box_uv=False, faces=plate_faces(cube))
            else:
                w, h, d = cube["size"]
                el.update(box_uv=True, mirror_uv=cube["mirror"], uv_offset=list(cube["uv"]),
                          faces={f: {"uv": r, "texture": 0} for f, r in box_faces(*cube["uv"], w, h, d).items()})
            elements.append(el)
            group["children"].append(el["uuid"])
        return group

    def tree(part, ab):
        group = build(part, ab)
        for child in part["children"]:
            child_ab = next(a for p, a in parts if p is child)
            group["children"].append(tree(child, child_ab))
        return group

    import io
    buf = io.BytesIO()
    texture.save(buf, "PNG")
    tex_name = dump["model"] + ".png"
    return {
        "meta": {"format_version": "4.10", "model_format": "modded_entity", "box_uv": True},
        "name": dump["model"], "model_identifier": "", "modded_entity_entity_class": "",
        "modded_entity_version": "Mojmaps", "modded_entity_flip_y": True,
        "visible_box": [1, 2, 0], "variable_placeholders": "", "variable_placeholder_buttons": [],
        "timeline_setups": [], "unhandled_root_fields": {},
        "resolution": {"width": dump["texW"], "height": dump["texH"]},
        "elements": elements, "outliner": [tree(dump["root"], parts[0][1])],
        "textures": [{
            "path": "", "name": tex_name, "folder": "", "namespace": "", "id": "0",
            "width": texture.width, "height": texture.height,
            "uv_width": dump["texW"], "uv_height": dump["texH"], "particle": False,
            "layers_enabled": False, "render_mode": "default", "render_sides": "auto",
            "frame_time": 1, "frame_order_type": "loop", "frame_order": "", "frame_interpolate": False,
            "visible": True, "internal": True, "saved": False, "uuid": str(uuid.uuid4()),
            "relative_path": "", "source": "data:image/png;base64," + base64.b64encode(buf.getvalue()).decode(),
        }],
    }


def main():
    dump = json.load(open(sys.argv[1]))
    texture = Image.open(sys.argv[3]).convert("RGBA") if len(sys.argv) > 3 else uv_template(dump)
    with open(sys.argv[2], "w") as f:
        json.dump(convert(dump, texture), f, indent=1)


if __name__ == "__main__":
    main()
