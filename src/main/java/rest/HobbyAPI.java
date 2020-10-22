package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.HobbyDTO;
import errorhandling.API_Exception;
import utils.EMF_Creator;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import static dtos.HobbyDTO.ALL;
import static dtos.HobbyDTO.NAME_ONLY;
import facades.HobbyFacade;
import facades.PersonFacade;
import java.util.List;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import static utils.StringUtil.isBlank;

@Path("hobby")
public class HobbyAPI {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();
       
    private static final HobbyFacade FACADE =  HobbyFacade.getInstance(EMF);
    private static final PersonFacade PERSON_FACADE =  PersonFacade.getInstance(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
            
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String demo() {
        return "{\"msg\":\"API Server is running\"}";
    }
    @Path("count")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getPersonCount() {
        long count = FACADE.getHobbyCount();
        return "{\"count\":"+count+"}";  //Done manually so no need for a DTO
    }
    
    @Path("all")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllHobbies( @QueryParam("include") String include) throws API_Exception {
        int toInclude = NAME_ONLY;
        if(!isBlank(include)){
            toInclude = handleInclude(include, toInclude);
        }
        List<HobbyDTO> personDTOs = FACADE.getAllHobbies(toInclude);
        return GSON.toJson(personDTOs);
    }
    
    @Path("add-hobby-to-person/{person-id}/{hobby-id}")
    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    public String addHobbyToPerson(@PathParam("person-id") int personId,@PathParam("hobby-id") String hobbyKey, @QueryParam("include") String include) throws API_Exception {
        int toInclude = NAME_ONLY;
        if(!isBlank(include)){
            toInclude = handleInclude(include, toInclude);
        }
        List<HobbyDTO> hobbyDTOs = PERSON_FACADE.addHobbyToPerson(personId, hobbyKey, toInclude);
        return GSON.toJson(hobbyDTOs);
    }
    
    @Path("remove-hobby-from-person/{person-id}/{hobby-id}")
    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    public String removeHobbyFromPerson(@PathParam("person-id") int personId,@PathParam("hobby-id") String hobbyKey, @QueryParam("include") String include) throws API_Exception {
        int toInclude = NAME_ONLY;
        if(!isBlank(include)){
            toInclude = handleInclude(include, toInclude);
        }
        List<HobbyDTO> hobbyDTOs = PERSON_FACADE.removeHobbyFromPerson(personId, hobbyKey, toInclude);
        return GSON.toJson(hobbyDTOs);
    }
    
    @Path("starts-with/{str}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getHobbiesThatStartsWith(@PathParam("str") String str, @QueryParam("include") String include) throws API_Exception {
        int toInclude = (include!= null && include.equals("all"))? ALL :NAME_ONLY;       
        List<HobbyDTO> personDTOs = FACADE.getHobbiesThatStartsWith(str,toInclude);
        return GSON.toJson(personDTOs);
    }
    
    private int handleInclude(String include, int toInclude) {
        String[] values = include.split("_");
        toInclude = 0;
        for(String val : values){
            if(val.equals("name")){
                toInclude = toInclude | NAME_ONLY;
            }
            if(val.equals("all")){
                toInclude = toInclude | ALL;
            }
        }
        return toInclude;
    }
}
