package com.bot.chef.service;
import com.bot.chef.model.Match;
import com.bot.chef.parser.ChefParserSecond;
import com.bot.chef.repo.MatchRepository;
import io.lettuce.core.ScriptOutputType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MatchService {
    private static final long CACHE_EXPIRATION = 10; // Время кэширования в минутах

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    @Lazy
    private ChefParserSecond parser; // Теперь вызываем метод парсинга

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Cacheable(value = "matches", key = "'matches'")
    public Map<String, String> getMatches() {
        List<Match> cachedMatches = (List<Match>) redisTemplate.opsForValue().get("matches");

        if (cachedMatches != null) {
            System.out.println(
                    "В Redis есть данные"
            );

            return convertToMap(cachedMatches);
        } else {
            System.out.println(
                    "В Redis нет данных. " +
                    "Начинаем парсинг."
            );
            parser.parsing();

            List<Match> allMatches = matchRepository.findAll();
            redisTemplate.opsForValue().set("matches", allMatches, CACHE_EXPIRATION, TimeUnit.MINUTES);

            return convertToMap(allMatches); // Сохраняем в Redis
        }
    }



    @CacheEvict(value = "matches", allEntries = true)
    public void save(Match match) {
        matchRepository.save(match);
    }

    @Cacheable(value = "match", key = "#matchTitle + '-' + #matchLink")
    public Optional<Match> findByMatchTitleAndMatchLink(String matchTitle, String matchLink) {
        return matchRepository.findByMatchTitleAndMatchLink(matchTitle, matchLink);
    }

    private Map<String, String> convertToMap(List<Match> matches) {
        return matches.stream()
                .sorted(Comparator.comparing(this::extractDateFromMatchTitle))
                .collect(Collectors.toMap(
                        Match::getMatchTitle,
                        Match::getMatchLink));
    }

    private LocalDate extractDateFromMatchTitle(Match match) {
        String matchTitle = match.getMatchTitle();
        String dateString = matchTitle.substring(matchTitle.lastIndexOf("-") + 2).trim();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH); // Убедитесь, что используете Locale
        return LocalDate.parse(dateString, formatter);
    }
}
