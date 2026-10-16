package dev.linqfy.bigCasares.modules.legendary;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LegendaryModelAssetsTest {
    private static final Path ASSETS = Path.of("pack/assets");
    private static JsonObject json(Path file) throws Exception {
        return JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }
    private static Set<String> sprites() throws Exception {
        var sprites = new HashSet<String>();
        var source = json(ASSETS.resolve("minecraft/atlases/items.json")).getAsJsonArray("sources").get(0).getAsJsonObject();
        for (var entry : source.getAsJsonArray("regions")) sprites.add(entry.getAsJsonObject().get("sprite").getAsString());
        return sprites;
    }

    @Test void nativeAtlasExtractsPowerOfTwoMaterialsInsideTheUnmodifiedSource() throws Exception {
        var sources = json(ASSETS.resolve("minecraft/atlases/items.json")).getAsJsonArray("sources");
        assertEquals(1, sources.size());
        var source = sources.get(0).getAsJsonObject();
        assertEquals("minecraft:unstitch", source.get("type").getAsString());
        assertEquals("bigcasares:legendary/material_atlas", source.get("resource").getAsString());
        var image = ImageIO.read(ASSETS.resolve("bigcasares/textures/legendary/material_atlas.png").toFile());
        assertNotNull(image);
        assertEquals(image.getWidth(), source.get("divisor_x").getAsInt());
        assertEquals(image.getHeight(), source.get("divisor_y").getAsInt());
        for (var region : source.getAsJsonArray("regions")) {
            var r = region.getAsJsonObject();
            int width = r.get("width").getAsInt(), height = r.get("height").getAsInt();
            assertTrue(width > 0 && (width & (width - 1)) == 0);
            assertTrue(height > 0 && (height & (height - 1)) == 0);
            assertTrue(r.get("x").getAsInt() >= 0 && r.get("y").getAsInt() >= 0);
            assertTrue(r.get("x").getAsInt() + width <= image.getWidth());
            assertTrue(r.get("y").getAsInt() + height <= image.getHeight());
        }
        assertEquals(64, sprites().size());
    }

    @Test void allEightModelsResolveTheirVirtualSpritesAndUseValidNativeCuboids() throws Exception {
        Set<String> sprites = sprites();
        for (var weapon : LegendaryWeapon.values()) {
            var model = json(ASSETS.resolve("bigcasares/models/item/" + weapon.id + ".json"));
            var textures = model.getAsJsonObject("textures");
            for (var element : model.getAsJsonArray("elements")) {
                var box = element.getAsJsonObject();
                for (int axis = 0; axis < 3; axis++) {
                    double from = box.getAsJsonArray("from").get(axis).getAsDouble();
                    double to = box.getAsJsonArray("to").get(axis).getAsDouble();
                    assertTrue(from >= -16 && from < to && to <= 32, weapon.id + " has an invalid cuboid");
                }
                int emission = box.get("light_emission").getAsInt();
                assertTrue(emission >= 0 && emission <= 15);
                for (var face : box.getAsJsonObject("faces").entrySet()) {
                    var data = face.getValue().getAsJsonObject();
                    String role = data.get("texture").getAsString().substring(1);
                    assertTrue(sprites.contains(textures.get(role).getAsString()), weapon.id + ": missing material " + role);
                    for (var coordinate : data.getAsJsonArray("uv")) assertTrue(coordinate.getAsDouble() >= 0 && coordinate.getAsDouble() <= 16);
                }
            }
            for (String view : Set.of("gui", "firstperson_righthand", "firstperson_lefthand", "ground", "fixed")) {
                assertNotNull(model.getAsJsonObject("display").get(view));
            }
            String particle = textures.get(textures.get("particle").getAsString().substring(1)).getAsString();
            assertTrue(sprites.contains(particle));
        }
    }

    @Test void existingItemIdsKeepTheirModelBindingsAndAtlasOnlyAddsAgreedWeapons() throws Exception {
        Set<String> names = new HashSet<>();
        for (var weapon : LegendaryWeapon.values()) {
            names.add(weapon.id);
            var definition = json(ASSETS.resolve("bigcasares/items/" + weapon.id + ".json")).getAsJsonObject("model");
            assertEquals("minecraft:model", definition.get("type").getAsString());
            assertEquals("bigcasares:item/" + weapon.id, definition.get("model").getAsString());
        }
        for (String sprite : sprites()) {
            String path = sprite.substring("bigcasares:item/legendary/".length());
            assertTrue(names.contains(path.substring(0, path.indexOf('/'))));
            assertFalse(sprite.contains("copper"));
        }
    }
}
