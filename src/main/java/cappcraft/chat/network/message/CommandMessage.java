package cappcraft.chat.network.message;

import cappcraft.chat.ChatPipe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;

public class CommandMessage implements IMessage {
    public final MessageType type = MessageType.COMMAND;
    public CommandTpye command = null;
    public String sender;
    public String value;
    @Expose(deserialize = false)
    private static DedicatedServer server = (DedicatedServer) FMLCommonHandler.instance().getMinecraftServerInstance();

    @Override
    public IMessage onDeseralize(JsonObject in) {
        command = CommandTpye.valueOf(in.get("command").getAsString());
        switch (command){
            case MCCOMMAND:
                sender = in.get("sender").getAsString();
                value = in.get("value").getAsString();
                if(sender.isEmpty() || value.isEmpty()){
                    throw new IllegalArgumentException("Command sender and value could not be null");
                }
        }
        ;return this;
    }

    @Override
    public void onSeralize(JsonObject in) {
    }

    @Override
    public void onReceived(ChannelHandlerContext ctx, Gson gson) {
        switch (command) {
            case RESTART:
                ChatPipe.proxy.restart();
                ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS))));
                break;
            case STOPSYNC:
                MinecraftForge.EVENT_BUS.unregister(ChatPipe.proxy.chatHandler);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS))));
                break;
            case STARTSYNC:
                MinecraftForge.EVENT_BUS.register(ChatPipe.proxy.chatHandler);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS))));
                break;
            case MCCOMMAND:
                execute(ctx, gson);
                break;
        }
    }

    private void execute(ChannelHandlerContext ctx, Gson gson){
        ICommandSender player  = server.getPlayerList().getPlayerByUsername(sender);
        if(player != null){
            server.addPendingCommand(value,new CommandSender(player, ctx, gson));
            return;
        }else if(sender.equals("Server")){
            server.addPendingCommand(value,new CommandSender(server, ctx, gson));
            return;
        }
        throw new IllegalArgumentException("Sender Not Exist");
    }
    private class CommandSender implements ICommandSender {
        ICommandSender sender;
        ChannelHandlerContext ctx;
        Gson gson;
        public CommandSender(ICommandSender sender,ChannelHandlerContext ctx, Gson gson)
        {
            this.sender = sender;
            this.ctx = ctx;
            this.gson = gson;
        }
        @Override
        public String getName() {
            return sender.getName();
        }

        @Override
        public ITextComponent getDisplayName() {
            return sender.getDisplayName();
        }

        @Override
        public void sendMessage(ITextComponent component) {
            ctx.channel().writeAndFlush(new TextWebSocketFrame(gson.toJson(new ResultMessage(RequestResult.SUCCESS,component.getUnformattedComponentText()))));
            sender.sendMessage(component);
        }

        @Override
        public boolean canUseCommand(int permLevel, String commandName) {
            return sender.canUseCommand(permLevel,commandName);
        }

        @Override
        public BlockPos getPosition() {
            return sender.getPosition();
        }

        @Override
        public Vec3d getPositionVector() {
            return sender.getPositionVector();
        }

        @Override
        public World getEntityWorld() {
            return sender.getEntityWorld();
        }

        @Nullable
        @Override
        public Entity getCommandSenderEntity() {
            return sender.getCommandSenderEntity();
        }

        @Override
        public boolean sendCommandFeedback() {
            return sender.sendCommandFeedback();
        }

        @Override
        public void setCommandStat(CommandResultStats.Type type, int amount) {
            sender.setCommandStat(type,amount);
        }

        @Nullable
        @Override
        public MinecraftServer getServer() {
            return server;
        }
    }
}
