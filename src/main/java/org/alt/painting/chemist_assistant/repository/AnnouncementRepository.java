package org.alt.painting.chemist_assistant.repository;

import org.alt.painting.chemist_assistant.domain.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAnnouncementByUserChatId(Long userChatId);

}
