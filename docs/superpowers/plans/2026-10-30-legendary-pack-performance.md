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
