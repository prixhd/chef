package com.bot.chef.service;

import com.bot.chef.configuration.ChefBotConfiguration;
import com.bot.chef.model.User;
import com.bot.chef.parser.ChefParserSecond;
import com.bot.chef.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@Slf4j
@Component
public class ChefBot extends TelegramLongPollingBot {

    static final String HELP_TEXT = """
            \uD83D\uDC4B\uD83C\uDFFD Привет! Я бот, который помогает тебе быть в курсе последних баскетбольных матчей.

            \uD83E\uDD0C\uD83C\uDFFD Вот что я умею:

            • Обновлять информацию о матчах регулярно.
            • Ты можешь задать мне вопросы о конкретных матчах или командах.

            \uD83D\uDCE3 Дополнительная информация:

            Для лучшего вывода названия матчей у нас есть сокращения:

            \uD83E\uDD85 Hawks -> ATL
            \uD83C\uDF40 Celtics -> BOS
            \uD83D\uDC34 Nets -> BKN
            \uD83D\uDC1D Hornets -> CHA
            \uD83D\uDC2E Bulls -> CHI
            ⚔️ Cavaliers -> CLE
            \uD83D\uDC34 Mavericks -> DAL
            ⚒️ Nuggets -> DEN
            \uD83D\uDD27 Pistons -> DET
            \uD83D\uDC4C Warriors -> GSW
            \uD83D\uDE80 Rockets -> HOU
            \uD83C\uDFCE️ Pacers -> IND
            ⛵ Clippers -> LAC
            \uD83C\uDFA5 Lakers -> LAL
            \uD83D\uDC3B Grizzlies -> MEM
            \uD83D\uDD25 Heat -> MIA
            \uD83E\uDD8C Bucks -> MIL
            \uD83D\uDC3A Timberwolves -> MIN
            ⚜️ Pelicans -> NOP
            \uD83D\uDDFD Knicks -> NYK
            \uD83D\uDCA5 Thunder -> OKC
            \uD83D\uDD2E Magic -> ORL
            \uD83D\uDD14 76ers -> PHI
            ☀️ Suns -> PHX
            \uD83C\uDF32 Blazers -> POR
            \uD83D\uDC51 Kings -> SAC
            \uD83C\uDF35 Spurs -> SAS
            \uD83C\uDF41 Raptors -> TOR
            \uD83C\uDFB7 Jazz -> UTA
            \uD83D\uDCAB Wizards -> WAS

            Помимо названия у нас есть сокращения названия этапов Play-off и Play-in:

            NBA Finals - Game -> Finals-G
            East Finals - Game -> East F-G
            West Finals - Game -> West F-G
            East Semifinals - Game -> East SF-G
            West Semifinals - Game -> West SF-G
            West 1st Round - Game -> West R1-G
            East 1st Round - Game -> East R1-G
            Play-In - East -> PI-East
            Play-In - West -> PI-West

            \uD83E\uDD33\uD83C\uDFFD Пример использования:

            1️⃣  Напиши /start чтобы начать.
            2️⃣  Напиши /matches чтобы получить список матчей.
            3️⃣  Используй /help для получения справки.

            Желаю удачи и приятного просмотра матчей! \uD83C\uDFC0""";

    static final String TEXT_FOR_NOBODY_COMMANDS = """
            \uD83E\uDD37\u200D♂️ Извините, я не понимаю вас.

            \uD83E\uDD33 Если вы хотите получить список матчей введите команду /matches.

            \uD83D\uDD04 Также можете перезапустить бота командой /start""";

    static final String ANSWER_FOR_COMMAND_START = """
            \uD83D\uDC4B Привет\s

            \uD83E\uDD16 Данный бот предоставляет доступ к последним матчам Национальной Баскетбольной Ассоциации.

            \uD83C\uDFC6 Благодаря нему вы всегда и везде сможете насладится матчами сильнейшей баскетбольной лиги.

            ❕Если Бот вам не отвечает, перезапустите командой /start

            Приятного пользования! \uD83C\uDF40""";

    static final String TEXT_FOR_SEARCH_MATCHES = "Выберете нужный матч из списка:";

    final ChefBotConfiguration config;

    private final UserRepository userRepository;

    private final MatchService matchService;

    private Map<String, String> listOfMatches;

