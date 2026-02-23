package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * S→C : ouvre le GUI de liste des structures sur le client.
 * Contient les IDs de toutes les structures disponibles.
 */
public record OpenStructureListPayload(List<String> structureIds) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenStructureListPayload> ID = new CustomPacketPayload.Type<>(
        ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "open_structure_list")
    );

    public static final StreamCodec<FriendlyByteBuf, OpenStructureListPayload> CODEC =
        StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.structureIds().size());
                for (String id : payload.structureIds()) {
                    buf.writeUtf(id);
                }
            },
            buf -> {
                int count = buf.readVarInt();
                List<String> ids = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    ids.add(buf.readUtf());
                }
                return new OpenStructureListPayload(ids);
            }
        );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
