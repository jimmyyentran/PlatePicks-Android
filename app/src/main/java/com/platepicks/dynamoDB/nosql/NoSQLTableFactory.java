package com.platepicks.dynamoDB.nosql;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;


public class NoSQLTableFactory {
    /** Singleton instance. */
    private volatile static NoSQLTableFactory instance;

    /** Map containing an instance of each of the supporting tables by table name. */
    private LinkedHashMap<String, NoSQLTableBase> supportedTablesMap = new LinkedHashMap<>();

    NoSQLTableFactory(final Context context) {
        final List<NoSQLTableBase> supportedTablesList = new ArrayList<>();
        supportedTablesList.add(new NoSQLTableFood());
        supportedTablesList.add(new NoSQLTableList());
        supportedTablesList.add(new NoSQLTableComment());
        for (final NoSQLTableBase table : supportedTablesList) {
            supportedTablesMap.put(table.getTableName(), table);
        }
    }

    public synchronized static NoSQLTableFactory instance(final Context context) {
        if (instance == null) {
            instance = new NoSQLTableFactory(context);
        }
        return instance;
    }

    public Collection<NoSQLTableBase> getNoSQLSupportedTables() {
        return supportedTablesMap.values();
    }


    public <T extends NoSQLTableBase> T getNoSQLTableByTableName(final String tableName) {
        return (T) supportedTablesMap.get(tableName);
    }
}
