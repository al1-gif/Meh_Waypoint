package net.shuuphe.mehwaypoint.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class ModPackets {
    public static void registerS2C() {
        PayloadTypeRegistry.playS2C().register(WaypointAddPayload.ID,    WaypointAddPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(WaypointRemovePayload.ID, WaypointRemovePayload.CODEC);
    }
}