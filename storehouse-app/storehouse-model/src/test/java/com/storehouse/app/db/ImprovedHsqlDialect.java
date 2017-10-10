package com.storehouse.app.db;

import org.hibernate.dialect.HSQLDialect;

/**
 * Purpose of this custom dialect is to remove the SchemaExport ERROR
 * in the in-memory database (H2).
 * 
 * @see https://hibernate.atlassian.net/browse/HHH-7002
 *
 * @author ejiafzh
 *
 */
public class ImprovedHsqlDialect extends HSQLDialect {
    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public boolean supportsIfExistsAfterTableName() {
        return false;
    }

    @Override
    public String getCascadeConstraintsString() {
        return " CASCADE ";
    }
}
