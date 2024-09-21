package com.bot.chef.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Entity
@Table(name = "matches")
@Data
@Getter
@Setter
public class Match implements Serializable {

    @Id
    @Column(name = "match_title", nullable = false)
    private String matchTitle;

    @Column(name = "match_link", nullable = false)
    private String matchLink;

    @Override
    public String toString() {
        return "Match{" +
                ", matchTitle='" + matchTitle + '\'' +
                ", matchLink='" + matchLink + '\'' +
                '}';
    }
}
