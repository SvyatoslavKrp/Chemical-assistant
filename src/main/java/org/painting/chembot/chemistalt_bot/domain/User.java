package org.painting.chembot.chemistalt_bot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id")
    private Long chatId;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "username")
    private String username;
    @Column(name = "registered_at")
    private Timestamp registeredAt;
    @OneToMany(mappedBy = "user")
    private List<Announcement> userAnnouncements;
    @OneToMany(mappedBy = "user")
    private List<Reminder> userReminders;
}
