package org.painting.chembot.chemistalt_bot.repository;

import org.painting.chembot.chemistalt_bot.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAnnouncementByUserChatId(Long userChatId);

}
