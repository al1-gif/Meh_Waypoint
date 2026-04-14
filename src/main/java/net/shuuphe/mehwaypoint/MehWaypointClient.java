package net.shuuphe.mehwaypoint;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.shuuphe.mehwaypoint.client.renderer.WaypointBlockEntityRenderer;
import net.shuuphe.mehwaypoint.integration.XaeroWaypointManager;
import net.shuuphe.mehwaypoint.network.WaypointAddPayload;
import net.shuuphe.mehwaypoint.network.WaypointRemovePayload;
import net.shuuphe.mehwaypoint.registry.ModBlockEntities;
import net.shuuphe.mehwaypoint.registry.ModBlocks;

public class MehWaypointClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(ModBlocks.WAYPOINT_BLOCK, BlockRenderLayer.CUTOUT);
        BlockEntityRendererFactories.register(ModBlockEntities.WAYPOINT_BLOCK_ENTITY, WaypointBlockEntityRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(WaypointAddPayload.ID, (payload, context) ->
                context.client().execute(() -> XaeroWaypointManager.addWaypoint(payload.pos(), payload.name()))
        );

        ClientPlayNetworking.registerGlobalReceiver(WaypointRemovePayload.ID, (payload, context) ->
                context.client().execute(() -> XaeroWaypointManager.removeWaypoint(payload.pos()))
        );
    }
}