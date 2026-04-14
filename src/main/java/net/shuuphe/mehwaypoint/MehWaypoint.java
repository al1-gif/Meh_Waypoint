package net.shuuphe.mehwaypoint;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.shuuphe.mehwaypoint.data.WaypointSavedData;
import net.shuuphe.mehwaypoint.entity.WaypointBlockEntity;
import net.shuuphe.mehwaypoint.network.*;
import net.shuuphe.mehwaypoint.registry.ModBlockEntities;
import net.shuuphe.mehwaypoint.registry.ModBlocks;
import net.shuuphe.mehwaypoint.registry.ModItems;
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
		ModPackets.registerC2S();
		ModItems.registerModItems();
		ServerPlayNetworking.registerGlobalReceiver(TeleportRequestPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			BlockPos pos = payload.pos();
			ServerWorld world = player.getEntityWorld();

			if (!(world.getBlockEntity(pos) instanceof WaypointBlockEntity)) return;

			context.server().execute(() ->
					player.teleportTo(new TeleportTarget(
							world,
							new Vec3d(pos.getX() + 1.5, pos.getY(), pos.getZ() + 0.5),
							Vec3d.ZERO,
							player.getYaw(),
							player.getPitch(),
							TeleportTarget.NO_OP
					))
			);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			ServerWorld world = player.getEntityWorld();
			WaypointSavedData data = WaypointSavedData.getOrCreate(server);
			for (BlockPos pos : data.getPositions()) {
				String name = "Waypoint";
				if (world.getBlockEntity(pos) instanceof WaypointBlockEntity be) {
					name = be.getName();
				}
				ServerPlayNetworking.send(player, new WaypointAddPayload(pos, name));
			}
		});
	}
}