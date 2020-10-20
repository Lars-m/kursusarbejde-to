package facades;

import dtos.PersonDTO;
import entities.Address;
import entities.CityInfo;
import entities.Person;
import errorhandling.API_Exception;
import java.util.List;
import utils.EMF_Creator;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static dtos.PersonDTO.SIMPLE;
import static dtos.PersonDTO.ADDRESS;
import static dtos.PersonDTO.PHONES;
import dtos.PhoneDTO;
import entities.Phone;
import java.util.Arrays;
import testutils.TestBase;
import testutils.TestUtils;

//Uncomment the line below, to temporarily disable this test
//@Disabled
public class PersonFacadeTest extends TestBase {

//    private static EntityManagerFactory emf;
//    private static PersonFacade facade;
//    private static Person p1;
//    private static Person p2;
//    private static Address a1;
//    private static Address a2;
//
//    //These two instances are set up before all tests, and therefore be used by all tests
//    private static CityInfo cityInfo2000;
//    private static CityInfo cityInfo2800;

    public PersonFacadeTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = PersonFacade.getInstance(emf);
        List<CityInfo> cityInfoes =TestUtils.makeCityCodesInDB(emf);
        cityInfo2800 = cityInfoes.get(0);
        cityInfo2000 = cityInfoes.get(1);
    }

    @AfterAll
    public static void tearDownClass() {
//        Clean up database after test is done or use a persistence unit with drop-and-create to start up clean on every test
    }

    @BeforeEach
    public void setUp() {
       List<Object> persons = TestUtils.makeTestPersonsInDB(cityInfo2800, cityInfo2000, emf);
       p1 = (Person) persons.get(0);
       p2 = (Person) persons.get(1);
       a1 = (Address) persons.get(2);
       a2 = (Address) persons.get(3);
    }

    @AfterEach
    public void tearDown() {
//        Remove any data after each test was run
    }

    @Test
    public void tesPersonCount() {
        assertEquals(2, facade.getPersonCount(), "Expects two rows in the database");
    }

    @Test
    public void testGetAllPersons() throws API_Exception {
        assertEquals(2, facade.getAllPersons().size(), "Expects two rows in the database");
    }

    private void assertSIMPLE_VALUES(PersonDTO p) {
        Assertions.assertNotNull(p.getFirstName());
        Assertions.assertNotNull(p.getLastName());
        Assertions.assertNotNull(p.getEmail());
        Assertions.assertNotNull(p.getId());
    }

    private void assertADDRESS_VALUES(PersonDTO p) {
        Assertions.assertNotNull(p.getStreet());
        Assertions.assertNotNull(p.getAdditionalInfo());
        Assertions.assertNotNull(p.getZip());
    }

    private void assertADDRESS_NOT_EXPECTED(PersonDTO p) {
        Assertions.assertNull(p.getStreet());
        Assertions.assertNull(p.getZip());
    }

    private void assertPHONE_VALUES(PersonDTO p) {
        Assertions.assertNotNull(p.getPhones());
    }

    private void assertPHONE_NOT_EXPECTED(PersonDTO p) {
        Assertions.assertNull(p.getPhones());
    }

    @Test
    public void testAddPerson() throws API_Exception {
        PersonDTO p = new PersonDTO("aaa", "bbb", "a@b.dk");
        p.setStreet("Lyngbyvej 100");
        p.setZip("2800");
        p.setPhones(Arrays.asList(new PhoneDTO("1111", "x1111")));
        PersonDTO newPerson = facade.addPerson(p);
        Assertions.assertNotNull(newPerson.getId(), "Expected an id for the new person");
        assertEquals(3, facade.getAllPersons().size(), "Expects three rows in the database");
    }

    @Test
    public void testAddPersonVerifyExistingAddressIsUsed() throws API_Exception {
        PersonDTO p = new PersonDTO("aaa", "bbb", "a@b.dk");
        p.setStreet(a1.getStreet());
        p.setZip(a1.getCityInfo().getZipCode());
        PersonDTO newPerson = facade.addPerson(p, SIMPLE | ADDRESS);
        assertEquals(a1.getStreet(), newPerson.getStreet(), "Shoul have a street, using an existing address");
        assertEquals(a1.getCityInfo().getZipCode(), newPerson.getZip(), "Shoul have a zip-codet, using an existing address");
        EntityManager em = emf.createEntityManager();
        try {
            long addressCount = (long) em.createQuery("SELECT COUNT(a) FROM Address a").getSingleResult();
            assertEquals(2, addressCount, "No new address should be added");
        } finally {
            em.close();
        }
    }

    @Test
    public void testAddPersonMissingArguments() {
        PersonDTO p = new PersonDTO();
        p.setFirstName("aaa");
        p.setLastName("bbb");
        //misses email
        Assertions.assertThrows(API_Exception.class, () -> facade.addPerson(p));
    }

    @Test
    public void testGetAllPersonsDefaultReturnValue() throws API_Exception {
        List<PersonDTO> personDTOs = facade.getAllPersons();
        PersonDTO first = personDTOs.get(0);
        assertADDRESS_VALUES(first);
        assertSIMPLE_VALUES(first);
        assertPHONE_NOT_EXPECTED(first);
    }

    @Test
    public void testGetAllPersonsSIMPLE() throws API_Exception {
        List<PersonDTO> personDTOs = facade.getAllPersons(SIMPLE);
        PersonDTO first = personDTOs.get(0);
        assertSIMPLE_VALUES(first);
        assertADDRESS_NOT_EXPECTED(first);
    }

    @Test
    public void testGetAllPersonsSIMPLE_ADDRESS_PHONE() throws API_Exception {
        List<PersonDTO> personDTOs = facade.getAllPersons(SIMPLE | ADDRESS | PHONES);
        PersonDTO first = personDTOs.get(0);
        assertADDRESS_VALUES(first);
        assertSIMPLE_VALUES(first);
        assertPHONE_VALUES(first);
    }

}
