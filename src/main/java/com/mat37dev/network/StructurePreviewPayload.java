package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * S→C : envoie les positions relatives des blocs non-air d'une structure
 * pour le rendu ghost côté client.
 */
public record StructurePreviewPayload(
        String structureId,
        List<BlockPos> relativeBlocks,
        Vec3i structureSize
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StructurePreviewPayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "structure_preview")
    );

    public static final StreamCodec<FriendlyByteBuf, StructurePreviewPayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.structureId());
                buf.writeVarInt(payload.relativeBlocks().size());
                for (BlockPos pos : payload.relativeBlocks()) {
                    buf.writeBlockPos(pos);
                }
                buf.writeVarInt(payload.structureSize().getX());
                buf.writeVarInt(payload.structureSize().getY());
                buf.writeVarInt(payload.structureSize().getZ());
            },
            buf -> {
                String id = buf.readUtf();
                int count = buf.readVarInt();
                List<BlockPos> blocks = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    blocks.add(buf.readBlockPos());
                }
                Vec3i size = new Vec3i(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
                return new StructurePreviewPayload(id, blocks, size);
            }
        );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
