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
}
