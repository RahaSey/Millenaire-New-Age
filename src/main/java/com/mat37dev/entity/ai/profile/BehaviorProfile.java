package com.mat37dev.entity.ai.profile;

import com.mat37dev.entity.MillVillagerEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.List;

/**
 * Profil de comportement immutable pour un villageois.
 *
 * <p>Chaque liste contient des paires (priorité, behavior) prêtes à être
 * passées à {@code Brain.addActivity()}.</p>
 */
public record BehaviorProfile(
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> core,
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> work,
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> meet,
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> rest,
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> panic,
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> idle
) {}
