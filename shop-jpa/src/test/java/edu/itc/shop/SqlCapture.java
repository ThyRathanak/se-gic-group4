package edu.itc.shop;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class SqlCapture implements StatementInspector {

    private static final List<String> statements =
        new CopyOnWriteArrayList<>();

    @Override
    public String inspect(String sql) {
        statements.add(sql);
        return sql;
    }

    public static void clear() {
        statements.clear();
    }

    public static List<String> getStatements() {
        return List.copyOf(statements);
    }
}