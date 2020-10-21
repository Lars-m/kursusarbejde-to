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

    public PersonDTO getPerson(int id, int whatToInclude) throws API_Exception {
        PersonDTO personDTO = null;
        EntityManager em = getEntityManager();
        try {
            Person person = em.find(Person.class, id);
            if (person == null) {
                throw new API_Exception(API_Exception.PERSON_NOT_FOUND, 404);
            }
            return new PersonDTO(person, whatToInclude);
        } finally {
            em.close();
        }
    }

    public PersonDTO addPerson(PersonDTO p) throws API_Exception {
        return addPerson(p, SIMPLE);
    }

    public PersonDTO editPerson(PersonDTO pDTO) throws API_Exception {
        return editPerson(pDTO, SIMPLE);
    }
    
    public List<PersonDTO> findPersonsInCity(String zip, int whatToInclude){
        EntityManager em = getEntityManager();
        try{
            TypedQuery<Person> query = em.createQuery("select p from Person p where p.address.cityInfo.zipCode = :zip", Person.class).setParameter("zip", zip);
            List<Person> persons = query.getResultList();
            return  PersonDTO.makePersonDTO_List(persons, whatToInclude);
        }finally {
            em.close();
        }
    }

    public PersonDTO editPerson(PersonDTO pDTO, int returnValuesToInclude) throws API_Exception {
        if ((isBlank(pDTO.getFirstName()) || isBlank(pDTO.getLastName()) || isBlank(pDTO.getEmail()))) {
            throw new API_Exception(API_Exception.INPUTS_MISSING, 400);
        }

        EntityManager em = getEntityManager();
        try {
            Person person = em.find(Person.class, pDTO.getId());
            if (person == null) {
                throw new API_Exception(API_Exception.PERSON_NOT_FOUND, 404);
            }

            person.setFirstName(pDTO.getFirstName());
            person.setLastName(pDTO.getLastName());
            person.setEmail(pDTO.getEmail());

            Address address = null;
            if (person.getAddress() == null) { // If Person did not previously have an address, make one
                if (!(isBlank(pDTO.getStreet()) && isBlank(pDTO.getZip()))) {
                    address = makeAddress(em, pDTO, address);
                    person.setAddress(address);
                }
            } else {
                if (!(isBlank(pDTO.getStreet()) && isBlank(pDTO.getZip()))) {
                    person.getAddress().setStreet(pDTO.getStreet());
                    person.getAddress().setAdditionalInfo(pDTO.getAdditionalInfo());
                    CityInfo cInfo = em.find(CityInfo.class, pDTO.getZip());
                    person.getAddress().setCityInfo(cInfo);
                }
            }

            // if (pDTO.getPhones() != null && pDTO.getPhones().size() > 0) {
            if (person.getPhones() == null || person.getPhones().isEmpty()) {
                person.setPhonesFromDTOs(pDTO.getPhones());
            } else {
                List<Phone> phonesToRemove = new ArrayList<>();
                person.getPhones().forEach(p -> {
                    PhoneDTO phoneDTO = findPhoneDTO(pDTO.getPhones(), p.getNumber());
                    if (phoneDTO == null) { //Must be a number we want to delete
                        phonesToRemove.add(p);
                        p.setPerson(null);
                    } else {
                        p.setDescription(phoneDTO.getDescription());
                    }
                });
                phonesToRemove.forEach(p -> {
                    person.getPhones().remove(p);
                });
            }
            em.getTransaction().begin();
            em.merge(person);
            em.getTransaction().commit();
            return new PersonDTO(person, returnValuesToInclude);
            //return new PersonDTO(person, SIMPLE);
        } finally {
            em.close();
        }
    }

    public boolean containsNumber(final List<PhoneDTO> list, final String number) {
        return list.stream().anyMatch(o -> o.getNumber().equals(number));
    }

    public static PhoneDTO findPhoneDTO(List<PhoneDTO> phoneDTOs, String number) {
        return phoneDTOs.stream().filter(n -> number.equals(n.getNumber())).findFirst().orElse(null);
    }

    public PersonDTO addPerson(PersonDTO p, int returnValuesToInclude) throws API_Exception {
        if ((isBlank(p.getFirstName()) || isBlank(p.getLastName()) || isBlank(p.getEmail()))) {
            throw new API_Exception(API_Exception.INPUTS_MISSING, 400);
        }

        EntityManager em = getEntityManager();
        try {
            Address address = null;
            if (!(isBlank(p.getStreet()) && isBlank(p.getZip()))) {
                address = makeAddress(em, p, address);
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

    private Address makeAddress(EntityManager em, PersonDTO p, Address address) {
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
        return address;
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
        PersonDTO pDTO = facade.addPerson(p, SIMPLE | ADDRESS | PHONES);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(pDTO));
    }
}
