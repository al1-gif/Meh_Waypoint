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

    private final SimpleInventory ingredientInv = new SimpleInventory(1);
    private final ScreenHandlerContext context;
    private final PropertyDelegate properties;
    private final BlockPos blockPos;
    public WaypointBlockScreenHandler(int syncId, PlayerInventory playerInv, BlockPos pos) {
        this(syncId, playerInv, ScreenHandlerContext.EMPTY, pos, new ArrayPropertyDelegate(2));
    }
    public WaypointBlockScreenHandler(int syncId, PlayerInventory playerInv,
                                      ScreenHandlerContext ctx, BlockPos pos, PropertyDelegate delegate) {
        super(ModScreenHandlers.WAYPOINT_BLOCK, syncId);
        this.context    = ctx;
        this.blockPos   = pos;
        this.properties = delegate;
        addProperties(properties);

        this.addSlot(new Slot(ingredientInv, 0, 136, 110));

        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 36 + col * 18, 137 + row * 18));
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 36 + col * 18, 195));
    }

    public SimpleInventory getIngredientInv() { return ingredientInv; }
    public BlockPos getBlockPos() { return blockPos; }
    public int getWaypointLevel() { return properties.get(0); }
    public boolean isEffectsActive() { return properties.get(1) != 0; }

    public static int getRequiredRubyCount(int currentLevel) {
        return switch (currentLevel) {
            case 1 -> 8;
            case 2 -> 16;
            case 3 -> 20;
            case 4 -> 26;
            case 5 -> 34;
            default -> Integer.MAX_VALUE;
        };
    }
    public boolean canUpgrade() {
        int level = properties.get(0);
        if (level >= 6) return false;
        ItemStack stack = ingredientInv.getStack(0);
        return !stack.isEmpty()
                && stack.getItem() == ModItems.RUBY
                && stack.getCount() >= getRequiredRubyCount(level);
    }
    public boolean canActivate() {
        ItemStack stack = ingredientInv.getStack(0);
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == Items.IRON_INGOT
                || item == Items.GOLD_INGOT
                || item == Items.EMERALD;
    }

    public boolean canConfirm() {
        return canUpgrade() || canActivate();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasStack()) return result;

        ItemStack stack = slot.getStack();
        result = stack.copy();

        if (index == 0) {
            if (!insertItem(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!insertItem(stack, 0, 1, false)) return ItemStack.EMPTY;
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
        context.run((world, pos) ->
                player.getInventory().offerOrDrop(ingredientInv.removeStack(0)));
    }
}
