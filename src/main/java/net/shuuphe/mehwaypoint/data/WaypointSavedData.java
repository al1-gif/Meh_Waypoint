package net.shuuphe.mehwaypoint.data;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WaypointSavedData extends PersistentState {

    private final Set<BlockPos> positions = new HashSet<>();

    private static final Codec<WaypointSavedData> CODEC = Codec.LONG.listOf().xmap(
            longs -> {
                WaypointSavedData data = new WaypointSavedData();
                longs.forEach(l -> data.positions.add(BlockPos.fromLong(l)));
                return data;
            },
            data -> data.positions.stream().map(BlockPos::asLong).toList()
    );

    public static final PersistentStateType<WaypointSavedData> TYPE = new PersistentStateType<>(
            "mehwaypoint",
            WaypointSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_MAP_DATA
    );

    public static WaypointSavedData getOrCreate(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }

    public void addPosition(BlockPos pos) {
        positions.add(pos.toImmutable());
        markDirty();
    }

    public void removePosition(BlockPos pos) {
        positions.remove(pos);
        markDirty();
    }

    public Set<BlockPos> getPositions() {
        return Collections.unmodifiableSet(positions);
    }
}