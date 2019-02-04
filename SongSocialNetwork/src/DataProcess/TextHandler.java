package DataProcess;
import People.Email;
import People.Person;
import People.PersonException;
import People.Sex;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TextHandler {
    private ArrayList<Person> people;
    private HashMap<Person, ArrayList<Person>> relations;

    public TextHandler() {
        people = new ArrayList<>();
        relations = new HashMap<>();
        readInfo();
    }

    //   TODO create method for reading relations!!!

    private void readInfo(){
        Scanner scanner = null;
        try{
            scanner = new Scanner(new FileReader("Sources/info.txt"));
            scanner.useDelimiter(",");
            while(scanner.hasNext()){
                Person p = new Person();
                p.setFirstname(scanner.next());
                p.setLastname(scanner.next());
                p.setUsername(scanner.next());
                p.setPassword(scanner.next());
                p.setAge(scanner.nextInt());
                p.setEmail(new Email(scanner.next()));
                p.setSex(new Sex(scanner.next()));
                people.add(p);
            }
        }catch(IOException e){
            System.err.println("Error on reading the Info file");
        }catch(PersonException e){
        }finally {
            if(scanner != null){
                scanner.close();
            }
        }
    }

    public ArrayList<Person> getPeople() {
        return people;
    }

    public HashMap<Person, ArrayList<Person>> getRelations() {
        return relations;
    }
}
