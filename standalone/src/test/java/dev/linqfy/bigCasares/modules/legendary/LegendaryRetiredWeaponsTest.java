package dev.linqfy.bigCasares.modules.legendary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegendaryRetiredWeaponsTest {
    @TempDir Path directory;

    @Test
    void removedItemsCannotBeUsedOrOfferedAlongsideTheSixRemainingWeapons() {
        assertEquals(Set.of("bloodlust", "frost_scythe", "shadow_blade", "windweaver", "earthbreaker", "thunder_spear"),
            LegendaryWeapon.available().stream().map(weapon -> weapon.id).collect(Collectors.toSet()));
        for (var weapon : List.of(LegendaryWeapon.VOID_KATANA, LegendaryWeapon.HYPERION)) {
            assertTrue(LegendaryWeapon.find(weapon.id).isEmpty());
            assertFalse(weapon.canCraft(weapon.ingredients));
            assertEquals(LegendaryControls.Decision.BLOCK_SPECIAL, LegendaryControls.decide(weapon, true, true, false));
        }
    }

    @Test
    void anOldPendingClaimCanBeConfirmedWithoutMakingTheRetiredPrizeAvailableAgain() throws Exception {
        var file = directory.resolve("state.properties");
        var store = new LegendaryStateStore(file);
        UUID player = UUID.randomUUID(), nonce = UUID.randomUUID();
        var session = new LegendaryCraftingSession(UUID.randomUUID(), LegendaryWeapon.VOID_KATANA, UUID.randomUUID(),
            "End", 0, 64, 0, 1000, 1_201_000, LegendarySearchAreas.create(0, 0, new Random(1)),
            LegendaryCraftingSession.Stage.CLAIMED, 4,
            List.of(new LegendaryCraftingSession.Pending(player, nonce, "claimed-inventory", LegendaryCraftingSession.TransactionKind.CLAIM)));
        store.put("pvp.void_katana", session.encode());
        var ledger = new LegendaryCraftingLedger(store, new Random(1));
        assertTrue(ledger.pending(player));
        assertTrue(ledger.all().isEmpty());
        ledger.confirm(LegendaryWeapon.VOID_KATANA, nonce);
        var restart = new LegendaryCraftingLedger(new LegendaryStateStore(file), new Random(1));
        assertFalse(restart.pending(player));
        assertTrue(restart.advance(2_000_000).isEmpty());
        assertEquals(LegendaryCraftingSession.Stage.CLAIMED, restart.history().getFirst().stage());
    }

    @Test
    void retiredRecipesCannotReserveAnEventOrConsumeItsUniqueSlot() throws Exception {
        var store = new LegendaryStateStore(directory.resolve("state.properties"));
        var ledger = new LegendaryCraftingLedger(store, new Random(1));
        for (var weapon : List.of(LegendaryWeapon.VOID_KATANA, LegendaryWeapon.HYPERION)) {
            assertThrows(IllegalArgumentException.class, () -> ledger.start(weapon, UUID.randomUUID(), "Overworld", 0, 64, 0,
                1000, new LegendaryCraftingSession.Pending(UUID.randomUUID(), UUID.randomUUID(), "snapshot",
                    LegendaryCraftingSession.TransactionKind.START)));
        }
        assertTrue(ledger.history().isEmpty());
        assertTrue(store.snapshot().isEmpty());
    }

    @Test
    void legacyRecordsStayRecoverableWithoutAdvancingProtectingTablesOrOfferingClaims() throws Exception {
        var file = directory.resolve("state.properties");
        var store = new LegendaryStateStore(file);
        UUID world = UUID.randomUUID(), player = UUID.randomUUID(), nonce = UUID.randomUUID();
        var retired = new LegendaryCraftingSession(UUID.randomUUID(), LegendaryWeapon.HYPERION, world, "Overworld", 0, 64, 0,
            1000, 1_201_000, LegendarySearchAreas.create(0, 0, new Random(1)), LegendaryCraftingSession.Stage.CRAFTING, 0,
            List.of(new LegendaryCraftingSession.Pending(player, nonce, "original-inventory", LegendaryCraftingSession.TransactionKind.START)));
        store.put("pvp.hyperion", retired.encode());
        var ledger = new LegendaryCraftingLedger(new LegendaryStateStore(file), new Random(1));
        assertTrue(ledger.all().isEmpty());
        assertEquals(1, ledger.history().size());
        assertTrue(ledger.pending(player));
        assertFalse(ledger.protectedCell(world, 0, 64, 0));
        assertTrue(ledger.advance(2_000_000).isEmpty());
        assertEquals(retired.encode(), new LegendaryStateStore(file).get("pvp.hyperion"));
        assertThrows(IllegalArgumentException.class, () -> ledger.claim(LegendaryWeapon.HYPERION,
            new LegendaryCraftingSession.Pending(UUID.randomUUID(), UUID.randomUUID(), "unused", LegendaryCraftingSession.TransactionKind.CLAIM)));
        ledger.start(LegendaryWeapon.BLOODLUST, world, "Overworld", 0, 64, 0, 2_000_000,
            new LegendaryCraftingSession.Pending(UUID.randomUUID(), UUID.randomUUID(), "new-inventory", LegendaryCraftingSession.TransactionKind.START));
        ledger.confirm(LegendaryWeapon.HYPERION, nonce);
        var restart = new LegendaryCraftingLedger(new LegendaryStateStore(file), new Random(1));
        assertFalse(restart.pending(player));
        assertEquals(List.of(LegendaryWeapon.BLOODLUST), restart.all().stream().map(LegendaryCraftingSession::weapon).toList());
        assertTrue(restart.find(LegendaryWeapon.HYPERION).orElseThrow().transactions().isEmpty());
    }
}
