"""Convert every dump_<Model>.json in a directory into a .bbmodel.

Usage: python3 convert_all.py <dump dir> <LMML littlemaid texture dir> <output dir>
"""
import json
import sys
from pathlib import Path

from PIL import Image

import to_bbmodel

DUMPS = Path(sys.argv[1])
TEX = Path(sys.argv[2])
OUT = Path(sys.argv[3])
OUT.mkdir(parents=True, exist_ok=True)

MODELS = {
    "ModelLittleMaid_Orign": "default/mob_littlemaid_0c.png",
    "ModelLittleMaid_Archetype": None,
    "ModelLittleMaid_Aug": "mmm_aug/aug_0c.png",
    "ModelLittleMaid_Beverly7": "b16color_beverly7/contract_c.png",
    "ModelLittleMaid_Chloe2": "b16color_chloe2/contract_c.png",
    "ModelLittleMaid_Elsa5": "e15color_elsa5/contract_c.png",
    "ModelLittleMaid_RX0": None,
    "ModelLittleMaid_SR2": "mmm_sr2/engageoctaver_0c.png",
    "ModelMulti_Classic64": None,
    "ModelMulti_Slim64": None,
    "ModelMulti_Stef": "stef_stef/stef_00.png",
    "ModelMulti_Steve": "steve_steve/steve_00.png",
}

for model, tex in MODELS.items():
    dump = json.load(open(DUMPS / f"dump_{model}.json"))
    texture = Image.open(TEX / tex).convert("RGBA") if tex else to_bbmodel.uv_template(dump)
    name = model.split("_", 1)[1]
    with open(OUT / f"{name}.bbmodel", "w") as f:
        json.dump(to_bbmodel.convert(dump, texture), f, indent=1)
    print(f"{name}: {dump['texW']}x{dump['texH']}, height {dump['height']:.2f}, texture {tex or 'UV template'}")
