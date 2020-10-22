package facades;

import errorhandling.API_Exception;
import dtos.HobbyDTO;
import entities.Hobby;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import utils.EMF_Creator;

public class HobbyFacade {
    
    private static HobbyFacade instance;
    private static EntityManagerFactory emf;

    private HobbyFacade() {
    }

    public static HobbyFacade getInstance(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new HobbyFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public List<HobbyDTO> getAllHobbies(int whatToInclude)  {
         EntityManager em = getEntityManager();
        try {
            List<Hobby> all = em.createQuery("SELECT h from Hobby h", Hobby.class).getResultList();
            return HobbyDTO.makeHobbyDTO_List(all, whatToInclude);
        } finally {
            em.close();
        }
    }
    
    public List<HobbyDTO> getHobbiesThatStartsWith(String searchString,int whatToInclude)  {
        String s = searchString.substring(0, 1).toUpperCase() + searchString.substring(1);
         EntityManager em = getEntityManager();
        try {
            List<Hobby> all = em.createQuery("SELECT h from Hobby h WHERE h.name LIKE  CONCAT(:s,'%')", Hobby.class).setParameter("s", searchString).getResultList();
            return HobbyDTO.makeHobbyDTO_List(all, whatToInclude);
        } finally {
            em.close();
        }
    }
    
    
   
    public long getHobbyCount() {
        EntityManager em = emf.createEntityManager();
        try {
            long hobbyCount = (long) em.createQuery("SELECT COUNT(h) FROM Hobby h").getSingleResult();
            return hobbyCount;
        } finally {
            em.close();
        }
    }

    public static void main(String[] args) throws API_Exception {
        EntityManagerFactory _emf = EMF_Creator.createEntityManagerFactory();
        HobbyFacade facade = HobbyFacade.getInstance(_emf);
        List<HobbyDTO> hobbies = facade.getHobbiesThatStartsWith("an", HobbyDTO.ALL);
        System.out.println("--> "+hobbies.size());
//System.out.println(facade.getHobbyCount());

    }
}
