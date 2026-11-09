package dev.linqfy.bigCasares.modules.schizophrenia;

import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Plays hallucinations. Everything here is sent to the victim only: per-player sounds, particles,
 * fake blocks, fake equipment, a private world border and mannequins hidden from everyone else.
 * Nothing touches real blocks, inventories or health.
 */
final class HallucinationDirector {

    static final String PHANTOM_TAG = "bigcasares_schizo_phantom";

    private static final List<Material> ROT_BLOCKS = List.of(
        Material.NETHER_WART_BLOCK, Material.CRYING_OBSIDIAN, Material.SCULK,
        Material.RED_CONCRETE, Material.MAGMA_BLOCK, Material.BLACK_CONCRETE, Material.REDSTONE_BLOCK);
    private static final Set<Material> LIGHT_SOURCES = Set.of(
        Material.TORCH, Material.WALL_TORCH, Material.SOUL_TORCH, Material.SOUL_WALL_TORCH,
        Material.LANTERN, Material.SOUL_LANTERN, Material.GLOWSTONE, Material.SEA_LANTERN,
        Material.JACK_O_LANTERN, Material.SHROOMLIGHT, Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
        Material.END_ROD, Material.COPPER_TORCH, Material.COPPER_WALL_TORCH, Material.COPPER_LANTERN);
    private static final List<Material> MASKS = List.of(
        Material.WITHER_SKELETON_SKULL, Material.SKELETON_SKULL, Material.CARVED_PUMPKIN,
        Material.ZOMBIE_HEAD, Material.CREEPER_HEAD);
    private static final List<Material> WEAPONS = List.of(
        Material.IRON_SWORD, Material.IRON_AXE, Material.DIAMOND_SWORD, Material.CROSSBOW, Material.TRIDENT);
    private static final List<Sound> ROARS = List.of(
        Sound.ENTITY_WARDEN_ROAR, Sound.ENTITY_RAVAGER_ROAR, Sound.ENTITY_ENDER_DRAGON_GROWL,
        Sound.ENTITY_GHAST_SCREAM, Sound.ENTITY_WITHER_AMBIENT);
    private static final List<Sound> MOB_SOUNDS = List.of(
        Sound.ENTITY_ZOMBIE_AMBIENT, Sound.ENTITY_SPIDER_AMBIENT, Sound.ENTITY_ENDERMAN_STARE,
        Sound.ENTITY_WOLF_GROWL, Sound.ENTITY_TNT_PRIMED, Sound.ENTITY_CAT_HISS,
        Sound.ENTITY_SKELETON_HORSE_AMBIENT, Sound.ENTITY_PHANTOM_AMBIENT, Sound.ENTITY_VEX_CHARGE);
    private static final Particle.DustOptions SHADOW = new Particle.DustOptions(Color.fromRGB(8, 8, 10), 1.6f);
    private static final Particle.DustOptions EYES = new Particle.DustOptions(Color.fromRGB(255, 20, 20), 0.7f);

    private final Plugin plugin;
    private final SchizophreniaSettings settings;
    private final RandomGenerator random;
    private final Map<Hallucination, Effect> effects = new EnumMap<>(Hallucination.class);

    @FunctionalInterface
    private interface Effect {
        boolean play(Player player, Episode episode, long now, int intensity);
    }

    HallucinationDirector(Plugin plugin, SchizophreniaSettings settings, RandomGenerator random) {
        this.plugin = plugin;
        this.settings = settings;
        this.random = random;
        effects.put(Hallucination.FOOTSTEPS, this::footsteps);
        effects.put(Hallucination.CREEPER_HISS, this::creeperHiss);
        effects.put(Hallucination.DISTANT_ROAR, this::distantRoar);
        effects.put(Hallucination.WHISPERS, this::whispers);
        effects.put(Hallucination.PHANTOM_CLONE, (p, e, now, i) -> spawnPhantom(p, e, now, PhantomClone.Behaviour.STARE,
            behind(p, between(settings.phantoms().minDistance(), settings.phantoms().maxDistance()), random.nextDouble()),
            randomIdentity(p), settings.phantoms().lifetimeSeconds() * 20L) != null);
        effects.put(Hallucination.SILHOUETTE, this::silhouette);
        effects.put(Hallucination.MINING_BEHIND, this::miningBehind);
        effects.put(Hallucination.MOB_SOUNDS, this::mobSounds);
        effects.put(Hallucination.DOOR_KNOCKING, this::doorKnocking);
        effects.put(Hallucination.HEARTBEAT, this::heartbeat);
        effects.put(Hallucination.FAKE_HURT, this::fakeHurt);
        effects.put(Hallucination.FAKE_CHAT, this::fakeChat);
        effects.put(Hallucination.DOPPELGANGER, this::doppelganger);
        effects.put(Hallucination.BLOCK_ROT, this::blockRot);
        effects.put(Hallucination.LIGHTS_OUT, this::lightsOut);
        effects.put(Hallucination.MASKED_PLAYERS, this::maskedPlayers);
        effects.put(Hallucination.RED_VIGNETTE, this::redVignette);
        effects.put(Hallucination.STALKER, (p, e, now, i) -> spawnPhantom(p, e, now, PhantomClone.Behaviour.STALKER,
            behind(p, settings.phantoms().maxDistance(), random.nextDouble() * 0.4), randomIdentity(p), 90 * 20L) != null);
        effects.put(Hallucination.GLITCH_TITLE, this::glitchTitle);
        effects.put(Hallucination.FAKE_EXPLOSION, this::fakeExplosion);
        effects.put(Hallucination.CAMERA_SNAP, this::cameraSnap);
        effects.put(Hallucination.ELDER_GUARDIAN, this::elderGuardian);
        effects.put(Hallucination.ETERNAL_NIGHT, this::eternalNight);
        effects.put(Hallucination.FAKE_THUNDER, this::fakeThunder);
        effects.put(Hallucination.SWARM, this::swarm);
        effects.put(Hallucination.FAKE_DEATH, this::fakeDeath);
    }

