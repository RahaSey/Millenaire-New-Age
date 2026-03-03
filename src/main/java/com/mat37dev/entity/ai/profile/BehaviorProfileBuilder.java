package com.mat37dev.entity.ai.profile;

import com.google.common.collect.ImmutableList;
import com.mat37dev.entity.MillVillagerEntity;
import com.mat37dev.entity.ai.behavior.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.Behavior;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder fluent pour construire un {@link BehaviorProfile}.
 *
 * <p>Les behaviors universels (core, meet, rest, panic, idle) sont ajoutés
 * automatiquement par {@link #build()}. Seuls les behaviors spécifiques au
 * rôle (work) doivent être ajoutés manuellement.</p>
 */
public class BehaviorProfileBuilder {

    private final List<Pair<Integer, Behavior<? super MillVillagerEntity>>> workBehaviors = new ArrayList<>();

    /**
     * Ajoute un behavior à l'activité WORK avec une priorité donnée.
     */
    public void addWorkBehavior(int priority, Behavior<? super MillVillagerEntity> behavior) {
        workBehaviors.add(Pair.of(priority, behavior));
    }

    /**
     * Construit le profil complet avec les behaviors universels.
     */
    public BehaviorProfile build() {
        // CORE — toujours actif
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> core = ImmutableList.of(
                Pair.of(0, new MillDoorInteractBehavior())
        );

        // WORK — spécifique au rôle (si aucun work behavior ajouté → wander par défaut)
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> work;
        if (workBehaviors.isEmpty()) {
            work = ImmutableList.of(Pair.of(0, new WanderAroundVillageBehavior()));
        } else {
            work = ImmutableList.copyOf(workBehaviors);
        }

        // MEET — socialisation au centre du village
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> meet = ImmutableList.of(
                Pair.of(0, new GoToVillageCenterBehavior()),
                Pair.of(1, new SocializeAtCenterBehavior()),
                Pair.of(2, new WanderAroundVillageBehavior())
        );

        // REST — rentrer et dormir
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> rest = ImmutableList.of(
                Pair.of(0, new GoHomeBehavior()),
                Pair.of(1, new SleepAtHomeBehavior())
        );

        // PANIC — désactivé pour le moment (pas de fuite)
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> panic = ImmutableList.of();

        // IDLE — fallback
        List<Pair<Integer, Behavior<? super MillVillagerEntity>>> idle = ImmutableList.of(
                Pair.of(0, new WanderAroundVillageBehavior())
        );

        return new BehaviorProfile(core, work, meet, rest, panic, idle);
    }
}
