package mod.nextblocks;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

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