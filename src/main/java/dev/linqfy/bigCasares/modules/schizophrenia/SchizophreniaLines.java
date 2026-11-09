package dev.linqfy.bigCasares.modules.schizophrenia;

import java.util.List;
import java.util.random.RandomGenerator;

/** Things the voices say. Plain strings so they stay easy to tweak and test. */
final class SchizophreniaLines {

    static final List<String> WHISPERS = List.of(
        "te estoy viendo",
        "date vuelta",
        "detrás tuyo",
        "no estás solo",
        "¿lo escuchaste?",
        "ya casi llego",
        "no confíes en ellos",
        "ellos hablan de vos",
        "abrí los ojos",
        "no mires",
        "sé dónde vivís",
        "quedate quieto",
        "¿por qué corrés?",
        "estuve acá todo el tiempo",
        "nadie más me ve",
        "contá los bloques",
        "escuchá las paredes",
        "no es real. ¿o sí?",
        "te están siguiendo",
        "mirá arriba"
    );

    static final List<String> TITLES = List.of(
        "NO ESTÁS SOLO",
        "DATE VUELTA",
        "TE VEO",
        "¿QUIÉN ESTÁ AHÍ?",
        "NO ES REAL",
        "DESPERTÁ",
        "CORRÉ",
        "ELLOS SABEN"
    );

    static final List<String> SUBTITLES = List.of(
        "las paredes respiran",
        "alguien te sigue",
        "no confíes en nadie",
        "¿escuchaste eso?",
        "tu cabeza miente",
        "no mires atrás"
    );

    /** Up to four lines each, written in red on a sign only the victim sees. */
    static final List<List<String>> WALL_MESSAGES = List.of(
        List.of("", "DATE", "VUELTA", ""),
        List.of("", "TE VEO", "", ""),
        List.of("NO", "ESTÁS", "SOLO", ""),
        List.of("", "ÉL ESTÁ", "ACÁ", ""),
        List.of("", "CORRÉ", "", ""),
        List.of("¿POR QUÉ", "NO ME", "MIRÁS?", ""),
        List.of("", "AYUDA", "", ""),
        List.of("NO", "DUERMAS", "", "")
    );

    static final List<String> PHANTOM_NAMES = List.of(
        "Herobrine", "nadie", "???", "Él", "tu_sombra", "observador", "null"
    );

    private SchizophreniaLines() {
    }

    static <T> T pick(List<T> lines, RandomGenerator random) {
        return lines.get(random.nextInt(lines.size()));
    }

    /** Replaces a share of the letters with §k-style noise markers handled by the caller. */
    static boolean[] glitchMask(String text, double share, RandomGenerator random) {
        boolean[] mask = new boolean[text.length()];
        for (int i = 0; i < text.length(); i++) {
            mask[i] = !Character.isWhitespace(text.charAt(i)) && random.nextDouble() < share;
        }
        return mask;
    }
}
