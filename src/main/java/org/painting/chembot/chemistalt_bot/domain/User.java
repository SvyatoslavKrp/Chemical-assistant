package org.painting.chembot.chemistalt_bot.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id")
    private Long chatId;
    @Column(name = "firstName")
    private String firstName;
    @Column(name = "lastName")
    private String lastName;
    @Column(name = "username")
    private String username;
    @Column(name = "registeredAt")
    private Timestamp registeredAt;

}