    /** Plays the hallucination; returns false when the surroundings could not support it. */
    boolean play(Hallucination hallucination, Player player, Episode episode, long now, int intensity) {
        boolean played = effects.get(hallucination).play(player, episode, now, intensity);
        if (played && intensity >= 4 && random.nextDouble() < 0.25) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 120, 0, false, false, false));
        }
        return played;
    }

    // ---------------------------------------------------------------- audio

    private boolean footsteps(Player player, Episode episode, long now, int intensity) {
        Location start = behind(player, 14 + random.nextInt(6), random.nextDouble() * 0.3);
        footstep(player, episode, start, now, 6, 0);
        return true;
    }

    private void footstep(Player player, Episode episode, Location at, long tick, int delay, int step) {
        episode.timeline().at(tick + delay, () -> {
            if (!player.isOnline() || at.getWorld() != player.getWorld()) {
                return;
            }
            Location target = player.getLocation();
            double distance = at.distance(target);
            if (distance < 1.6 || step > 40) {
                arrival(player);
                return;
            }
            playStepSound(player, at);
            Vector towards = target.toVector().subtract(at.toVector()).setY(0);
            if (towards.lengthSquared() > 1.0E-4) {
                towards.normalize().multiply(Math.min(1.1, distance));
            }
            Location next = at.clone().add(towards);
            footstep(player, episode, next, tick + delay, Math.max(2, delay - (step % 2 == 0 ? 1 : 0)), step + 1);
        });
    }

    private void arrival(Player player) {
        switch (random.nextInt(3)) {
            case 0 -> {
                player.sendHurtAnimation(random.nextFloat() * 360f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1f, 0.8f);
            }
            case 1 -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 1f, 0.6f);
                whisperChat(player, "detrás tuyo");
            }
            default -> {
                // The steps simply stop. Silence is worse.
            }
        }
    }

    private void playStepSound(Player player, Location at) {
        Block ground = at.getBlock().getRelative(BlockFace.DOWN);
        Sound step = ground.getType().isAir() ? Sound.BLOCK_STONE_STEP : ground.getBlockData().getSoundGroup().getStepSound();
        player.playSound(at, step, SoundCategory.PLAYERS, 0.9f, 0.85f + random.nextFloat() * 0.2f);
    }

    private boolean creeperHiss(Player player, Episode episode, long now, int intensity) {
        Location behind = behind(player, 1.3, random.nextDouble() * 0.2);
        player.playSound(behind, Sound.ENTITY_CREEPER_PRIMED, SoundCategory.HOSTILE, 1f, 0.5f);
        return true;
    }

    private boolean distantRoar(Player player, Episode episode, long now, int intensity) {
        Location far = around(player, 28 + random.nextInt(12));
        player.playSound(far, pick(ROARS), SoundCategory.HOSTILE, 3f, 0.45f + random.nextFloat() * 0.3f);
        return true;
    }

    private boolean whispers(Player player, Episode episode, long now, int intensity) {
        player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 1f, 0.5f + random.nextFloat() * 0.5f);
        int lines = 1 + random.nextInt(Math.max(1, intensity - 1));
        for (int i = 0; i < lines; i++) {
            String line = SchizophreniaLines.pick(SchizophreniaLines.WHISPERS, random);
            episode.timeline().at(now + 20L + i * 30L, () -> whisperChat(player, line));
        }
        return true;
    }

    private boolean miningBehind(Player player, Episode episode, long now, int intensity) {
        Location wall = behind(player, 3 + random.nextInt(3), random.nextDouble() * 0.5);
        int blocks = 2 + random.nextInt(3);
        long tick = now;
        for (int b = 0; b < blocks; b++) {
            for (int hit = 0; hit < 5; hit++) {
                tick += 4;
                episode.timeline().at(tick, () -> player.playSound(wall, Sound.BLOCK_STONE_HIT, SoundCategory.BLOCKS, 0.8f, 0.9f));
            }
            tick += 4;
            episode.timeline().at(tick, () -> player.playSound(wall, Sound.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1f, 0.9f));
            tick += 10;
        }
        return true;
    }

    private boolean mobSounds(Player player, Episode episode, long now, int intensity) {
        Sound sound = pick(MOB_SOUNDS);
        Location near = around(player, 2 + random.nextInt(5));
        player.playSound(near, sound, SoundCategory.HOSTILE, 1f, 0.7f + random.nextFloat() * 0.3f);
        if (random.nextBoolean()) {
            episode.timeline().at(now + 12, () -> {
                player.playSound(around(player, 8), Sound.ENTITY_SKELETON_SHOOT, SoundCategory.HOSTILE, 1f, 1f);
                episode.timeline().at(now + 22, () ->
                    player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT, SoundCategory.HOSTILE, 1f, 1f));
            });
        }
        return true;
    }

    private boolean doorKnocking(Player player, Episode episode, long now, int intensity) {
        Location door = around(player, 3 + random.nextInt(4));
        if (random.nextBoolean()) {
            for (int i = 0; i < 3; i++) {
                episode.timeline().at(now + i * 14L, () ->
                    player.playSound(door, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.HOSTILE, 0.9f, 0.8f));
            }
        } else {
            player.playSound(door, Sound.BLOCK_WOODEN_DOOR_OPEN, SoundCategory.BLOCKS, 1f, 0.9f);
            episode.timeline().at(now + 40, () ->
                player.playSound(door, Sound.BLOCK_WOODEN_DOOR_CLOSE, SoundCategory.BLOCKS, 1f, 0.9f));
            episode.timeline().at(now + 70, () -> playStepSound(player, around(player, 2)));
        }
        return true;
    }

    private boolean heartbeat(Player player, Episode episode, long now, int intensity) {
        long tick = now;
        int beats = 8 + intensity * 2;
        for (int beat = 0; beat < beats; beat++) {
            tick += Math.max(5, 18 - beat);
            boolean strong = beat % 2 == 0;
            episode.timeline().at(tick, () -> {
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.2f, 1f);
                player.sendActionBar(Component.text(strong ? "♥ ♥ ♥" : "♥", NamedTextColor.DARK_RED));
            });
        }
        episode.timeline().at(tick + 10, () -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, SoundCategory.PLAYERS, 1f, 0.7f);
            player.sendActionBar(Component.empty());
        });
        return true;
    }

    private boolean fakeChat(Player player, Episode episode, long now, int intensity) {
        String name = randomName(player);
        switch (random.nextInt(3)) {
            case 0 -> player.sendMessage(Component.text(name + " te susurra: "
                + SchizophreniaLines.pick(SchizophreniaLines.WHISPERS, random), NamedTextColor.GRAY, TextDecoration.ITALIC));
            case 1 -> {
                player.sendMessage(Component.text(name + " se ha unido a la partida", NamedTextColor.YELLOW));
                episode.timeline().at(now + 60 + random.nextInt(80), () ->
                    player.sendMessage(Component.text(name + " ha abandonado la partida", NamedTextColor.YELLOW)));
            }
            default -> player.sendMessage(Component.text(player.getName() + " te susurra: "
                + SchizophreniaLines.pick(SchizophreniaLines.WHISPERS, random), NamedTextColor.GRAY, TextDecoration.ITALIC));
        }
        return true;
    }

    // ---------------------------------------------------------------- visuals

    private boolean silhouette(Player player, Episode episode, long now, int intensity) {
        Location at = ground(behind(player, between(10, 18), 0.6 + random.nextDouble() * 0.4));
        if (at == null) {
            return false;
        }
        int frames = 30 + intensity * 6;
        boolean[] gone = {false};
        for (int frame = 0; frame < frames; frame++) {
            episode.timeline().at(now + frame * 3L, () -> drawSilhouette(player, at, gone));
        }
        return true;
    }

    private void drawSilhouette(Player player, Location feet, boolean[] gone) {
        if (gone[0] || !player.isOnline() || feet.getWorld() != player.getWorld()) {
            return;
        }
        if (looksAt(player, feet.clone().add(0, 1.0, 0), 9.0)) {
            player.spawnParticle(Particle.LARGE_SMOKE, feet.clone().add(0, 1, 0), 12, 0.2, 0.6, 0.2, 0.01);
            gone[0] = true;
            return;
        }
        float yaw = GazeMath.yawTowards(feet.getX(), feet.getZ(), player.getX(), player.getZ());
        Vector right = new Vector(Math.cos(Math.toRadians(yaw)), 0, Math.sin(Math.toRadians(yaw))).multiply(0.22);
        for (double y = 0.1; y < 1.45; y += 0.15) {
            double width = y > 0.75 ? 1.0 : 0.6;
            dust(player, feet.clone().add(right.clone().multiply(width)).add(0, y, 0), SHADOW);
            dust(player, feet.clone().add(right.clone().multiply(-width)).add(0, y, 0), SHADOW);
        }
        for (double a = 0; a < Math.PI * 2; a += Math.PI / 5) {
            dust(player, feet.clone().add(Math.cos(a) * 0.25, 1.75 + Math.sin(a) * 0.25, Math.sin(a) * 0.05), SHADOW);
        }
        Vector face = new Vector(-Math.sin(Math.toRadians(yaw)), 0, Math.cos(Math.toRadians(yaw))).multiply(0.3);
        dust(player, feet.clone().add(face).add(right.clone().multiply(0.4)).add(0, 1.78, 0), EYES);
        dust(player, feet.clone().add(face).add(right.clone().multiply(-0.4)).add(0, 1.78, 0), EYES);
    }

    private boolean fakeHurt(Player player, Episode episode, long now, int intensity) {
        player.sendHurtAnimation(random.nextFloat() * 360f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, SoundCategory.PLAYERS, 1f, 1f);
        fakeLowHealth(player, episode, now, 4 + random.nextInt(8));
        return true;
    }

    private void fakeLowHealth(Player player, Episode episode, long now, double lost) {
        double shown = Math.max(1.0, player.getHealth() - lost);
        player.sendHealthUpdate(shown, Math.max(0, player.getFoodLevel() - random.nextInt(6)), 0f);
        episode.timeline().restoreAt(now + 50 + random.nextInt(40), () -> {
            if (player.isOnline()) {
                player.sendHealthUpdate();
            }
        });
    }

    private boolean doppelganger(Player player, Episode episode, long now, int intensity) {
        Location at = behind(player, between(8, 14), 0.7 + random.nextDouble() * 0.3);
        PhantomClone clone = spawnPhantom(player, episode, now, PhantomClone.Behaviour.MIRROR, at,
            new Identity(ResolvableProfile.resolvableProfile(player.getPlayerProfile()), player.getName()), 60 * 20L);
        if (clone != null) {
            episode.timeline().at(now + 40, () -> whisperChat(player, "soy vos"));
        }
        return clone != null;
    }

    private boolean blockRot(Player player, Episode episode, long now, int intensity) {
        List<Block> visible = visibleSolidBlocks(player, 7, 40 + intensity * 10);
        if (visible.isEmpty()) {
            return false;
        }
        Location origin = player.getLocation();
        visible.sort((a, b) -> Double.compare(
            a.getLocation().distanceSquared(origin), b.getLocation().distanceSquared(origin)));
        player.playSound(origin, Sound.ENTITY_ZOMBIE_INFECT, SoundCategory.AMBIENT, 0.8f, 0.5f);
        long restoreAt = now + 140 + random.nextInt(80);
        BlockData flesh = pick(ROT_BLOCKS).createBlockData();
        for (int i = 0; i < visible.size(); i++) {
            Block block = visible.get(i);
            Location location = block.getLocation();
            episode.timeline().at(now + i / 2, () -> player.sendBlockChange(location, flesh));
            episode.timeline().restoreAt(restoreAt + (visible.size() - i) / 3, () -> restoreBlock(player, location));
        }
        return true;
    }

    private boolean lightsOut(Player player, Episode episode, long now, int intensity) {
        List<Block> lights = nearby(player, 12, block -> LIGHT_SOURCES.contains(block.getType()), 30);
        if (lights.isEmpty()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 120, 0, false, false, false));
            return true;
        }
        long restoreAt = now + 160 + random.nextInt(80);
        BlockData air = Material.AIR.createBlockData();
        BlockData dead = Material.BLACK_CONCRETE.createBlockData();
        for (int i = 0; i < lights.size(); i++) {
            Block block = lights.get(i);
            Location location = block.getLocation();
            BlockData replacement = block.getType().isOccluding() ? dead : air;
            episode.timeline().at(now + i * 3L, () -> {
                player.sendBlockChange(location, replacement);
                player.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.6f, 0.6f);
            });
            episode.timeline().restoreAt(restoreAt, () -> restoreBlock(player, location));
        }
        return true;
    }

    private boolean maskedPlayers(Player player, Episode episode, long now, int intensity) {
        List<Player> others = player.getWorld().getPlayers().stream()
            .filter(other -> other != player && other.getLocation().distanceSquared(player.getLocation()) < 48 * 48)
            .filter(player::canSee)
            .toList();
        if (others.isEmpty()) {
            return doppelganger(player, episode, now, intensity);
        }
        long restoreAt = now + 200 + random.nextInt(100);
        for (Player other : others) {
            Map<EquipmentSlot, ItemStack> mask = Map.of(
                EquipmentSlot.HEAD, new ItemStack(pick(MASKS)),
                EquipmentSlot.HAND, new ItemStack(pick(WEAPONS)));
            player.sendEquipmentChange(other, mask);
            episode.timeline().restoreAt(restoreAt, () -> {
                if (player.isOnline() && other.isOnline() && other.getWorld() == player.getWorld()) {
                    EntityEquipment real = other.getEquipment();
                    player.sendEquipmentChange(other, Map.of(
                        EquipmentSlot.HEAD, real.getHelmet(),
                        EquipmentSlot.HAND, real.getItemInMainHand()));
                }
            });
        }
        whisperChat(player, "ellos no son quienes dicen ser");
        return true;
    }

    private boolean redVignette(Player player, Episode episode, long now, int intensity) {
        WorldBorder border = plugin.getServer().createWorldBorder();
        border.setCenter(player.getLocation());
        border.setSize(2000);
        border.setWarningDistance(4000);
        player.setWorldBorder(border);
        player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, SoundCategory.PLAYERS, 1.5f, 0.6f);
        episode.timeline().restoreAt(now + 120 + random.nextInt(100), () -> {
            if (player.isOnline()) {
                player.setWorldBorder(null);
            }
        });
        return true;
    }

    private boolean glitchTitle(Player player, Episode episode, long now, int intensity) {
        String text = SchizophreniaLines.pick(SchizophreniaLines.TITLES, random);
        boolean[] mask = SchizophreniaLines.glitchMask(text, 0.15 + intensity * 0.05, random);
        TextComponent.Builder title = Component.text().color(NamedTextColor.DARK_RED);
        for (int i = 0; i < text.length(); i++) {
            Component letter = Component.text(text.charAt(i));
            title.append(mask[i] ? letter.decorate(TextDecoration.OBFUSCATED) : letter);
        }
        Component subtitle = Component.text(SchizophreniaLines.pick(SchizophreniaLines.SUBTITLES, random), NamedTextColor.GRAY);
        player.showTitle(Title.title(title.build(), subtitle,
            Title.Times.times(Duration.ZERO, Duration.ofMillis(900 + random.nextInt(900)), Duration.ofMillis(300))));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, SoundCategory.PLAYERS, 1f, 0.5f);
        return true;
    }

    private boolean fakeExplosion(Player player, Episode episode, long now, int intensity) {
        Location behind = behind(player, 1.2, 0.1);
        player.playSound(behind, Sound.ENTITY_CREEPER_PRIMED, SoundCategory.HOSTILE, 1f, 0.5f);
        episode.timeline().at(now + 30, () -> {
            if (random.nextDouble() < 0.25) {
                player.playSound(player.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, SoundCategory.HOSTILE, 1f, 0.5f);
                return;
            }
            Location blast = behind(player, 1.2, 0.1);
            player.playSound(blast, Sound.ENTITY_GENERIC_EXPLODE, SoundCategory.HOSTILE, 1.4f, 0.9f);
            player.spawnParticle(Particle.EXPLOSION_EMITTER, blast, 1);
            player.sendHurtAnimation(random.nextFloat() * 360f);
            fakeLowHealth(player, episode, now + 30, 10);
        });
        return true;
    }

    private boolean cameraSnap(Player player, Episode episode, long now, int intensity) {
        Location at = behind(player, 4 + random.nextInt(3), 0.0);
        PhantomClone clone = spawnPhantom(player, episode, now, PhantomClone.Behaviour.SNAP, at, randomIdentity(player), 40L);
        if (clone == null) {
            return false;
        }
        episode.timeline().at(now + 4, () -> {
            if (player.isOnline() && clone.entity().isValid()) {
                player.lookAt(clone.entity(), LookAnchor.EYES, LookAnchor.EYES);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, SoundCategory.HOSTILE, 1f, 0.5f);
            }
        });
        episode.timeline().at(now + 18, () -> vanish(player, clone, false));
        return true;
    }

    private boolean elderGuardian(Player player, Episode episode, long now, int intensity) {
        player.showElderGuardian(false);
        whisperChat(player, SchizophreniaLines.pick(SchizophreniaLines.WHISPERS, random));
        return true;
    }

    private boolean eternalNight(Player player, Episode episode, long now, int intensity) {
        player.setPlayerTime(18000L, false);
        player.setPlayerWeather(WeatherType.DOWNFALL);
        long end = now + 600 + random.nextInt(300);
        for (long t = now + 80; t < end; t += 120 + random.nextInt(120)) {
            episode.timeline().at(t, () -> player.playSound(around(player, 20), Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                SoundCategory.WEATHER, 1.5f, 0.6f));
        }
        episode.timeline().restoreAt(end, () -> {
            if (player.isOnline()) {
                player.resetPlayerTime();
                player.resetPlayerWeather();
            }
        });
        return true;
    }

    private boolean fakeThunder(Player player, Episode episode, long now, int intensity) {
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 2f, 0.8f);
        Location strike = ground(around(player, 5 + random.nextInt(5)));
        if (strike != null) {
            Location fire = strike.getBlock().getLocation();
            player.sendBlockChange(fire, Material.FIRE.createBlockData());
            player.spawnParticle(Particle.LARGE_SMOKE, fire.clone().add(0.5, 0.5, 0.5), 20, 0.3, 0.6, 0.3, 0.02);
            episode.timeline().restoreAt(now + 60, () -> restoreBlock(player, fire));
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 12, 0, false, false, false));
        return true;
    }

    private boolean swarm(Player player, Episode episode, long now, int intensity) {
        int count = 6 + random.nextInt(4);
        double offset = random.nextDouble() * Math.PI * 2;
        int spawned = 0;
        for (int i = 0; i < count; i++) {
            double angle = offset + i * (Math.PI * 2 / count);
            double distance = between(7, 12);
            Location at = player.getLocation().add(Math.cos(angle) * distance, 0, Math.sin(angle) * distance);
            if (spawnPhantom(player, episode, now, PhantomClone.Behaviour.STARE, at, randomIdentity(player), 30 * 20L, true) != null) {
                spawned++;
            }
        }
        if (spawned > 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.HOSTILE, 1f, 0.6f);
            whisperChat(player, "todos te estamos mirando");
        }
        return spawned > 0;
    }

    private boolean fakeDeath(Player player, Episode episode, long now, int intensity) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 70, 0, false, false, false));
        player.sendHurtAnimation(random.nextFloat() * 360f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, SoundCategory.PLAYERS, 1f, 1f);
        player.showTitle(Title.title(
            Component.text("¡Has muerto!", NamedTextColor.RED),
            Component.text("Puntuación: 0", NamedTextColor.WHITE),
            Title.Times.times(Duration.ZERO, Duration.ofMillis(2500), Duration.ofMillis(200))));
        episode.timeline().at(now + 55, () -> player.showTitle(Title.title(
            Component.empty(),
            Component.text("...todavía no", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC),
            Title.Times.times(Duration.ZERO, Duration.ofMillis(1500), Duration.ofMillis(500)))));
        return true;
    }

    // ---------------------------------------------------------------- phantoms

    record Identity(ResolvableProfile profile, String name) {
    }

    PhantomClone spawnPhantom(
        Player player, Episode episode, long now, PhantomClone.Behaviour behaviour,
        Location near, Identity identity, long lifetimeTicks
    ) {
        return spawnPhantom(player, episode, now, behaviour, near, identity, lifetimeTicks, false);
    }

    private PhantomClone spawnPhantom(
        Player player, Episode episode, long now, PhantomClone.Behaviour behaviour,
        Location near, Identity identity, long lifetimeTicks, boolean ignoreCap
    ) {
        if (!ignoreCap && episode.phantoms().size() >= settings.phantoms().maxPerPlayer()) {
            return null;
        }
        Location at = ground(near);
        if (at == null) {
            return null;
        }
        at.setYaw(GazeMath.yawTowards(at.getX(), at.getZ(), player.getX(), player.getZ()));
        at.setPitch(0f);
        Mannequin mannequin = at.getWorld().spawn(at, Mannequin.class, entity -> {
            entity.setVisibleByDefault(false);
            entity.setPersistent(false);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.setAI(false);
            entity.setCollidable(false);
            entity.setGravity(false);
            entity.setImmovable(true);
            entity.addScoreboardTag(PHANTOM_TAG);
            entity.setProfile(identity.profile());
            entity.customName(Component.text(identity.name()));
            entity.setCustomNameVisible(true);
            entity.setDescription(null);
        });
        player.showEntity(plugin, mannequin);
        PhantomClone clone = new PhantomClone(mannequin, behaviour, now + lifetimeTicks);
        episode.phantoms().add(clone);
        return clone;
    }

    /** Moves, turns or dissolves every phantom of one victim. */
    void updatePhantoms(Player player, Episode episode, long now) {
        SchizophreniaSettings.Phantoms config = settings.phantoms();
        for (PhantomClone clone : new ArrayList<>(episode.phantoms())) {
            Mannequin entity = clone.entity();
            if (!entity.isValid() || entity.getWorld() != player.getWorld()
                || entity.getLocation().distanceSquared(player.getLocation()) > 80 * 80 || clone.expired(now)) {
                clone.remove();
                episode.phantoms().remove(clone);
                continue;
            }
            Location eye = entity.getLocation().add(0, 1.6, 0);
            boolean watched = looksAt(player, eye, config.gazeThresholdDegrees()) && player.hasLineOfSight(entity);
            double distance = entity.getLocation().distance(player.getLocation());
            switch (clone.behaviour()) {
                case STARE -> {
                    if (watched || distance < config.vanishDistance()) {
                        vanish(player, clone, true);
                    } else {
                        face(entity, player);
                    }
                }
                case MIRROR -> {
                    face(entity, player);
                    if (watched) {
                        clone.watchedFor(clone.watchedFor() + 2);
                    }
                    if (clone.watchedFor() > 60 || distance < config.vanishDistance()) {
                        player.playSound(eye, Sound.ENTITY_WITCH_CELEBRATE, SoundCategory.HOSTILE, 1f, 0.4f);
                        vanish(player, clone, false);
                    }
                }
                case STALKER -> stalk(player, episode, clone, watched, distance, now);
                case SNAP -> face(entity, player);
            }
        }
    }

    private void stalk(Player player, Episode episode, PhantomClone clone, boolean watched, double distance, long now) {
        Mannequin entity = clone.entity();
        if (watched) {
            clone.watchedFor(clone.watchedFor() + 2);
            if (clone.watchedFor() > 50) {
                vanish(player, clone, true);
            }
            return;
        }
        if (distance < 1.8) {
            jumpscare(player, episode, now);
            vanish(player, clone, false);
            return;
        }
        Vector step = player.getLocation().toVector().subtract(entity.getLocation().toVector()).setY(0);
        if (step.lengthSquared() < 1.0E-4) {
            return;
        }
        step.normalize().multiply(0.45);
        Location next = ground(entity.getLocation().add(step));
        if (next == null) {
            vanish(player, clone, true);
            return;
        }
        next.setYaw(GazeMath.yawTowards(next.getX(), next.getZ(), player.getX(), player.getZ()));
        entity.teleport(next);
        entity.setBodyYaw(next.getYaw());
        if (random.nextInt(6) == 0) {
            playStepSound(player, next);
        }
    }

    private void jumpscare(Player player, Episode episode, long now) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, SoundCategory.HOSTILE, 1.5f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, SoundCategory.HOSTILE, 1.5f, 0.6f);
        player.sendHurtAnimation(random.nextFloat() * 360f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0, false, false, false));
        player.showTitle(Title.title(
            Component.text("TE ENCONTRÉ", NamedTextColor.DARK_RED, TextDecoration.BOLD),
            Component.empty(),
            Title.Times.times(Duration.ZERO, Duration.ofMillis(700), Duration.ofMillis(200))));
        fakeLowHealth(player, episode, now, 6);
    }

    void vanish(Player player, PhantomClone clone, boolean poof) {
        if (clone.entity().isValid() && player.isOnline() && poof) {
            Location at = clone.entity().getLocation().add(0, 1, 0);
            player.spawnParticle(Particle.LARGE_SMOKE, at, 15, 0.25, 0.7, 0.25, 0.01);
            player.playSound(at, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, SoundCategory.HOSTILE, 0.6f, 0.5f);
        }
        clone.remove();
    }

    void removeAllPhantoms(Episode episode) {
        episode.phantoms().forEach(PhantomClone::remove);
        episode.phantoms().clear();
    }

    private static void face(Mannequin entity, Player player) {
        Location location = entity.getLocation();
        float yaw = GazeMath.yawTowards(location.getX(), location.getZ(), player.getX(), player.getZ());
        double dy = player.getEyeLocation().getY() - (location.getY() + 1.6);
        double horizontal = Math.hypot(player.getX() - location.getX(), player.getZ() - location.getZ());
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.max(0.1, horizontal)));
        entity.setRotation(yaw, pitch);
        entity.setBodyYaw(yaw);
    }

    private Identity randomIdentity(Player victim) {
        List<? extends Player> others = victim.getServer().getOnlinePlayers().stream()
            .filter(other -> !other.equals(victim)).toList();
        if (!others.isEmpty() && random.nextDouble() < 0.75) {
            Player other = others.get(random.nextInt(others.size()));
            return new Identity(ResolvableProfile.resolvableProfile(other.getPlayerProfile()), other.getName());
        }
        if (random.nextBoolean()) {
            return new Identity(ResolvableProfile.resolvableProfile(victim.getPlayerProfile()), victim.getName());
        }
        return new Identity(Mannequin.defaultProfile(), SchizophreniaLines.pick(SchizophreniaLines.PHANTOM_NAMES, random));
    }

    // ---------------------------------------------------------------- helpers

    private String randomName(Player victim) {
        List<String> names = new ArrayList<>(victim.getServer().getOnlinePlayers().stream()
            .filter(other -> !other.equals(victim)).map(Player::getName).toList());
        OfflinePlayer[] offline = victim.getServer().getOfflinePlayers();
        for (int i = 0; i < Math.min(5, offline.length); i++) {
            String name = offline[random.nextInt(offline.length)].getName();
            if (name != null && !name.equals(victim.getName())) {
                names.add(name);
            }
        }
        names.addAll(SchizophreniaLines.PHANTOM_NAMES.subList(0, 2));
        return names.get(random.nextInt(names.size()));
    }

    private void whisperChat(Player player, String line) {
        if (player.isOnline()) {
            player.sendMessage(Component.text(line, NamedTextColor.DARK_GRAY, TextDecoration.ITALIC));
        }
    }

    private static void restoreBlock(Player player, Location location) {
        if (player.isOnline() && location.getWorld() == player.getWorld()) {
            player.sendBlockChange(location, location.getBlock().getBlockData());
        }
    }

    private boolean looksAt(Player player, Location target, double thresholdDegrees) {
        Location eye = player.getEyeLocation();
        Vector look = eye.getDirection();
        return GazeMath.angleTo(look.getX(), look.getY(), look.getZ(),
            eye.getX(), eye.getY(), eye.getZ(), target.getX(), target.getY(), target.getZ()) < thresholdDegrees;
    }

    private Location behind(Player player, double distance, double side) {
        double[] offset = GazeMath.offset(player.getLocation().getYaw(), side, random.nextBoolean(), distance);
        return player.getLocation().add(offset[0], 0, offset[1]);
    }

    private Location around(Player player, double distance) {
        double angle = random.nextDouble() * Math.PI * 2;
        return player.getLocation().add(Math.cos(angle) * distance, random.nextDouble() * 2 - 1, Math.sin(angle) * distance);
    }

    /** Closest spot near {@code near} with two passable blocks over solid ground, or null. */
    private static Location ground(Location near) {
        World world = near.getWorld();
        if (world == null) {
            return null;
        }
        int x = near.getBlockX();
        int z = near.getBlockZ();
        if (!world.isChunkLoaded(x >> 4, z >> 4)) {
            return null;
        }
        int baseY = near.getBlockY();
        for (int i = 0; i <= 12; i++) {
            int dy = (i % 2 == 0) ? i / 2 : -(i + 1) / 2;
            int y = baseY + dy;
            if (y <= world.getMinHeight() || y + 1 >= world.getMaxHeight()) {
                continue;
            }
            Block feet = world.getBlockAt(x, y, z);
            if (feet.isPassable() && !feet.isLiquid()
                && feet.getRelative(BlockFace.UP).isPassable()
                && feet.getRelative(BlockFace.DOWN).getType().isSolid()) {
                return new Location(world, near.getX(), y, near.getZ());
            }
        }
        return null;
    }

    private List<Block> visibleSolidBlocks(Player player, int radius, int limit) {
        return nearby(player, radius, block -> block.getType().isOccluding() && touchesAir(block), limit);
    }

    private List<Block> nearby(Player player, int radius, java.util.function.Predicate<Block> filter, int limit) {
        List<Block> found = new ArrayList<>();
        Location origin = player.getLocation();
        World world = player.getWorld();
        for (int x = -radius; x <= radius && found.size() < limit; x++) {
            for (int y = -radius / 2; y <= radius / 2 + 2 && found.size() < limit; y++) {
                for (int z = -radius; z <= radius && found.size() < limit; z++) {
                    if (random.nextInt(3) != 0 || x * x + z * z > radius * radius) {
                        continue;
                    }
                    int by = origin.getBlockY() + y;
                    if (by <= world.getMinHeight() || by >= world.getMaxHeight()) {
                        continue;
                    }
                    Block block = world.getBlockAt(origin.getBlockX() + x, by, origin.getBlockZ() + z);
                    if (filter.test(block)) {
                        found.add(block);
                    }
                }
            }
        }
        return found;
    }

    private static boolean touchesAir(Block block) {
        for (BlockFace face : List.of(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
            BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)) {
            if (block.getRelative(face).getType().isAir()) {
                return true;
            }
        }
        return false;
    }

    private void dust(Player player, Location at, Particle.DustOptions options) {
        player.spawnParticle(Particle.DUST, at.getX(), at.getY(), at.getZ(), 1, 0, 0, 0, 0, options);
    }

    private double between(double min, double max) {
        return min + random.nextDouble() * (max - min);
    }

    private <T> T pick(List<T> values) {
        return values.get(random.nextInt(values.size()));
    }
}
