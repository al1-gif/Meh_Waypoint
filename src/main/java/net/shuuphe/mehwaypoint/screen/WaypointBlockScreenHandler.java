package net.shuuphe.mehwaypoint.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.shuuphe.mehwaypoint.registry.ModItems;
import net.shuuphe.mehwaypoint.registry.ModScreenHandlers;

public class WaypointBlockScreenHandler extends ScreenHandler {
    private static final int INVENTORY_START = 1;
    private static final int INVENTORY_END = 28;
    private static final int HOTBAR_START = 28;
    private static final int HOTBAR_END = 37;

    public static final Item[][] UPGRADE_ITEMS = {
            { Items.EMERALD,     Items.REDSTONE,  Items.GOLD_INGOT,  Items.IRON_INGOT  },
            { Items.EMERALD,     Items.REDSTONE,  Items.GOLD_INGOT,  Items.IRON_INGOT  },
            { Items.EMERALD,     Items.EMERALD,   Items.GOLD_INGOT,  Items.DIAMOND     },
            { Items.EMERALD,     Items.REDSTONE,  Items.GOLD_BLOCK,  Items.DIAMOND     },
            { Items.NETHER_STAR, Items.REDSTONE,  Items.GOLD_BLOCK,  Items.IRON_BLOCK  },
    };

    private final SimpleInventory ingredientInv = new SimpleInventory(1);
    private final ScreenHandlerContext context;
    private final PropertyDelegate levelDelegate;
    private final BlockPos blockPos;

    public WaypointBlockScreenHandler(int syncId, PlayerInventory playerInv, BlockPos pos) {
        this(syncId, playerInv, ScreenHandlerContext.EMPTY, pos, new ArrayPropertyDelegate(1));
    }

    public WaypointBlockScreenHandler(int syncId, PlayerInventory playerInv,
                                      ScreenHandlerContext ctx, BlockPos pos, PropertyDelegate delegate) {
        super(ModScreenHandlers.WAYPOINT_BLOCK, syncId);
        this.context = ctx;
        this.blockPos = pos;
        this.levelDelegate = delegate;
        addProperties(levelDelegate);

        this.addSlot(new Slot(ingredientInv, 0, 136, 110));

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 36 + col * 18, 137 + row * 18));

        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 36 + col * 18, 195));
    }

    public SimpleInventory getIngredientInv() { return ingredientInv; }
    public BlockPos getBlockPos() { return blockPos; }
    public int getWaypointLevel() { return levelDelegate.get(0); }

    public boolean canUpgrade() {
        int level = levelDelegate.get(0);
        if (level >= 5) return false;

        ItemStack payment = ingredientInv.getStack(0);
        if (payment.isEmpty()) return false;

        Item item = payment.getItem();
        return item == Items.EMERALD || item == ModItems.RUBY ||
                item == Items.GOLD_INGOT || item == Items.IRON_INGOT;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasStack()) return result;

        ItemStack stack = slot.getStack();
        result = stack.copy();

        if (index < 4) {
            if (!insertItem(stack, 4, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!insertItem(stack, 0, 4, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setStack(ItemStack.EMPTY);
        else slot.markDirty();
        return result;
    }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        context.run((world, pos) -> {
            player.getInventory().offerOrDrop(ingredientInv.removeStack(0));
        });
    }
}