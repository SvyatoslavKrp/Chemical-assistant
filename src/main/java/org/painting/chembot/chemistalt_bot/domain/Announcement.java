package org.painting.chembot.chemistalt_bot.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "announcements")
public class Announcement {

    public Announcement(String announcement, User user, Timestamp date) {
        this.announcement = announcement;
        this.user = user;
        this.date = date;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "announcement")
    private String announcement;

    @Column(name = "date")
    private Timestamp date;

    @ManyToOne
    private User user;

}
