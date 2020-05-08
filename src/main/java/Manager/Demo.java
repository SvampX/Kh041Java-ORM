package Manager;




import Manager.pack.Phone;
import Manager.pack.User;
import crud.CrudServices;
import crud.RelationsLoader;

import java.sql.SQLException;
import java.util.Set;

public class Demo {

    public static void main(String[] args) throws SQLException {

            Manager manager = Manager.getInstance();
            System.out.println("manager worked");

        Phone phone = new Phone();
        phone.setNumber("12-12-121");
        User user = new User();
        user.setAge(12);
        user.setFirstName("Bodya");
        user.setLastName("Angazalka");
        user.setPhone(phone);
        phone.setUser(user);

        manager.add(phone);
        manager.addLinkedObject(user);


      /*  Phone readedPhone = (Phone) manager.read(1, Phone.class);
        System.out.println("Phone = " + readedPhone);
        Set<Object> entities = manager.readAll(Phone.class);
        System.out.println("\n All entities from phone table");
        entities.forEach(System.out::println);

        Phone phone = new Phone();
        phone.setId(2);
        phone.setNumber("546846");
        manager.update(phone);

        int id = 3;
        manager.delete(id, Phone.class);

        Phone phone = new Phone();
        phone.setNumber("937-99-92");
        Set<Object> phoneSet;
        phoneSet = manager.readEntityByPartialInitializedInstance(phone);
        phoneSet.forEach(phon -> {
            System.out.println("id = " + ((Phone) phon).getId() + "   " + ((Phone) phon).getNumber());
        });*/



    }
}
