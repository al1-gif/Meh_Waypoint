package net.shuuphe.mehwaypoint.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.shuuphe.mehwaypoint.MehWaypoint;
import net.shuuphe.mehwaypoint.screen.WaypointBlockScreenHandler;

public class ModScreenHandlers {

    public static ScreenHandlerType<WaypointBlockScreenHandler> WAYPOINT_BLOCK;

    public static void register() {
        WAYPOINT_BLOCK = Registry.register(
                Registries.SCREEN_HANDLER,
                Identifier.of(MehWaypoint.MOD_ID, "waypoint_block"),
                new ExtendedScreenHandlerType<>(
                        (syncId, inv, pos) -> new WaypointBlockScreenHandler(syncId, inv, pos),
                        BlockPos.PACKET_CODEC
                )
        );
    }
}