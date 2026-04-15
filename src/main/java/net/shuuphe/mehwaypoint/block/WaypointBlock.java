package net.shuuphe.mehwaypoint.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
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
import net.shuuphe.mehwaypoint.screen.WaypointBlockScreenHandler;
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

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                         @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world instanceof ServerWorld serverWorld) {
            String name = (world.getBlockEntity(pos) instanceof WaypointBlockEntity be)
                    ? be.getName() : "Waypoint";
            WaypointSavedData.getOrCreate(serverWorld.getServer()).addPosition(pos);
            WaypointAddPayload payload = new WaypointAddPayload(pos, name);
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 net.minecraft.entity.player.PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            if (!(world.getBlockEntity(pos) instanceof WaypointBlockEntity be)) return ActionResult.PASS;
            if (!(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;

            ArrayPropertyDelegate delegate = new ArrayPropertyDelegate(1);
            delegate.set(0, be.getLevel());

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