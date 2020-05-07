package annotations.handlers.configuration;

import annotations.*;

import java.sql.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "secondTable")
public class SimpleEntity {

    @Id
    @SequenceGenerator(name = "genesis", initialValue = 42, allocationSize = 2)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genesis")
    @Column(name = "id")
    int nameId;

    @Column
    String userName;

    @ManyToMany(tableName = "test", inverseJoinColumnsName = "secondId", inverseJoinColumnsReferencedName = "id")
    Set<ExtendedEntity> extendedEntities = new HashSet<>();

    @Column(name = "today_date")
    Date todayDate;

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

    @Override
    public String toString() {
        return "SimpleEntity{" +
                "nameId=" + nameId +
                ", userName='" + userName + '\'' +
                '}';
    }
}
