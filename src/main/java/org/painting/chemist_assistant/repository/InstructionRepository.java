package org.painting.chemist_assistant.repository;

import org.painting.chemist_assistant.domain.Instruction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructionRepository extends JpaRepository<Instruction, Long> {
}
