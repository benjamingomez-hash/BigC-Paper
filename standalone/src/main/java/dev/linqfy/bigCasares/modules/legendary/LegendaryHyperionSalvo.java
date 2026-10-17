package dev.linqfy.bigCasares.modules.legendary;

final class LegendaryHyperionSalvo {
    static final int LAST_SHOT = 21;
    static final int MAX_AGE = 121;
    private LegendaryHyperionSalvo() { }

    static boolean firesAt(int age) { return age >= 1 && age <= LAST_SHOT && (age - 1) % 5 == 0; }
}
