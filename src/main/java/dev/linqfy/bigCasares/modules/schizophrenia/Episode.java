package dev.linqfy.bigCasares.modules.schizophrenia;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** One player's ongoing breakdown. Lives only while the player is online; the start time persists. */
final class Episode {

    private final UUID playerId;
    private final long startedAtMillis;
    private final int startIntensity;
    private final EpisodeTimeline timeline = new EpisodeTimeline();
    private final List<PhantomClone> phantoms = new ArrayList<>();
    private long nextHallucinationTick;
    private Hallucination previous;

    Episode(UUID playerId, long startedAtMillis, int startIntensity, long firstHallucinationTick) {
        this.playerId = playerId;
        this.startedAtMillis = startedAtMillis;
        this.startIntensity = EpisodeMath.clampIntensity(startIntensity);
        this.nextHallucinationTick = firstHallucinationTick;
    }

    UUID playerId() {
        return playerId;
    }

    long startedAtMillis() {
        return startedAtMillis;
    }

    int startIntensity() {
        return startIntensity;
    }

    EpisodeTimeline timeline() {
        return timeline;
    }

    List<PhantomClone> phantoms() {
        return phantoms;
    }

    long nextHallucinationTick() {
        return nextHallucinationTick;
    }

    void nextHallucinationTick(long tick) {
        this.nextHallucinationTick = tick;
    }

    Hallucination previous() {
        return previous;
    }

    void previous(Hallucination hallucination) {
        this.previous = hallucination;
    }

    int intensity(long nowMillis, double minutesPerIntensity) {
        return EpisodeMath.intensity(startIntensity, nowMillis - startedAtMillis, minutesPerIntensity);
    }
}
