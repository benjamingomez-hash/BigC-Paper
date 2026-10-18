package dev.linqfy.bigCasares.modules.legendary;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public enum LegendaryWeapon {
    BLOODLUST("bloodlust", "Bloodlust", "NETHERITE_SWORD", "blood-fury", "Furia de Sangre", "Santuario de Sangre", "NETHER", "CRIMSON_FOREST",
        Map.of("NETHERITE_SWORD", 1, "REDSTONE_BLOCK", 4, "GHAST_TEAR", 2, "NETHERITE_BLOCK", 2)),
    FROST_SCYTHE("frost_scythe", "Frost Scythe", "DIAMOND_HOE", "glacial-storm", "Tormenta Glacial", "Santuario Glacial", "NORMAL", "FROZEN_PEAKS",
        Map.of("DIAMOND_HOE", 1, "BLUE_ICE", 4, "HEART_OF_THE_SEA", 2, "DIAMOND_BLOCK", 2)),
    SHADOW_BLADE("shadow_blade", "Shadow Blade", "NETHERITE_SWORD", "shadow-assassin", "Asesino de Sombras", "Santuario de Sombras", "NORMAL", "DEEP_DARK",
        Map.of("NETHERITE_SWORD", 1, "ENDER_PEARL", 4, "PHANTOM_MEMBRANE", 2, "CRYING_OBSIDIAN", 2)),
    HYPERION("hyperion", "Hyperion", "BLAZE_ROD", "arcane-destruction", "Destrucción Arcana", "Santuario Arcano", "THE_END", "END_HIGHLANDS",
        Map.of("BLAZE_ROD", 1, "AMETHYST_BLOCK", 4, "NETHER_STAR", 2, "GOLD_BLOCK", 2)),
    WINDWEAVER("windweaver", "Windweaver", "MACE", "divine-tornado", "Impulso Divino", "Santuario del Viento", "NORMAL", "JAGGED_PEAKS",
        Map.of("MACE", 1, "WIND_CHARGE", 4, "FEATHER", 2, "IRON_BLOCK", 2)),
    EARTHBREAKER("earthbreaker", "Earthbreaker", "NETHERITE_AXE", "titan-earthquake", "Terremoto Titán", "Santuario de la Tierra", "NORMAL", "DRIPSTONE_CAVES",
        Map.of("NETHERITE_AXE", 1, "DEEPSLATE", 4, "EMERALD_BLOCK", 2, "NETHERITE_INGOT", 2)),
    VOID_KATANA("void_katana", "Void Katana", "DIAMOND_SWORD", "void-dimension", "Dimensión del Vacío", "Santuario del Vacío", "THE_END", "END_HIGHLANDS",
        Map.of("DIAMOND_SWORD", 1, "ENDER_EYE", 4, "CHORUS_FRUIT", 2, "PURPUR_BLOCK", 2)),
    THUNDER_SPEAR("thunder_spear", "Thunder Spear", "NETHERITE_SPEAR", "thunder-javelin", "Jabalina del Trueno", "Santuario de Tormentas", "NORMAL", "STONY_PEAKS",
        Map.of("NETHERITE_SPEAR", 1, "LIGHTNING_ROD", 4, "HEART_OF_THE_SEA", 2, "COPPER_BLOCK", 2));

    public static final long COOLDOWN_MILLIS = 40_000;
    public static final int STORM_TICKS = 200;
    public static final int LIGHTNING_INTERVAL_TICKS = 40;
    public final String id;
    public final String displayName;
    public final String material;
    public final String abilityId;
    public final String abilityName;
    public final String altarName;
    public final String environment;
    public final String biome;
    public final Map<String, Integer> ingredients;

    LegendaryWeapon(String id, String displayName, String material, String abilityId, String abilityName,
                    String altarName, String environment, String biome, Map<String, Integer> ingredients) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.abilityId = abilityId;
        this.abilityName = abilityName;
        this.altarName = altarName;
        this.environment = environment;
        this.biome = biome;
        this.ingredients = Map.copyOf(ingredients);
    }

    public static Optional<LegendaryWeapon> find(String id) {
        return Arrays.stream(values()).filter(weapon -> weapon.id.equalsIgnoreCase(id)).findFirst();
    }

    public boolean canCraft(Map<String, Integer> inventory) {
        return ingredients.entrySet().stream().allMatch(entry -> inventory.getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    public String abilityDescription() {
        return switch (this) {
            case BLOODLUST -> "Fuerza III durante 10 segundos.";
            case FROST_SCYTHE -> "Aura de 8 s y radio 6: congelación y Lentitud IV para cualquier entidad viva enemiga.";
            case SHADOW_BLADE -> "Emboscada: teletransporte tras el rival, 14 de daño mágico, Debilidad II y Oscuridad; Invisibilidad y Velocidad III durante 5 s.";
            case HYPERION -> "Ráfaga de cinco bolas de fuego explosivas de ghast; apunta mientras disparas.";
            case WINDWEAVER -> "Impulso vertical para atacar con la maza; protege la primera caída del salto durante un máximo de 10 s.";
            case EARTHBREAKER -> "Cráter de radio 8, profundidad 4/3/2/1; un golpe de 2 corazones de salud que ignora armadura y reducciones.";
            case VOID_KATANA -> "Tres cortes con desplazamiento y Oscuridad.";
            case THUNDER_SPEAR -> "Campo eléctrico de 10 s que paraliza a ambos; rayos dañinos solo al impacto directo. Regresa; Lunge III nativo.";
        };
    }
}
