package annotations.handlers.configuration;

import annotations.Column;
import annotations.Entity;
import annotations.Id;

@Entity
public class SimpleEntity {

    @Id
    @Column
    int nameId;

    @Column
    String userName;

    public SimpleEntity() {
    }

    public int getNameId() {
        return nameId;
    }

    public void setNameId(int nameId) {
        this.nameId = nameId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
