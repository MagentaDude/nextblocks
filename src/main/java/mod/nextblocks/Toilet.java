package mod.nextblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static mod.nextblocks.Main.SEWAGE;

public class Toilet extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    private final Map<Direction, VoxelShape> SHAPE = makeShapes();

    public Toilet(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(FULL, false));
    }

    private void dropSewage(Level level, BlockPos pos) {
        ItemEntity sewage = new ItemEntity(level,
                pos.getX() + 0.5, pos.getY() - 0.25, pos.getZ() + 0.5,
                new ItemStack((ItemLike) SEWAGE)
        );
        sewage.setPickUpDelay(10);
        level.addFreshEntity(sewage);
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 2, 1);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        FoodData foodData =  player.getFoodData();
        boolean full = state.getValue(FULL);

        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5) > 1 // Checks if close by
                || (!full && foodData.getFoodLevel() <= 6 && !player.isCreative())) // Checks for full or in creative
            {
                return InteractionResult.CONSUME;
            }
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, state.setValue(FULL, !full));
            if (full) {
                dropSewage(level, pos);
            } else {
                if (!player.isCreative()) {
                    foodData.setFoodLevel(foodData.getFoodLevel() - 6); // Only makes the player hungry if they're in creative
                }
                foodData.setSaturation(20);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getValue(FULL) && newState.getBlock() != state.getBlock() && !movedByPiston) {
            dropSewage(level, pos);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.nxtblx.toilet"));
        super.appendHoverText(stack, ctx, components, flag);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(FULL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    private Map<Direction, VoxelShape> makeShapes() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0, 1, 0.125, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0, 0.25, 1, 0.125, 0.78125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.25, 0.25, 0.125, 0.78125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.78125, 1, 0.125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.125, 0.125, 0.875, 0.59375, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.125, 0.25, 0.875, 0.59375, 0.78125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.125, 0.25, 0.25, 0.59375, 0.78125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.125, 0.78125, 0.875, 0.5625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.5625, 0.78125, 0.875, 1.3125, 1), BooleanOp.OR);

        shape.optimize();
        VoxelShape eastShape = rotateShape(shape);
        VoxelShape southShape = rotateShape(eastShape);
        return Map.of(
                Direction.NORTH, shape,
                Direction.EAST, eastShape,
                Direction.SOUTH, southShape,
                Direction.WEST, rotateShape(southShape)
        );
    }

    public VoxelShape rotateShape(VoxelShape shape) {
        AtomicReference<VoxelShape> out = new AtomicReference<>(Shapes.empty());
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            out.set(Shapes.join(out.get(), Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX), BooleanOp.OR));
        });
        return out.get();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
}