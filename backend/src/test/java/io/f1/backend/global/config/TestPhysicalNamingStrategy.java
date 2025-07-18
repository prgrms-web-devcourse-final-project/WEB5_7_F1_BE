package io.f1.backend.global.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class TestPhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        if (name == null) return null;

        String tableName = name.getText();
        if ("user".equalsIgnoreCase(tableName)) {
            return Identifier.toIdentifier("user_test");
        }

        return super.toPhysicalTableName(name, context);
    }
}
