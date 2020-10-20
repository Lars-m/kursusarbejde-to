package facades;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.PersonDTO;
import dtos.PhoneDTO;
import entities.Address;
import entities.CityInfo;
import entities.Person;
import entities.Phone;
import errorhandling.API_Exception;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import static utils.StringUtil.isBlank;
import static dtos.PersonDTO.ID_ONLY;
import static dtos.PersonDTO.SIMPLE;
import static dtos.PersonDTO.ADDRESS;
import static dtos.PersonDTO.PHONES;
import java.util.ArrayList;

public class PersonFacade {

    private static PersonFacade instance;
    private static EntityManagerFactory emf;

    private PersonFacade() {
    }

    public static PersonFacade getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public List<PersonDTO> getAllPersons() throws API_Exception {
        return getAllPersons(SIMPLE | ADDRESS);
    }

    public List<PersonDTO> getAllPersons(int whatToInclude) throws API_Exception {
        List<PersonDTO> personDTOs = null;
        EntityManager em = getEntityManager();
        try {
            List<Person> all = em.createQuery("SELECT p from Person p", Person.class).getResultList();
            return PersonDTO.makePersonDTO_List(all, whatToInclude);
        } finally {
            em.close();
        }
    }
    
    public PersonDTO addPerson(PersonDTO p) throws API_Exception {
        return addPerson(p,SIMPLE);
    }

    public PersonDTO addPerson(PersonDTO p,int returnValuesToInclude) throws API_Exception {
        if ((isBlank(p.getFirstName()) || isBlank(p.getLastName()) || isBlank(p.getEmail()))) {
            throw new API_Exception(API_Exception.INPUTS_MISSING, 400);
        }

        EntityManager em = getEntityManager();
        try {
            Address address = null;
            if (!( isBlank(p.getStreet()) && isBlank(p.getZip()))) {
                CityInfo cityInfo = em.find(CityInfo.class, p.getZip());
                TypedQuery<Address> query = em.createQuery("SELECT a FROM Address a WHERE a.street = :street AND a.cityInfo = :cityInfo", Address.class);
                query.setParameter("street", p.getStreet());
                query.setParameter("cityInfo", cityInfo);
                try {
                    address = query.getSingleResult();
                } catch (NoResultException e) {
                }

                if (address == null) {
                    address = new Address(p.getStreet(), p.getAdditionalInfo(), cityInfo);
                }
            }
            Person person = new Person(p.getFirstName(), p.getLastName(), p.getEmail(), address);
            if (p.getPhones() != null && p.getPhones().size() > 0) {
                p.getPhones().forEach((pDTO) -> {
                    person.addPhone(new Phone(pDTO));
                });
            }
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();
            return new PersonDTO(person, returnValuesToInclude);
            //return new PersonDTO(person, SIMPLE);
        } finally {
            em.close();
        }
    }

    public long getPersonCount() {
        EntityManager em = emf.createEntityManager();
        try {
            long renameMeCount = (long) em.createQuery("SELECT COUNT(r) FROM Person r").getSingleResult();
            return renameMeCount;
        } finally {
            em.close();
        }
    }

    public static void main(String[] args) throws API_Exception {
        PersonFacade facade = PersonFacade.getInstance(utils.EMF_Creator.createEntityManagerFactory());
        PersonDTO p = new PersonDTO();
        p.setFirstName("Kurt");
        p.setLastName("Wonnegut");
        p.setEmail("a@b.dk");
        p.setStreet("Lyngbyvej 45");
        p.setZip("2800");

        List<PhoneDTO> phones = Arrays.asList(new PhoneDTO("333333", "private"), new PhoneDTO("666667", "work"));
        p.setPhones(phones);
        PersonDTO pDTO = facade.addPerson(p,SIMPLE|ADDRESS|PHONES);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(pDTO));
    }
}
