package net.shuuphe.mehwaypoint.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.shuuphe.mehwaypoint.MehWaypoint;

public class ModItems {
    private static RegistryKey<Item> key(String path) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MehWaypoint.MOD_ID, path));
    }

    public static final Item RUBY = new Item(
            new Item.Settings().registryKey(key("ruby")));

    public static void registerModItems() {
        Registry.register(Registries.ITEM, Identifier.of(MehWaypoint.MOD_ID, "ruby"), RUBY);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(RUBY);
        });
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(ModBlocks.WAYPOINT_BLOCK);
        });
    }
}
