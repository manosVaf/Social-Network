package People;

public class Sex{
    private String sex;

    public Sex(String sex) throws PersonException{
        if(sex.equals("Male") || sex.equals("Female")) this.sex = sex;
        else throw new PersonException("Error on initializing sex");
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}
