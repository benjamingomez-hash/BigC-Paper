package dev.linqfy.bigCasares.modules.legendary;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

class LegendaryModelAssetsTest {
    private static final Path ASSETS = Path.of("pack/assets");

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    @Test
    void suppliedTexturesAreCopiedExactlyAndLegacyAtlasRegionsRemainAvailable() throws Exception {
        var sources =
                json(ASSETS.resolve("minecraft/atlases/items.json")).getAsJsonArray("sources");
        assertEquals(7, sources.size());
        var singles = new HashSet<String>();
        int historical = 0;
        for (var source : sources) {
            var s = source.getAsJsonObject();
            if (s.get("type").getAsString().equals("minecraft:single"))
                singles.add(s.get("resource").getAsString());
            else historical += s.getAsJsonArray("regions").size();
        }
        assertEquals(112, historical);
        for (var weapon : LegendaryWeapon.available()) {
            assertTrue(singles.contains("bigcasares:item/user/" + weapon.id));
            var original = json(Path.of("models/reference-study/" + weapon.id + ".bbmodel"));
            byte[] embedded =
                    Base64.getDecoder()
                            .decode(
                                    original.getAsJsonArray("textures")
                                            .get(0)
                                            .getAsJsonObject()
                                            .get("source")
                                            .getAsString()
                                            .split(",", 2)[1]);
            byte[] exported =
                    Files.readAllBytes(
                            ASSETS.resolve("bigcasares/textures/item/user/" + weapon.id + ".png"));
            assertArrayEquals(embedded, exported, weapon.id + " texture changed");
            var image = ImageIO.read(new ByteArrayInputStream(exported));
            assertEquals(16, image.getWidth());
            assertEquals(16, image.getHeight());
        }
        assertEquals(5, singles.size());
    }

