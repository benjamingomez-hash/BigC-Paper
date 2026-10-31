# Preserve weapon appearance while reducing client rendering cost

The user reports fewer than 20 FPS with the 2.0.15 supplied-model pack. Keep the
five approved silhouettes, colors, original Blockbench projects, gameplay and
persistent identifiers. Retain an exact pack backup before editing generated assets.

1. Measure the current client in a fixed scene and inspect animation/model costs.
2. Remove only hidden opaque surfaces and merge compatible coplanar palette faces.
   Preserve visible material coverage and world geometry; leave complex parts
   unchanged when an exact safe optimization is unavailable.
3. Resample animation poses without changing clip durations or gameplay timing;
   update both item-model JSON references and server frame sequences, retaining
   routing compatibility for the previous frame IDs.
4. Ensure pack updates preserve backups and manual customized packs. Ship a new
   independent JAR and a pack ZIP with pack.mcmeta at the archive root.
5. Test geometry invariants, frame routing, persistence/metadata and packaging;
   compare real Minecraft rendering/frame measurements under the same conditions.
6. Publish the reviewed branch and exact Windows Defender-checked artifacts with
   measured results and limits. Restore the private native fixture byte for byte.

## Completion

Source b5cd073 on feat/legendary-six-weapons. Final geometry stores 16,711 faces
instead of 188,184 (91.12% less), 128 geometry meshes instead of342, preserving
all eleven clip durations and every prior frame ID. Earthbreaker213cuboids/841faces;
Shadow138/778 unchanged because whole-sprite translucency requires original quad
order. Transparent animation sharing preserves contiguous original runs.

107 Java tests,8 Python optimizer tests,7,287 unambiguous surface rays and eight
native-renderer Shadow pose comparisons passed. Static rest pixels match; mean
animated-region channel error below1.17/255. Normalized item metadata preserved,
static-idle option still plays attacks and returns to rest. Main build1,014tests.
No sustained FPS uplift established on software OpenGL: empty-scene controls
with and without the original pack both around25FPS. Do not promise user hardware
FPS; detailed data and limitation recorded in performance-verification-2.0.16.json.

Windows run41163346528 passed five Defender scans; publish its exact JAR, ZIP and
inner pack bytes with hashes. Restore10baselinefixturefiles and client options,
archive test modifications, remove test resource pack, stop owned processes.
