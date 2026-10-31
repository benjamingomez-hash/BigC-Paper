package dev.linqfy.legendary;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;

class LegendaryPackInstallerTest {
    @TempDir Path root;

    private static String hash(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    @Test
    void firstInstallAndOwnedUpgradeRetainAnExactPreviousCopy() throws Exception {
        Path target = root.resolve("legendary-pack.zip");
        byte[] old = {1, 2, 3}, next = {4, 5, 6};
        LegendaryPackInstaller.install(target, old, Set.of());
        var result = LegendaryPackInstaller.install(target, next, Set.of());
        assertArrayEquals(next, Files.readAllBytes(target));
        assertArrayEquals(old, Files.readAllBytes(result.backup()));
        assertFalse(result.customized());
        var repeat = LegendaryPackInstaller.install(target, next, Set.of());
        assertNull(repeat.backup());
        assertArrayEquals(old, Files.readAllBytes(result.backup()));
    }

    @Test
    void oldBundledPackWithoutMarkerCanUpgradeAndBackUp() throws Exception {
        Path target = root.resolve("legendary-pack.zip");
        byte[] old = {1, 2, 3};
        Files.write(target, old);
        var result = LegendaryPackInstaller.install(target, new byte[] {9}, Set.of(hash(old)));
        assertArrayEquals(old, Files.readAllBytes(result.backup()));
        assertArrayEquals(new byte[] {9}, result.bytes());
    }

    @Test
    void customPackAndManualEditsArePreservedAcrossNewPluginVersions() throws Exception {
        Path target = root.resolve("legendary-pack.zip");
        byte[] customized = {7, 8, 9};
        Files.write(target, customized);
        var custom = LegendaryPackInstaller.install(target, new byte[] {1}, Set.of());
        assertTrue(custom.customized());
        assertArrayEquals(customized, Files.readAllBytes(target));
        Files.delete(target);
        LegendaryPackInstaller.install(target, new byte[] {1}, Set.of());
        Files.write(target, customized);
        var edited = LegendaryPackInstaller.install(target, new byte[] {2}, Set.of());
        assertTrue(edited.customized());
        assertArrayEquals(customized, edited.bytes());
    }

    @Test
    void damagedBackupNeverAllowsOverwritingTheActivePack() throws Exception {
        Path target = root.resolve("legendary-pack.zip");
        byte[] old = {1, 2, 3};
        Files.write(target, old);
        Path backup = root.resolve("backups/legendary-pack-" + hash(old) + ".zip");
        Files.createDirectories(backup.getParent());
        Files.write(backup, new byte[] {0});
        assertThrows(
                java.io.IOException.class,
                () -> LegendaryPackInstaller.install(target, new byte[] {2}, Set.of(hash(old))));
        assertArrayEquals(old, Files.readAllBytes(target));
    }
}
