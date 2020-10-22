package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dtos.PersonDTO;
import errorhandling.API_Exception;
import utils.EMF_Creator;
import facades.PersonFacade;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import static dtos.PersonDTO.SIMPLE;
import static dtos.PersonDTO.ADDRESS;
import static dtos.PersonDTO.PHONES;
import static dtos.PersonDTO.HOBBIES_SIMPLE;
import static dtos.PersonDTO.ALL;

import java.util.List;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import static utils.StringUtil.isBlank;

@Path("person")
public class PersonAPI {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();
       
    private static final PersonFacade FACADE =  PersonFacade.getInstance(EMF);
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
        long count = FACADE.getPersonCount();
        return "{\"count\":"+count+"}";  //Done manually so no need for a DTO
    }
    
    @Path("all")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllPersons( @QueryParam("include") String include) throws API_Exception {
        int toInclude = SIMPLE;
        if(!isBlank(include)){
            toInclude = handleInclude(include, toInclude);
        }
        List<PersonDTO> personDTOs = FACADE.getAllPersons(toInclude);
        return GSON.toJson(personDTOs);
    }
    
    @Path("{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getPerson(@PathParam("id") int id, @QueryParam("include") String include) throws API_Exception {
        int toInclude = SIMPLE;
        if(!isBlank(include)){
            toInclude = handleInclude(include, toInclude);
        }
        PersonDTO personDTO = FACADE.getPerson(id,toInclude);
        return GSON.toJson(personDTO);
    }
    
    @Path("all-in-city/{zip}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllInCity(@PathParam("zip") String zip, @QueryParam("include") String include) throws API_Exception {
        int toInclude = SIMPLE;
        if(!isBlank(include)){
            toInclude = handleInclude(include, toInclude);
        }
        List<PersonDTO> personDTOs = FACADE.findPersonsInCity(zip, toInclude);
        return GSON.toJson(personDTOs);
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public String addPerson(String person) throws API_Exception {
        PersonDTO pDTO = GSON.fromJson(person, PersonDTO.class);
        pDTO = FACADE.addPerson(pDTO);
        return GSON.toJson(pDTO);
    }
    
    @PUT
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    public String editPerson(String person,@QueryParam("include") String include) throws API_Exception {
        int toInclude = SIMPLE;
        if(!isBlank(include)){
            toInclude = handleInclude(include, toInclude);
        }
        PersonDTO pDTO = GSON.fromJson(person, PersonDTO.class);
        pDTO = FACADE.editPerson(pDTO,toInclude);
        return GSON.toJson(pDTO);
    }
    
    
    private int handleInclude(String include, int toInclude) {
        String[] values = include.split("_");
        toInclude = 0;
        for(String val : values){
            if(val.equals("all")){
                toInclude = toInclude | ALL;
                return toInclude;
            }
            if(val.equals("simple")){
                toInclude = toInclude | SIMPLE;
            }
            if(val.equals("address")){
                toInclude = toInclude | ADDRESS;
            }
            if(val.equals("phones")){
                toInclude = toInclude | PHONES;
            }
            if(val.equals("hobby-simple")){
                toInclude = toInclude | HOBBIES_SIMPLE;
            }
        }
        return toInclude;
    }
}
