package com.bot.chef.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
public class ChefParser {

    private static final Map<String, String> teamEmojis = new HashMap<>();
    private static final Map<String, String> teamReduction = new HashMap<>();

    static {
        teamReduction.put("Atlanta Hawks", "ATL");
        teamReduction.put("Boston Celtics", "BOS");
        teamReduction.put("Brooklyn Nets", "BKN");
        teamReduction.put("Charlotte Hornets", "CHA");
        teamReduction.put("Chicago Bulls", "CHI");
        teamReduction.put("Cleveland Cavaliers", "CLE");
        teamReduction.put("Dallas Mavericks", "DAL");
        teamReduction.put("Denver Nuggets", "DEN");
        teamReduction.put("Detroit Pistons", "DET");
        teamReduction.put("Golden State Warriors", "GSW");
        teamReduction.put("Houston Rockets", "HOU");
        teamReduction.put("Indiana Pacers", "IND");
        teamReduction.put("Los Angeles Clippers", "LAC");
        teamReduction.put("Los Angeles Lakers", "LAL");
        teamReduction.put("Memphis Grizzles", "MEM");
        teamReduction.put("Miami Heat", "MIA");
        teamReduction.put("Milwaukee Bucks", "MIL");
        teamReduction.put("Minnesota Timberwolves", "MIN");
        teamReduction.put("New Orleans Pelicans", "NOP");
        teamReduction.put("New York Knicks", "NYK");
        teamReduction.put("Oklahoma City Thunder", "OKC");
        teamReduction.put("Orlando Magic", "ORL");
        teamReduction.put("Philadelphia 76ers", "PHI");
        teamReduction.put("Phoenix Suns", "PHX");
        teamReduction.put("Portland Trail Blazers", "POR");
        teamReduction.put("Sacramento Kings", "SAC");
        teamReduction.put("San Antonio Spurs", "SAS");
        teamReduction.put("Toronto Raptors", "TOR");
        teamReduction.put("Utah Jazz", "UTA");
        teamReduction.put("Washington Wizards", "WAS");
    }

    static {
        teamEmojis.put("USA", "🇺🇸");
        teamEmojis.put("France", "🇫🇷");
        teamEmojis.put("Serbia", "🇷🇸");
        teamEmojis.put("ATL", "\uD83E\uDD85");
        teamEmojis.put("BOS", "\uD83C\uDF40");
        teamEmojis.put("BKN", "\uD83D\uDC34");
        teamEmojis.put("CHA", "\uD83D\uDC1D");
        teamEmojis.put("CHI", "\uD83D\uDC2E");
        teamEmojis.put("CLE", "⚔\uFE0F");
        teamEmojis.put("DAL", "\uD83D\uDC34");
        teamEmojis.put("DEN", "⚒\uFE0F");
        teamEmojis.put("DET", "\uD83D\uDD27");
        teamEmojis.put("GSW", "\uD83D\uDC4C");
        teamEmojis.put("HOU", "\uD83D\uDE80");
        teamEmojis.put("IND", "\uD83C\uDFCE\uFE0F");
        teamEmojis.put("LAC", "⛵");
        teamEmojis.put("LAL", "\uD83C\uDFA5");
        teamEmojis.put("MEM", "\uD83D\uDC3B");
        teamEmojis.put("MIA", "\uD83D\uDD25");
        teamEmojis.put("MIL", "\uD83E\uDD8C");
        teamEmojis.put("MIN", "\uD83D\uDC3A");
        teamEmojis.put("NOP", "⚜\uFE0F");
        teamEmojis.put("NYK", "\uD83D\uDDFD");
        teamEmojis.put("OKC", "\uD83D\uDCA5");
        teamEmojis.put("ORL", "\uD83D\uDD2E");
        teamEmojis.put("PHI", "\uD83D\uDD14");
        teamEmojis.put("PHX", "☀\uFE0F");
        teamEmojis.put("POR", "\uD83C\uDF32");
        teamEmojis.put("SAC", "\uD83D\uDC51");
        teamEmojis.put("SAS", "\uD83C\uDF35");
        teamEmojis.put("TOR", "\uD83C\uDF41");
        teamEmojis.put("UTA", "\uD83C\uDFB7");
        teamEmojis.put("WAS", "\uD83D\uDCAB");
    }

    public Map<String, String> parsing() {

        Map<String, String> listMatches = new LinkedHashMap<>();

        try {
            var document = Jsoup.connect("https://fishkernba.com/").get();
            var list = document.select("#featured-thumbnail");


            for (var el : list) {
                String matchTitle = formatMatchText(splitText(el.attr("title")));
                String matchLink = el.attr("href");

                listMatches.put(matchTitle, matchLink);
            }

            return listMatches;

        } catch (IOException e) {
            log.error("There was an error parsing the site: {}", e.getMessage());
        }

        return listMatches;
    }

    public static String splitText(String text) {
        String textToReplay = "Full Game Replay";

        return text.replace(textToReplay, "");
    }

    // Метод для форматирования текста с добавлением смайлика рядом с названием команды
    public static String formatMatchText(String matchText) {
        for (Map.Entry<String, String> reduction : teamReduction.entrySet()) {
            matchText = matchText.replace(reduction.getKey(), reduction.getValue());
        }

        for (Map.Entry<String, String> emoji : teamEmojis.entrySet()) {
            matchText = matchText.replace(emoji.getKey(), emoji.getKey() + " " + emoji.getValue());
        }

        return matchText;
    }
}
