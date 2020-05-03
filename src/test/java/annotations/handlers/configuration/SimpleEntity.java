package annotations.handlers.configuration;

import annotations.*;

@Entity()
@Table(name = "2ndTable")
public class SimpleEntity {

    @ManyToMany(tableName = "test",inverseJoinColumnsName = "secondId")
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
