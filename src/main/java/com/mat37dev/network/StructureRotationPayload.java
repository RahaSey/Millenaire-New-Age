package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * S→C : synchronise la rotation de la Baguette de Placement vers le client.
 */
public record StructureRotationPayload(int rotation) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<StructureRotationPayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "structure_rotation")
    );

    public static final StreamCodec<FriendlyByteBuf, StructureRotationPayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeVarInt(payload.rotation()),
            buf -> new StructureRotationPayload(buf.readVarInt())
        );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
