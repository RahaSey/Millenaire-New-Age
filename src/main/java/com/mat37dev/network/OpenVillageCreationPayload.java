package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.civilization.Civilization;
import com.mat37dev.civilization.VillageType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * S→C : ouvre le GUI de création de village.
 * Contient la position du bloc d'or + la liste des civilisations disponibles.
 */
public record OpenVillageCreationPayload(
        BlockPos goldPos,
        List<CivInfo> civilizations
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<OpenVillageCreationPayload> ID =
        new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(MillenaireNewAge.MOD_ID, "open_village_creation")
        );

    public static final StreamCodec<FriendlyByteBuf, OpenVillageCreationPayload> CODEC =
        StreamCodec.of(
            OpenVillageCreationPayload::encode,
            OpenVillageCreationPayload::decode
        );

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() { return ID; }

    /** Crée le payload depuis une liste de civilisations full. */
    public static OpenVillageCreationPayload from(BlockPos goldPos, List<Civilization> civs) {
        List<CivInfo> infos = new ArrayList<>(civs.size());
        for (Civilization civ : civs) {
            List<VillageTypeInfo> vtInfos = new ArrayList<>();
            for (VillageType vt : civ.villageTypes()) {
                vtInfos.add(new VillageTypeInfo(
                    vt.id(), vt.displayName(),
                    vt.minBuildings(), vt.maxBuildings(), vt.hasWalls()
                ));
            }
            infos.add(new CivInfo(civ.id(), civ.displayName(), vtInfos));
        }
        return new OpenVillageCreationPayload(goldPos, infos);
    }

    // ── Sérialisation ─────────────────────────────────────────────────────────

    private static void encode(FriendlyByteBuf buf, OpenVillageCreationPayload p) {
        buf.writeBlockPos(p.goldPos());
        buf.writeVarInt(p.civilizations().size());
        for (CivInfo ci : p.civilizations()) {
            buf.writeUtf(ci.id());
            buf.writeUtf(ci.displayName());
            buf.writeVarInt(ci.villageTypes().size());
            for (VillageTypeInfo vt : ci.villageTypes()) {
                buf.writeUtf(vt.id());
                buf.writeUtf(vt.displayName());
                buf.writeVarInt(vt.minBuildings());
                buf.writeVarInt(vt.maxBuildings());
                buf.writeBoolean(vt.hasWalls());
            }
        }
    }

    private static OpenVillageCreationPayload decode(FriendlyByteBuf buf) {
        BlockPos goldPos = buf.readBlockPos();
        int civCount = buf.readVarInt();
        List<CivInfo> civs = new ArrayList<>(civCount);
        for (int i = 0; i < civCount; i++) {
            String civId   = buf.readUtf();
            String civName = buf.readUtf();
            int vtCount    = buf.readVarInt();
            List<VillageTypeInfo> vtInfos = new ArrayList<>(vtCount);
            for (int j = 0; j < vtCount; j++) {
                vtInfos.add(new VillageTypeInfo(
                    buf.readUtf(), buf.readUtf(),
                    buf.readVarInt(), buf.readVarInt(),
                    buf.readBoolean()
                ));
            }
            civs.add(new CivInfo(civId, civName, vtInfos));
        }
        return new OpenVillageCreationPayload(goldPos, civs);
    }

    // ── Types de données ──────────────────────────────────────────────────────

    public record CivInfo(String id, String displayName, List<VillageTypeInfo> villageTypes) {}

    public record VillageTypeInfo(
            String id, String displayName,
            int minBuildings, int maxBuildings,
            boolean hasWalls) {}
}
