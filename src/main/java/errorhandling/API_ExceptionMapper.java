package errorhandling;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


public class API_ExceptionMapper implements ExceptionMapper<API_Exception> 
{
    @Context 
    ServletContext context;
    
    static Gson gson = new GsonBuilder().setPrettyPrinting().create();   
    @Override
    public Response toResponse(API_Exception ex) {
       Logger.getLogger(API_ExceptionMapper.class.getName()).log(Level.SEVERE, null, ex);
       boolean isDebug = context.getInitParameter("debug").equals("true");
       ExceptionDTO err = new ExceptionDTO(ex.getErrorCode(),ex.getMessage(),ex,isDebug);
       return Response
               .status(ex.getErrorCode())
               .entity(gson.toJson(err))
               .type(MediaType.APPLICATION_JSON)
               .build();
	}
}