package testutils;

import dtos.PersonDTO;
import static dtos.PersonDTO.ALL;
import dtos.PhoneDTO;
import entities.Address;
import entities.CityInfo;
import entities.Person;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static testutils.TestBase.cityInfo2800;
import static testutils.TestBase.emf;
import static testutils.TestBase.p1;

public class TestUtils {

    public static List<Object> makeTestPersonsInDB(CityInfo cityInfo2800, CityInfo cityInfo2000, EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            List<PhoneDTO> phones1 = Arrays.asList(new PhoneDTO("123", "x123"), new PhoneDTO("1234", "x1234"));
            List<PhoneDTO> phones2 = Arrays.asList(new PhoneDTO("111", "x111"));
            Address a1 = new Address("Lyngbyvej 23", "xxx", cityInfo2800);
            Person p1 = new Person("Kurt", "Wonnegut", "a@b.dk", a1);
            p1.setPhonesFromDTOs(phones1);
            Address a2 = new Address("Roskildevej 23", "xxx", cityInfo2000);
            Person p2 = new Person("Janne", "Wonnegut", "j@b.dk", a2);
            p2.setPhonesFromDTOs(phones2);
            
            Person personThatSharesAddressWithP1 = new Person("Signe","Wonnegut","signe@a.dk",a1);

            em.getTransaction().begin();
            em.createNamedQuery("Phone.deleteAllRows").executeUpdate();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.createNamedQuery("Address.deleteAllRows").executeUpdate();

            em.persist(p1);
            em.persist(p2);
            em.persist(personThatSharesAddressWithP1);
            em.getTransaction().commit();
            return Arrays.asList(p1, p2, personThatSharesAddressWithP1, a1,a2);
        } finally {
            em.close();
        }
    }

    public static List<CityInfo> makeCityCodesInDB(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Phone.deleteAllRows").executeUpdate();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.createNamedQuery("Address.deleteAllRows").executeUpdate();
            em.createQuery("DELETE FROM CityInfo").executeUpdate();
            em.getTransaction().commit();
//            em.flush();
            em.getTransaction().begin();
            CityInfo cityInfo2000 = new CityInfo("2000", "Frederiksberg");
            CityInfo cityInfo2800 = new CityInfo("2800", "Lyngby");
            em.persist(cityInfo2000);
            em.persist(cityInfo2800);
            em.getTransaction().commit();
            return Arrays.asList(cityInfo2800, cityInfo2000);

        } finally {
            em.close();
        }
    }
    
    public static PhoneDTO findPhoneDTO(List<PhoneDTO> phoneDTOs, String number) {
        return phoneDTOs.stream().filter(n -> number.equals(n.getNumber())).findFirst().orElse(null);
    }
    
    public static long numberOfAddresses(){
         EntityManager em = emf.createEntityManager();
        try {
            long addressCount = (long) em.createQuery("SELECT COUNT(a) FROM Address a").getSingleResult();
            return addressCount;
        } finally {
            em.close();
        }
    }
    
    public static PersonDTO makeJanFromLyngby(){
        PersonDTO p = new PersonDTO("Jan", "Olsen", "jan@b.dk");
        p.setStreet("Lyngbyvej 100");
        p.setZip("2800");
        p.setPhones(Arrays.asList(new PhoneDTO("1111", "x1111")));
        return p;
    }
    
    public static PersonDTO makeCloneWithNameChanges(Person p,String fName,String lName, String _email){
        String firstName = fName!= null ? fName : p.getFirstName();
        String lastName = lName != null ? lName : p.getLastName();
        String email = _email != null ? _email : p.getEmail();
        Person p1Clone = new Person(firstName,lastName,email, null);
        p1Clone.setId(p.getId());
        p1Clone.setAddress(new Address(p.getAddress().getStreet(), p.getAddress().getAdditionalInfo(), p.getAddress().getCityInfo()));
        p1Clone.setPhonesFromDTOs(PhoneDTO.makePhoneDTO_List(p.getPhones()));
        return new PersonDTO(p1Clone, ALL);
    }
    
    public static void assertSIMPLE_VALUES(PersonDTO p) {
        Assertions.assertNotNull(p.getFirstName());
        Assertions.assertNotNull(p.getLastName());
        Assertions.assertNotNull(p.getEmail());
        Assertions.assertNotNull(p.getId());
    }

    public static void assertADDRESS_VALUES(PersonDTO p) {
        Assertions.assertNotNull(p.getStreet());
        Assertions.assertNotNull(p.getAdditionalInfo());
        Assertions.assertNotNull(p.getZip());
    }

    public static void assertADDRESS_NOT_EXPECTED(PersonDTO p) {
        Assertions.assertNull(p.getStreet());
        Assertions.assertNull(p.getZip());
    }

    public static void assertPHONE_VALUES(PersonDTO p) {
        Assertions.assertNotNull(p.getPhones());
    }

    public static void assertPHONE_NOT_EXPECTED(PersonDTO p) {
        Assertions.assertNull(p.getPhones());
    }

}
