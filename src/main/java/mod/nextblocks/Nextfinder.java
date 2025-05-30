package mod.nextblocks;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class Nextfinder extends Item {
    public Nextfinder(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player client, InteractionHand hand) {
        if (!level.isClientSide) {
            ServerPlayer player = (ServerPlayer) client;
            CommandSourceStack source = player.createCommandSourceStack().withPermission(Commands.LEVEL_GAMEMASTERS);
            level.getServer().getCommands().performPrefixedCommand(source, "locate structure #nxtblx:hideout");
        }
        return InteractionResult.SUCCESS;
    }
}