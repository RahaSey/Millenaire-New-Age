package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DeleteStructurePayload(String structureId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<DeleteStructurePayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "delete_structure")
    );

    public static final StreamCodec<FriendlyByteBuf, DeleteStructurePayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.structureId()),
            buf -> new DeleteStructurePayload(buf.readUtf())
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
