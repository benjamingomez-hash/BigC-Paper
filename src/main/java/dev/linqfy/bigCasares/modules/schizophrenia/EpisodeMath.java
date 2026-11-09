package dev.linqfy.bigCasares.modules.schizophrenia;

/** Pure timing rules: how bad an episode has become and how often it strikes. */
final class EpisodeMath {

    static final int MAX_INTENSITY = 5;

    private EpisodeMath() {
    }

    static int intensity(int startIntensity, long elapsedMillis, double minutesPerIntensity) {
        int base = clampIntensity(startIntensity);
        if (elapsedMillis <= 0) {
            return base;
        }
        double minutes = elapsedMillis / 60_000.0;
        return clampIntensity(base + (int) Math.floor(minutes / minutesPerIntensity));
    }

    static int clampIntensity(int intensity) {
        return Math.max(1, Math.min(MAX_INTENSITY, intensity));
    }

    /** Seconds until the next hallucination: slowest at intensity 1, fastest at 5, with jitter in [0,1). */
    static double intervalSeconds(int intensity, int slowest, int fastest, double jitter) {
        double progress = (clampIntensity(intensity) - 1) / (double) (MAX_INTENSITY - 1);
        double base = slowest + (fastest - slowest) * progress;
        double spread = 0.6 + 0.8 * Math.max(0.0, Math.min(1.0, jitter));
        return Math.max(1.0, base * spread);
    }

    static boolean expired(long elapsedMillis, int maxEpisodeMinutes) {
        return maxEpisodeMinutes > 0 && elapsedMillis >= maxEpisodeMinutes * 60_000L;
    }

    static boolean naturalOnsetConditions(
        boolean alone, int lightLevel, double y, SchizophreniaSettings.Natural natural
    ) {
        return alone && (lightLevel <= natural.maxLightLevel() || y < natural.deepBelowY());
    }
}
