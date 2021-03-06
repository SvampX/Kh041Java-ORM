package demo.pack;

import annotations.*;

@Entity
@Table(name = "phones")
public class Phone {

    @Id
    @GeneratedValue
    @Column(name = "phone_id")
    private int id;

    @Column(name = "number")
    private String number;

    @OneToOne
    @JoinColumn
    private User user;

    public Phone() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "annotations.more.packages.pack.Phone{" +
                "id=" + id +
                ", number='" + number + '\'' +
                '}';
    }
}
