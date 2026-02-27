package com.mat37dev.network;

import com.mat37dev.MillenaireNewAge;
import com.mat37dev.culture.Culture;
import com.mat37dev.culture.VillageType;
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
 * Contient la position du bloc d'or + la liste des cultures disponibles.
 */
public record OpenVillageCreationPayload(
        BlockPos goldPos,
        List<CultureInfo> cultures
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

    /** Crée le payload depuis une liste de cultures full. */
    public static OpenVillageCreationPayload from(BlockPos goldPos, List<Culture> cultures) {
        List<CultureInfo> infos = new ArrayList<>(cultures.size());
        for (Culture culture : cultures) {
            List<VillageTypeInfo> vtInfos = new ArrayList<>();
            for (VillageType vt : culture.villageTypes()) {
                int minB = vt.requiredBuildingIds().size() + vt.minStarterBuildings();
                int maxB = vt.requiredBuildingIds().size() + vt.maxStarterBuildings();
                vtInfos.add(new VillageTypeInfo(vt.id(), vt.displayName(), minB, maxB, vt.hasWalls()));
            }
            infos.add(new CultureInfo(culture.id(), culture.displayName(), vtInfos));
        }
        return new OpenVillageCreationPayload(goldPos, infos);
    }

    // ── Sérialisation ─────────────────────────────────────────────────────────

    private static void encode(FriendlyByteBuf buf, OpenVillageCreationPayload p) {
        buf.writeBlockPos(p.goldPos());
        buf.writeVarInt(p.cultures().size());
        for (CultureInfo ci : p.cultures()) {
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
        int cultureCount = buf.readVarInt();
        List<CultureInfo> cultures = new ArrayList<>(cultureCount);
        for (int i = 0; i < cultureCount; i++) {
            String cultureId   = buf.readUtf();
            String cultureName = buf.readUtf();
            int vtCount        = buf.readVarInt();
            List<VillageTypeInfo> vtInfos = new ArrayList<>(vtCount);
            for (int j = 0; j < vtCount; j++) {
                vtInfos.add(new VillageTypeInfo(
                    buf.readUtf(), buf.readUtf(),
                    buf.readVarInt(), buf.readVarInt(),
                    buf.readBoolean()
                ));
            }
            cultures.add(new CultureInfo(cultureId, cultureName, vtInfos));
        }
        return new OpenVillageCreationPayload(goldPos, cultures);
    }

    // ── Types de données ──────────────────────────────────────────────────────

    public record CultureInfo(String id, String displayName, List<VillageTypeInfo> villageTypes) {}

    public record VillageTypeInfo(
            String id, String displayName,
            int minBuildings, int maxBuildings,
            boolean hasWalls) {}
}
