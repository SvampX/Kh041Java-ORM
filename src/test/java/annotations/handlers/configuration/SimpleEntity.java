package annotations.handlers.configuration;

import annotations.*;

import java.util.HashSet;
import java.util.Set;

@Entity()
@Table(name = "2ndTable")
public class SimpleEntity {

    @Id
    @Column(name = "id")
    int nameId;

    @Column
    String userName;

    @ManyToMany(tableName = "test", joinColumnsName = "secondId", joinColumnsReferencedName = "id")
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
