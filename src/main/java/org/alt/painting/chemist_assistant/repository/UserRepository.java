package org.alt.painting.chemist_assistant.repository;

import org.alt.painting.chemist_assistant.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {


}
