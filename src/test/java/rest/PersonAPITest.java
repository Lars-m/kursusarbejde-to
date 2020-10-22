package rest;

import dtos.PersonDTO;
import dtos.PhoneDTO;
import entities.Address;
import entities.CityInfo;
import entities.Person;
import utils.EMF_Creator;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
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
import org.hamcrest.Matchers;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
        List<CityInfo> cityInfoes = TestUtils.makeCityCodesInDB(emf);
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
        personThatSharesAddressWithP1 = (Person) persons.get(2);
        a1 = (Address) persons.get(3);
        a2 = (Address) persons.get(4);
    }

    @Test
    public void testServerIsUp() {
        given().when().get("/person").then().statusCode(200);
    }

    //This test assumes the database contains two rows
    @Test
    public void testServerStatus() throws Exception {
        given()
                .contentType("application/json")
                .get("/person/").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("msg", equalTo("API Server is running"));
    }
    
    @Test
    public void testAddPerson() {
         given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(TestUtils.makeJanFromLyngby())
                .when()
                .post("/person")
                .then()
                .body("firstName", equalTo("Jan"))
                .body("lastName", equalTo("Olsen"))
                .body("$", hasKey("id"));
    }
    
    @Test
    public void testEditPerson() throws Exception {
         given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(TestUtils.makeCloneWithNameChanges(p1, null, "Hansen", "new@email.com"))
                .when()
                .put("/person")
                .then()
                .body("firstName", equalTo("Kurt"))
                .body("lastName", equalTo("Hansen"))
                .body("email", equalTo("new@email.com"))
                .body("id", equalTo(p1.getId()));
         
         testPersonCount(); //Number of persons should be unchanged
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
                .body("count", equalTo(3));
    }

    @Test
    public void testGetAllPersonsDefault() throws Exception {
        //https://stackoverflow.com/questions/13803316/access-elements-of-an-anonymous-array-via-jsonpath-in-restassured
        given()
                .contentType("application/json")
                .get("/person/all").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstName", hasItems("Kurt", "Janne"))
                .body("email", hasItems("a@b.dk", "j@b.dk"))
                .body("lastName", hasItems("Wonnegut"));
    }

    @Test
    public void testGetAllPersonsDefaultOnlyExpectedValues() throws Exception {
        List<PersonDTO> pDTOs
                = given()
                        .contentType("application/json")
                        .get("/person/all").then()
                        .extract().body().jsonPath().getList(".", PersonDTO.class);
        PersonDTO first = pDTOs.get(0);
        TestUtils.assertSIMPLE_VALUES(first);
        TestUtils.assertADDRESS_NOT_EXPECTED(first);
        TestUtils.assertPHONE_NOT_EXPECTED(first);
    }

    @Test
    public void testGetAllPersonsGivenResponseValues() throws Exception {
        List<PersonDTO> pDTOs
                = given()
                        .queryParam("include", "simple_address_phones")
                        .contentType("application/json")
                        .get("/person/all")
                        .then()
                        .extract().body().jsonPath().getList(".", PersonDTO.class);
        PersonDTO first = pDTOs.get(0);
        TestUtils.assertSIMPLE_VALUES(first);
        TestUtils.assertADDRESS_VALUES(first);
        TestUtils.assertPHONE_VALUES(first);
    }

    @Test
    public void testGetAllInCity() throws Exception {

        given()
                .queryParam("include", "simple_address")
                .contentType("application/json")
                .get("/person/all-in-city/2800").then()
                .body("firstName", hasItems("Kurt", "Signe"))
                .body("email", hasItems("a@b.dk", "signe@a.dk"))
                .body("lastName", hasItems("Wonnegut"))
                .body(".",hasSize(2));
    }
}
