package net.shuuphe.mehwaypoint.integration;

import net.minecraft.util.math.BlockPos;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import xaero.map.mods.SupportMods;

import java.util.*;

public class XaeroWaypointManager {

    private static final int BLUE = 6;
    private static final Map<BlockPos, Waypoint> tracked = new HashMap<>();
    private static final Set<BlockPos> trackedPositions = new HashSet<>();
    public static boolean allowRemoval = false;

    public static void addWaypoint(BlockPos pos, String name) {
        if (!SupportMods.minimap()) return;
        try {
            MinimapWorld world = SupportMods.xaeroMinimap.getWaypointWorld();
            if (world == null) return;
            if (tracked.containsKey(pos)) return;
            for (WaypointSet set : world.getIterableWaypointSets()) {
                for (Waypoint candidate : set.getWaypoints()) {
                    if (candidate.getX() == pos.getX()
                            && candidate.getY() == pos.getY()
                            && candidate.getZ() == pos.getZ()) {
                        tracked.put(pos.toImmutable(), candidate);
                        trackedPositions.add(pos.toImmutable());
                        SupportMods.xaeroMinimap.requestWaypointsRefresh();
                        return;
                    }
                }
            }
            WaypointSet set = world.getCurrentWaypointSet();
            if (set == null) return;

            Waypoint wp = new Waypoint(
                    pos.getX(), pos.getY(), pos.getZ(),
                    name, "Waypoint", BLUE, 0, false, true
            );
            set.add(wp);
            tracked.put(pos.toImmutable(), wp);
            trackedPositions.add(pos.toImmutable());

            MinimapSession session = (MinimapSession) BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session != null) {
                session.getWorldManagerIO().saveWorld(world);
                SupportMods.xaeroMinimap.requestWaypointsRefresh();
            }
        } catch (Exception e) {}
    }

    public static void removeWaypoint(BlockPos pos) {
        if (!SupportMods.minimap()) return;
        try {
            MinimapWorld world = SupportMods.xaeroMinimap.getWaypointWorld();
            if (world == null) return;

            Waypoint wp = tracked.remove(pos);
            trackedPositions.remove(pos);

            allowRemoval = true;
            for (WaypointSet set : world.getIterableWaypointSets()) {
                if (wp != null) {
                    set.remove(wp);
                } else {
                    List<Waypoint> toRemove = new ArrayList<>();
                    for (Waypoint candidate : set.getWaypoints()) {
                        if (candidate.getX() == pos.getX()
                                && candidate.getY() == pos.getY()
                                && candidate.getZ() == pos.getZ()) {
                            toRemove.add(candidate);
                        }
                    }
                    for (Waypoint w : toRemove) set.remove(w);
                }
            }
            allowRemoval = false;

            MinimapSession session = (MinimapSession) BuiltInHudModules.MINIMAP.getCurrentSession();
            if (session != null) {
                session.getWorldManagerIO().saveWorld(world);
                SupportMods.xaeroMinimap.requestWaypointsRefresh();
            }
        } catch (Exception e) {
            allowRemoval = false;
        }
    }

    public static BlockPos getPosFor(Object original) {
        for (Map.Entry<BlockPos, Waypoint> entry : tracked.entrySet()) {
            if (entry.getValue() == original) return entry.getKey();
        }
        return null;
    }

    public static boolean isMehWaypoint(Object original) {
        if (tracked.containsValue(original)) return true;
        if (original instanceof Waypoint wp) {
            return trackedPositions.contains(new BlockPos(wp.getX(), wp.getY(), wp.getZ()));
        }
        return false;
    }
}