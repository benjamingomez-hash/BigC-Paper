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

## Completion

Published source on feat/legendary-six-weapons, commit42f92f8. Standalone clean
build:100tests passed; full main repository clean build:1014tests passed. Native
Paper/two-client checks cover actual model rendering, supplied animation metadata,
new skills, armor, native inputs, kill progression and finite cleanup. Attribute
swapping captured before listeners and delayed storm after equipment change both
awarded one level to the original correct weapon, never both. Full test report
and limitations: standalone/docs/user-abilities-native-2.0.15.json.

Windows run41155884363 passed all five Defender scans. Published exact Windows
JAR/ZIP and SHA256 without rebuilding. All classes and pack files match the tested
Linux payload; one empty-directory ZIP entry is the only packaging difference.
Restored all ten original native-fixture files and original main client options
byte for byte, preserving modified test data separately. Final PvP balance remains
for playtesting as requested; WorldGuard was not installed in the native fixture.
