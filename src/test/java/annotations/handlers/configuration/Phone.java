package annotations.handlers.configuration;

import annotations.Column;
import annotations.Entity;
import annotations.GeneratedValue;
import annotations.Id;
import annotations.OneToOne;
import annotations.Table;

@Entity
@Table(name = "phones")
public class Phone {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name = "number")
    private String number;

    @OneToOne
    private User user;
}
