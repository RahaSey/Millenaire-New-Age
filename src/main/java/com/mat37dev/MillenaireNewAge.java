package com.mat37dev;

import com.mat37dev.init.MillBlocks;
import com.mat37dev.init.MillItemGroups;
import com.mat37dev.init.MillItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MillenaireNewAge implements ModInitializer {

    public static final String MOD_ID = "millenaire-new-age";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Order matters: blocks before items, items before groups.
        MillBlocks.initialize();
        MillItems.initialize();
        MillItemGroups.initialize();

        LOGGER.info("Millenaire: New Age initialized.");
    }
}