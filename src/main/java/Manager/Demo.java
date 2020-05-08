package Manager;


import Manager.pack.Car;
import Manager.pack.Phone;
import Manager.pack.User;

import java.sql.SQLException;
import java.util.Set;

public class Demo {

    public static void main(String[] args) throws SQLException {

        Manager manager = Manager.getInstance();
        manager.start();
        User user = new User();

        initOneToOneObjects(user);
        manager.add(user);
        user.setFirstName("Billy");
        manager.add(user);
        user.setLastName("Milligan");
        manager.add(user.getCar());

        manager.addWithRelations(user);
        manager.addWithRelations(user);

        Set<Object> users = manager.readAll(User.class);
        Set<Object> cars = manager.readAll(Car.class);
        Set<Object> phones = manager.readAll(Phone.class);

        System.out.println("-------------------------------------------------");

        showEntitiesCollections(users);
        showEntitiesCollections(cars);
        showEntitiesCollections(phones);

        System.out.println("-------------------------------------------------");

        User randomUser = (User) users.iterator().next();
        System.out.println("Id = " + randomUser.getId());
        randomUser.setFirstName("Bruce");
        randomUser.setLastName("Vayne");
        randomUser.setAge(40);


        manager.update(randomUser);
        users = manager.readAll(User.class);
        showEntitiesCollections(users);

        System.out.println("-------------------------------------------------");

        manager.delete(2, User.class);
        users = manager.readAll(User.class);
        showEntitiesCollections(users);

        manager.clean();
    }

    private static void showEntitiesCollections(Set<Object> entities) {
        System.out.println();
        for (Object obj : entities) {
            System.out.println("entity = " + obj);
        }
        System.out.println();
    }

    private static void initOneToOneObjects(User user) {
        user.setFirstName("Alex");
        user.setLastName("Lens");
        user.setAge(25);

        Phone phone = new Phone();
        phone.setNumber("937-99-92");
        phone.setUser(user);
        user.setPhone(phone);

        Car car = new Car("DEAWO", "DE-937-99-92-WO");
        car.setUser(user);
        user.setCar(car);
    }
}
