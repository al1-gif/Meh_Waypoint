package net.shuuphe.mehwaypoint.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockPos;
import net.shuuphe.mehwaypoint.integration.XaeroWaypointManager;
import net.shuuphe.mehwaypoint.network.TeleportRequestPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;
import xaero.map.mods.SupportMods;
import xaero.map.mods.gui.Waypoint;
import xaero.map.mods.gui.WaypointReader;

import java.lang.reflect.Method;
import java.util.ArrayList;

@Mixin(value = WaypointReader.class, remap = false)
public class WaypointReaderMixin {

    @Inject(method = "getRightClickOptions", at = @At("HEAD"), cancellable = true)
    private void onGetRightClickOptions(Waypoint element, IRightClickableElement target,
                                        CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        if (!XaeroWaypointManager.isMehWaypoint(element.getOriginal())) return;
        BlockPos pos = XaeroWaypointManager.getPosFor(element.getOriginal());
        if (pos == null) return;
        String dimension = XaeroWaypointManager.getDimensionFor(pos);
        ArrayList<RightClickOption> options = new ArrayList<>();
        options.add(new RightClickOption(element.getName(), options.size(), target) {
            @Override public void onAction(Screen screen) {}
        });

        options.add(new RightClickOption("Confirm Teleport", options.size(), target) {
            @Override
            public void onAction(Screen screen) {
                ClientPlayNetworking.send(new TeleportRequestPayload(pos, dimension));
                screen.close();
            }
        });
        options.add(new RightClickOption("Rename Waypoint", options.size(), target) {
            @Override
            public void onAction(Screen screen) {
                if (!SupportMods.minimap()) return;
                try {
                    Class<?> guiMapClass = Class.forName("xaero.map.gui.GuiMap");
                    if (!guiMapClass.isInstance(screen)) return;
                    Method openWaypoint = SupportMods.xaeroMinimap.getClass()
                            .getMethod("openWaypoint", guiMapClass, Waypoint.class);
                    openWaypoint.invoke(SupportMods.xaeroMinimap, screen, element);
                } catch (Exception ignored) {}
            }
        });

        options.add(new RightClickOption("Cancel", options.size(), target) {
            @Override public void onAction(Screen screen) { screen.close(); }
        });

        cir.setReturnValue(options);
    }
}