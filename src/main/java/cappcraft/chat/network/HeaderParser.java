package cappcraft.chat.network;

import io.netty.util.ByteProcessor;
import io.netty.util.internal.AppendableCharSequence;

public class HeaderParser implements ByteProcessor {
    private final AppendableCharSequence seq;
    private final int maxLength;
    private int size;

    public HeaderParser(AppendableCharSequence seq, int maxLength) {
        this.seq = seq;
        this.maxLength = maxLength;
    }

    @Override
    public boolean process(byte value) throws Exception {
        if(++size > maxLength)
            return true;
        char nextByte = (char) value;
        seq.append(nextByte);
        return false;
    }
}
