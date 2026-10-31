# User-supplied legendary models and animations

Authorized scope: integrate all five supplied projects and eleven animations in
the standalone Paper 26.2 plugin, retaining five active weapons and existing
combat/persistence rules. Frost, Void Katana and Hyperion remain retired.
Preserve uploaded originals and previous model proposals. No external plugin
or client mod dependency will be added.

Implementation: sample the real Blockbench animation engine at 20 Hz, bake native
26.2 Euler cuboid item models from scene matrices, retain individual textures,
and select frames through an owned custom-model-data float. Keep GUI icons static
and hand-swap animation disabled. Main-hand attacks and accepted ability activation
select supplied clips; idle loops play while held; spear displays use its rayo
clip. Animation updates must skip pending inventory transactions, preserve all
other item metadata and clean up on stop/quit/drop.

Validation: compare sampled cuboid transforms/UVs with Blockbench, test frame
selection and interruption/recovery, full standalone build, real Paper/client
capture including animation frame changes and unchanged weapon rules, then Windows
Defender workflow. Publish version 2.0.15 using the exact clean Windows artifacts,
with originals, sources, documentation, hashes and download URLs on the existing
feature branch. Public preview approval is granted by “integra todo”.
