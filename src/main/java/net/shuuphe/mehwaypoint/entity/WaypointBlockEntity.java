package net.shuuphe.mehwaypoint.entity;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.shuuphe.mehwaypoint.registry.ModBlockEntities;

public class WaypointBlockEntity extends BlockEntity {

    private String name = "Waypoint";

    public WaypointBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WAYPOINT_BLOCK_ENTITY, pos, state);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markDirty();
    }

    @Override
    protected void readData(ReadView view) {
        this.name = view.read("name", Codec.STRING).orElse("Waypoint");
    }

    @Override
    protected void writeData(WriteView view) {
        view.put("name", Codec.STRING, this.name);
    }
}