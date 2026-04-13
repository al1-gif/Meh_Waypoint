package net.shuuphe.mehwaypoint.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.shuuphe.mehwaypoint.MehWaypoint;
import net.shuuphe.mehwaypoint.block.entity.WaypointBlockEntity;

public class ModBlockEntities {

    public static final BlockEntityType<WaypointBlockEntity> WAYPOINT_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(MehWaypoint.MOD_ID, "waypoint_block_entity"),
                    FabricBlockEntityTypeBuilder.create(
                            WaypointBlockEntity::new, ModBlocks.WAYPOINT_BLOCK
                    ).build()
            );

    public static void register() {
        MehWaypoint.LOGGER.info("[MehWaypoint] Registering block entities...");
    }
}