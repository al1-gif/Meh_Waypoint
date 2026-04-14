package net.shuuphe.mehwaypoint.mixin;

import net.shuuphe.mehwaypoint.integration.XaeroWaypointManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.set.WaypointSet;

@Mixin(value = WaypointSet.class, remap = false)
public class WaypointSetMixin {

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void onRemove(Waypoint waypoint, CallbackInfo ci) {
        if (XaeroWaypointManager.isMehWaypoint(waypoint) && !XaeroWaypointManager.allowRemoval) {
            ci.cancel();
        }
    }
}