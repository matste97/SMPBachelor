package no.ntnu.SMPBachelor.util;

import no.ntnu.SMPBachelor.models.*;
import no.ntnu.SMPBachelor.repositories.RoleRepository;
import no.ntnu.SMPBachelor.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DummyDataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private static final Logger logger = LoggerFactory.getLogger(DummyDataInitializer.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        makeUsers();
    }

    private String createHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private void makeUsers(){
        Optional<User> rootUserExist = userRepository.findByUsername("admin@smp.no");
        if(rootUserExist.isEmpty()){
            String pass = System.getenv("SMPBACHELOR_PASS");
            logger.info("Importing user data");
            User root = new User("admin@smp.no", createHash(pass));

            Role user = new Role("ROLE_USER");
            Role admin = new Role("ROLE_ADMIN");
            roleRepository.save(user);
            roleRepository.save(admin);

            root.addRole(user);
            root.addRole(admin);

            userRepository.save(root);
            logger.info("User data finished importing");
        }
        else {
            logger.info("User data already in database, no need to import");
        }
    }
}
