package demo;

import demo.pack.Address;
import demo.pack.Phone;
import demo.pack.User;

import java.sql.SQLException;

public class Demo {
    public static void main(String[] args) throws SQLException {
        Manager manager = Manager.getInstance();
        System.out.println("manager worked");
        manager.startWork();

        User user1 = new User();
        user1.setAge(20);
        user1.setFirstName("Ivan");
        user1.setLastName("Ivanov");
        manager.add(user1);

        User user2 = new User();
        user2.setAge(30);
        user2.setFirstName("Lena");
        user2.setLastName("Ivanova");
        manager.add(user2);

        User user3 = new User();
        user3.setAge(25);
        user3.setFirstName("Joy");
        user3.setLastName("Stevens");
        manager.add(user3);

        User userFromTable = (User) manager.get(3, User.class);
        System.out.println(userFromTable.toString());

        Phone phone = new Phone();
        phone.setNumber("123654789");
        manager.add(phone);

        Phone phone1 = new Phone();
        phone.setNumber("122554668");
        manager.add(phone1);

        Address address1 = new Address();
        address1.setCity("City");
        address1.setCountry("Country");
        address1.setPostalCode("5599");
        address1.setStreetName("Some Street");
        address1.setType("business");
        manager.add(address1);

        Address address2 = new Address();
        address2.setCity("Las Angeles");
        address2.setCountry("Country");
        address2.setPostalCode("5588");
        address2.setStreetName("Some Street");
        address2.setType("shipping");
        manager.add(address2);

        Address addressFromTb = (Address) manager.get(2, Address.class);
        System.out.println(addressFromTb.toString());
        addressFromTb.setCountry("USA");
        manager.update(addressFromTb);
        manager.delete(1, User.class);
    }
}
