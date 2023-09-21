package org.painting.chembot.chemistalt_bot.repository;

import org.painting.chembot.chemistalt_bot.domain.Malfunction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MalfunctionRepository extends JpaRepository<Malfunction, Long> {
}
