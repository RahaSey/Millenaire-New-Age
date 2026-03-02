package com.mat37dev.entity.ai.status;

import net.minecraft.ChatFormatting;

/**
 * Statut courant d'un villageois, affiché sous son nom.
 *
 * <p>Sérialisé en byte dans {@code SynchedEntityData} pour la synchronisation client.</p>
 */
public enum VillagerStatus {
    IDLE("status.millenaire-new-age.idle", ChatFormatting.GRAY),
    GOING_TO_WORK("status.millenaire-new-age.going_to_work", ChatFormatting.BLUE),
    WORKING("status.millenaire-new-age.working", ChatFormatting.BLUE),
    GOING_HOME("status.millenaire-new-age.going_home", ChatFormatting.GRAY),
    SLEEPING("status.millenaire-new-age.sleeping", ChatFormatting.GRAY),
    SOCIALIZING("status.millenaire-new-age.socializing", ChatFormatting.GRAY),
    WANDERING("status.millenaire-new-age.wandering", ChatFormatting.GRAY),
    FLEEING("status.millenaire-new-age.fleeing", ChatFormatting.RED),
    HOUSEWORK("status.millenaire-new-age.housework", ChatFormatting.BLUE);

    private final String translationKey;
    private final ChatFormatting color;

    VillagerStatus(String translationKey, ChatFormatting color) {
        this.translationKey = translationKey;
        this.color = color;
    }

    public String getTranslationKey() { return translationKey; }
    public ChatFormatting getColor() { return color; }

    /** Convertit un byte (index ordinal) en VillagerStatus. */
    public static VillagerStatus fromByte(byte b) {
        VillagerStatus[] values = values();
        return (b >= 0 && b < values.length) ? values[b] : IDLE;
    }

    /** Convertit ce statut en byte pour SynchedEntityData. */
    public byte toByte() {
        return (byte) ordinal();
    }
}
