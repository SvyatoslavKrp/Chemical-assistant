package org.painting.chembot.chemistalt_bot.repository;

import org.painting.chembot.chemistalt_bot.domain.Instruction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructionRepository extends JpaRepository<Instruction, Long> {
}
