package com.bot.chef.parser;

import com.bot.chef.configuration.ChefBotConfiguration;
import com.bot.chef.model.Match;
import com.bot.chef.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class ChefParserSecond {

    private static final Map<String, String> teamReduction = new HashMap<>();

    @Autowired
    private ChefBotConfiguration config;

    @Autowired
    private MatchService matchService;


    static {
        teamReduction.put("Hawks", "ATL");
        teamReduction.put("Celtics", "BOS");
        teamReduction.put("Nets", "BKN");
        teamReduction.put("Hornets", "CHA");
        teamReduction.put("Bulls", "CHI");
        teamReduction.put("Cavaliers", "CLE");
        teamReduction.put("Mavericks", "DAL");
        teamReduction.put("Nuggets", "DEN");
        teamReduction.put("Pistons", "DET");
        teamReduction.put("Warriors", "GSW");
        teamReduction.put("Rockets", "HOU");
        teamReduction.put("Pacers", "IND");
        teamReduction.put("Clippers", "LAC");
        teamReduction.put("Lakers", "LAL");
        teamReduction.put("Grizzlies", "MEM");
        teamReduction.put("Heat", "MIA");
        teamReduction.put("Bucks", "MIL");
        teamReduction.put("Timberwolves", "MIN");
        teamReduction.put("Pelicans", "NOP");
        teamReduction.put("Knicks", "NYK");
        teamReduction.put("Thunder", "OKC");
        teamReduction.put("Magic", "ORL");
        teamReduction.put("76ers", "PHI");
        teamReduction.put("Suns", "PHX");
        teamReduction.put("Blazers", "POR");
        teamReduction.put("Kings", "SAC");
        teamReduction.put("Spurs", "SAS");
        teamReduction.put("Raptors", "TOR");
        teamReduction.put("Jazz", "UTA");
        teamReduction.put("Wizards", "WAS");
    }

    public void parsing() {
        try {
            Document document = fetchHtmlDocument();
            extractMatchData(document);
        } catch (IOException e) {
            log.error("There was an error parsing the site: {}", e.getMessage());
        }
    }

    private Document fetchHtmlDocument() throws IOException {
        return Jsoup.connect(config.getUrl()).get();
    }

    private void extractMatchData(Document document) {
        Elements matchElements = document.select(".thumbnail");
        CompletableFuture.runAsync(() -> {
            for (Element matchElement : matchElements) {
                String matchTitle = formatMatchText(splitText(matchElement.select("a").attr("title")));
                if (matchTitle != null && !matchTitle.isEmpty()) {
                    String matchLink = config.getUrl() + matchElement.select("a").attr("href");
                    Optional<Match> existingMatch = matchService.findByMatchTitleAndMatchLink(matchTitle, matchLink);
                    if (existingMatch.isEmpty()) {
                        Match match = new Match();
                        match.setMatchTitle(matchTitle);
                        match.setMatchLink(matchLink);
                        matchService.save(match);
                    }
                }
            }
            log.info("Парсинг завершён. Все новые матчи сохранены.");
        });
    }

    public static String splitText(String text) {
        if (text != null) {
            text = text.replace("NBA Summer League", "SL");
            text = text.replace("Full Game Replay ", "");
            text = text.replace("NBA Finals - Game ", "Finals-G");
            text = text.replace("East Finals - Game ", "East F-G");
            text = text.replace("West Finals - Game ", "West F-G");
            text = text.replace("East Semifinals - Game ", "East SF-G");
            text = text.replace("West Semifinals - Game ", "West SF-G");
            text = text.replace("West 1st Round - Game ", "West R1-G");
            text = text.replace("East 1st Round - Game ", "East R1-G");
            text = text.replace("Play-In - East", "PI-East");
            text = text.replace("Play-In - West", "PI-West");
        }
        return text;
    }

    public static String formatMatchText(String matchText) {
        if (matchText != null) {
            for (Map.Entry<String, String> reduction : teamReduction.entrySet()) {
                matchText = matchText.replace(reduction.getKey(), reduction.getValue());
            }
        }
        return matchText;
    }
}
