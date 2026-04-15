package net.shuuphe.mehwaypoint.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.shuuphe.mehwaypoint.MehWaypoint;

public record WaypointUpgradePayload() implements CustomPayload {

    public static final CustomPayload.Id<WaypointUpgradePayload> ID =
            new CustomPayload.Id<>(Identifier.of(MehWaypoint.MOD_ID, "waypoint_upgrade"));

    public static final PacketCodec<RegistryByteBuf, WaypointUpgradePayload> CODEC =
            PacketCodec.unit(new WaypointUpgradePayload());

    @Override
    public CustomPayload.Id<WaypointUpgradePayload> getId() { return ID; }
}