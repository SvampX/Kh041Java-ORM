package annotations.handlers;

public class ForeignKey {
    private DBColumn myTableKey;
    private DBColumn otherTableKey;
    private DBTable otherTable;
    private RelationType relationType;
    private boolean hasRelationsTable = false;

    public ForeignKey() {
    }

    public ForeignKey(DBColumn myTableKey, DBColumn otherTableKey, DBTable otherTable, RelationType relationType, boolean hasRelationsTable) {
        this.myTableKey = myTableKey;
        this.otherTableKey = otherTableKey;
        this.otherTable = otherTable;
        this.relationType = relationType;
        this.hasRelationsTable = hasRelationsTable;
    }

    public DBColumn getMyTableKey() {
        return myTableKey;
    }

    public void setMyTableKey(DBColumn myTableKey) {
        this.myTableKey = myTableKey;
    }

    public DBColumn getOtherTableKey() {
        return otherTableKey;
    }

    public void setOtherTableKey(DBColumn otherTableKey) {
        this.otherTableKey = otherTableKey;
    }

    public DBTable getOtherTable() {
        return otherTable;
    }

    public void setOtherTable(DBTable otherTable) {
        this.otherTable = otherTable;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public boolean isHasRelationsTable() {
        return hasRelationsTable;
    }

    public void setHasRelationsTable(boolean hasRelationsTable) {
        this.hasRelationsTable = hasRelationsTable;
    }
}
