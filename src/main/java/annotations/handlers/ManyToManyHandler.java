package annotations.handlers;


import annotations.Entity;
import annotations.JoinColumn;
import annotations.JoinTable;
import annotations.ManyToMany;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.*;

import static annotations.handlers.EntityToTableMapper.getTables;

public class ManyToManyHandler {
    // private static Set<Field> secondEntFields;
    private static Reflections reflections;
    private static Set<Class<?>> entitiesSet;

    //private static String mappedBy = "";

    // private static JoinTable joinTable;
    // private static DBColumn joinColumn = new DBColumn();
    // private static DBColumn inverseJoinColumn = new DBColumn();
    // private static ForeignKey joinColumnKey = new ForeignKey();
    // private static ForeignKey inverseJoinColumnKey = new ForeignKey();
    //DBTable relationTable = new DBTable();


    // private static Set<JoinTable> joinTables;
    private static List<ManyToMany> manyToManyList = new ArrayList<>();
    private static List<DBTable> relationTables = new ArrayList<>();
    private static Map<ManyToMany, Class> manyToManyClassMap = new HashMap<>();
    private static Map<ManyToMany, DBTable> manyToManyTableMap = new HashMap<>();
    private static Map<ManyToMany, Field> manyToManyFieldMap = new HashMap<>();
    //private static Set<DBColumn> columnSet = new HashSet<>();


    private static void findMtmAnnotInEntities() {

        Set<DBTable> dbTableSet = EntityToTableMapper.getTables();

        for (DBTable table : dbTableSet) {
            Class classs = table.getMyEntityClass();
            for (Field field : classs.getDeclaredFields()) {
                ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                if (mtm != null) {
                    manyToManyList.add(mtm);
                    manyToManyClassMap.put(mtm, classs);
                    manyToManyTableMap.put(mtm, table);
                    manyToManyFieldMap.put(mtm, field);
                }
            }
        }
    }

    private static void createTables() {

        findMtmAnnotInEntities();

        while (manyToManyList.size() != 0) {
            ManyToMany mtm = manyToManyList.get(0);
            ManyToMany secondMtm = null;
            manyToManyList.remove(mtm);
            for (int i = 0; i < manyToManyList.size(); i++) {
                if (mtm.tableName().equals(manyToManyList.get(i).tableName())) {
                    secondMtm = manyToManyList.get(i);
                    manyToManyList.remove(secondMtm);
                }
            }
            determinePrimeMtm(mtm, secondMtm);
            DBTable dbTable = new DBTable();

            dbTable.setName(mtm.tableName());
            dbTable.setColumnSet(createColumnSet(mtm, secondMtm));
            createForeignKeys(dbTable, mtm, secondMtm);


        }
    }

    private static void createForeignKeys(DBTable relationTable, ManyToMany first, ManyToMany second) {
        DBTable originTable = manyToManyTableMap.get(first);
        DBTable secondOriginTable = manyToManyTableMap.get(second);

        addForeignKey(relationTable, originTable, first);
        addForeignKey(relationTable, secondOriginTable, second);









    }
    private static void addForeignKey(DBTable first, DBTable second, ManyToMany mtm) {
        ForeignKey fk = new ForeignKey();
        fk.setHasRelations(true);
        fk.setRelationType(RelationType.ManyToMany);
        fk.setMyTableKey(findColumnByName(mtm.joinColumnsName(), first));
        fk.setOtherTableKey(findColumnByField(mtm, second));
        fk.setOtherTable(second);

        first.getForeignKeys().add(fk);
    }


    /*private static void createForeignKeyFromMyColumn(ManyToMany mtm, DBColumn dbColumn) {
        ForeignKey fk = new ForeignKey();
        fk.setHasRelations(true);
        fk.setRelationType(RelationType.ManyToMany);
        fk.setMyTableKey(dbColumn);
        fk.setOtherTableKey(findColumn(mtm, manyToManyTableMap.get(mtm)));
        fk.setOtherTable(manyToManyTableMap.get(mtm));
    }*/
    private static DBColumn findColumnByField(ManyToMany mtm, DBTable table) {

        for (DBColumn dbColumn : table.getColumnSet()) {
            if (dbColumn.getField().equals(manyToManyFieldMap.get(mtm)))
                return dbColumn;
        }


        return null;
    }
    private static DBColumn findColumnByName(String name, DBTable table) {

        for (DBColumn dbColumn : table.getColumnSet()) {
            if (dbColumn.getName().equals(name))
                return dbColumn;
        }


        return null;
    }



    private static void determinePrimeMtm(ManyToMany first, ManyToMany second) {
        if (first.mappedBy().equals("") || first.inverseJoinColumnsName().equals("")) {
            ManyToMany tmp;
            tmp = first;
            first = second;
            second = first;
        }
    }

