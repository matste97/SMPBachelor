package no.ntnu.SMPBachelor.repositories;

import no.ntnu.SMPBachelor.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    default List<User> findAllUsers() {
        return findAll();
    }
}

