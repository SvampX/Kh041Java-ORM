package crud;

import annotations.handlers.DBColumn;

import java.util.ArrayList;
import java.util.List;

public class ExistingQuery {
    private String query;
    private final List<DBColumn> columnsOrder;

    public ExistingQuery() {
        columnsOrder = new ArrayList<>();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<DBColumn> getColumnsOrder() {
        return columnsOrder;
    }

    public void addColumn(DBColumn dbColumn) {
        columnsOrder.add(dbColumn);
    }
}
