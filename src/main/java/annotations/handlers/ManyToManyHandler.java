package annotations.handlers;

import annotations.ManyToMany;

import java.lang.reflect.Field;
import java.util.*;


public class ManyToManyHandler {

    private final static List<ManyToMany> manyToManyList = new ArrayList<>();
    private final static Map<ManyToMany, DBTable> manyToManyTableMap = new HashMap<>();
    private final static Map<ManyToMany, Field> manyToManyFieldMap = new HashMap<>();
    private final static Map<ManyToMany, DBColumn> manyToManyReferencedColumnMap = new HashMap<>();
    private final static List<DBTable> relationTables = new ArrayList<>();

    private static void findMtmAnnotInEntities() {

        Set<DBTable> dbTableSet = EntityToTableMapper.getTables();

        for (DBTable table : dbTableSet) {
            Class classs = table.getMyEntityClass();
            for (Field field : classs.getDeclaredFields()) {
                ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                if (mtm != null) {
                    manyToManyReferencedColumnMap.put(mtm, findColumnByName(returnRightReferencedName(mtm), table));
                    manyToManyList.add(mtm);
                    manyToManyTableMap.put(mtm, table);
                    manyToManyFieldMap.put(mtm, field);
                }
            }
        }
    }


    public static void createJoinTables() {
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

        ForeignKey fk1 = getForeignKey(relationTable, originTable, first);
        ForeignKey fk2 = getForeignKey(relationTable, secondOriginTable, second);
        relationTable.getForeignKeys().add(fk1);
        relationTable.getForeignKeys().add(fk2);

        originTable.getForeignKeys().add(switchColumnsInKey(fk1, relationTable));
        secondOriginTable.getForeignKeys().add(switchColumnsInKey(fk2, relationTable));

        //EntityToTableMapper.getTables().add(relationTable);
        relationTables.add(relationTable);
    }

    private static ForeignKey switchColumnsInKey(ForeignKey key, DBTable dbTable) {
        ForeignKey fk = new ForeignKey();
        fk.setOtherTable(dbTable);
        fk.setMyTableKey(key.getOtherTableKey());
        fk.setOtherTableKey(key.getMyTableKey());
        fk.setRelationType(RelationType.ManyToMany);
        fk.setHasRelationsTable(true);
        return fk;
    }

    private static ForeignKey getForeignKey(DBTable first, DBTable second, ManyToMany mtm) {
        ForeignKey fk = new ForeignKey();
        fk.setHasRelationsTable(true);
        fk.setRelationType(RelationType.ManyToMany);
        fk.setMyTableKey(findColumnByName(returnRightName(mtm), first));
        fk.setOtherTableKey(manyToManyReferencedColumnMap.get(mtm));
        fk.setOtherTable(second);

        return fk;
    }


    private static DBColumn findColumnByField(ManyToMany mtm, DBTable table) {
        for (DBColumn dbColumn : table.getColumnSet()) {
            if (dbColumn.getField().equals(manyToManyFieldMap.get(mtm)))
                return dbColumn;
        }
        return null;
    }

    private static DBColumn findColumnByName(String name, DBTable table) {
        if(table.getPrimaryKey() != null){
            if (table.getPrimaryKey().getName().equals(name))
                return table.getPrimaryKey();
        }
        for (DBColumn dbColumn : table.getColumnSet()) {
            if (dbColumn.getName().equals(name))
                return dbColumn;
        }
        return null;
    }

    private static void determinePrimeMtm(ManyToMany first, ManyToMany second) {
        if (first.joinColumnsName().equals("")) {
            ManyToMany tmp;
            tmp = first;
            first = second;
            second = tmp;
        }
    }

    private static Set<DBColumn> createColumnSet(ManyToMany first, ManyToMany second) {
        Set<DBColumn> columnSet = new HashSet<>();
        columnSet.add(createColumn(first));
        columnSet.add(createColumn(second));
        return columnSet;
    }

    private static DBColumn createColumn(ManyToMany mtm) {

        DBColumn dbColumn = new DBColumn();
        dbColumn.setName(returnRightName(mtm));
        dbColumn.setField(manyToManyReferencedColumnMap.get(mtm).getField());
        dbColumn.setType(EntityToTableMapper.getColumnType(
                manyToManyReferencedColumnMap.get(mtm).getField()));

        return dbColumn;
    }

    private static String returnRightName(ManyToMany mtm) {
        if (mtm.inverseJoinColumnsName().equals("")) {
            return mtm.joinColumnsName();
        } else {
            return mtm.inverseJoinColumnsName();
        }

    }

    private static String returnRightReferencedName(ManyToMany mtm) {
        if (mtm.inverseJoinColumnsReferencedName().equals("")) {
            return mtm.joinColumnsReferencedName();
        } else {
            return mtm.inverseJoinColumnsReferencedName();
        }
    }

    public static List<ManyToMany> getManyToManyList() {
        return manyToManyList;
    }

    public static Map<ManyToMany, Field> getManyToManyFieldMap() {
        return manyToManyFieldMap;
    }

    public static List<DBTable> getRelationTables() {
        return relationTables;
    }
}
