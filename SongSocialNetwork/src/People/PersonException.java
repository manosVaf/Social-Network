package People;

public class PersonException extends Exception {
    private String message;

    @Override
    public String getMessage() {
        return message;
    }

    public PersonException(String message) {
        this.message = message + "\nPerson couldn't created";
        System.err.println(message);
    }
}
