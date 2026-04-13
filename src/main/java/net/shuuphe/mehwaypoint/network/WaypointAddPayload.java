package net.shuuphe.mehwaypoint.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.shuuphe.mehwaypoint.MehWaypoint;

public record WaypointAddPayload(BlockPos pos, String name) implements CustomPayload {
    public static final CustomPayload.Id<WaypointAddPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MehWaypoint.MOD_ID, "waypoint_add"));

    public static final PacketCodec<RegistryByteBuf, WaypointAddPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, WaypointAddPayload::pos,
                    PacketCodecs.STRING,   WaypointAddPayload::name,
                    WaypointAddPayload::new
            );

    @Override
    public CustomPayload.Id<WaypointAddPayload> getId() { return ID; }
}