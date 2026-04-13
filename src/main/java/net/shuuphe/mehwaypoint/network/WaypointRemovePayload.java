package net.shuuphe.mehwaypoint.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.shuuphe.mehwaypoint.MehWaypoint;

public record WaypointRemovePayload(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<WaypointRemovePayload> ID =
            new CustomPayload.Id<>(Identifier.of(MehWaypoint.MOD_ID, "waypoint_remove"));

    public static final PacketCodec<RegistryByteBuf, WaypointRemovePayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, WaypointRemovePayload::pos,
                    WaypointRemovePayload::new
            );

    @Override
    public CustomPayload.Id<WaypointRemovePayload> getId() { return ID; }
}