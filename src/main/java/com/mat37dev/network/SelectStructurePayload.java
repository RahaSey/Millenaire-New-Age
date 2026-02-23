package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C→S : le joueur a cliqué "Placer" dans la GUI de liste — lui donne
 * la Baguette de Placement configurée avec la structure choisie.
 */
public record SelectStructurePayload(String structureId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SelectStructurePayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "select_structure")
    );

    public static final StreamCodec<FriendlyByteBuf, SelectStructurePayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.structureId()),
            buf -> new SelectStructurePayload(buf.readUtf())
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
