package com.bot.chef.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "users")
@Data
@Getter
@Setter
public class User implements Serializable {

    @Id
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "first_name", nullable = true)
    private String firstName;

    @Column(name = "last_name", nullable = true)
    private String lastName;

    @Column(name = "user_name", nullable = true)
    private String userName;

    @Column(name = "registered_at", nullable = true)
    private Timestamp registeredAt;

    @Override
    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
