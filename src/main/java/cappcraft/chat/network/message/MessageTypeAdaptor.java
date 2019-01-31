package cappcraft.chat.network.message;

import cappcraft.chat.ChatPipe;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashSet;

public class MessageTypeAdaptor extends com.google.gson.TypeAdapter<IMessage> {
    public static final MessageTypeAdaptor INSTANCE = new MessageTypeAdaptor();
    private HashSet<IMessageType> MessageTypeMap = new HashSet<>();
    private Gson gson = new Gson();
    @Override
    public void write(JsonWriter in, IMessage msg) throws IOException {
        JsonObject json = new JsonObject();
        msg.onSeralize(json);
        in.value(json.toString());
    }

    @Override
    public IMessage read(JsonReader in) throws IOException {
        in.setLenient(true);
        JsonObject json = gson.fromJson(in, JsonObject.class);
        for (IMessageType type:
             MessageTypeMap) {
            if(json.has(type.getJsonTypeName())){
                Class<? extends IMessage> clazz = type.getJsonTypeClass(json.get(type.getJsonTypeName()));
                if(clazz != null) {
                    try {
                        IMessage message = clazz.newInstance();
                        return message.onDeseralize(json);
                    } catch (InstantiationException | IllegalAccessException e) {
                        ChatPipe.logger.warn("No-args constructor for class " + clazz.getName() + " does not exist. Add one to solve this problem.", e);
                    }
                }
            }
        }
        return null;
    }

    public void registerMessage(IMessageType type) throws IllegalArgumentException{
        if(!MessageTypeMap.add(type)){
            throw new IllegalArgumentException("Can't register same MessageType");
        }
    }
}
