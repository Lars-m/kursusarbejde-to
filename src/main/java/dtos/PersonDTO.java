package dtos;

import entities.Address;
import entities.Person;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Transient;

public class PersonDTO {

    @Transient
    public transient static int ID_ONLY = 1;
    @Transient
    public transient static int SIMPLE = 2;
    @Transient
    public transient static int ADDRESS = 4;
    @Transient
    public transient static int PHONES = 8;
    @Transient
    public transient static int HOBBIES_SIMPLE = 16;
    @Transient
    public transient static int HOBBIES_ALL = 32;

    private Integer id;
    private String email;
    private String firstName;
    private String lastName;
    private String street;
    private String additionalInfo;
    private String zip;
    private String city;
    private String phoneNumbersAsString;
    private String hobbiesAsString;
    private List<HobbyDTO> hobbies;
    private List<PhoneDTO> phones;

    public PersonDTO(){};
    public PersonDTO(Person _person, int personDetails) {
        if ((personDetails & ID_ONLY) == ID_ONLY) {
            this.id = _person.getId();
        }
        if ((personDetails & SIMPLE) == SIMPLE) {
            this.firstName = _person.getFirstName();
            this.lastName = _person.getLastName();
            this.email = _person.getEmail();
            this.id = _person.getId();
        }
        if ((personDetails & ADDRESS) == ADDRESS) {
            Address address = _person.getAddress();
            if(address !=null){
                this.street = address.getStreet();
                this.additionalInfo = address.getAdditionalInfo();
                this.zip = address.getCityInfo().getZipCode();
                this.city = address.getCityInfo().getCity();
            }
        }
        if ((personDetails & PHONES) == PHONES) {
           this.phones = PhoneDTO.makeHobbyDTO_List(_person.getPhones());
        }
        if ((personDetails & HOBBIES_SIMPLE) == HOBBIES_SIMPLE) {
          this.hobbiesAsString = _person.getHobbies().stream().map(n -> n.getName()).collect(Collectors.joining(","));
        }
        if ((personDetails & HOBBIES_ALL) == HOBBIES_ALL) {
          this.hobbies = HobbyDTO.makeHobbyDTO_List(_person.getHobbies());
        }
    }

    //Constructor with required values for add
    public PersonDTO( String firstName, String lastName,String email) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    
    
    /*public static List<PhoneDTO> makeHobbyDTO_List(List<Phone> phones) {
        List<PhoneDTO> phoneDTOs = new ArrayList<>();
        phones.forEach((phone) -> phoneDTOs.add(new PhoneDTO(phone)));
        return phoneDTOs;
    }*/
    public static List<PersonDTO> makePersonDTO_List(List<Person> persons,int whatToInclude){
        List<PersonDTO> personDTOs = new ArrayList();
        persons.forEach((person) -> personDTOs.add(new PersonDTO(person,whatToInclude)));
        return personDTOs;
    }

    public Integer getId() {
        return id;
    }

    public String getEmailNotNull() {
        return email != null ? email : "";
    }
    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }
    public String getFirstNameNotNull() {
        return firstName != null ? firstName : "";
    }

    public String getLastName() {
        return lastName;
    }

    public String getStreet() {
        return street;
    }

    public String getAdditionalInfo() {
        return additionalInfo != null ? additionalInfo: "" ;
    }

    public String getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public String phoneNumbersAsString() {
        return phoneNumbersAsString;
    }

    public String getHobbiesAsString() {
        return hobbiesAsString;
    }

    public List<HobbyDTO> getHobbies() {
        return hobbies;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setHobbies(List<HobbyDTO> hobbies) {
        this.hobbies = hobbies;
    }

    public List<PhoneDTO> getPhones() {
        return phones;
    }

    public void setPhones(List<PhoneDTO> phones) {
        this.phones = phones;
    }
    
    
    
    

}
