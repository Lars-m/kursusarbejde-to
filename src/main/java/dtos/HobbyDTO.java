package dtos;

import static dtos.PersonDTO.ALL;
import static dtos.PersonDTO.ID_ONLY;
import entities.Hobby;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Transient;

public class HobbyDTO {

    @Transient
    public transient static int NAME_ONLY = 1;
    @Transient
    public transient static int ALL = 2;

    private String name;
    private String wikiLink;
    private String category;
    private String type;

    public HobbyDTO(Hobby hobby, int details) {
        this.name = hobby.getName();
        if (((details & ALL) == ALL)) {
            this.wikiLink = hobby.getWikiLink();
            this.category = hobby.getCategory();
            this.type = hobby.getType();
        }
    }

    public static List<HobbyDTO> makeHobbyDTO_List(List<Hobby> hobbies,int details) {
        List<HobbyDTO> hobbyDTOs = new ArrayList<>();
        hobbies.forEach((hobby) -> hobbyDTOs.add(new HobbyDTO(hobby,details)));
        return hobbyDTOs;
    }

    public String getName() {
        return name;
    }

    public String getWikiLink() {
        return wikiLink;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

}
