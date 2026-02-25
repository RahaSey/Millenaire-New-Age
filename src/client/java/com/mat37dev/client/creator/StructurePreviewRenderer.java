package com.mat37dev.client.creator;

import com.mat37dev.creator.StructurePlacerItem;
import com.mat37dev.mixin.StructureTemplateAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Rendu du preview fantôme lors du port de la Baguette de Placement.
 *
 * <p>Charge le template NBT depuis {@code creator_output/structures/} et rend
 * chaque bloc avec son vrai modèle, semi-transparent (effet Schematica).</p>
 *
 * <p>Fallback vers cubes bleutés si le fichier n'est pas accessible.</p>
 */
@Environment(EnvType.CLIENT)
public class StructurePreviewRenderer {

    /** Alpha des blocs fantômes (40 % opaque) */
    private static final float BLOCK_ALPHA = 0.40f;

    // Couleur fallback (bleu schématique)
    private static final float FB_R = 0.15f, FB_G = 0.40f, FB_B = 1.00f, FB_A = 0.35f;

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(StructurePreviewRenderer::afterEntities);
    }

    private static void afterEntities(WorldRenderContext ctx) {
        if (!CreatorClientState.hasPreview()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Vérifier que le joueur tient la baguette de placement
        if (!(mc.player.getMainHandItem().getItem() instanceof StructurePlacerItem)) return;

        // Calcul de l'origine (doit matcher StructurePlacerItem.use/useOn)
        BlockPos originPos;
        net.minecraft.world.phys.HitResult hit = mc.player.pick(128.0, 0.0f, false);
        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.world.phys.BlockHitResult bhr = (net.minecraft.world.phys.BlockHitResult) hit;
            originPos = bhr.getBlockPos().relative(bhr.getDirection());
        } else {
            originPos = mc.player.blockPosition().relative(mc.player.getDirection(), 5);
        }

        PoseStack poseStack = ctx.matrices();
        if (poseStack == null) return;
        MultiBufferSource consumers = ctx.consumers();
        if (consumers == null) return;

        Vec3 camPos  = mc.gameRenderer.getMainCamera().getPosition();
        int  rotation = CreatorClientState.getRotation();
        Vec3i size    = CreatorClientState.getPreviewSize();

        // ── Essai de rendu avec vrais blocs ──────────────────────────────────
        String structureId = CreatorClientState.getPreviewStructureId();
        StructureTemplate template = tryLoadTemplate(mc, structureId);

        if (template != null) {
            renderRealBlocks(mc, template, originPos, rotation, camPos, poseStack, consumers);
        } else {
            // Fallback : cubes bleutés
            renderFallbackCubes(
                CreatorClientState.getPreviewBlocks(), originPos,
                rotation, size, camPos, poseStack, consumers);
        }

        // ── Boîte englobante (toujours affichée) ─────────────────────────────
        Vec3i rotatedSize = applyRotationToSize(size, rotation);
        VertexConsumer lines = consumers.getBuffer(RenderType.lines());
        double ox = originPos.getX() - camPos.x;
        double oy = originPos.getY() - camPos.y;
        double oz = originPos.getZ() - camPos.z;
        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);
        ShapeRenderer.renderLineBox(poseStack.last(), lines,
            0.0, 0.0, 0.0,
            rotatedSize.getX(), rotatedSize.getY(), rotatedSize.getZ(),
            1.0f, 1.0f, 0.0f, 1.0f);
        poseStack.popPose();
    }

    // ── Rendu vrais blocs ─────────────────────────────────────────────────────

    private static void renderRealBlocks(Minecraft mc,
                                         StructureTemplate template,
                                         BlockPos originPos,
                                         int rotation,
                                         Vec3 camPos,
                                         PoseStack poseStack,
                                         MultiBufferSource consumers) {
        var palettes = ((StructureTemplateAccessor) template).getPalettes();
        if (palettes.isEmpty()) return;

        Rotation mcRotation = StructurePlacerItem.toMcRotation(rotation);
        var blockRenderer   = mc.getBlockRenderer();

        // Source avec réduction d'alpha : redirige tout vers RenderType.translucentMovingBlock()
        AlphaMultiBufferSource alphaSource = new AlphaMultiBufferSource(consumers, BLOCK_ALPHA);

        for (StructureTemplate.StructureBlockInfo info : palettes.getFirst().blocks()) {
            BlockState state = info.state();
            if (state.isAir()) continue;

            BlockPos relPos   = info.pos();
            BlockPos rotated  = applyRotation(relPos, rotation,
                new Vec3i(template.getSize().getX(),
                          template.getSize().getY(),
                          template.getSize().getZ()));
            BlockPos worldPos = originPos.offset(rotated);

            double dx = worldPos.getX() - camPos.x;
            double dy = worldPos.getY() - camPos.y;
            double dz = worldPos.getZ() - camPos.z;

            // Appliquer la rotation au block state (pour logs, escaliers, etc.)
            BlockState rotatedState = state.rotate(mcRotation);

            poseStack.pushPose();
            poseStack.translate(dx, dy, dz);
            blockRenderer.renderSingleBlock(
                rotatedState,
                poseStack,
                alphaSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    // ── Fallback cubes bleutés ────────────────────────────────────────────────

    private static void renderFallbackCubes(List<BlockPos> blocks,
                                            BlockPos originPos,
                                            int rotation,
                                            Vec3i size,
                                            Vec3 camPos,
                                            PoseStack poseStack,
                                            MultiBufferSource consumers) {
        // RenderType.lines() est garanti disponible dans AFTER_ENTITIES
        VertexConsumer lines = consumers.getBuffer(RenderType.lines());

        for (BlockPos rel : blocks) {
            BlockPos rotated  = applyRotation(rel, rotation, size);
            BlockPos worldPos = originPos.offset(rotated);
            double dx = worldPos.getX() - camPos.x;
            double dy = worldPos.getY() - camPos.y;
            double dz = worldPos.getZ() - camPos.z;

            poseStack.pushPose();
            poseStack.translate(dx, dy, dz);
            ShapeRenderer.renderLineBox(poseStack.last(), lines,
                0.0, 0.0, 0.0, 1.0, 1.0, 1.0,
                FB_R, FB_G, FB_B, 0.8f);
            poseStack.popPose();
        }
    }

    // ── Chargement du template ────────────────────────────────────────────────

    private static StructureTemplate tryLoadTemplate(Minecraft mc, String structureId) {
        if (structureId == null || mc.level == null) return null;
        String sanitized = structureId.replace(':', '/');
        
        // 1. Dossier creator local (mods/MillenaireNewAge)
        Path localPath = mc.gameDirectory.toPath()
            .resolve("mods/MillenaireNewAge/creator_structures/" + sanitized + ".nbt");

        // 2. Dossier src/main/resources (dev mode helper)
        String relPath = "data/millenaire-new-age/structure/" + sanitized + ".nbt";
        Path srcPath = mc.gameDirectory.toPath().resolve("src/main/resources").resolve(relPath);
        
        // Tente ../src si lancé depuis /run
        if (!Files.exists(srcPath)) {
            srcPath = mc.gameDirectory.toPath().resolve("../src/main/resources").resolve(relPath);
        }

        Path finalPath = Files.exists(localPath) ? localPath : (Files.exists(srcPath) ? srcPath : null);

        if (finalPath != null) {
            try {
                CompoundTag nbt = NbtIo.readCompressed(finalPath, NbtAccounter.unlimitedHeap());
                StructureTemplate template = new StructureTemplate();
                template.load(mc.level.holderLookup(Registries.BLOCK), nbt);
                return template;
            } catch (IOException e) {
                return null;
            }
        }
        
        return null;
    }

    // ── Rotation ─────────────────────────────────────────────────────────────

    private static BlockPos applyRotation(BlockPos rel, int rotation, Vec3i size) {
        return switch (rotation & 3) {
            case 1 -> new BlockPos(size.getZ() - 1 - rel.getZ(), rel.getY(), rel.getX());
            case 2 -> new BlockPos(size.getX() - 1 - rel.getX(), rel.getY(), size.getZ() - 1 - rel.getZ());
            case 3 -> new BlockPos(rel.getZ(), rel.getY(), size.getX() - 1 - rel.getX());
            default -> rel;
        };
    }

    private static Vec3i applyRotationToSize(Vec3i size, int rotation) {
        return switch (rotation & 3) {
            case 1, 3 -> new Vec3i(size.getZ(), size.getY(), size.getX());
            default   -> size;
        };
    }

    // ── Wrappers alpha ────────────────────────────────────────────────────────

    /**
         * MultiBufferSource qui redirige tous les buffers vers RenderType.translucent()
         * et applique une réduction d'alpha à chaque vertex.
         */
        private record AlphaMultiBufferSource(MultiBufferSource delegate, float alpha) implements MultiBufferSource {

        @Override
            public @NotNull VertexConsumer getBuffer(RenderType renderType) {
                // Tout passe par translucentMovingBlock pour activer le blending alpha
                VertexConsumer base = delegate.getBuffer(RenderType.translucentMovingBlock());
                return new AlphaVertexConsumer(base, alpha);
            }
        }

    /**
     * Wrapper VertexConsumer qui divise l'alpha de chaque vertex par {@code alpha}.
     */
    private record AlphaVertexConsumer(VertexConsumer delegate, float alpha)
            implements VertexConsumer {

        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(x, y, z);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
            delegate.setColor(r, g, b, (int)(a * alpha));
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv1(int i, int j) {
            delegate.setUv1(i, j);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv2(int i, int j) {
            delegate.setUv2(i, j);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setNormal(float nx, float ny, float nz) {
            delegate.setNormal(nx, ny, nz);
            return this;
        }
    }
}
