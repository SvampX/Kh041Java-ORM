package crud;

import annotations.handlers.DBTable;
import annotations.handlers.EntityToTableMapper;
import connections.ConnectionToDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class RelationsLoader {
    private Connection connection;
    private Set<DBTable> tables;

    public RelationsLoader() {
        init();
    }

    private void init() {
        try {
            ConnectionToDB connectionToDB = ConnectionToDB.getInstance();
            tables = EntityToTableMapper.getTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public Object read() {
//        if(hasRelationsCheck(clazz)){
//
//        }
//    }
}
