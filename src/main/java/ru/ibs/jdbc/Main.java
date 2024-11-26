package ru.ibs.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        DataSource dataSource = getPooledDataSource();
        String sql = "select * from actor where actor_id = 1";
        List<Actor> actors = new ArrayList<>();


        try (Connection connection = dataSource.getConnection()) {
            statement1(connection, sql, actors);
            //statement2(connection, sql, actors);
            //preparedStatement(connection, sql, actors);
        } catch (SQLException e) {
            System.err.println("Something wrong happened: " + e.getMessage());
            throw e;
        }

        System.out.println(actors);
    }

    private static void statement1(Connection connection, String sql, List<Actor> actors) throws SQLException {
        try (Statement s = connection.createStatement()) {
            boolean isResultSet = s.execute(sql);
            if (isResultSet) {
                try (ResultSet rs = s.getResultSet()) {
                    while (rs.next()) {
                        actors.add(new Actor(rs.getString("actor_id"), rs.getString("first_name"), rs.getString("last_name")));
                    }
                }
            }
        }
    }

    private static void statement2(Connection connection, String sql, List<Actor> actors) throws SQLException {
        try (
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery(sql)
        ) {
            while (rs.next()) {
                actors.add(new Actor(rs.getString("actor_id"), rs.getString("first_name"), rs.getString("last_name")));
            }
        }
    }

    private static void preparedStatement(Connection connection, String sql, List<Actor> actors) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < 5; i++) {
                ps.setInt(1, i);

                try (ResultSet rs =  ps.executeQuery()) {
                    while (rs.next()) {
                        actors.add(new Actor(rs.getString("actor_id"), rs.getString("first_name"), rs.getString("last_name")));
                    }
                }
            }
        }
    }

    private static DataSource getDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[] { "localhost" });
        dataSource.setPortNumbers(new int[] { 5431 });
        dataSource.setDatabaseName("pagila");
        dataSource.setUser("postgres");
        dataSource.setPassword("pg_secret");
        return dataSource;
    }

    private static DataSource getPooledDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://localhost:5431/pagila");
        dataSource.setUsername("postgres");
        dataSource.setPassword("pg_secret");
        return dataSource;
    }

    record Actor(String actorId, String firstName, String lastName) { }
}