package net.shuuphe.mehwaypoint.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public class WaypointSavedData extends PersistentState {

    private final Map<BlockPos, String> positionDimensions = new HashMap<>();
    private record Entry(long posLong, String dimension) {}
    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.LONG.fieldOf("pos").forGetter(Entry::posLong),
            Codec.STRING.fieldOf("dim").forGetter(Entry::dimension)
    ).apply(inst, Entry::new));

    private static final Codec<WaypointSavedData> CODEC =
            Codec.either(ENTRY_CODEC.listOf(), Codec.LONG.listOf())
                    .xmap(
                            either -> either.map(
                                    entries -> {
                                        WaypointSavedData d = new WaypointSavedData();
                                        entries.forEach(e -> d.positionDimensions.put(
                                                BlockPos.fromLong(e.posLong()), e.dimension()));
                                        return d;
                                    },
                                    longs -> {
                                        WaypointSavedData d = new WaypointSavedData();
                                        longs.forEach(l -> d.positionDimensions.put(
                                                BlockPos.fromLong(l), "minecraft:overworld"));
                                        return d;
                                    }
                            ),
                            data -> Either.left(
                                    data.positionDimensions.entrySet().stream()
                                            .map(e -> new Entry(e.getKey().asLong(), e.getValue()))
                                            .toList()
                            )
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
    public void addPosition(BlockPos pos, String dimension) {
        positionDimensions.put(pos.toImmutable(), dimension);
        markDirty();
    }

    public void removePosition(BlockPos pos) {
        positionDimensions.remove(pos);
        markDirty();
    }
    public Set<BlockPos> getPositions() {
        return Collections.unmodifiableSet(positionDimensions.keySet());
    }
    public String getDimension(BlockPos pos) {
        return positionDimensions.getOrDefault(pos, "minecraft:overworld");
    }
    public Map<BlockPos, String> getPositionDimensions() {
        return Collections.unmodifiableMap(positionDimensions);
    }
}