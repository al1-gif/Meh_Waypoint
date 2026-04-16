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
    static final String OUR_SYMBOL = "W";

    private static final Map<BlockPos, Waypoint> tracked = new HashMap<>();
    private static final Set<BlockPos> trackedPositions = new HashSet<>();
    private static final Map<BlockPos, String> waypointNames = new HashMap<>();
    private static final Map<BlockPos, String> waypointDimensions = new HashMap<>();

    public static boolean allowRemoval = false;
    private static final Map<BlockPos, String> pendingAdds = new LinkedHashMap<>();
    private static final Map<BlockPos, String> pendingAddDims = new LinkedHashMap<>();
    private static final Set<BlockPos> pendingRemoves = new HashSet<>();
    private static Set<BlockPos> pendingSyncValid = null;
    private static boolean needsRefresh = false;

    public static void tick() {
        if (!SupportMods.minimap()) return;

        MinimapWorld world;
        MinimapSession session;
        try {
            world = SupportMods.xaeroMinimap.getWaypointWorld();
            session = (MinimapSession) BuiltInHudModules.MINIMAP.getCurrentSession();
        } catch (Exception e) { return; }

        if (world == null || session == null) return;

        if (!pendingAdds.isEmpty()) {
            Map<BlockPos, String> copy = new LinkedHashMap<>(pendingAdds);
            Map<BlockPos, String> dimCopy = new LinkedHashMap<>(pendingAddDims);
            pendingAdds.clear();
            pendingAddDims.clear();
            for (Map.Entry<BlockPos, String> e : copy.entrySet()) {
                addWaypoint(e.getKey(), e.getValue(), dimCopy.get(e.getKey()));
            }
        }

        if (!pendingRemoves.isEmpty()) {
            Set<BlockPos> copy = new HashSet<>(pendingRemoves);
            pendingRemoves.clear();
            for (BlockPos pos : copy) {
                removeWaypoint(pos);
            }
        }

        if (pendingSyncValid != null) {
            Set<BlockPos> validSet = pendingSyncValid;
            pendingSyncValid = null;
            cleanupOrphans(world, validSet);
        }

        ensureAllWaypointsPresent(world);

        if (needsRefresh) {
            needsRefresh = false;
            try {
                session.getWorldManagerIO().saveWorld(world);
                SupportMods.xaeroMinimap.requestWaypointsRefresh();
            } catch (Exception e) {
                needsRefresh = true;
            }
        }
    }

    private static void ensureAllWaypointsPresent(MinimapWorld world) {
        if (trackedPositions.isEmpty()) return;
        try {
            Set<BlockPos> presentInWorld = new HashSet<>();
            for (WaypointSet set : world.getIterableWaypointSets()) {
                for (Waypoint w : set.getWaypoints()) {
                    if (OUR_SYMBOL.equals(w.getSymbol())) {
                        presentInWorld.add(new BlockPos(w.getX(), w.getY(), w.getZ()));
                    }
                }
            }
            for (BlockPos pos : new HashSet<>(trackedPositions)) {
                if (presentInWorld.contains(pos)) continue;
                WaypointSet set = world.getCurrentWaypointSet();
                if (set == null) continue;
                String name = waypointNames.getOrDefault(pos, "Waypoint");
                Waypoint wp = new Waypoint(
                        pos.getX(), pos.getY(), pos.getZ(),
                        name, OUR_SYMBOL, BLUE, 0, false, true
                );
                set.add(wp);
                tracked.put(pos.toImmutable(), wp);
                needsRefresh = true;
            }
        } catch (Exception ignored) {}
    }

    public static void queueAdd(BlockPos pos, String name, String dimension) {
        pendingRemoves.remove(pos);
        BlockPos immutable = pos.toImmutable();
        pendingAdds.put(immutable, name);
        pendingAddDims.put(immutable, dimension);
        waypointNames.put(immutable, name);
        waypointDimensions.put(immutable, dimension);
        trackedPositions.add(immutable);
    }

    public static void queueRemove(BlockPos pos) {
        BlockPos immutable = pos.toImmutable();
        pendingAdds.remove(immutable);
        pendingAddDims.remove(immutable);
        pendingRemoves.add(immutable);
        waypointNames.remove(immutable);
        waypointDimensions.remove(immutable);
        trackedPositions.remove(immutable);
    }

    public static void queueSync(List<Long> validLongs) {
        Set<BlockPos> validSet = new HashSet<>();
        for (long l : validLongs) validSet.add(BlockPos.fromLong(l));
        pendingSyncValid = validSet;
    }

    public static void clearState() {
        tracked.clear();
        trackedPositions.clear();
        waypointNames.clear();
        waypointDimensions.clear();
        pendingAdds.clear();
        pendingAddDims.clear();
        pendingRemoves.clear();
        pendingSyncValid = null;
        needsRefresh = false;
    }

    public static String getDimensionFor(BlockPos pos) {
        return waypointDimensions.getOrDefault(pos.toImmutable(), "minecraft:overworld");
    }

    private static void cleanupOrphans(MinimapWorld world, Set<BlockPos> validSet) {
        if (!SupportMods.minimap()) return;
        try {
            allowRemoval = true;
            for (WaypointSet set : world.getIterableWaypointSets()) {
                List<Waypoint> toRemove = new ArrayList<>();
                for (Waypoint candidate : set.getWaypoints()) {
                    if (!OUR_SYMBOL.equals(candidate.getSymbol())) continue;
                    BlockPos p = new BlockPos(candidate.getX(), candidate.getY(), candidate.getZ());
                    if (!validSet.contains(p)) {
                        toRemove.add(candidate);
                        tracked.remove(p);
                        trackedPositions.remove(p);
                        waypointNames.remove(p);
                        waypointDimensions.remove(p);
                    }
                }
                for (Waypoint w : toRemove) set.remove(w);
            }
            allowRemoval = false;
            needsRefresh = true;
        } catch (Exception e) {
            allowRemoval = false;
        }
    }

    public static void addWaypoint(BlockPos pos, String name, String dimension) {
        if (!SupportMods.minimap()) return;
        BlockPos immutable = pos.toImmutable();
        waypointNames.put(immutable, name);
        if (dimension != null) waypointDimensions.put(immutable, dimension);

        try {
            MinimapWorld world = SupportMods.xaeroMinimap.getWaypointWorld();
            if (world == null) {
                pendingAdds.put(immutable, name);
                if (dimension != null) pendingAddDims.put(immutable, dimension);
                return;
            }
            if (tracked.containsKey(pos)) return;

            for (WaypointSet set : world.getIterableWaypointSets()) {
                for (Waypoint candidate : set.getWaypoints()) {
                    if (candidate.getX() == pos.getX()
                            && candidate.getY() == pos.getY()
                            && candidate.getZ() == pos.getZ()
                            && OUR_SYMBOL.equals(candidate.getSymbol())) {
                        tracked.put(immutable, candidate);
                        trackedPositions.add(immutable);
                        needsRefresh = true;
                        return;
                    }
                }
            }

            WaypointSet set = world.getCurrentWaypointSet();
            if (set == null) return;

            Waypoint wp = new Waypoint(
                    pos.getX(), pos.getY(), pos.getZ(),
                    name, OUR_SYMBOL, BLUE, 0, false, true
            );
            set.add(wp);
            tracked.put(immutable, wp);
            trackedPositions.add(immutable);
            needsRefresh = true;
        } catch (Exception e) {}
    }

    public static void removeWaypoint(BlockPos pos) {
        if (!SupportMods.minimap()) return;
        try {
            MinimapWorld world = SupportMods.xaeroMinimap.getWaypointWorld();
            if (world == null) {
                pendingRemoves.add(pos.toImmutable());
                return;
            }

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
                                && candidate.getZ() == pos.getZ()
                                && OUR_SYMBOL.equals(candidate.getSymbol())) {
                            toRemove.add(candidate);
                        }
                    }
                    for (Waypoint w : toRemove) set.remove(w);
                }
            }
            allowRemoval = false;
            needsRefresh = true;
        } catch (Exception e) {
            allowRemoval = false;
        }
    }

    public static BlockPos getPosFor(Object original) {
        for (Map.Entry<BlockPos, Waypoint> entry : tracked.entrySet()) {
            if (entry.getValue() == original) return entry.getKey();
        }
        if (original instanceof Waypoint wp) {
            BlockPos p = new BlockPos(wp.getX(), wp.getY(), wp.getZ());
            if (trackedPositions.contains(p)) return p;
        }
        return null;
    }

    public static boolean isMehWaypoint(Object original) {
        if (tracked.containsValue(original)) return true;
        if (original instanceof Waypoint wp) {
            if (!OUR_SYMBOL.equals(wp.getSymbol())) return false;
            return trackedPositions.contains(new BlockPos(wp.getX(), wp.getY(), wp.getZ()));
        }
        return false;
    }
}