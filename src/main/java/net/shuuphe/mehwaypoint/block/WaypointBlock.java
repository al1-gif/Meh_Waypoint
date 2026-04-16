package net.shuuphe.mehwaypoint.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shuuphe.mehwaypoint.data.WaypointSavedData;
import net.shuuphe.mehwaypoint.entity.WaypointBlockEntity;
import net.shuuphe.mehwaypoint.network.WaypointAddPayload;
import net.shuuphe.mehwaypoint.network.WaypointRemovePayload;
import net.shuuphe.mehwaypoint.registry.ModBlockEntities;
import net.shuuphe.mehwaypoint.screen.WaypointBlockScreenHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public class WaypointBlock extends BlockWithEntity {

    public static final MapCodec<WaypointBlock> CODEC = createCodec(WaypointBlock::new);

    public WaypointBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new WaypointBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.WAYPOINT_BLOCK_ENTITY, WaypointBlockEntity::tick);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                         @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        NbtComponent customData = itemStack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null && world.getBlockEntity(pos) instanceof WaypointBlockEntity be) {
            NbtCompound nbt = customData.copyNbt();
            if (nbt.contains("level")) {
                be.setLevel(nbt.getInt("level").get());
            }
        }

        if (world instanceof ServerWorld serverWorld) {
            String name = (world.getBlockEntity(pos) instanceof WaypointBlockEntity be)
                    ? be.getName() : "Waypoint";
            String dim = serverWorld.getRegistryKey().getValue().toString();
            WaypointSavedData.getOrCreate(serverWorld.getServer()).addPosition(pos, dim);
            WaypointAddPayload payload = new WaypointAddPayload(pos, name, dim);
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state,
                           @Nullable BlockEntity blockEntity, ItemStack tool) {
        player.incrementStat(net.minecraft.stat.Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005F);

        if (!player.isCreative()) {
            ItemStack drop = new ItemStack(this.asItem());
            if (blockEntity instanceof WaypointBlockEntity be && be.getLevel() > 1) {
                NbtCompound nbt = new NbtCompound();
                nbt.putInt("level", be.getLevel());
                drop.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
            }
            dropStack(world, pos, drop);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 net.minecraft.entity.player.PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            if (!(world.getBlockEntity(pos) instanceof WaypointBlockEntity be)) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ArrayPropertyDelegate delegate = new ArrayPropertyDelegate(2);
            delegate.set(0, be.getLevel());
            delegate.set(1, be.isEffectsActive() ? 1 : 0);

            serverPlayer.openHandledScreen(new ExtendedScreenHandlerFactory<BlockPos>() {
                @Override
                public BlockPos getScreenOpeningData(ServerPlayerEntity p) { return pos; }

                @Override
                public Text getDisplayName() { return Text.literal("Waypoint Powers"); }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv,
                                                net.minecraft.entity.player.PlayerEntity p) {
                    return new WaypointBlockScreenHandler(syncId, inv,
                            ScreenHandlerContext.create(world, pos), pos, delegate);
                }
            });
        }
        return ActionResult.SUCCESS;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!state.isOf(world.getBlockState(pos).getBlock())) {
            WaypointSavedData.getOrCreate(world.getServer()).removePosition(pos);
            WaypointRemovePayload payload = new WaypointRemovePayload(pos);
            for (ServerPlayerEntity player : world.getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        }
        super.onStateReplaced(state, world, pos, moved);
    }
}