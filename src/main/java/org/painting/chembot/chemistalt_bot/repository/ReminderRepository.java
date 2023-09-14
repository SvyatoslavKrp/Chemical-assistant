package org.painting.chembot.chemistalt_bot.repository;

import org.painting.chembot.chemistalt_bot.domain.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
}
