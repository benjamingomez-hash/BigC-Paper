package dev.linqfy.bigCasares.modules.schizophrenia;

import org.bukkit.entity.Mannequin;

/**
 * A fake player only its victim can see. Behaviours:
 * STARE stands still and vanishes when looked at; STALKER creeps closer whenever it is not
 * watched and jumpscares on arrival; SNAP is pinned for a forced camera turn.
 */
final class PhantomClone {

    enum Behaviour { STARE, STALKER, MIRROR, SNAP }

    private final Mannequin entity;
    private final Behaviour behaviour;
    private final long expiresAtTick;
    private int watchedFor;

    PhantomClone(Mannequin entity, Behaviour behaviour, long expiresAtTick) {
        this.entity = entity;
        this.behaviour = behaviour;
        this.expiresAtTick = expiresAtTick;
    }

    Mannequin entity() {
        return entity;
    }

    Behaviour behaviour() {
        return behaviour;
    }

    boolean expired(long tick) {
        return tick >= expiresAtTick;
    }

    int watchedFor() {
        return watchedFor;
    }

    void watchedFor(int ticks) {
        this.watchedFor = ticks;
    }

    void remove() {
        if (entity.isValid()) {
            entity.remove();
        }
    }
}
