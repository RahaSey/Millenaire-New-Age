package com.mat37dev.village;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Nivelle le terrain sous l'empreinte d'un bâtiment avant placement.
 */
public class TerrainAdapter {

    private TerrainAdapter() {}

    private static final int SLOPE_RADIUS = 3;
    private static final int CLEARANCE_HEIGHT = 40;

    /** Blocs ignorés lors du calcul de la hauteur (arbres, végétaux décoratifs, neige). */
    private static final Set<Block> IGNORED_FOR_HEIGHT = Set.of(
        Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.SPRUCE_LOG, Blocks.JUNGLE_LOG,
        Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.CHERRY_LOG, Blocks.MANGROVE_LOG,
        Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.JUNGLE_LEAVES,
        Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.CHERRY_LEAVES, Blocks.MANGROVE_LEAVES,
        Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES,
        // Végétaux 1 bloc (herbe courte, fougère, herbe haute, grande fougère)
        Blocks.SHORT_GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN,
        Blocks.SNOW, Blocks.VINE, Blocks.SUGAR_CANE, Blocks.CACTUS, Blocks.CORNFLOWER,
            Blocks.WILDFLOWERS, Blocks.POPPY, Blocks.DANDELION, Blocks.ALLIUM, Blocks.AZURE_BLUET,
            Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP
    );

    public static int adapt(ServerLevel level, BlockPos origin, int sizeX, int sizeZ) {
        return adapt(level, origin, sizeX, sizeZ, 0, List.of());
    }

    public static int adapt(ServerLevel level, BlockPos origin, int sizeX, int sizeZ, int padding) {
        return adapt(level, origin, sizeX, sizeZ, padding, List.of());
    }

