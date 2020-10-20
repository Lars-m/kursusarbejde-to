/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import javax.persistence.Column;

@Entity
public class Hobby implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(length = 50)
    private String name;
    
    private String wikiLink;
    private String category;
    private String type;


    @ManyToMany(mappedBy = "hobbies")
    private Set<Person> persons = new HashSet<>();

    
    public List<Person> getPersons() {
        return new ArrayList<>(persons);
    }
    
    public void addPerson(Person p){
        persons.add(p);
    }
    public String getWikiLink() {
        return wikiLink;
    }

    public void setWikiLink(String wikiLink) {
        this.wikiLink = wikiLink;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   
    
}
