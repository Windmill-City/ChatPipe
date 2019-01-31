package cappcraft.chat.network.message;

import com.google.gson.JsonElement;

public enum MessageType implements IMessageType{
    CHAT(ChatMessage.class),
    COMMAND(CommandMessage.class),
    RESULT(ResultMessage.class);

    private Class<? extends IMessage> value;

    MessageType(Class<? extends IMessage> Class) {
        value = Class;
    }

    @Override
    public String getJsonTypeName() {
        return "type";
    }

    @Override
    public Class<? extends IMessage> getJsonTypeClass(JsonElement typeValue) {
        for (MessageType type :MessageType.values()){
            if (type.name().equals(typeValue.getAsString())){
                return type.value;
            }
        }
        return null;
    }
}
