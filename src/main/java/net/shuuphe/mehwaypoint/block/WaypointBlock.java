package net.shuuphe.mehwaypoint.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shuuphe.mehwaypoint.block.entity.WaypointBlockEntity;
import net.shuuphe.mehwaypoint.network.WaypointAddPayload;
import net.shuuphe.mehwaypoint.network.WaypointRemovePayload;
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
            WaypointAddPayload payload = new WaypointAddPayload(pos, name);
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!state.isOf(world.getBlockState(pos).getBlock())) {
            WaypointRemovePayload payload = new WaypointRemovePayload(pos);
            for (ServerPlayerEntity player : world.getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        }
        super.onStateReplaced(state, world, pos, moved);
    }
}