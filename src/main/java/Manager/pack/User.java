package Manager.pack;

import annotations.*;

import java.util.ArrayList;
import java.util.List;

/**
 * This class created for testing purpose
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "age")
    private int age;

    @OneToOne
    private Phone phone;

    @OneToOne
    private Car car;

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }
    @OneToMany(mappedBy = "userDetails")
    private List<Address> addresses;

    @ManyToMany(tableName = "user_book", joinColumnsName = "user_id", joinColumnsReferencedName = "id")
    public List<Book> myBooks = new ArrayList<>();

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

//    public List<Address> getAddresses() {
//        return addresses;
//    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", age=" + age +
                '}';
    }
}
