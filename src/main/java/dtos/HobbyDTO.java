package dtos;

import entities.Hobby;
import java.util.ArrayList;
import java.util.List;

public class HobbyDTO {

    private String name;
    private String wikiLink;
    private String category;
    private String type;

    public HobbyDTO(Hobby hobby) {
        this.name = hobby.getName();
        this.wikiLink = hobby.getWikiLink();
        this.category = hobby.getCategory();
        this.type = hobby.getType();
    }

    public static List<HobbyDTO> makeHobbyDTO_List(List<Hobby> hobbies) {
        List<HobbyDTO> hobbyDTOs = new ArrayList<>();
        hobbies.forEach((hobby) -> hobbyDTOs.add(new HobbyDTO(hobby)));
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
