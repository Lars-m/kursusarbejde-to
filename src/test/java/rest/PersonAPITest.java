package rest;

import dtos.PersonDTO;
import entities.Address;
import entities.CityInfo;
import entities.Person;
import utils.EMF_Creator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.parsing.Parser;
import java.net.URI;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import testutils.TestBase;
import testutils.TestUtils;


//@Disabled
public class PersonAPITest extends TestBase {

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    private static PersonDTO r1, r2;

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        List<CityInfo> cityInfoes =TestUtils.makeCityCodesInDB(emf);
        cityInfo2800 = cityInfoes.get(0);
        cityInfo2000 = cityInfoes.get(1);

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer() {
        //System.in.read();
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
    }

    @BeforeEach
    public void setUp() {
      
        List<Object> persons = TestUtils.makeTestPersonsInDB(cityInfo2800, cityInfo2000, emf);
      
        p1 = (Person) persons.get(0);
        p2 = (Person) persons.get(1);
        a1 = (Address) persons.get(2);
        a2 = (Address) persons.get(3);
    }

    @Test
    public void testServerIsUp() {
        given().when().get("/person").then().statusCode(200);
    }

    //This test assumes the database contains two rows
    @Test
    public void testDummyMsg() throws Exception {
        given()
                .contentType("application/json")
                .get("/person/").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("msg", equalTo("API Server is running"));
    }
    
    @Test
    public void testGetAllUsers() {
        given()
                .contentType("application/json")
                .get("/person/").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("msg", equalTo("API Server is running"));
    }

    @Test
    public void testPersonCount() throws Exception {
        given()
                .contentType("application/json")
                .get("/person/count").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("count", equalTo(2));
    }
    
    @Test
    public void testGetAllPersonsDefault() throws Exception {
        //https://stackoverflow.com/questions/13803316/access-elements-of-an-anonymous-array-via-jsonpath-in-restassured
        given()
                .contentType("application/json")
                .get("/person/all").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstName",hasItems("Kurt","Janne"))
                .body("email",hasItems("a@b.dk","j@b.dk"))
                .body("lastName", hasItems("Wonnegut"));     
    }
    
    @Test
    public void testGetAllPersonsDefaultOnlyExpectedValues() throws Exception {
        List<PersonDTO> pDTOs = 
        given()
                .contentType("application/json")
                .get("/person/all").then()
                .extract().body().jsonPath().getList(".",PersonDTO.class);
        PersonDTO first = pDTOs.get(0);
        TestUtils.assertSIMPLE_VALUES(first);
        TestUtils.assertADDRESS_NOT_EXPECTED(first);
        TestUtils.assertPHONE_NOT_EXPECTED(first);
    }
    
    @Test
    public void testGetAllPersonsGivenResponseValues() throws Exception {
        List<PersonDTO> pDTOs = 
        given()
                .queryParam("include","simple_address_phones")
                .contentType("application/json")
                .get("/person/all")
                .then()
                .extract().body().jsonPath().getList(".",PersonDTO.class);
        PersonDTO first = pDTOs.get(0);
        TestUtils.assertSIMPLE_VALUES(first);
        TestUtils.assertADDRESS_VALUES(first);
        TestUtils.assertPHONE_VALUES(first);
    }
}
