package net.shuuphe.mehwaypoint;

import net.fabricmc.api.ModInitializer;
import net.shuuphe.mehwaypoint.network.ModPackets;
import net.shuuphe.mehwaypoint.registry.ModBlockEntities;
import net.shuuphe.mehwaypoint.registry.ModBlocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MehWaypoint implements ModInitializer {

	public static final String MOD_ID = "mehwaypoint";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[MehWaypoint] Initialising...");
		ModBlocks.register();
		ModBlockEntities.register();
		ModPackets.registerS2C();
	}
}