package dev.linqfy.bigCasares.duperestore;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.EnumSet;

import static dev.linqfy.bigCasares.duperestore.FlagLease.Key.PISTONS;
import static dev.linqfy.bigCasares.duperestore.FlagLease.Key.TRIPWIRE_HOOKS;
import static org.junit.jupiter.api.Assertions.*;

class FlagLeaseTest {
    @Test void enablesRequestedFlagsAndRestoresOriginals() {
        FakeFlags flags = new FakeFlags();
        FlagLease lease = FlagLease.apply(flags, EnumSet.allOf(FlagLease.Key.class));
        assertTrue(flags.get(PISTONS));
        assertTrue(flags.get(TRIPWIRE_HOOKS));
        lease.close();
        assertFalse(flags.get(PISTONS));
        assertFalse(flags.get(TRIPWIRE_HOOKS));
        lease.close();
    }

    @Test void disabledOptionPreservesServerSetting() {
        FakeFlags flags = new FakeFlags();
        flags.set(PISTONS, true);
        try (FlagLease ignored = FlagLease.apply(flags, EnumSet.of(TRIPWIRE_HOOKS))) {
            assertTrue(flags.get(PISTONS));
            assertTrue(flags.get(TRIPWIRE_HOOKS));
        }
        assertTrue(flags.get(PISTONS));
        assertFalse(flags.get(TRIPWIRE_HOOKS));
    }

    @Test void unrequestedFalseOptionStaysFalse() {
        FakeFlags flags = new FakeFlags();
        try (FlagLease ignored = FlagLease.apply(flags, EnumSet.of(TRIPWIRE_HOOKS))) {
            assertFalse(flags.get(PISTONS));
        }
    }

    @Test void rollsBackEarlierWritesWhenAnotherFlagFails() {
        FakeFlags flags = new FakeFlags();
        flags.failOnHookEnable = true;
        assertThrows(IllegalStateException.class,
                () -> FlagLease.apply(flags, EnumSet.allOf(FlagLease.Key.class)));
        assertFalse(flags.get(PISTONS));
        assertFalse(flags.get(TRIPWIRE_HOOKS));
    }

    @Test void doesNotOverwriteExternalChangesWhenClosing() {
        FakeFlags flags = new FakeFlags();
        FlagLease lease = FlagLease.apply(flags, EnumSet.of(PISTONS));
        flags.set(PISTONS, false);
        flags.set(TRIPWIRE_HOOKS, true);
        lease.close();
        assertFalse(flags.get(PISTONS));
        assertTrue(flags.get(TRIPWIRE_HOOKS));
    }

    private static final class FakeFlags implements FlagLease.Access {
        private final EnumMap<FlagLease.Key, Boolean> values = new EnumMap<>(FlagLease.Key.class);
        private boolean failOnHookEnable;
        public boolean get(FlagLease.Key key) { return values.getOrDefault(key, false); }
        public void set(FlagLease.Key key, boolean value) {
            if (failOnHookEnable && key == TRIPWIRE_HOOKS && value) throw new IllegalStateException("write failed");
            values.put(key, value);
        }
    }
}
