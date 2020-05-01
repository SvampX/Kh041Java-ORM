package annotations.handlers;


import annotations.Entity;
import annotations.JoinColumn;
import annotations.JoinTable;
import annotations.ManyToMany;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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





    private static Set<JoinTable> joinTables;
    private static Set<ManyToMany> manyToManySet;
    private static Set<DBTable> relationTables;
    private static Map<JoinTable, Class> joinTableClassMap = new HashMap<>();
    private static Map<JoinTable, Field> joinTableFieldMap = new HashMap<>();
    private static Set<DBColumn> columnSet = new HashSet<>();



    private static void findJoinTablesAndMtmAnnotInEntities() {
        entitiesSet = generateEntitySet();
        for(Class classs : entitiesSet){
            for(Field field : classs.getDeclaredFields()){
                JoinTable jt = field.getAnnotation(JoinTable.class);
                ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                if(jt != null && mtm != null){
                    
                    joinTables.add(jt);
                    joinTableClassMap.put(jt, classs);
                    joinTableFieldMap.put(jt,field);
                }
            }
        }
    }
    private static void createTables() {

        for(int i = 0; i < joinTables.size()/2; i++){
            DBTable dbTable = new DBTable();




        }
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

        for(Class classs : entitiesSet){
            for(Field field : classs.getDeclaredFields()){
                ManyToMany mtm = field.getAnnotation(ManyToMany.class);
                if(mtm != null){
                    JoinTable jt = field.getAnnotation(JoinTable.class);
                    if(jt != null){
                        joinColumn.setName(joinColumns[0].name());
                        joinColumn.setField(findFieldByName(joinColumns[0].referencedColumnName(), classs));
                        //joinColumn.setType();

                        dbColumnSet.add(joinColumn);

                        joinColumnKey.setMyTableKey(findColumnByName(joinColumns[0].referencedColumnName(), classs));
                        joinColumnKey.setOtherTableKey(joinColumn);
                        joinColumnKey.setOtherTable(relationTable);
                        joinColumnKey.setRelationType(RelationType.ManyToMany);
                        joinColumnKey.setHasRelations(true);

                    }else{
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
    private DBColumn findColumnByName(String name, Class classs){
       for(DBTable dbtable : EntityToTableMapper.getTables()){
           if(dbtable.getMyEntityClass() == classs) {
               for (DBColumn dbColumn : dbtable.getColumnSet()) {
                if(dbColumn.getName().equals(name))
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
    private static Set<Class<?>> generateEntitySet(){
        return reflections.getTypesAnnotatedWith(Entity.class, true);
    }

}
