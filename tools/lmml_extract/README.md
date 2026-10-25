# LittleMaid model extractor

Turns the maid models compiled into LittleMaidModelLoader (LMML) into Blockbench projects.

How it works: `src/` holds recording replacements for LMML's `ModelRenderer`, `ModelBox` and
`ModelPlate` plus a few empty Minecraft stubs. `Dump.java` runs each real model class from the LMML
jar against them (`initModel` → `setLivingAnimations` → `setRotationAngles` with zero motion) and
prints the resulting part tree as JSON; `to_bbmodel.py` converts that to `.bbmodel`.

```bash
mkdir lmml && (cd lmml && unzip -q /path/to/LMML-1.18.2-4.6.7-Forge.jar)
mkdir out && javac -nowarn -d out -cp lmml $(find src -name "*.java")
for m in ModelLittleMaid_Orign ModelLittleMaid_Archetype ModelLittleMaid_Aug ModelLittleMaid_Beverly7 \
         ModelLittleMaid_Chloe2 ModelLittleMaid_Elsa5 ModelLittleMaid_RX0 ModelLittleMaid_SR2 \
         ModelMulti_Classic64 ModelMulti_Slim64 ModelMulti_Stef ModelMulti_Steve; do
  java -XX:+UnlockDiagnosticVMOptions -XX:-BytecodeVerificationRemote -XX:-BytecodeVerificationLocal \
       -cp out:lmml Dump $m > dump_$m.json
done
python3 convert_all.py . lmml/assets/minecraft/textures/entity/littlemaid ../../blockbench/littlemaid_models
```

Requires JDK 17+ and Pillow.
