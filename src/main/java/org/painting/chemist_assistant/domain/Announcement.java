package org.painting.chemist_assistant.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "announcements")
public class Announcement {

    public Announcement(String announcement, User user, LocalDate date) {
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
    private LocalDate date;

    @ManyToOne
    private User user;

}
