package dev.linqfy.bigCasares.modules.schizophrenia;

import org.bukkit.entity.Mannequin;

/**
 * A fake player only its victim can see. Behaviours:
 * STARE stands still and vanishes when looked at; STALKER creeps closer whenever it is not
 * watched, freezes under your gaze and is simply gone when it arrives; MIRROR wears your own face and
 * stares back until you blink; SNAP is pinned for a forced camera turn; BEHIND keeps standing
 * right at your back and screams when you turn around; WATCHER stands far away all episode long
 * and relocates when stared at; ALTERNATE wears a friend's face,
 * walks right up to you insisting it is them, and then stops being them.
 */
final class PhantomClone {

    enum Behaviour { STARE, STALKER, MIRROR, SNAP, BEHIND, WATCHER, ALTERNATE }

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
