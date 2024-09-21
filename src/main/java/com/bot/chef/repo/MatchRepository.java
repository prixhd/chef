package com.bot.chef.repo;

import com.bot.chef.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByMatchTitleAndMatchLink(String matchTitle, String matchLink);
}
