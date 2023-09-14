package org.painting.chembot.chemistalt_bot.repository;

import org.painting.chembot.chemistalt_bot.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
