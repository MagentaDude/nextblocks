package mod.nextblocks;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Supplier;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "nxtblx";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MODID);

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(MODID);

    public static final DeferredBlock<Block> NEXTBASE = BLOCKS.registerBlock("nextbase", Nextbase::new, BlockBehaviour.Properties.of()
            .destroyTime(-1)
            .sound(SoundType.SHROOMLIGHT)
            .lightLevel(state -> 15)
            .noOcclusion()
    );
    public static final DeferredItem<BlockItem> NEXTBASE_ITEM = ITEMS.registerSimpleBlockItem("nextbase", NEXTBASE, new Item.Properties()
            .rarity(Rarity.RARE)
    );

    public static final DeferredBlock<Block> CINEMA = BLOCKS.registerSimpleBlock("cinema", BlockBehaviour.Properties.of()
            .destroyTime(1)
            .sound(SoundType.WET_SPONGE)
            .lightLevel(state -> 3)
    );
    public static final DeferredItem<BlockItem> CINEMA_ITEM = ITEMS.registerSimpleBlockItem("cinema", CINEMA, new Item.Properties()
            .rarity(Rarity.UNCOMMON));

    public static final DeferredBlock<Block> TOILET = BLOCKS.registerBlock("toilet", Toilet::new, BlockBehaviour.Properties.of()
            .strength(1)
            .sound(SoundType.DECORATED_POT)
            .noOcclusion()
    );
    public static final DeferredItem<BlockItem> TOILET_ITEM = ITEMS.registerSimpleBlockItem("toilet", TOILET);

    public static final DeferredItem<PickaxeItem> NEXTPICK = ITEMS.registerItem("nextpick",
            props -> new PickaxeItem(ToolMaterial.GOLD, 1, -2.8f, props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flag) {
                    if (Screen.hasShiftDown()) {
                        components.add(Component.translatable("tooltip.nxtblx.nextpick"));
                    } else {
                        components.add(Component.translatable("tooltip.nxtblx.prompt"));
                    }
                    super.appendHoverText(stack, ctx, components, flag);
                }
            });

    public static final DeferredItem<Nextfinder> NEXTFINDER = ITEMS.registerItem("nextfinder",
            Nextfinder::new);

    public static final DeferredItem<Item> SEWAGE = ITEMS.registerItem("sewage",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flag) {
                    components.add(Component.translatable("tooltip.nxtblx.sewage"));
                    super.appendHoverText(stack, ctx, components, flag);
                }
            });

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NXTBLX_TAB = TABS.register("nxtblx_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.nxtblx"))
            .icon(() -> NEXTPICK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(NEXTPICK.get());
                output.accept(NEXTFINDER.get());
                output.accept(NEXTBASE_ITEM.get());
                output.accept(CINEMA_ITEM.get());
                output.accept(TOILET_ITEM.get());
                output.accept(SEWAGE.get());
            }).build());

    public static final Supplier<EntityType<NextblockEntity>> NEXTBLOCK = ENTITY_TYPES.registerEntityType(
            "nextblock", NextblockEntity::new, MobCategory.CREATURE,
            builder -> builder.sized(1, 1).eyeHeight(1)
    );

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("The nextblocks are loading...");
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        LOGGER.info("The nextblocks are here.");
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (!level.isClientSide) {
            BlockPos pos = event.getPos();
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (level.getBlockState(pos).is(NEXTBASE.get()) && stack.is(NEXTPICK.asItem())) {
                level.removeBlock(pos, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1, 1);
                level.addFreshEntity(new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        NEXTBASE.toStack()));
            }
        }
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide()) {return;}
        ServerLevel level = (ServerLevel) event.getLevel();
        ChunkPos chunk = event.getChunk().getPos();

        for (int y = level.getMinY(); y < level.getMaxY(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos pos = new BlockPos(chunk.getMinBlockX() + x, y, chunk.getMinBlockZ() + z);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(NEXTBASE)) {
                        state.tick(level, pos, RandomSource.create());
                    }
                }
            }
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(NEXTBLOCK.get(), NextblockEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(NEXTBLOCK.get(), NextblockRenderer::new);
        }
    }
}