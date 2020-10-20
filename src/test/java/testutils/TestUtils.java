package testutils;

import dtos.PersonDTO;
import dtos.PhoneDTO;
import entities.Address;
import entities.CityInfo;
import entities.Person;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Assertions;

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

            em.getTransaction().begin();
            em.createNamedQuery("Phone.deleteAllRows").executeUpdate();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.createNamedQuery("Address.deleteAllRows").executeUpdate();

            em.persist(p1);
            em.persist(p2);
            em.getTransaction().commit();
            return Arrays.asList(p1, p2,a1,a2);
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
