package dtos;

import entities.Phone;
import java.util.ArrayList;
import java.util.List;


public class PhoneDTO {
    private String number;
    private String description;

    public PhoneDTO(Phone phone) {
        this.number = phone.getNumber();
        this.description = phone.getDescription();
    }
    public PhoneDTO(String number, String des) {
        this.number = number;
        this.description = des;
    }
    
     public static List<PhoneDTO> makePhoneDTO_List(List<Phone> phones) {
        List<PhoneDTO> phoneDTOs = new ArrayList<>();
        phones.forEach((phone) -> phoneDTOs.add(new PhoneDTO(phone)));
        return phoneDTOs;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
     
}
