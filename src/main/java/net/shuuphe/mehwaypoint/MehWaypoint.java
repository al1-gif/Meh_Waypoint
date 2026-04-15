package net.shuuphe.mehwaypoint;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
import net.shuuphe.mehwaypoint.registry.ModScreenHandlers;
import net.shuuphe.mehwaypoint.screen.WaypointBlockScreenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MehWaypoint implements ModInitializer {

	public static final String MOD_ID = "mehwaypoint";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[MehWaypoint] Initialising...");
		ModBlocks.register();
		ModBlockEntities.register();
		ModScreenHandlers.register();
		ModPackets.registerS2C();
		ModPackets.registerC2S();
		ModItems.registerModItems();
		ModEvents.registerModEvents();

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

		ServerPlayNetworking.registerGlobalReceiver(WaypointUpgradePayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				if (!(player.currentScreenHandler instanceof WaypointBlockScreenHandler handler)) return;

				BlockPos pos = handler.getBlockPos();
				ServerWorld world = player.getEntityWorld();
				if (!(world.getBlockEntity(pos) instanceof WaypointBlockEntity be)) return;

				if (handler.canUpgrade()) {
					int level = be.getLevel();
					if (level < 6) {
						int cost = WaypointBlockScreenHandler.getRequiredRubyCount(level);
						handler.getIngredientInv().getStack(0).decrement(cost);
						be.setLevel(level + 1);
						LOGGER.info("[MehWaypoint] {} upgraded waypoint at {} to level {}",
								player.getName().getString(), pos, level + 1);
					}
				} else if (handler.canActivate()) {
					Item ingredient = handler.getIngredientInv().getStack(0).getItem();
					handler.getIngredientInv().getStack(0).decrement(1);
					be.setEffectsActive(true);
					LOGGER.info("[MehWaypoint] {} activated waypoint at {} using {}",
							player.getName().getString(), pos,
							ingredient == Items.IRON_INGOT ? "Iron Ingot"
							: ingredient == Items.GOLD_INGOT ? "Gold Ingot"
							: "Emerald");
				}

				player.closeHandledScreen();
			});
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			ServerWorld world = player.getEntityWorld();
			WaypointSavedData data = WaypointSavedData.getOrCreate(server);

			for (BlockPos pos : data.getPositions()) {
				String name = "Waypoint";
				if (world.getBlockEntity(pos) instanceof WaypointBlockEntity be)
					name = be.getName();
				ServerPlayNetworking.send(player, new WaypointAddPayload(pos, name));
			}

			List<Long> validLongs = data.getPositions().stream()
					.map(BlockPos::asLong)
					.toList();
			ServerPlayNetworking.send(player, new WaypointSyncPayload(validLongs));
		});
	}
}
