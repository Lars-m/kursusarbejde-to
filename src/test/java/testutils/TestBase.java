package testutils;

import entities.Address;
import entities.CityInfo;
import entities.Person;
import facades.PersonFacade;
import javax.persistence.EntityManagerFactory;

public class TestBase {
    protected static EntityManagerFactory emf;
    protected static PersonFacade facade;
    protected static Person p1;
    protected static Person p2;
    protected static Address a1;
    protected static Address a2;

    //These two instances are set up before all tests, and therefore be used by all tests
    protected static CityInfo cityInfo2000;
    protected static CityInfo cityInfo2800;    
}
