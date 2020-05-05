package annotations.handlers.configuration;

import annotations.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "secondTable")
public class SimpleEntity {

    @Id
    @Column(name = "id")
    int nameId;

    @Column
    String userName;

    @ManyToMany(tableName = "test", inverseJoinColumnsName = "secondId", inverseJoinColumnsReferencedName = "id")
    Set<ExtendedEntity> extendedEntities = new HashSet<>();

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
