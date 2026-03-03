package com.mat37dev.entity.ai.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jetbrains.annotations.NotNull;

/**
 * Navigation au sol pour les villageois Millénaire.
 *
 * <p>Utilise {@link MillWalkNodeEvaluator} qui traite portes, portillons et trappes
 * comme de l'air pour le pathfinding. Cela permet aux villageois de naviguer
 * librement à travers les bâtiments multi-portes (château, etc.).</p>
 *
 * <p>Le {@code maxVisitedNodes} est augmenté à un minimum de 4000 pour permettre
 * la navigation dans les grands bâtiments multi-étages (château avec escaliers).</p>
 */
public class MillPathNavigation extends GroundPathNavigation {

    private static final int MIN_VISITED_NODES = 4000;

    public MillPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new MillWalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanFloat(true);
        return new PathFinder(this.nodeEvaluator, Math.max(maxVisitedNodes, MIN_VISITED_NODES));
    }
}
