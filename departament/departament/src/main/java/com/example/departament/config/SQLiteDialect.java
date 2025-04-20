package com.example.departament.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

import java.sql.Types;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        // No es necesario registrar los tipos específicamente en las versiones más recientes
    }

    public String getTypeName(int code) {
        switch (code) {
            case Types.BIT:
            case Types.BOOLEAN:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return "integer";
            case Types.BIGINT:
                return "bigint";
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
            case Types.DECIMAL:
                return "real";
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return "text";
            case Types.DATE:
                return "date";
            case Types.TIME:
                return "time";
            case Types.TIMESTAMP:
                return "timestamp";
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return "blob";
            case Types.CLOB:
                return "clob";
            default:
                return "text";
        }
    }

    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    public boolean hasAlterTable() {
        return false;
    }

    public boolean dropConstraints() {
        return false;
    }

    public String getAddColumnString() {
        return "add column";
    }

    public String getForUpdateString() {
        return "";
    }

    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    public boolean supportsCascadeDelete() {
        return false;
    }

    public String getDropForeignKeyString() {
        return "";
    }

    public String getAddForeignKeyConstraintString(String constraintName, String[] foreignKey, String referencedTable, String[] primaryKey, boolean referencesPrimaryKey) {
        return "";
    }

    public String getAddPrimaryKeyConstraintString(String constraintName) {
        return "";
    }

    public boolean supportsLimit() {
        return true;
    }

    public boolean bindLimitParametersInReverseOrder() {
        return true;
    }

    public String getLimitString(String query, boolean hasOffset) {
        return query + (hasOffset ? " limit ? offset ?" : " limit ?");
    }

    private static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {
        public boolean supportsIdentityColumns() {
            return true;
        }

        public String getIdentitySelectString(String table, String column, int type) {
            return "select last_insert_rowid()";
        }

        public String getIdentityColumnString(int type) {
            return "integer primary key autoincrement";
        }
    }
}
