package net.shuuphe.mehwaypoint.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.shuuphe.mehwaypoint.integration.XaeroWaypointManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointReader;

import java.util.ArrayList;

@Mixin(value = WaypointReader.class, remap = false)
public class WaypointReaderMixin {

    @Inject(method = "getRightClickOptions", at = @At("HEAD"), cancellable = true)
    private void onGetRightClickOptions(Waypoint element, IRightClickableElement target,
                                        CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        if (!XaeroWaypointManager.isMehWaypoint(element.getOriginal())) return;

        ArrayList<RightClickOption> options = new ArrayList<>();
        options.add(new RightClickOption(element.getName(), options.size(), target) {
            @Override
            public void onAction(Screen screen) {}
        });
        options.add(new RightClickOption("Confirm Teleport", options.size(), target) {
            @Override
            public void onAction(Screen screen) {
                // TODO: send teleport packet to server
                screen.close();
            }
        });
        options.add(new RightClickOption("Cancel", options.size(), target) {
            @Override
            public void onAction(Screen screen) {
                screen.close();
            }
        });

        cir.setReturnValue(options);
    }
}