    /**
     * Nivelle le terrain pour un bâtiment en préservant les empreintes voisines déjà placées.
     *
     * <p>Dans la zone de {@code padding} blocs autour de l'empreinte principale, si une cellule
     * tombe dans l'empreinte d'un bâtiment de {@code placed}, elle est ignorée (fondation ET void).
     * Cela empêche de détruire les murs d'un bâtiment adjacent lors du nivelage.</p>
     *
     * @param placed empreintes des bâtiments déjà posés (peut être vide)
     */
    public static int adapt(ServerLevel level, BlockPos origin, int sizeX, int sizeZ, int padding,
                             List<PlacedBuilding> placed) {
        int startX = origin.getX() - padding;
        int startZ = origin.getZ() - padding;
        int totalX = sizeX + 2 * padding;
        int totalZ = sizeZ + 2 * padding;

        detectVanillaStructures(level, origin, sizeX, sizeZ);

        // Calcul de la hauteur cible (MÉDIANE au niveau de l'AIR au-dessus du sol)
        int count = totalX * totalZ;
        int[] heights = new int[count];
        int idx = 0;
        for (int x = 0; x < totalX; x++) {
            for (int z = 0; z < totalZ; z++) {
                heights[idx++] = getNaturalAirHeight(level, startX + x, startZ + z);
            }
        }
        int targetH = count > 0 ? computeMedian(heights, count) : origin.getY();

        clearEntities(level, startX, startZ, totalX, totalZ, targetH);

        // Pass unique : Fondation, Remplissage et "Void"
        for (int x = 0; x < totalX; x++) {
            for (int z = 0; z < totalZ; z++) {
                int wx = startX + x;
                int wz = startZ + z;

                // Protection : dans la zone de padding, ne pas toucher les bâtiments déjà placés.
                boolean inPadding = padding > 0
                        && (x < padding || x >= padding + sizeX || z < padding || z >= padding + sizeZ);
                if (inPadding && !placed.isEmpty() && isWithinAnyFootprint(wx, wz, placed)) continue;

                neutralizeLava(level, wx, wz, level.getMinY(), targetH + 10);

                // a) Fondation : On assure une base solide SOUS le niveau de pose.
                for (int y = targetH - 1; y >= targetH - 10; y--) {
                    BlockPos p = new BlockPos(wx, y, wz);
                    BlockState s = level.getBlockState(p);
                    if (canBeReplacedByFoundation(s)) {
                        level.setBlock(p, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        break;
                    }
                }

                // b) VOID : On vide tout ce qui est au niveau de pose et au-dessus
                for (int y = targetH; y < targetH + CLEARANCE_HEIGHT; y++) {
                    BlockPos pos = new BlockPos(wx, y, wz);
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }

        applySlopes(level, startX, startZ, totalX, totalZ, targetH, placed);

        return targetH;
    }

    /**
     * Trouve le premier bloc d'air au-dessus du vrai sol.
     */
    private static int getNaturalAirHeight(ServerLevel level, int x, int z) {
        int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, h, z);
        
        while (pos.getY() > level.getMinY()) {
            BlockState state = level.getBlockState(pos);
            // Ignorer les végétaux décoratifs non-solides (fleurs, herbe, fougères, buissons,
            // plantes doubles) : on scanne à travers eux pour trouver le vrai sol solide.
            // Ces blocs seront supprimés par la passe VOID qui part du sol réel.
            Block block = state.getBlock();
            boolean isDecorative = block instanceof BushBlock || block instanceof DoublePlantBlock;

            // On s'arrête sur le premier bloc solide (non décoratif) ou eau
            if (state.is(Blocks.WATER) || state.is(Blocks.GRASS_BLOCK)
                    || (!state.isAir() && !isDecorative && !IGNORED_FOR_HEIGHT.contains(block))) {
                return pos.getY() + 1;
            }
            pos.move(0, -1, 0);
        }
        return h;
    }

    private static void detectVanillaStructures(ServerLevel level, BlockPos origin, int sizeX, int sizeZ) {
        var structures = level.structureManager().getAllStructuresAt(origin);
        if (!structures.isEmpty()) {
            String names = String.join(", ", structures.keySet().stream()
                .map(Object::toString)
                .toList());
            MillenaireNewAge.LOGGER.info("[MNA] Structure(s) vanilla détectée(s) en {} : {}. Elles seront écrasées.", 
                origin.toShortString(), names);
        }
    }

    private static void clearEntities(ServerLevel level, int x, int z, int w, int d, int targetH) {
        AABB area = new AABB(x, targetH - 2, z, x + w, targetH + CLEARANCE_HEIGHT, z + d);
        List<Entity> entities = level.getEntities(null, area);
        for (Entity e : entities) {
            if (!(e instanceof Player) && !(e instanceof ItemEntity) && !(e instanceof ExperienceOrb)) {
                e.discard();
            }
        }
    }

    private static void applySlopes(ServerLevel level, int startX, int startZ, int totalX, int totalZ,
                                     int targetH, List<PlacedBuilding> placed) {
        for (int x = -SLOPE_RADIUS; x < totalX + SLOPE_RADIUS; x++) {
            for (int z = -SLOPE_RADIUS; z < totalZ + SLOPE_RADIUS; z++) {
                if (x >= 0 && x < totalX && z >= 0 && z < totalZ) continue;

                int distX = (x < 0) ? -x : (x >= totalX) ? x - totalX + 1 : 0;
                int distZ = (z < 0) ? -z : (z >= totalZ) ? z - totalZ + 1 : 0;
                int dist = Math.max(distX, distZ);

                if (dist > SLOPE_RADIUS) continue;

                int wx = startX + x;
                int wz = startZ + z;

                // Ne pas toucher aux empreintes des bâtiments voisins déjà placés.
                if (!placed.isEmpty() && isWithinAnyFootprint(wx, wz, placed)) continue;

                int naturalAirH = getNaturalAirHeight(level, wx, wz);
                int interpAirH = targetH + (naturalAirH - targetH) * dist / (SLOPE_RADIUS + 1);

                if (naturalAirH > interpAirH) {
                    for (int y = interpAirH; y < naturalAirH + 5; y++) {
                        level.setBlock(new BlockPos(wx, y, wz), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                } else if (naturalAirH < interpAirH) {
                    for (int y = naturalAirH; y < interpAirH; y++) {
                        level.setBlock(new BlockPos(wx, y, wz), Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    /**
     * Retourne {@code true} si le bloc world (wx, wz) se trouve dans l'empreinte
     * d'au moins un bâtiment de la liste.
     */
    private static boolean isWithinAnyFootprint(int wx, int wz, List<PlacedBuilding> placed) {
        for (PlacedBuilding pb : placed) {
            if (wx >= pb.x() && wx < pb.x() + pb.sizeX()
                    && wz >= pb.z() && wz < pb.z() + pb.sizeZ()) {
                return true;
            }
        }
        return false;
    }

    private static boolean canBeReplacedByFoundation(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA) 
            || IGNORED_FOR_HEIGHT.contains(state.getBlock())
            || state.getBlock() instanceof FlowerBlock
            || state.getBlock() instanceof BushBlock;
    }

    private static void neutralizeLava(ServerLevel level, int x, int z, int minY, int maxY) {
        for (int y = minY; y <= maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).getBlock() == Blocks.LAVA) {
                level.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static int computeMedian(int[] values, int count) {
        Arrays.sort(values, 0, count);
        if (count % 2 == 1) {
            return values[count / 2];
        }
        return (values[count / 2 - 1] + values[count / 2]) / 2;
    }
}