    private static Set<DBColumn> createColumnSet(ManyToMany mtm, ManyToMany secondMtm) {
        Set<DBColumn> columnSet = new HashSet<>();

        DBColumn dbColumn = new DBColumn();
        dbColumn.setName(mtm.joinColumnsName());
        dbColumn.setField(manyToManyFieldMap.get(mtm));
        dbColumn.setType(EntityToTableMapper.getColumnType(manyToManyFieldMap.get(mtm)));
        // createForeignKeyFromMyTable(mtm, dbColumn);
        columnSet.add(dbColumn);

        dbColumn = new DBColumn();
        dbColumn.setName(mtm.inverseJoinColumnsName());
        dbColumn.setField(manyToManyFieldMap.get(secondMtm));
        dbColumn.setType(EntityToTableMapper.getColumnType(manyToManyFieldMap.get(secondMtm)));
        // createForeignKeyFromMyTable(secondMtm, dbColumn);
        columnSet.add(dbColumn);


        return columnSet;
    }































    /*
    public static void inspectEntities() {
        entitiesSet = generateEntitySet();


        for(Class classs : entitiesSet){
            for(Field field : classs.getDeclaredFields()){
                ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                if(mtm != null){
                 //   mappedBy = mtm.mappedBy();
                   Set<DBTable> tablesSet = EntityToTableMapper.getTables();
                   for(DBTable dbt : tablesSet){
                       if(dbt.getMyEntityClass() == classs){
                           dbt.getForeignKeys().add(new ForeignKey(dbt.getPrimaryKey(), ));
                       }
                   }
                }
            }
        }
        relationTable.setColumnSet(dbColumnSet);
    }
    */


    public DBTable generateJoinTable() throws NoSuchFieldException {
        findJoinTable();
        entitiesSet = generateEntitySet();
        JoinColumn[] joinColumns = joinTable.joinColumns();
        JoinColumn[] inverseJoinColumns = joinTable.inverseJoinColumns();
        Set<DBColumn> dbColumnSet = new HashSet<>();

        for (Class classs : entitiesSet) {
            for (Field field : classs.getDeclaredFields()) {
                ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                if (mtm != null) {
                    JoinTable jt = field.getAnnotation(JoinTable.class);
                    if (jt != null) {
                        joinColumn.setName(joinColumns[0].name());
                        joinColumn.setField(findFieldByName(joinColumns[0].referencedColumnName(), classs));
                        //joinColumn.setType();

                        dbColumnSet.add(joinColumn);

                        joinColumnKey.setMyTableKey(findColumnByName(joinColumns[0].referencedColumnName(), classs));
                        joinColumnKey.setOtherTableKey(joinColumn);
                        joinColumnKey.setOtherTable(relationTable);
                        joinColumnKey.setRelationType(RelationType.ManyToMany);
                        joinColumnKey.setHasRelations(true);

                    } else {
                        joinColumn.setName(inverseJoinColumns[0].name());
                        joinColumn.setField(findFieldByName(inverseJoinColumns[0].referencedColumnName(), classs));
                        // joinColumn.setType();

                        dbColumnSet.add(inverseJoinColumn);

                        inverseJoinColumnKey.setMyTableKey(findColumnByName(inverseJoinColumns[0].referencedColumnName(), classs));
                        inverseJoinColumnKey.setOtherTableKey(inverseJoinColumn);
                        inverseJoinColumnKey.setOtherTable(relationTable);
                        inverseJoinColumnKey.setRelationType(RelationType.ManyToMany);
                        inverseJoinColumnKey.setHasRelations(true);


                    }

                }
            }
        }


        relationTable.setColumnSet(dbColumnSet);
        return relationTable;
    }

    private DBColumn findColumnByName(String name, Class classs) {
        for (DBTable dbtable : EntityToTableMapper.getTables()) {
            if (dbtable.getMyEntityClass() == classs) {
                for (DBColumn dbColumn : dbtable.getColumnSet()) {
                    if (dbColumn.getName().equals(name))
                        return dbColumn;
                }
            }
        }
        return null;
    }


    /*
        JoinTable joinTable = field.getAnnotation(JoinTable.class);
        if(joinTable != null) {
            relationTable.setName(joinTable.name());

            JoinColumn[] joinColumns = joinTable.joinColumns();
            JoinColumn[] inverseJoinColumns = joinTable.inverseJoinColumns();

            inspectJoinColumns(field, joinTable.joinColumns());
            inspectJoinColumns(field, joinTable.inverseJoinColumns());
        }else if(joinTable == null) {
            secondEntFields =
*/


    private Field findFieldByName(String name, Class classs) throws NoSuchFieldException {
        return classs.getDeclaredField(name);
    }

    private static void inspectJoinColumns(Field field, JoinColumn[] joinColumns) {
        for (JoinColumn ijc : joinColumns) {
            DBColumn joinColumn = new DBColumn();
            joinColumn.setField(field);
            joinColumn.setName(ijc.name());
            // joinColumn.setType(?????????);
            dbColumnSet.add(joinColumn);
        }
    }

    public static String getMappedBy() {
        return mappedBy;
    }

    public static void setReflections(Reflections reflections) {
        ManyToManyHandler.reflections = reflections;
    }

    private static Set<Class<?>> generateEntitySet() {
        return reflections.getTypesAnnotatedWith(Entity.class, true);
    }

}
