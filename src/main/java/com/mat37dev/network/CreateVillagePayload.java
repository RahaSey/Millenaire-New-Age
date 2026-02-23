package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C→S : le joueur confirme la création d'un village.
 * Contient l'ID de civilisation, l'ID du type de village et la position du bloc d'or.
 */
public record CreateVillagePayload(
        String civId,
        String villageTypeId,
        BlockPos goldPos
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<CreateVillagePayload> ID =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "create_village")
        );

    public static final StreamCodec<FriendlyByteBuf, CreateVillagePayload> CODEC =
        StreamCodec.of(
            (buf, p) -> {
                buf.writeUtf(p.civId());
                buf.writeUtf(p.villageTypeId());
                buf.writeBlockPos(p.goldPos());
            },
            buf -> new CreateVillagePayload(buf.readUtf(), buf.readUtf(), buf.readBlockPos())
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return ID; }
}
