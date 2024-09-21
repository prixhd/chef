package com.bot.chef.service;
import com.bot.chef.model.Match;
import com.bot.chef.parser.ChefParserSecond;
import com.bot.chef.repo.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    @Lazy
    private ChefParserSecond parser; // Теперь вызываем метод парсинга

    @Autowired
    private RedisTemplate<String, List<Match>> redisTemplate;

    private static final String MATCH_CACHE_KEY = "matches";
    private static final long CACHE_EXPIRATION = 10; // Время кэширования в минутах

    public Map<String, String> getMatches() {
        List<Match> cachedMatches = redisTemplate.opsForValue().get(MATCH_CACHE_KEY);

        if (cachedMatches != null) {
            System.out.println("В Redis есть данные");
            return convertToMap(cachedMatches);
        } else {
            System.out.println("В Redis нет данных. Начинаем парсинг.");
            parser.parsing(); // Вызов метода парсинга, чтобы сохранить данные в БД
            return fetchAndCacheMatches();
        }
    }

    private Map<String, String> fetchAndCacheMatches() {
        List<Match> matchList = matchRepository.findAll(); // Получение всех матчей из БД

        if (matchList.isEmpty()) {
            return Map.of(); // Возвращаем пустую карту вместо LinkedHashMap
        }

        // Сохраняем в кэш
        redisTemplate.opsForValue().set(MATCH_CACHE_KEY, matchList, CACHE_EXPIRATION, TimeUnit.MINUTES);
        System.out.println("Сохранилось в Redis");

        return convertToMap(matchList); // Возвращаем карту матчей
    }

    private Map<String, String> convertToMap(List<Match> matches) {
        return matches.stream()
                .collect(Collectors.toMap(
                        Match::getMatchTitle,
                        Match::getMatchLink));
    }

    public void save(Match match) {
        matchRepository.save(match);
    }

    public Optional<Match> findByMatchTitleAndMatchLink(String matchTitle, String matchLink) {
        return matchRepository.findByMatchTitleAndMatchLink(matchTitle, matchLink);
    }
}
