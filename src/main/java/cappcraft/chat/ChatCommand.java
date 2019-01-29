package cappcraft.chat;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class ChatCommand extends CommandBase {

    @Override
    public String getName() {
        return "chatpipe";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/chatpipe reload";
    }

    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if(args.length == 1 && args[0].equals("reload")) {
            Config.reload();
            sender.sendMessage(new TextComponentString("Reloaded Config"));
        }else {
            sender.sendMessage(new TextComponentString("/chatpipe reload"));
        }
    }
}
