package ru.ibs.h2;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

/**
 * 1. Необходимо создать экземпляр DataSource для создания соединений.
 * 2. Используя Statement и загруженный из файла StaplerzDBSchema.ddl скрипт создать БД.
 * 3. Вывести на экран стоимость товара с SKU 0005 (используем PreparedStatement)
 * 4. Добавить новый товар: SKU = '0007', NAME = 'Ball', DESCRIPTION = 'A ball', PRICE = 1.25;
 * 5. Обновить цену товара: SKU = '0007', PRICE = 0.99;
 * 6. Удалить товар: SKU = '0007';
 *
 * Повторить шаги 1-6 используя NamedParameterJdbcTemplate
 */
public class Main {
    public static void main(String[] args) throws IOException, SQLException, URISyntaxException {
        Server server = Server.createTcpServer("-ifNotExists").start();
        DataSource dataSource = getH2DataSource();

        Path path = Paths.get(Main.class.getClassLoader()
                .getResource("StaplerzDBSchema.ddl").toURI());

        String script = Files.readString(path);

        //TODO

        server.stop();
    }

    /**
     * Необходимо сконфигурировать DataSource для подключения к БД.
     * Строка подключения: jdbc:h2:tcp://localhost:9092/mem:testdb
     * Пользователь: sa
     * Пароль: sa
     */
    private static DataSource getH2DataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();

        return dataSource;
    }
}