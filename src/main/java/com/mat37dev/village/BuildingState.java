package com.mat37dev.village;

public enum BuildingState {
    PLANNED,
    UNDER_CONSTRUCTION,
    INTACT,
    DAMAGED,
    RUINED,
    DESTROYED;

    public boolean isStanding() {
        return this == INTACT || this == DAMAGED || this == RUINED;
    }

    public boolean isDamaged() {
        return this == DAMAGED || this == RUINED || this == DESTROYED;
    }

    public static BuildingState fromHealth(int currentHealth, int maxHealth) {
        if (currentHealth <= 0) return DESTROYED;
        float ratio = (float) currentHealth / maxHealth;
        if (ratio >= 1.0f) return INTACT;
        if (ratio >= 0.5f) return DAMAGED;
        return RUINED;
    }
}
