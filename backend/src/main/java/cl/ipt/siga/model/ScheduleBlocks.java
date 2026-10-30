package cl.ipt.siga.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Helpers for schedule blocks such as "L2" (Monday, module 2).
 * Days: L (lunes), M (martes), W (miércoles), J (jueves), V (viernes).
 * Modules: 1 to 6. Each block equals 2 pedagogical hours.
 */
public final class ScheduleBlocks {

    public static final String DAYS = "LMWJV";
    public static final int MIN_MODULE = 1;
    public static final int MAX_MODULE = 6;
    public static final int HOURS_PER_BLOCK = 2;

    /** Orders blocks by day of the week and then by module. */
    public static final Comparator<String> ORDER = Comparator
            .comparingInt((String block) -> DAYS.indexOf(block.charAt(0)))
            .thenComparingInt(block -> block.charAt(1) - '0');

    private ScheduleBlocks() {
    }

    public static String normalize(String block) {
        return block == null ? "" : block.trim().toUpperCase();
    }

    public static boolean isValid(String block) {
        if (block.length() != 2 || DAYS.indexOf(block.charAt(0)) < 0) {
            return false;
        }
        int module = block.charAt(1) - '0';
        return module >= MIN_MODULE && module <= MAX_MODULE;
    }

    public static List<String> sorted(Collection<String> blocks) {
        return blocks.stream().sorted(ORDER).toList();
    }
}
