package annotations.handlers.configuration;

import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.Id;
import annotations.OneToMany;
import annotations.OneToOne;
import annotations.Table;

import java.util.List;

/**
 * This class created for testing purpose
 */
@Entity
@Table(name = "test_users")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "age")
    private int age;

    @OneToOne
    private Phone phone;

    @OneToMany(mappedBy = "userDetails")
    private List<Address> addresses;

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
