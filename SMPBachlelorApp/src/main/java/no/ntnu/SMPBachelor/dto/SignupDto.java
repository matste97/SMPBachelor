package no.ntnu.SMPBachelor.dto;



/**
 * Data transfer object (DTO) for data from the sign-up form
 */
public class SignupDto {
    private final String username;
    private final String password;
    private final String repeat;

    public SignupDto(String username, String password, String repeat) {
        this.username = username;
        this.password = password;
        this.repeat = repeat;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRepeat() {
        return repeat;
    }
}