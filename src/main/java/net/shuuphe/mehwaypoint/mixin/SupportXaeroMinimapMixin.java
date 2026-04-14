package net.shuuphe.mehwaypoint.mixin;

import net.shuuphe.mehwaypoint.integration.XaeroWaypointManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.mods.SupportXaeroMinimap;
import xaero.map.mods.gui.Waypoint;

@Mixin(value = SupportXaeroMinimap.class, remap = false)
public class SupportXaeroMinimapMixin {

    @Inject(method = "deleteWaypoint", at = @At("HEAD"), cancellable = true)
    private void onDeleteWaypoint(Waypoint waypoint, CallbackInfo ci) {
        if (XaeroWaypointManager.isMehWaypoint(waypoint.getOriginal())) {
            ci.cancel();
        }
    }
}