package ru.ibs.spring;

import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;



public class Main {
    private static final String SELECT_ACTOR_NAMED = """
        select *
        from actor
        where actor_id = :id
    """;

    private static final String SELECT_ACTOR = """
        select *
        from actor
        where actor_id = ?
    """;


    public static void main(String[] args) {
        DataSource dataSource = getDataSource();

        namedParameterJdbcTemplate(dataSource);

    }

    private static void namedParameterJdbcTemplate(DataSource dataSource) {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);

        List<Actor> actors = template.query(
        SELECT_ACTOR_NAMED,
            Map.of("id", 10),
            (rs, rowNum) -> new Actor(rs.getString("actor_id"), rs.getString("first_name"), rs.getString("last_name"))
        );

        System.out.println(actors);
    }

    private static void jdbcTemplate(DataSource dataSource) {
        JdbcTemplate template = new JdbcTemplate(dataSource);

        List<Actor> actors = template.query(
            SELECT_ACTOR,
            (PreparedStatement ps) -> ps.setInt(1, 10),
            (rs, rowNum) -> new Actor(rs.getString("actor_id"), rs.getString("first_name"), rs.getString("last_name"))
        );

        System.out.println(actors);
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

    record Actor(String actorId, String firstName, String lastName) { }
}