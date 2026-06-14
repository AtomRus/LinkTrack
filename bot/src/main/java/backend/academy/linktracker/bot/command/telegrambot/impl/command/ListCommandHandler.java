// ListCommandHandler.java
package backend.academy.linktracker.bot.command.telegrambot.impl.command;

import backend.academy.linktracker.bot.command.annotaion.Command;
import backend.academy.linktracker.bot.command.requirement.AbstractRequirement;
import backend.academy.linktracker.bot.command.telegrambot.AbstractTelegramCommandHandler;
import backend.academy.linktracker.bot.command.telegrambot.CommandNameEnum;
import backend.academy.linktracker.bot.command.telegrambot.UserState;
import backend.academy.linktracker.bot.exception.ResourceNotFoundException;
import backend.academy.linktracker.bot.exception.TagNotFoundException;
import backend.academy.linktracker.bot.grpc.ScrapperGrpcService;
import backend.academy.linktracker.bot.model.Link;
import backend.academy.linktracker.bot.service.TelegramBotService;
import com.pengrad.telegrambot.model.Update;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
@Command(
        command = "/list",
        description = "Получить список отслеживаемых ссылок",
        states = {UserState.MENU})
public class ListCommandHandler extends AbstractTelegramCommandHandler {

    private final ScrapperGrpcService scrapperGrpcService;
    private final TelegramBotService telegramBotService;

    public ListCommandHandler(
            TelegramBotService telegramBotService,
            Map<String, AbstractRequirement> allRequirements,
            ScrapperGrpcService scrapperGrpcService) {
        super(telegramBotService, allRequirements);
        this.scrapperGrpcService = scrapperGrpcService;
        this.telegramBotService = telegramBotService;
    }

    @Override
    protected void processCommand(Update update) {
        Long chatId = update.message().chat().id();
        String text = update.message().text();

        String[] parts = text.split(" ", 2);
        List<Link> list = null;

        if (parts.length > 1) {
            String tag = parts[1].trim().toLowerCase();

            if (!tag.matches("^[а-яА-ЯёЁa-zA-Z]+$")) {
                telegramBotService.sendMessage(chatId, "Введен некорректный тег! Используйте только буквы");
                return;
            }
            try {
                list = scrapperGrpcService.getLinksByTags(chatId, tag);
            } catch (TagNotFoundException tagNotFoundException) {
                telegramBotService.sendMessage(chatId, "По такому тегу ссылок нет!");
                return;
            }
        } else {
            try {
                list = scrapperGrpcService.getLinks(chatId);
            } catch (ResourceNotFoundException e) {
                telegramBotService.sendMessage(chatId, "Отслеживаемых ссылок нет");
                return;
            }
        }

        String userList =
                list.stream().map(link -> "* " + link.getLink().toString()).collect(Collectors.joining("\n"));
        telegramBotService.sendMessage(chatId, "Ваши ссылки:\n" + userList);
    }

    @Override
    public String getHandlerName() {
        return CommandNameEnum.LIST.getName();
    }
}
