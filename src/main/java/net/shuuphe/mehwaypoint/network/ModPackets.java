package net.shuuphe.mehwaypoint.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ModPackets {
    public static void registerS2C() {
        PayloadTypeRegistry.playS2C().register(WaypointAddPayload.ID,    WaypointAddPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WaypointRemovePayload.ID, WaypointRemovePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WaypointSyncPayload.ID,   WaypointSyncPayload.CODEC);
    }

    public static void registerC2S() {
        PayloadTypeRegistry.playC2S().register(TeleportRequestPayload.ID, TeleportRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(WaypointUpgradePayload.ID, WaypointUpgradePayload.CODEC);
    }
}