package annotations.handlers;

import annotations.handlers.configuration.Address;
import annotations.handlers.configuration.Car;
import annotations.handlers.configuration.Phone;
import annotations.handlers.configuration.User;
import connections.ConnectionToDB;
import crud.CrudServices;
import org.junit.jupiter.api.BeforeAll;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LinkTest {
    private static Connection connection;
    private static Set<DBTable> tables;
    private static CrudServices crudServices;
    private static List<User> users = new ArrayList<>();
    private static List<Car> cars = new ArrayList<>();
    private static List<Phone> phones = new ArrayList<>();
    private static List<Address> addresses = new ArrayList<>();

    @BeforeAll
    void init() {
        ConnectionToDB connectionToDB;
        try {
            connectionToDB = ConnectionToDB.getInstance();
            connection = connectionToDB.getConnection();
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
        tables = EntityToTableMapper.getTables();
        crudServices = new CrudServices();

        Car mazda = new Car(11, "Mazda", "S3B4A");
        Car audi = new Car(13, "Audi", "P6FGH");
        Car skoda = new Car(5, "Skoda", "SS3GH");
        Car nissan = new Car(2, "Nissan", "N8KK32");
        Car porsche = new Car(18, "Porsche", "77NJG");
        cars.add(mazda);
        cars.add(skoda);
        cars.add(audi);
        cars.add(nissan);
        cars.add(porsche);

        User nastya = new User();
        nastya.setAge(23);
        nastya.setFirstName("Nastya");
        nastya.setLastName("Vlasova");
        nastya.setId(33);
        nastya.myCars.add(mazda);
        nastya.myCars.add(audi);
        mazda.getPersons().add(nastya);
        audi.getPersons().add(nastya);

        User vasia = new User();
        vasia.setAge(30);
        vasia.setFirstName("Vasiliy");
        vasia.setLastName("Vlasov");
        vasia.setId(31);
        vasia.myCars.add(mazda);
        vasia.myCars.add(audi);
        mazda.getPersons().add(vasia);
        audi.getPersons().add(vasia);

        User vlada = new User();
        vlada.setAge(29);
        vlada.setFirstName("Vlada");
        vlada.setLastName("Volkova");
        vlada.setId(36);
        vlada.myCars.add(skoda);
        vlada.myCars.add(nissan);
        vlada.myCars.add(porsche);
        skoda.getPersons().add(vlada);
        nissan.getPersons().add(vlada);
        porsche.getPersons().add(vlada);

        User petya = new User();
        petya.setAge(31);
        petya.setFirstName("Petr");
        petya.setLastName("Volkov");
        petya.setId(39);
        petya.myCars.add(skoda);
        petya.myCars.add(nissan);
        petya.myCars.add(porsche);
        skoda.getPersons().add(petya);
        nissan.getPersons().add(petya);
        porsche.getPersons().add(petya);

        users.add(nastya);
        users.add(vasia);
        users.add(vlada);
        users.add(petya);

        Phone phone1 = new Phone();
        phone1.setNumber("0333333333");
        phone1.setId(41);
        phone1.setUser(nastya);
        nastya.setPhone(phone1);

        Phone phone2 = new Phone();
        phone2.setNumber("0444444444");
        phone2.setId(43);
        phone2.setUser(vasia);
        vasia.setPhone(phone2);

        Phone phone3 = new Phone();
        phone3.setNumber("0555555555");
        phone3.setId(47);
        phone3.setUser(vlada);
        vlada.setPhone(phone3);

        Phone phone4 = new Phone();
        phone4.setNumber("0666666666");
        phone4.setId(48);
        phone4.setUser(petya);
        petya.setPhone(phone4);

        phones.add(phone1);
        phones.add(phone2);
        phones.add(phone3);
        phones.add(phone4);

        Address firstAdress = new Address();
        firstAdress.setCity("Kharkiv");
        firstAdress.setCountry("Ukraine");
        firstAdress.setId(1233757852L);
        firstAdress.setPostalCode("69065");
        firstAdress.setStreetName("Soborniy");
        firstAdress.setType("house");

        Address secondAdress = new Address();
        firstAdress.setCity("Kyiv");
        firstAdress.setCountry("Ukraine");
        firstAdress.setId(126556852L);
        firstAdress.setPostalCode("33345");
        firstAdress.setStreetName("Gagarina");
        firstAdress.setType("flat");

        Address thirdAdress = new Address();
        firstAdress.setCity("Lviv");
        firstAdress.setCountry("Ukraine");
        firstAdress.setId(64684686482L);
        firstAdress.setPostalCode("45545");
        firstAdress.setStreetName("Tverskaya");
        firstAdress.setType("house");

        Address fourthAdress = new Address();
        firstAdress.setCity("Odessa");
        firstAdress.setCountry("Ukraine");
        firstAdress.setId(23123852L);
        firstAdress.setPostalCode("23245");
        firstAdress.setStreetName("Romenskaya");
        firstAdress.setType("flat");

        addresses.add(firstAdress);
        addresses.add(secondAdress);
        addresses.add(thirdAdress);
        addresses.add(fourthAdress);

        nastya.getAddresses().add(firstAdress);
        nastya.getAddresses().add(secondAdress);
        vasia.getAddresses().add(firstAdress);
        vasia.getAddresses().add(secondAdress);
        firstAdress.setUserDetails(nastya);
        firstAdress.setUserDetails(vasia);

        vlada.getAddresses().add(thirdAdress);
        vlada.getAddresses().add(fourthAdress);
        petya.getAddresses().add(thirdAdress);
        petya.getAddresses().add(fourthAdress);
        thirdAdress.setUserDetails(vlada);
        fourthAdress.setUserDetails(petya);


    }
}
