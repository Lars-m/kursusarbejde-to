package entities;

import dtos.PhoneDTO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

/**
 *
 * @author Plaul
 */
@Entity
@NamedQuery(name = "Person.deleteAllRows", query = "DELETE from Person")
public class Person implements Serializable {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Address address;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String email;
    private String firstName;
    private String lastName;

    public Person(String firstName, String lastName,String email,Address address) {
        this.address = address;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Person() {
    }
    

    
    
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
    
    @ManyToMany
    private final Set<Hobby> hobbies = new HashSet();
    
    @OneToMany(mappedBy = "person", cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    private List<Phone> phones = new ArrayList();

    public List<Hobby> getHobbies() {
        return new ArrayList<>(hobbies);
    }
    
    public List<Phone> getPhones() {
        return phones;
    }
    
    public void setPhonesFromDTOs(List<PhoneDTO> phoneDTOs){
        phones = new ArrayList<>();
        phoneDTOs.stream().forEach((p)-> 
        {
            Phone phone = new Phone(p.getNumber(),p.getDescription());
            phone.setPerson(this);
            phones.add(phone);  
        });
    }
       
    public String getPhonesAsString(){
        return phones.stream().map(n -> n.getNumber() + "("+n.getDescription()+")").collect(Collectors.joining(","));
    }
    
    public void addHobby(Hobby hobby){
        hobbies.add(hobby);
        hobby.addPerson(this); 
    }
    
    public void addPhone(Phone p){
        phones.add(p);
        p.setPerson(this);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

  
    
}
