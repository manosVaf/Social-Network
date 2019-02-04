package DataProcess;
import People.Person;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;


public class NeoHandler {
    private Driver driver;
    private Session session;
    private TextHandler textHandler;

    public NeoHandler() {
        textHandler = new TextHandler();
        initializeGraphNodes();
    }

    //TODO method for building the graph

    private void initializeGraphNodes() {
        initializeDatabase();
        int nodes = 0;
        for (Person p : textHandler.getPeople()) {
            StatementResult result = session.run(buildNeoCreateString(p));
            if (result.hasNext()) {
                nodes++;
            }
        }
        System.out.println(nodes + " nodes added successfully in Graph Database!");
        closeConnDatabase();
    }

    private void initializeDatabase(){
        driver = GraphDatabase.driver("bolt://localhost");
        session = driver.session();
    }

    private boolean closeConnDatabase(){
        if(session != null && driver != null){
            session.close();
            driver.close();
            return true;
        }else{
            System.err.println("Connection can't close!");
            return false;
        }
    }

    private String buildNeoCreateString(Person p) {
        return "create(person:Person{name:'" + p.getFirstname().concat("\n" + p.getLastname())
                + "',firstname:'" + p.getFirstname()
                + "',lastname:'" + p.getLastname()
                + "',username:'" + p.getUsername()
                + "',password:'" + p.getPassword()
                + "',email:'" + p.getEmail().getEmail()
                + "',sex:'" + p.getSex().getSex() + "'}) return person";
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
