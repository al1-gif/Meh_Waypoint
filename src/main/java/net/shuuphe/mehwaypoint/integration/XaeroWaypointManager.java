package net.shuuphe.mehwaypoint.integration;

import net.minecraft.util.math.BlockPos;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.mods.SupportMods;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class XaeroWaypointManager {

    private static final int BLUE = 6;
    private static final Map<BlockPos, Waypoint> tracked = new HashMap<>();

    public static void addWaypoint(BlockPos pos, String name) {
        if (!SupportMods.minimap()) return;
        try {
            MinimapWorld world = SupportMods.xaeroMinimap.getWaypointWorld();
            if (world == null) return;

            WaypointSet set = world.getCurrentWaypointSet();
            if (set == null) return;
            Waypoint wp = new Waypoint(
                    pos.getX(), pos.getY(), pos.getZ(),
                    name, "Waypoint", BLUE, 0, false, true
            );
            set.add(wp);
            tracked.put(pos, wp);

            MinimapSession session = (MinimapSession) BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session != null) {
                session.getWorldManagerIO().saveWorld(world);
            }
        } catch (Exception e) {
        }
    }

    public static void removeWaypoint(BlockPos pos) {
        if (!SupportMods.minimap()) return;
        Waypoint wp = tracked.remove(pos);
        if (wp == null) return;
        try {
            MinimapWorld world = SupportMods.xaeroMinimap.getWaypointWorld();
            if (world == null) return;

            Iterator<WaypointSet> sets = world.getIterableWaypointSets().iterator();
            while (sets.hasNext()) {
                sets.next().remove(wp);
            }

            MinimapSession session = (MinimapSession) BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session != null) {
                session.getWorldManagerIO().saveWorld(world);
            }
        } catch (Exception e) {
        }
    }

    public static boolean isMehWaypoint(Object original) {
        return tracked.containsValue(original);
    }
}