    @Test
    void everyBakedPoseUsesNativeCoordinatesResolvedTexturesAndTheSameParent() throws Exception {
        try (var paths = Files.walk(ASSETS.resolve("bigcasares/models/item"))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                var model = json(path);
                if (model.has("parent")) {
                    String parent = model.get("parent").getAsString();
                    assertTrue(
                            Files.exists(
                                    ASSETS.resolve(
                                            "bigcasares/models/"
                                                    + parent.split(":", 2)[1]
                                                    + ".json")));
                }
                if (!model.has("elements")) continue;
                for (var item : model.getAsJsonArray("elements")) {
                    var e = item.getAsJsonObject();
                    for (int axis = 0; axis < 3; axis++) {
                        double from = e.getAsJsonArray("from").get(axis).getAsDouble(),
                                to = e.getAsJsonArray("to").get(axis).getAsDouble();
                        assertTrue(
                                Double.isFinite(from)
                                        && Double.isFinite(to)
                                        && from < to
                                        && from >= -16
                                        && to <= 32,
                                path.toString());
                    }
                    if (e.has("rotation")) {
                        var r = e.getAsJsonObject("rotation");
                        assertEquals(3, r.getAsJsonArray("origin").size());
                        if (r.has("axis"))
                            assertTrue(Set.of("x", "y", "z").contains(r.get("axis").getAsString()));
                        else
                            for (String axis : Set.of("x", "y", "z"))
                                assertTrue(Double.isFinite(r.get(axis).getAsDouble()));
                    }
                    for (var face : e.getAsJsonObject("faces").entrySet()) {
                        assertNotNull(face.getValue().getAsJsonObject().get("texture"));
                        for (var uv : face.getValue().getAsJsonObject().getAsJsonArray("uv"))
                            assertTrue(uv.getAsDouble() >= 0 && uv.getAsDouble() <= 16);
                    }
                }
            }
        }
    }

    @Test
    void everyAnimationFrameResolvesAndGuiKeepsAStableRestPoseWithoutSwapBobbing()
            throws Exception {
        LegendaryAnimationCatalog catalog;
        try (var input =
                Files.newInputStream(
                        Path.of("src/main/resources/legendary-animations.properties"))) {
            catalog = LegendaryAnimationCatalog.load(input);
        }
        var exported =
                json(Path.of("docs/user-models-export-2.0.15.json")).getAsJsonObject("weapons");
        int clips = 0;
        for (var weapon : LegendaryWeapon.available()) {
            var definition = json(ASSETS.resolve("bigcasares/items/" + weapon.id + ".json"));
            assertFalse(definition.get("hand_animation_on_swap").getAsBoolean());
            var select = definition.getAsJsonObject("model");
            assertEquals("minecraft:display_context", select.get("property").getAsString());
            var gui = select.getAsJsonArray("cases").get(0).getAsJsonObject();
            assertEquals("gui", gui.get("when").getAsString());
            assertEquals(
                    "bigcasares:item/" + weapon.id,
                    gui.getAsJsonObject("model").get("model").getAsString());
            var entries = select.getAsJsonObject("fallback").getAsJsonArray("entries");
            var frames = new HashMap<Integer, String>();
            for (var entry : entries) {
                var e = entry.getAsJsonObject();
                var graph = e.getAsJsonObject("model");
                checkGraph(graph);
                assertNull(frames.put(e.get("threshold").getAsInt(), graph.toString()));
            }

            for (var clip :
                    exported.getAsJsonObject(weapon.id).getAsJsonObject("clips").entrySet()) {
                var sequence = catalog.clip(weapon, clip.getKey());
                assertNotNull(sequence);
                assertEquals(
                        clip.getValue().getAsJsonObject().get("ticks").getAsInt(),
                        sequence.duration());
                for (int frame : sequence.sequence()) assertTrue(frames.containsKey(frame));
                clips++;
            }
        }
        assertEquals(11, clips);
    }

    private static void checkGraph(JsonObject graph) throws Exception {
        if (graph.get("type").getAsString().equals("minecraft:composite")) {
            assertTrue(graph.getAsJsonArray("models").size() <= 4);
            for (var child : graph.getAsJsonArray("models")) checkGraph(child.getAsJsonObject());
            return;
        }
        assertEquals("minecraft:model", graph.get("type").getAsString());
        String model = graph.get("model").getAsString();
        assertTrue(
                Files.exists(
                        ASSETS.resolve("bigcasares/models/" + model.split(":", 2)[1] + ".json")),
                model);
        if (graph.has("transformation")) {
            var matrix = graph.getAsJsonArray("transformation");
            assertEquals(16, matrix.size());
            for (var value : matrix) assertTrue(Double.isFinite(value.getAsDouble()));
            assertEquals(1, matrix.get(15).getAsDouble());
        }
    }

    @Test
    void optimizedMeshesReduceStoredFacesAndRetainEveryOldFrameId() throws Exception {
        var optimization =
                json(Path.of("docs/model-optimization-2.0.16.json")).getAsJsonObject("weapons");
        var source =
                json(Path.of("docs/user-models-export-2.0.15.json")).getAsJsonObject("weapons");
        long before = 0, after = 0;
        for (var weapon : LegendaryWeapon.available()) {
            var stats = optimization.getAsJsonObject(weapon.id);
            before += stats.get("storedFacesBefore").getAsLong();
            long actualFaces =
                    faceCount(ASSETS.resolve("bigcasares/models/item/" + weapon.id + ".json"));
            Path optimized = ASSETS.resolve("bigcasares/models/item/optimized/" + weapon.id);
            if (Files.exists(optimized))
                try (var files = Files.list(optimized)) {
                    for (Path model : files.filter(p -> p.toString().endsWith(".json")).toList())
                        actualFaces += faceCount(model);
                }
            assertEquals(stats.get("storedFacesAfter").getAsLong(), actualFaces);
            after += actualFaces;
            var graph =
                    json(ASSETS.resolve("bigcasares/items/" + weapon.id + ".json"))
                            .getAsJsonObject("model")
                            .getAsJsonObject("fallback");
            assertEquals(
                    source.getAsJsonObject(weapon.id).get("uniquePoses").getAsInt(),
                    graph.getAsJsonArray("entries").size());
        }
        assertTrue(after < before / 5, "Stored face payload should fall by at least 80%");
    }

    private static long faceCount(Path model) throws Exception {
        long faces = 0;
        for (var element : json(model).getAsJsonArray("elements"))
            faces += element.getAsJsonObject().getAsJsonObject("faces").size();
        return faces;
    }

    @Test
    void translucentShadowRetainsEveryOriginalCubeAndItsOrder() throws Exception {
        try (var archive = new java.util.zip.ZipFile("models/archive/legendary-pack-2.0.15.zip")) {
            try (var input =
                    archive.getInputStream(
                            archive.getEntry("assets/bigcasares/models/item/shadow_blade.json"))) {
                var original =
                        JsonParser.parseString(
                                        new String(
                                                input.readAllBytes(),
                                                java.nio.charset.StandardCharsets.UTF_8))
                                .getAsJsonObject();
                assertEquals(
                        original.getAsJsonArray("elements"),
                        json(ASSETS.resolve("bigcasares/models/item/shadow_blade.json"))
                                .getAsJsonArray("elements"));
            }
        }
    }
}
