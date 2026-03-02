package com.mat37dev.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

/**
 * Registre des types de mémoire du Brain des villageois Millenaire.
 *
 * <p>Les mémoires avec un Codec sont persistées dans le NBT du Brain.
 * Les mémoires sans Codec sont runtime-only (effacées au rechargement).</p>
 */
public class MillMemories {

    /** Position de la maison assignée. Persistée dans le Brain NBT. */
    public static final MemoryModuleType<BlockPos> HOME_POS = register(
            "home_pos", new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));

    /** Position du lieu de travail assigné. Persistée dans le Brain NBT. */
    public static final MemoryModuleType<BlockPos> WORK_POS = register(
            "work_pos", new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));

    /** Position du lit assigné dans la maison. Persistée dans le Brain NBT. */
    public static final MemoryModuleType<BlockPos> HOME_BED_POS = register(
            "home_bed_pos", new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));

    /** Cible de balade temporaire (runtime uniquement). */
    public static final MemoryModuleType<BlockPos> WANDER_TARGET = register(
            "wander_target", new MemoryModuleType<>(Optional.empty()));

    /** Joueur le plus proche dans le rayon de vision (runtime uniquement). */
    public static final MemoryModuleType<Player> NEAREST_PLAYER = register(
            "nearest_player", new MemoryModuleType<>(Optional.empty()));

    /** Position du centre du village. Persistée dans le Brain NBT. */
    public static final MemoryModuleType<BlockPos> VILLAGE_CENTER_POS = register(
            "village_center_pos", new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));

    /** Position de l'entrée (porte) de la maison assignée. Runtime — recalculée au besoin. */
    public static final MemoryModuleType<BlockPos> HOME_ENTRANCE_POS = register(
            "home_entrance_pos", new MemoryModuleType<>(Optional.empty()));

    /** Menace la plus proche (monstre) détectée par ThreatSensor (runtime uniquement). */
    public static final MemoryModuleType<LivingEntity> ATTACK_TARGET = register(
            "attack_target", new MemoryModuleType<>(Optional.empty()));

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static <T> MemoryModuleType<T> register(String name, MemoryModuleType<T> type) {
        return Registry.register(
                BuiltInRegistries.MEMORY_MODULE_TYPE,
                ResourceLocation.fromNamespaceAndPath("millenaire-new-age", name),
                type);
    }

    /** Déclenche le chargement statique de la classe (initialisation du registre). */
    public static void init() {}
}
