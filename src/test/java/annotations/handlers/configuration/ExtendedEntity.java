package annotations.handlers.configuration;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import annotations.Table;

@Entity(name = "EntityName")
@Table(name = "table_name")
public class ExtendedEntity {

    @Id(name = "name_id")
    int nameId;

    @Column(name = "user_name")
    String userName;

    String realName;

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

    @Column(name = "real_name")
    public void setRealName(String realName) {
        this.realName = realName;
    }
}
