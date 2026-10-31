package dev.linqfy.legendary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/** Upgrade owned packs, preserving an exact backup; leave operator customizations intact. */
final class LegendaryPackInstaller {
    record Result(byte[] bytes, Path backup, boolean customized) {
        Result {
            bytes = bytes.clone();
        }

        @Override
        public byte[] bytes() {
            return bytes.clone();
        }
    }

    static Result install(Path destination, byte[] bundled, Set<String> previousBundledHashes)
            throws IOException {
        Files.createDirectories(destination.toAbsolutePath().getParent());
        Path marker = destination.resolveSibling("legendary-pack-bundled.sha256");
        String incoming = hash(bundled);
        Path backup = null;
        if (Files.exists(destination)) {
            byte[] existing = Files.readAllBytes(destination);
            String current = hash(existing);
            if (current.equals(incoming)) {
                atomicWrite(marker, (incoming + "\n").getBytes(StandardCharsets.US_ASCII));
                return new Result(existing, null, false);
            }
            String lastOwned =
                    Files.exists(marker)
                            ? Files.readString(marker, StandardCharsets.US_ASCII).trim()
                            : "";
            if (!current.equals(lastOwned) && !previousBundledHashes.contains(current)) {
                return new Result(existing, null, true);
            }
            backup =
                    destination
                            .resolveSibling("backups")
                            .resolve("legendary-pack-" + current + ".zip");
            Files.createDirectories(backup.getParent());
            if (!Files.exists(backup)) atomicWrite(backup, existing);
            if (!hash(Files.readAllBytes(backup)).equals(current))
                throw new IOException(
                        "La copia anterior del pack no coincide; se conserva el archivo activo.");
        }
        atomicWrite(destination, bundled);
        atomicWrite(marker, (incoming + "\n").getBytes(StandardCharsets.US_ASCII));
        return new Result(bundled, backup, false);
    }

    private static String hash(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static void atomicWrite(Path destination, byte[] bytes) throws IOException {
        Path temporary =
                Files.createTempFile(
                        destination.toAbsolutePath().getParent(), "legendary-pack-", ".tmp");
        try {
            Files.write(temporary, bytes);
            Files.move(
                    temporary,
                    destination,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
