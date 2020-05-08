package Manager;

import annotations.handlers.*;
import connections.ConnectionToDB;
import crud.CrudServices;
import crud.RelationsLoader;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

public class Manager {

    private static Manager instance;

    private ConnectionToDB connectionToDB;
    private CrudServices crudServices;
    RelationsLoader relationsLoader;
    Set<DBTable> tables;

    private Manager() throws SQLException {
        connectionToDB = ConnectionToDB.getInstance();
        crudServices = new CrudServices();
        crudServices.setConnection(connectionToDB.getConnection());
        relationsLoader = new RelationsLoader();
        tables = EntityToTableMapper.getTables();
    }

    public static synchronized Manager getInstance() throws SQLException {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
    }

    public void start() {
        RelationsWithOneHandler relationsWithOneHandler = new RelationsWithOneHandler();
        relationsWithOneHandler.handle(EntityHandler.getEntitiesSet());
        crudServices.initTables(connectionToDB.getConnection());
    }

    public void clean() {
        StringBuilder dropTablesQuery = new StringBuilder();
        String dialect = null;
        try {
            dialect = ConnectionToDB.getInstance().getDialect();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assert dialect != null;
        if (dialect.equalsIgnoreCase("mysql")) {
            dropTablesQuery.append("SET FOREIGN_KEY_CHECKS=0;\n");
        }
        List<DBTable> relationTables = ManyToManyHandler.getRelationTables();
        for (DBTable table : tables) {
            dropTablesQuery.append("DROP TABLE IF EXISTS ").append(table.getName()).append(" CASCADE;\n");
        }
        for (DBTable table : relationTables) {
            dropTablesQuery.append("DROP TABLE IF EXISTS ").append(table.getName()).append(" CASCADE;\n");
        }
        try {
            Statement statement = connectionToDB.getConnection().createStatement();
            statement.execute(dropTablesQuery.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Object read(Object objectId, Class<?> clazz) {
        return crudServices.readById(objectId, clazz);
    }
    public Set<Object> readAll(Class<?> clazz) {
        return crudServices.readAll(clazz);
    }

    public Set<Object> getAll(Class<?> clazz) {
        return getAll(clazz);
    }

    public void add(Object object) {
        crudServices.create(object);

    }
    public void addWithRelations(Object object){
        relationsLoader.create(object);
    }

    public void update(Object object) {
        crudServices.update(object);
    }

    public void delete(Object id, Class<?> clazz) {
        crudServices.delete(id, clazz);
    }
    public Set<Object> readEntityByPartialInitializedInstance(Object entity) {
        return crudServices.readEntityByPartialInitializedInstance(entity);
    }
}