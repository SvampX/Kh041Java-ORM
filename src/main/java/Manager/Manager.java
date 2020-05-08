package Manager;

import annotations.handlers.DBTable;
import annotations.handlers.EntityHandler;
import annotations.handlers.EntityToTableMapper;
import annotations.handlers.RelationsWithOneHandler;
import connections.ConnectionToDB;
import crud.CrudServices;
import crud.RelationsLoader;

import java.sql.SQLException;
import java.util.Set;

public class Manager {

    private static Manager instance;

    private ConnectionToDB connectionToDB;
    private CrudServices crudServices;
    RelationsLoader relationsLoader;

    private Manager() throws SQLException {
        connectionToDB = ConnectionToDB.getInstance();
        crudServices = new CrudServices();
        relationsLoader = new RelationsLoader();
        Set<DBTable> tables = EntityToTableMapper.getTables();
        RelationsWithOneHandler relationsWithOneHandler = new RelationsWithOneHandler();
        relationsWithOneHandler.handle(EntityHandler.getEntitiesSet());
        crudServices.initTables(connectionToDB.getConnection());
        crudServices.setConnection(connectionToDB.getConnection());
    }

    public static synchronized Manager getInstance() throws SQLException {
        if (instance == null) {
            instance = new Manager();
        }
        return instance;
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
    public void addLinkedObject(Object object){
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