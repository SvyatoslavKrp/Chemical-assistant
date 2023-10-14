package org.alt.painting.chemist_assistant.repository;

import org.alt.painting.chemist_assistant.domain.Malfunction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MalfunctionRepository extends JpaRepository<Malfunction, Long> {
}
