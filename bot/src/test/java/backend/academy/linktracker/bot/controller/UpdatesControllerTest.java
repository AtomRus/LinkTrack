package backend.academy.linktracker.bot.controller;

import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.dto.LinkUpdateEvent;
import backend.academy.linktracker.bot.service.LinkUpdateHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UpdatesControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldAcceptUpdate() throws Exception {
        LinkUpdateHandler linkUpdateHandler = org.mockito.Mockito.mock(LinkUpdateHandler.class);
        UpdatesController controller = new UpdatesController(linkUpdateHandler);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        LinkUpdateEvent event = new LinkUpdateEvent(1L, "https://example.com", "d", List.of(1L));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/updates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(event)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                        .isOk());

        verify(linkUpdateHandler).handleEvent(event);
    }
}
