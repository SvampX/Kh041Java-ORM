package Manager.pack;

import annotations.*;

@Entity()
@Table(name = "cars")
public class Car {

    @Id
    @GeneratedValue()
    @Column(name = "id")
    private int id;

    @Column(name = "brand")
    private String brandName;

    @Column(name = "license_plate")
    private String licensePlate;

//    @ManyToMany(tableName = "person_car", inverseJoinColumnsName = "CarId", inverseJoinColumnsReferencedName = "id")
//    private List<User> persons = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Car(String brandName, String licensePlate) {
        this.brandName = brandName;
        this.licensePlate = licensePlate;
    }

    public Car() {
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

//    public List<User> getPersons() {
//        return persons;
//    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", brandName='" + brandName + '\'' +
                ", licensePlate='" + licensePlate + '\'' +
                '}';
    }
}

