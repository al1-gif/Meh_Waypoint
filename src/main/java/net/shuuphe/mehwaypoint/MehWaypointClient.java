package net.shuuphe.mehwaypoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.shuuphe.mehwaypoint.client.renderer.WaypointBlockEntityRenderer;
import net.shuuphe.mehwaypoint.client.screen.WaypointBlockScreen;
import net.shuuphe.mehwaypoint.integration.XaeroWaypointManager;
import net.shuuphe.mehwaypoint.network.WaypointAddPayload;
import net.shuuphe.mehwaypoint.network.WaypointRemovePayload;
import net.shuuphe.mehwaypoint.network.WaypointSyncPayload;
import net.shuuphe.mehwaypoint.registry.ModBlockEntities;
import net.shuuphe.mehwaypoint.registry.ModBlocks;
import net.shuuphe.mehwaypoint.registry.ModScreenHandlers;

public class MehWaypointClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(ModBlocks.WAYPOINT_BLOCK, BlockRenderLayer.CUTOUT);
        BlockEntityRendererFactories.register(ModBlockEntities.WAYPOINT_BLOCK_ENTITY, WaypointBlockEntityRenderer::new);

        ClientTickEvents.END_CLIENT_TICK.register(client -> XaeroWaypointManager.tick());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                XaeroWaypointManager.clearState()
        );
        ClientPlayNetworking.registerGlobalReceiver(WaypointAddPayload.ID, (payload, context) ->
                context.client().execute(() -> XaeroWaypointManager.queueAdd(payload.pos(), payload.name()))
        );
        ClientPlayNetworking.registerGlobalReceiver(WaypointRemovePayload.ID, (payload, context) ->
                context.client().execute(() -> XaeroWaypointManager.queueRemove(payload.pos()))
        );
        ClientPlayNetworking.registerGlobalReceiver(WaypointSyncPayload.ID, (payload, context) ->
                context.client().execute(() -> XaeroWaypointManager.queueSync(payload.positions()))
        );
        HandledScreens.register(ModScreenHandlers.WAYPOINT_BLOCK, WaypointBlockScreen::new);
    }
}