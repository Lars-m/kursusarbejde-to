
import entities.CityInfo;
import entities.Hobby;
import entities.Person;
import javax.persistence.EntityManager;


public class MakeTables {

    public static void main(String[] args) {

        EntityManager em = utils.EMF_Creator.createEntityManagerFactory().createEntityManager();
        try{
            em.find(CityInfo.class, "2000");
            em.find(Hobby.class, "2000");
            em.find(Person.class, 2000);
        }finally{
            em.close();
        }

    }

}
