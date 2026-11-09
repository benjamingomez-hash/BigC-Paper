package dev.linqfy.bigCasares.modules.schizophrenia;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.random.RandomGenerator;

/** Owns every active episode, drives the hallucination clock and handles onset and cure. */
final class SchizophreniaRuntime {

    static final long TICK_PERIOD = 2L;

    private final Plugin plugin;
    private final SchizophreniaSettings settings;
    private final HallucinationPicker picker;
    private final HallucinationDirector director;
    private final RandomGenerator random = new SplittableRandom();
    private final LongSupplier clock;
    private final Map<UUID, Episode> episodes = new HashMap<>();
    private final NamespacedKey startedKey;
    private final NamespacedKey intensityKey;
    private final NamespacedKey immuneKey;
    private long tick;
    private long nextNaturalCheck;

    SchizophreniaRuntime(Plugin plugin, SchizophreniaSettings settings, LongSupplier clock) {
        this.plugin = plugin;
        this.settings = settings;
        this.clock = clock;
        this.picker = new HallucinationPicker(settings.disabledHallucinations());
        this.director = new HallucinationDirector(plugin, settings, random);
        this.startedKey = new NamespacedKey(plugin, "schizophrenia_started");
        this.intensityKey = new NamespacedKey(plugin, "schizophrenia_intensity");
        this.immuneKey = new NamespacedKey(plugin, "schizophrenia_immune_until");
        this.nextNaturalCheck = settings.natural().checkIntervalSeconds() * 20L;
    }

    // ---------------------------------------------------------------- lifecycle

    void tick() {
        tick += TICK_PERIOD;
        long now = clock.getAsLong();
        for (Episode episode : new ArrayList<>(episodes.values())) {
            Player player = plugin.getServer().getPlayer(episode.playerId());
            if (player == null || !player.isOnline()) {
                suspend(episode.playerId());
                continue;
            }
            try {
                tickEpisode(player, episode, now);
            } catch (RuntimeException failure) {
                plugin.getLogger().log(Level.WARNING, "[Esquizofrenia] Falló una alucinación de " + player.getName(), failure);
            }
        }
        if (settings.natural().enabled() && tick >= nextNaturalCheck) {
            nextNaturalCheck = tick + settings.natural().checkIntervalSeconds() * 20L;
            naturalOnset(now);
        }
    }

