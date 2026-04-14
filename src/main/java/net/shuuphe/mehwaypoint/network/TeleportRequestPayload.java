package net.shuuphe.mehwaypoint.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.shuuphe.mehwaypoint.MehWaypoint;

public record TeleportRequestPayload(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<TeleportRequestPayload> ID =
            new CustomPayload.Id<>(Identifier.of(MehWaypoint.MOD_ID, "teleport_request"));

    public static final PacketCodec<RegistryByteBuf, TeleportRequestPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, TeleportRequestPayload::pos,
                    TeleportRequestPayload::new
            );

    @Override
    public CustomPayload.Id<TeleportRequestPayload> getId() { return ID; }
}