    @Autowired
    public ChefBot(ChefBotConfiguration config, UserRepository userRepository, MatchService matchService) {
        this.config = config;
        this.matchService = matchService;
        this.userRepository = userRepository;
        List<BotCommand> listOfCommands = new ArrayList<>();

        listOfCommands.add(new BotCommand("/start", "\uD83D\uDD04 Перезапуск"));
        listOfCommands.add(new BotCommand("/matches", "⛹️ Показать список матчей"));
        listOfCommands.add(new BotCommand("/help", "❓ Помощь и список команд"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot command list: {}", e.getMessage());
        }

        // Инициализация listOfMatches после инициализации matchService
        initializeMatches();
    }

    private void initializeMatches() {
        this.listOfMatches = matchService.getMatches();

    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText() ) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start":

                    registerUser(update.getMessage());

                    startCommandReceived(chatId);
                    break;
                case "/matches":
                    matchesCommandReceived(chatId);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default:

                    sendMessage(chatId, TEXT_FOR_NOBODY_COMMANDS);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.equals("SEARCH_MATCHES")) {

                searchMatchesCommandLine(chatId, messageId);
            } else {

                // Получаем значение из HashMap по callbackData
                String valueFromHashMap = listOfMatches.get(callbackData);
                String textLink = "Нажмите чтобы посмотреть матч";
                String messageToAnswer = "Вы выбрали матч: " + callbackData + "\n\n Приятного просмотра\uD83E\uDD17";

                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(textLink); // Устанавливаем текст ссылки
                button.setUrl(valueFromHashMap); // Устанавливаем URL из HashMap
                row.add(button);
                rows.add(row);
                markup.setKeyboard(rows);

                // Отправляем сообщение с кнопкой
                EditMessageText message = new EditMessageText();
                message.setChatId(chatId);
                message.setMessageId((int) messageId);
                message.setText(messageToAnswer);
                message.setReplyMarkup(markup);

                executeEditMessageText("Error occurred on answer with send text link: {}", message);

            }
        }
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()){

            Long chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("User saved: {}", user);
        }
    }

    private void searchMatchesCommandLine(long chatId, long messageId) {
        EditMessageText message = new EditMessageText();

        message.setText(ChefBot.TEXT_FOR_SEARCH_MATCHES);
        message.setChatId(chatId);
        message.setMessageId((int) messageId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(createInlineKeyboard());
        message.setReplyMarkup(inlineKeyboardMarkup);

        executeEditMessageText("Error occurred with the command with /start and searching matches: {}", message);


    }

    private void startCommandReceived(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(ANSWER_FOR_COMMAND_START);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(); 
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> inlineKeyboardButtons = new ArrayList<>();
        var searchButton = new InlineKeyboardButton();

        searchButton.setText("Найди матчи ➝");
        searchButton.setCallbackData("SEARCH_MATCHES");

        inlineKeyboardButtons.add(searchButton);
        keyboardRows.add(inlineKeyboardButtons);
        inlineKeyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(inlineKeyboardMarkup);

        executeSendMessage("Error occurred with the command /start: {}", message);

    }

    private void matchesCommandReceived(long chatId) {
        SendMessage message = new SendMessage();

        message.setText(ChefBot.TEXT_FOR_SEARCH_MATCHES);
        message.setChatId(chatId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        inlineKeyboardMarkup.setKeyboard(createInlineKeyboard());
        message.setReplyMarkup(inlineKeyboardMarkup);

        executeSendMessage("Error occurred with the command /matches: {}", message);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        executeSendMessage("Error occurred on send message: {}", message);
    }

    private List<List<InlineKeyboardButton>> createInlineKeyboard() {
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>(); // Текущий ряд кнопок
        int buttonCount = 0; // Счетчик для кнопок
        int allButtonCount = 0;

        for (Map.Entry<String, String> entry : listOfMatches.entrySet()) {
            String matchText = entry.getKey(); // Текст кнопки

            // Значение из HashMap

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(matchText);
            button.setCallbackData(matchText); // Используем matchValue как callbackData

            currentRow.add(button);
            buttonCount++;
            allButtonCount++;
            if (allButtonCount <= 10) {
                // Создаем новый ряд, если в текущем ряду 2 кнопок
                if (++buttonCount % 2 == 0) { // Например, по 2 кнопки в ряд
                    keyboardRows.add(currentRow);
                    currentRow = new ArrayList<>(); // Новый ряд
                }
            } else {
                break;
            }
        }

        // Добавляем последний ряд, если в нем меньше 5 кнопок
        if (!currentRow.isEmpty()) {
            keyboardRows.add(currentRow);
        }

        return keyboardRows;
    }

    private void executeSendMessage(String textForLog, SendMessage message) {
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error(textForLog, e.getMessage());
        }
    }

    private void executeEditMessageText(String textForLog, EditMessageText message) {
        try {
            execute(message);
        }
        catch (TelegramApiException e) {
            log.error(textForLog, e.getMessage());
        }
    }
}
