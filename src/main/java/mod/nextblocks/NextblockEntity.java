package mod.nextblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

import static mod.nextblocks.Main.CINEMA;
import static mod.nextblocks.Main.NEXTPICK;

public class NextblockEntity extends Monster {
    private static final EntityDataAccessor<String> BLOCK =
            SynchedEntityData.defineId(NextblockEntity.class, EntityDataSerializers.STRING);
    public NextblockEntity(EntityType<? extends NextblockEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(0, new MeleeAttackGoal(this, 1, true));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 5);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("block", getBlock());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("block")) {
            this.entityData.set(BLOCK, tag.getString("block"));
        }
    }

    public void randomBlock() {
        this.entityData.set(BLOCK, List.of(
                "minecraft:grass_block",
                "minecraft:stone",
                "minecraft:oak_planks",
                "minecraft:oak_log",
                "minecraft:cobblestone",
                "minecraft:bookshelf",
                "minecraft:hay_bale",
                "nxtblx:cinema"
        ).get(this.random.nextInt(8)));
    }

    public String getBlock() {
        return this.entityData.get(BLOCK);
    }

    public static Block blockFromId(String blockName) {
        Optional<Holder.Reference<Block>> block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(blockName));
        if (block.isPresent()) {
            return block.get().value();
        }
        return CINEMA.get();
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        spawnAtLocation(level, NEXTPICK);
        spawnAtLocation(level, blockFromId(getBlock()).asItem());
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 10;
    }

    @Override
    public boolean isColliding(BlockPos pos, BlockState state) {
        if (state.getBlock() == Blocks.WHITE_BANNER) {
            return true;
        }
        return super.isColliding(pos, state);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BLOCK, "nxtblx:cinema");
        super.defineSynchedData(builder);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }
}
