package no.ntnu.SMPBachelor.repositories;

import no.ntnu.SMPBachelor.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findOneByName(String name);
}