    private void tickEpisode(Player player, Episode episode, long now) {
        if (EpisodeMath.expired(now - episode.startedAtMillis(), settings.maxEpisodeMinutes())) {
            end(player, false);
            player.sendMessage(Component.text("Las voces se cansaron... por ahora.", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
            return;
        }
        episode.timeline().runDue(tick, this::logCueFailure);
        director.updatePhantoms(player, episode, tick);
        if (tick < episode.nextHallucinationTick() || player.isDead()) {
            return;
        }
        int intensity = episode.intensity(now, settings.minutesPerIntensity());
        picker.pick(intensity, episode.previous(), random).ifPresent(hallucination -> {
            boolean played = director.play(hallucination, player, episode, tick, intensity);
            if (!played) {
                director.play(Hallucination.WHISPERS, player, episode, tick, intensity);
            }
            episode.previous(hallucination);
        });
        scheduleNext(episode, intensity);
    }

    private void scheduleNext(Episode episode, int intensity) {
        double seconds = EpisodeMath.intervalSeconds(intensity,
            settings.slowestIntervalSeconds(), settings.fastestIntervalSeconds(), random.nextDouble());
        episode.nextHallucinationTick(tick + Math.round(seconds * 20.0));
    }

    void shutdown() {
        for (UUID playerId : new ArrayList<>(episodes.keySet())) {
            suspend(playerId);
        }
        removeStrayPhantoms();
    }

    /** Leftover mannequins can only exist after a crash mid-episode; they are not persistent, but be sure. */
    void removeStrayPhantoms() {
        plugin.getServer().getWorlds().forEach(world -> world.getEntities().stream()
            .filter(entity -> entity.getScoreboardTags().contains(HallucinationDirector.PHANTOM_TAG))
            .forEach(Entity::remove));
    }

    // ---------------------------------------------------------------- episodes

    boolean start(Player player, int intensity) {
        if (episodes.containsKey(player.getUniqueId())) {
            return false;
        }
        long now = clock.getAsLong();
        int clamped = EpisodeMath.clampIntensity(intensity);
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(startedKey, PersistentDataType.LONG, now);
        data.set(intensityKey, PersistentDataType.INTEGER, clamped);
        data.remove(immuneKey);
        episodes.put(player.getUniqueId(), new Episode(player.getUniqueId(), now, clamped, tick + 40L));
        return true;
    }

    /** Re-attaches a persisted episode after a relog: logging out is not a cure. */
    void resume(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        Long started = data.get(startedKey, PersistentDataType.LONG);
        if (started == null || episodes.containsKey(player.getUniqueId())) {
            return;
        }
        if (EpisodeMath.expired(clock.getAsLong() - started, settings.maxEpisodeMinutes())) {
            data.remove(startedKey);
            data.remove(intensityKey);
            return;
        }
        int intensity = data.getOrDefault(intensityKey, PersistentDataType.INTEGER, 1);
        episodes.put(player.getUniqueId(), new Episode(player.getUniqueId(), started, intensity, tick + 200L));
    }

    void suspend(UUID playerId) {
        Episode episode = episodes.remove(playerId);
        if (episode != null) {
            episode.timeline().flush(this::logCueFailure);
            director.removeAllPhantoms(episode);
        }
    }

    void end(Player player, boolean grantImmunity) {
        suspend(player.getUniqueId());
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.remove(startedKey);
        data.remove(intensityKey);
        if (grantImmunity && settings.immunityMinutesAfterCure() > 0) {
            data.set(immuneKey, PersistentDataType.LONG, clock.getAsLong() + settings.immunityMinutesAfterCure() * 60_000L);
        }
        player.resetPlayerTime();
        player.resetPlayerWeather();
        player.setWorldBorder(null);
        player.sendHealthUpdate();
    }

    boolean isAffected(Player player) {
        return episodes.containsKey(player.getUniqueId());
    }

    Optional<Integer> intensity(Player player) {
        Episode episode = episodes.get(player.getUniqueId());
        return episode == null ? Optional.empty()
            : Optional.of(episode.intensity(clock.getAsLong(), settings.minutesPerIntensity()));
    }

    boolean trigger(Player player, Hallucination hallucination) {
        Episode episode = episodes.get(player.getUniqueId());
        if (episode == null) {
            return false;
        }
        return director.play(hallucination, player, episode, tick, EpisodeMath.MAX_INTENSITY);
    }

    boolean ownsPhantom(Entity entity) {
        return entity.getScoreboardTags().contains(HallucinationDirector.PHANTOM_TAG);
    }

    /** The victim swung at a ghost: it was never there. */
    void dispelPhantom(Player player, Entity entity) {
        Episode episode = episodes.get(player.getUniqueId());
        if (episode == null) {
            entity.remove();
            return;
        }
        for (PhantomClone clone : new ArrayList<>(episode.phantoms())) {
            if (clone.entity().equals(entity)) {
                player.playSound(entity.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, SoundCategory.HOSTILE, 1f, 0.5f);
                director.vanish(player, clone, true);
                episode.phantoms().remove(clone);
                return;
            }
        }
    }

    List<String> statusLines() {
        long now = clock.getAsLong();
        List<String> lines = new ArrayList<>();
        lines.add("§5Esquizofrenia §7- episodios activos: §f" + episodes.size());
        for (Episode episode : episodes.values()) {
            Player player = plugin.getServer().getPlayer(episode.playerId());
            String name = player == null ? episode.playerId().toString() : player.getName();
            long minutes = (now - episode.startedAtMillis()) / 60_000L;
            lines.add("§7- §f" + name + " §7intensidad §d" + episode.intensity(now, settings.minutesPerIntensity())
                + "§7, hace §f" + minutes + " min§7, fantasmas §f" + episode.phantoms().size());
        }
        return lines;
    }

    // ---------------------------------------------------------------- Lobotomizador

    void lobotomize(Player surgeon, Player patient, LobotomizerItem item) {
        if (!isAffected(patient)) {
            surgeon.sendActionBar(Component.text("No había nada que lobotomizar... ¿o sí?", NamedTextColor.GRAY));
            patient.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 80, 0, false, false, true));
            return;
        }
        end(patient, true);
        int stun = settings.lobotomizer().stunSeconds() * 20;
        if (stun > 0) {
            patient.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, stun, 1, false, false, true));
            patient.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, stun, 0, false, false, true));
            patient.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, Math.min(stun, 140), 0, false, false, true));
        }
        patient.playSound(patient.getLocation(), Sound.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 1f, 0.5f);
        patient.playSound(patient.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.PLAYERS, 0.8f, 1.2f);
        surgeon.playSound(surgeon.getLocation(), Sound.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.7f, 0.5f);
        patient.showTitle(Title.title(
            Component.text("*bonk*", NamedTextColor.GRAY, TextDecoration.ITALIC),
            Component.text("las voces se callaron", NamedTextColor.DARK_GRAY),
            Title.Times.times(Duration.ZERO, Duration.ofMillis(2500), Duration.ofMillis(1000))));
        patient.sendMessage(Component.text(surgeon.getName() + " te hizo una lobotomía. Silencio, al fin.", NamedTextColor.GRAY));
        surgeon.sendMessage(Component.text("Le hiciste una lobotomía a " + patient.getName() + ". Las voces se fueron.",
            NamedTextColor.GREEN));
        int cost = settings.lobotomizer().durabilityCost();
        if (cost > 0 && item.isLobotomizer(surgeon.getInventory().getItemInMainHand())) {
            surgeon.damageItemStack(EquipmentSlot.HAND, cost);
        }
    }

    // ---------------------------------------------------------------- natural onset

    private void naturalOnset(long now) {
        SchizophreniaSettings.Natural natural = settings.natural();
        if (natural.chance() <= 0) {
            return;
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isAffected(player) || immune(player, now) || !natural.allowsWorld(player.getWorld().getName())
                || (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)) {
                continue;
            }
            int light = player.getEyeLocation().getBlock().getLightLevel();
            if (EpisodeMath.naturalOnsetConditions(alone(player, natural.aloneRadius()), light, player.getY(), natural)
                && random.nextDouble() < natural.chance()) {
                start(player, 1);
            }
        }
    }

    private boolean immune(Player player, long now) {
        Long until = player.getPersistentDataContainer().get(immuneKey, PersistentDataType.LONG);
        return until != null && until > now;
    }

    private static boolean alone(Player player, int radius) {
        double squared = (double) radius * radius;
        for (Player other : player.getWorld().getPlayers()) {
            if (other != player && other.getGameMode() != GameMode.SPECTATOR
                && other.getLocation().distanceSquared(player.getLocation()) <= squared) {
                return false;
            }
        }
        return true;
    }

    private void logCueFailure(RuntimeException failure) {
        plugin.getLogger().log(Level.FINE, "[Esquizofrenia] cue failed", failure);
    }
}
