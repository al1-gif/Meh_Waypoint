package net.shuuphe.mehwaypoint.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.shuuphe.mehwaypoint.MehWaypoint;

import java.util.List;

public record WaypointSyncPayload(List<Long> positions) implements CustomPayload {
    public static final CustomPayload.Id<WaypointSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MehWaypoint.MOD_ID, "waypoint_sync"));

    public static final PacketCodec<RegistryByteBuf, WaypointSyncPayload> CODEC =
            PacketCodecs.VAR_LONG.collect(PacketCodecs.toList())
                    .xmap(WaypointSyncPayload::new, WaypointSyncPayload::positions)
                    .cast();

    @Override
    public CustomPayload.Id<WaypointSyncPayload> getId() { return ID; }
}