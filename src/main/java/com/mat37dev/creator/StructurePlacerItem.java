package com.mat37dev.creator;

import com.mat37dev.network.StructureRotationPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.NotNull;

/**
 * Baguette de Placement.
 *
 * <ul>
 *   <li>Clic droit sur bloc (sans Shift) = placer la structure</li>
 *   <li>Clic droit sur bloc (avec Shift) = rotation +90°</li>
 *   <li>Clic droit dans le vide = rotation +90° (fallback)</li>
 * </ul>
 *
 * <p>La structure sélectionnée et la rotation sont stockées dans {@link CustomData}
 * de l'ItemStack. L'ID est aussi mis en cache dans {@link CreatorSession} du joueur.</p>
 */
public class StructurePlacerItem extends Item {

    private static final String TAG_STRUCTURE = "structure_id";
    private static final String TAG_ROTATION  = "rotation";

    public StructurePlacerItem(Properties props) {
        super(props);
    }

    // ── Clic droit sur bloc ──────────────────────────────────────────────────

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;

        if (ctx.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        ItemStack stack = ctx.getItemInHand();

        // Shift + clic = rotation
        if (ctx.isSecondaryUseActive()) {
            cycleRotationServer(serverPlayer, stack);
            return InteractionResult.SUCCESS;
        }

        // Clic normal = placement
        return placeStructureServer(serverPlayer, stack, ctx.getClickedPos().relative(ctx.getClickedFace()));
    }

    // ── Clic droit (dans le vide ou global) ──────────────────────────────────

    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        // Si on fait Shift + Clic dans le vide
        if (player.isSecondaryUseActive()) {
            cycleRotationServer(serverPlayer, stack);
            return InteractionResult.SUCCESS;
        }

        // Sinon, on essaie de placer là où on regarde (même dans l'air)
        // Raycast jusqu'à 128 blocs
        net.minecraft.world.phys.HitResult hit = player.pick(128.0, 0.0f, false);
        BlockPos pos;
        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            net.minecraft.world.phys.BlockHitResult bhr = (net.minecraft.world.phys.BlockHitResult) hit;
            pos = bhr.getBlockPos().relative(bhr.getDirection());
        } else {
            // Dans le vide : place à 5 blocs devant le joueur
            pos = player.blockPosition().relative(player.getDirection(), 5);
        }

        return placeStructureServer(serverPlayer, stack, pos);
    }

    private InteractionResult placeStructureServer(ServerPlayer player, ItemStack stack, BlockPos origin) {
        String structureId = getStructureId(stack);
        if (structureId == null || structureId.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                "§c[MNA] Aucune structure sélectionnée. Utilisez /mna creator structure list")
            );
            return InteractionResult.FAIL;
        }

        int rot = getRotation(stack);
        Rotation mcRotation = toMcRotation(rot);

        boolean placed = StructureSaveManager.placeStructure(
            player.level().getServer(), player.level(),
            structureId, origin, Mirror.NONE, mcRotation);

        if (placed) {
            player.sendSystemMessage(Component.literal(
                "§a[MNA] Structure '§f" + structureId + "§a' placée en §f" + origin.toShortString()
                + " §a(rotation §f" + (rot * 90) + "°§a)"));
        } else {
            player.sendSystemMessage(Component.literal(
                "§c[MNA] Impossible de placer '§f" + structureId + "§c' — fichier introuvable."));
        }

        return InteractionResult.SUCCESS;
    }

    // ── Rotation ─────────────────────────────────────────────────────────────

    private static void cycleRotationServer(ServerPlayer player, ItemStack stack) {
        int current = getRotation(stack);
        int next    = (current + 1) % 4;
        setRotation(stack, next);

        // Sync vers le client pour le renderer
        ServerPlayNetworking.send(player, new StructureRotationPayload(next));

        player.sendSystemMessage(Component.literal(
            "§b[MNA] Rotation §f" + (next * 90) + "°"
                + "§b (Shift+clic pour changer, clic pour placer)"
        ).withStyle(ChatFormatting.AQUA));
    }

    // ── Données de l'item ────────────────────────────────────────────────────

    public static String getStructureId(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        return tag.contains(TAG_STRUCTURE) ? tag.getString(TAG_STRUCTURE).orElse(null) : null;
    }

    public static int getRotation(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return 0;
        CompoundTag tag = data.copyTag();
        return tag.contains(TAG_ROTATION) ? tag.getInt(TAG_ROTATION).orElse(0) : 0;
    }

    public static void setStructureId(ItemStack stack, String id) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, existing -> {
            CompoundTag tag = existing.copyTag();
            tag.putString(TAG_STRUCTURE, id);
            return CustomData.of(tag);
        });
    }

    public static void setRotation(ItemStack stack, int rotation) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, existing -> {
            CompoundTag tag = existing.copyTag();
            tag.putInt(TAG_ROTATION, rotation & 3);
            return CustomData.of(tag);
        });
    }

    // ── Conversion rotation ──────────────────────────────────────────────────

    public static Rotation toMcRotation(int r) {
        return switch (r & 3) {
            case 1  -> Rotation.CLOCKWISE_90;
            case 2  -> Rotation.CLOCKWISE_180;
            case 3  -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }
}
