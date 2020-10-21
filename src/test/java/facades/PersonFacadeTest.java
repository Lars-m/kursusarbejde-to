package facades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import static dtos.PersonDTO.ALL;

import dtos.PhoneDTO;
import entities.Phone;
import java.util.Arrays;
import testutils.TestBase;
import testutils.TestUtils;

//Uncomment the line below, to temporarily disable this test
//@Disabled
public class PersonFacadeTest extends TestBase {

    private static Gson gson = new Gson();
    public PersonFacadeTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = PersonFacade.getInstance(emf);
        List<CityInfo> cityInfoes = TestUtils.makeCityCodesInDB(emf);
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

    @Test
    public void testGetPerson() throws API_Exception {
        PersonDTO pDTO = facade.getPerson(p1.getId(), SIMPLE);
        assertEquals("Kurt", pDTO.getFirstName(), "Expects to find Kurt Wonnegut");
         TestUtils.assertADDRESS_NOT_EXPECTED(pDTO);
         TestUtils.assertPHONE_NOT_EXPECTED(pDTO);
    }
    @Test
    public void testGetPersonWithAll() throws API_Exception {
        PersonDTO pDTO = facade.getPerson(p1.getId(), SIMPLE|ADDRESS|PHONES);
        assertEquals("Kurt", pDTO.getFirstName(), "Expects to find Kurt Wonnegut");
         TestUtils.assertADDRESS_VALUES(pDTO);
         TestUtils.assertPHONE_VALUES(pDTO);
    }
    
    @Test
    public void getPersonNotExisting() {
        Assertions.assertThrows(API_Exception.class, () -> facade.getPerson(73846578, SIMPLE));
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
    public void testFindPersonsInCity(){
        List<PersonDTO> pDTOs = facade.findPersonsInCity("2800", SIMPLE|ADDRESS);
        assertEquals(pDTOs.size(), 1,"Expected to find one Person in Lyngby");
        assertEquals(pDTOs.get(0).getZip(), "2800","Expected to find a person from Lyngby");
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
        TestUtils.assertADDRESS_VALUES(first);
        TestUtils.assertSIMPLE_VALUES(first);
        TestUtils.assertPHONE_NOT_EXPECTED(first);
    }

    @Test
    public void testGetAllPersonsSIMPLE() throws API_Exception {
        List<PersonDTO> personDTOs = facade.getAllPersons(SIMPLE);
        PersonDTO first = personDTOs.get(0);
        TestUtils.assertSIMPLE_VALUES(first);
        TestUtils.assertADDRESS_NOT_EXPECTED(first);
    }

    @Test
    public void testGetAllPersonsSIMPLE_ADDRESS_PHONE() throws API_Exception {
        List<PersonDTO> personDTOs = facade.getAllPersons(SIMPLE | ADDRESS | PHONES);
        PersonDTO first = personDTOs.get(0);
        TestUtils.assertADDRESS_VALUES(first);
        TestUtils.assertSIMPLE_VALUES(first);
        TestUtils.assertPHONE_VALUES(first);
    }
    
    @Test
    public void testEditPersonNewLastnameEmail() throws API_Exception {
        Person p1Clone = new Person(p1.getFirstName(),"Hansen","new@email.com",null);
        p1Clone.setId(p1.getId());
        p1Clone.setAddress(new Address(p1.getAddress().getStreet(),p1.getAddress().getAdditionalInfo(),cityInfo2800));
        p1Clone.setPhonesFromDTOs(PhoneDTO.makePhoneDTO_List(p1.getPhones()));
        PersonDTO pDTO = new PersonDTO(p1Clone, ALL);
        PersonDTO editedPerson = facade.editPerson(pDTO);
        assertEquals(editedPerson.getLastName(),"Hansen", "Expected name to be changed to Hansen");
        assertEquals(editedPerson.getEmail(),"new@email.com", "Expected email to be changed to new@email.com");
        assertEquals(2, facade.getAllPersons().size(), "Expects two rows in the database");
         
        EntityManager em = emf.createEntityManager();
        try {
            long addressCount = (long) em.createQuery("SELECT COUNT(a) FROM Address a").getSingleResult();
            assertEquals(2, addressCount, "No new address should be added");
        } finally {
            em.close();
        }
    }
     @Test
    public void testEditPersonRemovePhone() throws API_Exception {
         System.out.println("TESTING");
        final String NUMBER_TO_REMOVE = "123";
        Person p1Clone = new Person(p1.getFirstName(), p1.getLastName(),p1.getEmail(), a1);
        p1Clone.setId(p1.getId());
        p1Clone.setAddress(new Address(p1.getAddress().getStreet(),p1.getAddress().getAdditionalInfo(),cityInfo2800));
        List<PhoneDTO> phoneDTOs = PhoneDTO.makePhoneDTO_List(p1.getPhones());
        phoneDTOs.remove(TestUtils.findPhoneDTO(phoneDTOs, NUMBER_TO_REMOVE)); //Remove a phonenumber
        p1Clone.setPhonesFromDTOs(phoneDTOs);
        
        PersonDTO pDTO = new PersonDTO(p1Clone, ALL);
        PersonDTO editedPerson = facade.editPerson(pDTO,ALL);
        assertEquals(editedPerson.getPhones().size(),1,"Expected only one phone number since 111 was removed");
        EntityManager em = emf.createEntityManager();
        try {
          em.createQuery("select p from Phone p where p.number = :number", Phone.class).setParameter("number",NUMBER_TO_REMOVE).getSingleResult();
          throw new AssertionError("Should not find phone 123");
        } catch(Exception e){}
          finally {
            em.close();
        }
        
           
    }

}
