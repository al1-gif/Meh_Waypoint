package net.shuuphe.mehwaypoint;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.shuuphe.mehwaypoint.registry.ModItems;

public class ModEvents {

    public static void registerModEvents() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) return;

            LootPool.Builder pool = null;

            if (key.equals(LootTables.BURIED_TREASURE_CHEST)) {
                pool = createRubyPool(3, 4);
            }
            else if (key.equals(LootTables.SHIPWRECK_TREASURE_CHEST)) {
                pool = createRubyPool(5, 6);
            }
            else if (key.equals(LootTables.VILLAGE_TOOLSMITH_CHEST) || key.equals(LootTables.VILLAGE_WEAPONSMITH_CHEST)) {
                pool = createRubyPool(4, 5);
            }
            else if (key.equals(LootTables.ANCIENT_CITY_CHEST)) {
                pool = LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(7, 9))
                        .conditionally(RandomChanceLootCondition.builder(0.4f))
                        .with(ItemEntry.builder(ModItems.RUBY));
            }
            else if (key.equals(LootTables.IGLOO_CHEST_CHEST) || key.equals(LootTables.BASTION_BRIDGE_CHEST) || key.equals(LootTables.BASTION_TREASURE_CHEST)) {
                pool = createRubyPool(2, 3);
            }
            else if (key.equals(LootTables.DESERT_PYRAMID_CHEST)) {
                pool = LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(3, 5))
                        .conditionally(RandomChanceLootCondition.builder(0.6f))
                        .with(ItemEntry.builder(ModItems.RUBY));
            }
            else if (key.equals(LootTables.END_CITY_TREASURE_CHEST)){
                pool = LootPool.builder()
                        .rolls(UniformLootNumberProvider.create(2, 3))
                        .conditionally(RandomChanceLootCondition.builder(0.4f))
                        .with(ItemEntry.builder(ModItems.RUBY));
            }

            if (pool != null) {
                tableBuilder.pool(pool);
            }
        });
    }
    private static LootPool.Builder createRubyPool(float min, float max) {
        return LootPool.builder()
                .rolls(UniformLootNumberProvider.create(min, max))
                .with(ItemEntry.builder(ModItems.RUBY));
    }
}
