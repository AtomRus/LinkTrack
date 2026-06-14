package backend.academy.linktracker.scrapper.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KafkaEmitTimestampHeaderTest {

    @Test
    void shouldEncodeAndDecodeMillis() {
        byte[] encoded = KafkaEmitTimestampHeader.encodeMillis(1_700_000_000_123L);
        assertThat(KafkaEmitTimestampHeader.decodeMillis(encoded)).isEqualTo(1_700_000_000_123L);
    }

    @Test
    void shouldReturnMinusOneForInvalidPayload() {
        assertThat(KafkaEmitTimestampHeader.decodeMillis(null)).isEqualTo(-1L);
        assertThat(KafkaEmitTimestampHeader.decodeMillis(new byte[] {1, 2})).isEqualTo(-1L);
    }
}
