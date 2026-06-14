package backend.academy.linktracker.scrapper.kafka;

import java.nio.ByteBuffer;

public final class KafkaEmitTimestampHeader {
    public static final String NAME = "lt-emitted-at";

    private KafkaEmitTimestampHeader() {}

    public static byte[] encodeMillis(long millis) {
        return ByteBuffer.allocate(Long.BYTES).putLong(millis).array();
    }

    public static long decodeMillis(byte[] bytes) {
        if (bytes == null || bytes.length < Long.BYTES) {
            return -1L;
        }
        return ByteBuffer.wrap(bytes).getLong();
    }
}
