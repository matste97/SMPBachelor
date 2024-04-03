package no.ntnu.SMPBachelor.repositories;

import no.ntnu.SMPBachelor.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

