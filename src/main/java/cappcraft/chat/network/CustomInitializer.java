package cappcraft.chat.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;

import java.util.HashMap;

@ChannelHandler.Sharable
public class CustomInitializer extends ChannelInitializer {
    public static final CustomInitializer INSTANCE = new CustomInitializer();
    private static final HashMap<String, ChannelInitializer> channelInitializers = new HashMap<>();


    public ChannelInitializer registerInitializer(String name, ChannelInitializer channelInitializer){
        if(channelInitializers.containsKey(name)){
            throw new RuntimeException("This Initializer has been registered");
        }
        channelInitializers.put(name,channelInitializer);
        return channelInitializer;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        for (ChannelInitializer init :
                channelInitializers.values()) {
            ch.pipeline().addLast(init);
        }
    }
}
