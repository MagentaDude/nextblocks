package mod.nextblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.world.chunk.TicketController;

import java.util.List;

import static mod.nextblocks.Main.NEXTBLOCK;
import static mod.nextblocks.Main.NEXTPICK;

public class Nextbase extends Block {
    private static final TicketController ticketController =
            new TicketController(ResourceLocation.parse("nxtblx:chunkloader"));

    public Nextbase(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(NEXTPICK.asItem())) {
            if (!level.isClientSide) {
                player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
                level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 2, 1);
                Vec3 center = pos.getCenter();

                LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.JUNGLE_TEMPLE);
                LootParams.Builder paramBuilder = new LootParams.Builder((ServerLevel) level)
                        .withParameter(LootContextParams.ORIGIN, center);

                List<ItemStack> loot = lootTable.getRandomItems(paramBuilder.create(LootContextParamSets.VAULT));
                level.addFreshEntity(new ItemEntity(level, center.x, center.y, center.z, loot.getFirst()));
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                32, true) != null && level
                .getEntitiesOfClass(NextblockEntity.class, AABB.ofSize(pos.getCenter(), 16, 16, 16))
                .isEmpty()) {
            NEXTBLOCK.get().spawn(level, pos.above(), EntitySpawnReason.SPAWNER).randomBlock();
        }
        level.scheduleTick(pos, this, 200);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 0);
        }
    }
}