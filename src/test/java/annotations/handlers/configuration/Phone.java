package annotations.handlers.configuration;

import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.Id;
import annotations.JoinColumn;
import annotations.OneToOne;
import annotations.Table;

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
}
