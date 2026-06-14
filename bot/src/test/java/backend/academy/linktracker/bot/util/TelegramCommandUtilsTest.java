package backend.academy.linktracker.bot.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TelegramCommandUtilsTest {

    @Test
    void shouldExtractPlainCommand() {
        assertThat(TelegramCommandUtils.extractCommand("/help")).isEqualTo("/help");
    }

    @Test
    void shouldExtractCommandWithBotSuffix() {
        assertThat(TelegramCommandUtils.extractCommand("/help@link_tracker_bot")).isEqualTo("/help");
    }

    @Test
    void shouldExtractCommandWithArguments() {
        assertThat(TelegramCommandUtils.extractCommand("/list extra")).isEqualTo("/list");
    }

    @Test
    void shouldExtractCommandWithBotSuffixAndArguments() {
        assertThat(TelegramCommandUtils.extractCommand("/track@my_bot arg")).isEqualTo("/track");
    }
}
