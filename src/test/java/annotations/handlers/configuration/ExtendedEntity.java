package annotations.handlers.configuration;

import annotations.*;

@Entity(name = "EntityName")
@Table(name = "1stTable")
public class ExtendedEntity {


    @ManyToMany(tableName = "test", joinColumnsName = "firstId")
    @Id
    @Column(name = "name_id")
    int nameId;

    @Column(name = "user_name")
    String userName;

    @Column(name = "real_name")
    String realName;

    @Column(name = "java_object")
    SimpleEntity simpleEntity;

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

    public void setSimpleEntity(SimpleEntity simpleEntity) {
        this.simpleEntity = simpleEntity;
    }
}
