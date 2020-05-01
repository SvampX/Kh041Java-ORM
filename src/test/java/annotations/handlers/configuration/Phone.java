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
    @Column
    private int id;

    @Column
    private String number;

    @OneToOne
    private User user;
}
