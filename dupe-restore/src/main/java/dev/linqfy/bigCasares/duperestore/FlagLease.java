package dev.linqfy.bigCasares.duperestore;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

final class FlagLease implements AutoCloseable {
    enum Key { PISTONS, TRIPWIRE_HOOKS }
    interface Access {
        boolean get(Key key);
        void set(Key key, boolean value);
    }

    private final Access access;
    private final EnumMap<Key, Boolean> originals = new EnumMap<>(Key.class);
    private boolean closed;

    private FlagLease(Access access) { this.access = access; }

    static FlagLease apply(Access access, EnumSet<Key> requested) {
        FlagLease lease = new FlagLease(access);
        // Validate every read before making any change to Paper's live settings.
        EnumMap<Key, Boolean> before = new EnumMap<>(Key.class);
        for (Key key : Key.values()) before.put(key, access.get(key));
        try {
            for (Key key : requested) {
                if (!before.get(key)) {
                    access.set(key, true);
                    lease.originals.put(key, false);
                }
            }
            return lease;
        } catch (RuntimeException failure) {
            try { lease.close(); } catch (RuntimeException rollback) { failure.addSuppressed(rollback); }
            throw failure;
        }
    }

    @Override public void close() {
        if (closed) return;
        closed = true;
        RuntimeException failure = null;
        for (Map.Entry<Key, Boolean> entry : originals.entrySet()) {
            try {
                if (access.get(entry.getKey())) access.set(entry.getKey(), entry.getValue());
            } catch (RuntimeException error) {
                if (failure == null) failure = error;
                else failure.addSuppressed(error);
            }
        }
        if (failure != null) throw failure;
    }
}
