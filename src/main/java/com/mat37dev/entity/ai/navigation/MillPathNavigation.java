package com.mat37dev.entity.ai.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

/**
 * Navigation au sol pour les villageois Millénaire.
 *
 * <p>Différence avec la navigation vanilla : le {@link WalkNodeEvaluator} est configuré
 * avec {@code canOpenDoors=true} pour que le <b>pathfinder lui-même</b> planifie des
 * chemins à travers les portes fermées (type {@code WALKABLE_DOOR}).
 * Vanilla ne fait que {@code canPassDoors=true} (portes ouvertes uniquement).</p>
 */
public class MillPathNavigation extends GroundPathNavigation {

    public MillPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int maxVisitedNodes) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.nodeEvaluator.setCanOpenDoors(true);
        this.nodeEvaluator.setCanFloat(true);
        return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
    }
}
