package net.shuuphe.mehwaypoint.registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.shuuphe.mehwaypoint.MehWaypoint;
import net.shuuphe.mehwaypoint.block.WaypointBlock;

public class ModBlocks {

    public static final Block WAYPOINT_BLOCK = register("waypoint_block");

    private static Block register(String name) {
        Identifier id = Identifier.of(MehWaypoint.MOD_ID, name);
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);

        Block block = new WaypointBlock(
                AbstractBlock.Settings.create()
                        .registryKey(key)
                        .strength(1.5f)
                        .nonOpaque()
        );

        Registry.register(Registries.BLOCK, key, block);
        Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id))));
        return block;
    }

    public static void register() {
        MehWaypoint.LOGGER.info("[MehWaypoint] Registering blocks...");
    }
}