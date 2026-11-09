package dev.linqfy.bigCasares.modules.schizophrenia;

import java.util.Locale;
import java.util.Optional;

/**
 * Everything the brain can invent. {@code minIntensity} gates the nastier ones behind a longer
 * untreated episode; {@code weight} is the relative chance once unlocked.
 */
public enum Hallucination {
    FOOTSTEPS(1, 10),
    CREEPER_HISS(1, 8),
    DISTANT_ROAR(1, 6),
    WHISPERS(1, 8),
    PHANTOM_CLONE(1, 10),
    SILHOUETTE(1, 6),
    MINING_BEHIND(2, 5),
    MOB_SOUNDS(2, 6),
    DOOR_KNOCKING(2, 4),
    HEARTBEAT(2, 5),
    FAKE_HURT(2, 5),
    FAKE_CHAT(2, 6),
    DOPPELGANGER(2, 4),
    BLOCK_ROT(3, 5),
    LIGHTS_OUT(3, 4),
    MASKED_PLAYERS(3, 5),
    RED_VIGNETTE(3, 4),
    STALKER(3, 6),
    GLITCH_TITLE(3, 5),
    FAKE_EXPLOSION(4, 3),
    CAMERA_SNAP(4, 3),
    ELDER_GUARDIAN(4, 2),
    ETERNAL_NIGHT(4, 3),
    FAKE_THUNDER(4, 3),
    SWARM(5, 3),
    FAKE_DEATH(5, 2);

    private final int minIntensity;
    private final int weight;

    Hallucination(int minIntensity, int weight) {
        this.minIntensity = minIntensity;
        this.weight = weight;
    }

    public int minIntensity() {
        return minIntensity;
    }

    public int weight() {
        return weight;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static Optional<Hallucination> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        for (Hallucination value : values()) {
            if (value.name().equals(normalized)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
