package People;

public class Email {
    private String email;

    public Email(String email) throws PersonException {
        if(!email.contains("@")) throw new PersonException("Email format is wrong!!\nCharacter @ doesn't exists");
        if(!email.contains(".")) throw new PersonException("Email format is wrong!!\nCharacter . doesn't exists");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
