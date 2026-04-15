package net.shuuphe.mehwaypoint.entity;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.shuuphe.mehwaypoint.registry.ModBlockEntities;

import java.util.List;

public class WaypointBlockEntity extends BlockEntity {

    private String name = "Waypoint";
    private int level = 1;
    private boolean effectsActive = false;

    public WaypointBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WAYPOINT_BLOCK_ENTITY, pos, state);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; markDirty(); }

    public int getLevel() { return level; }
    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(level, 6));
        markDirty();
    }

    public boolean isEffectsActive() { return effectsActive; }
    public void setEffectsActive(boolean active) {
        this.effectsActive = active;
        markDirty();
    }

    public static void tick(World world, BlockPos pos, BlockState state, WaypointBlockEntity be) {
        if (world.isClient()) return;
        if (!be.effectsActive) return;
        if (world.getTime() % 80L != 0L) return;

        int level = be.getLevel();
        if (level < 1) return;

        double range = (level <= 2) ? 15 : (level <= 5) ? 60 : 120;
        int duration = 9 * 20;

        Box box = new Box(pos).expand(range);
        List<PlayerEntity> players = world.getNonSpectatingEntities(PlayerEntity.class, box);

        int regenAmp = (level >= 2) ? 1 : 0;
        for (PlayerEntity p : players)
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, duration, regenAmp, true, true));

        if (level >= 4)
            for (PlayerEntity p : players)
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, duration, 0, true, true));

        if (level >= 5)
            for (PlayerEntity p : players)
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, duration, 0, true, true));

        if (level >= 6)
            for (PlayerEntity p : players)
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, duration, 0, true, true));
    }

    @Override
    protected void readData(ReadView view) {
        this.name         = view.read("name",          Codec.STRING).orElse("Waypoint");
        this.level        = Math.max(1, view.read("level", Codec.INT).orElse(1));
        this.effectsActive = view.read("effectsActive", Codec.BOOL).orElse(false);
    }

    @Override
    protected void writeData(WriteView view) {
        view.put("name",          Codec.STRING, this.name);
        view.put("level",         Codec.INT,    this.level);
        view.put("effectsActive", Codec.BOOL,   this.effectsActive);
    }
}
