package annotations.handlers.configuration;

import annotations.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "EntityName")
@Table(name = "firstTable")
public class ExtendedEntity {

    @Id
    @GeneratedValue
    @Column(name = "name_id")
    int nameId;

    @Column(name = "user_name")
    String userName;

    @Column(name = "real_name")
    String realName;

    /*@Column(name = "java_object")
    String simpleEntity;*/
    @Column(name = "java_object")
    SimpleEntity simpleEntity;

    @ManyToMany(tableName = "test", joinColumnsName = "firstId", joinColumnsReferencedName = "name_id")
    Set<SimpleEntity> simpleEntities = new HashSet<>();

    public ExtendedEntity() {
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public SimpleEntity getSimpleEntity() {
        return simpleEntity;
    }
//    public String getSimpleEntity() {
//        return simpleEntity;
//    }

    public void setSimpleEntity(SimpleEntity simpleEntity) {
        this.simpleEntity = simpleEntity;
    }
    /*public void setSimpleEntity(String simpleEntity) {
        this.simpleEntity = simpleEntity;
    }*/
}
