package backend.academy.linktracker.bot.controller;

import backend.academy.linktracker.bot.dto.LinkUpdateEvent;
import backend.academy.linktracker.bot.service.LinkUpdateHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UpdatesController {
    private final LinkUpdateHandler linkUpdateHandler;

    @PostMapping("/updates")
    public ResponseEntity<Void> postUpdate(@RequestBody LinkUpdateEvent event) {
        linkUpdateHandler.handleEvent(event);
        return ResponseEntity.ok().build();
    }
}
