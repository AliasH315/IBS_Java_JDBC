package ru.ibs.h2;

import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.Server;
import org.w3c.dom.ls.LSOutput;

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

        System.out.println("Start application!");

        try {
            Connection connection = dataSource.getConnection();
            System.out.println("Connection successfully created");
            try (Statement statement = connection.createStatement()) {
                statement.execute(script);

                //Выводим на экран стоимость товара с SKU 0005
                selectSKU(connection,"0005");

                //Добавляем новый товар: SKU = '0007', NAME = 'Ball', DESCRIPTION = 'A ball', PRICE = 1.25
                addNewItems(connection,"0007","Ball", "A ball", 1.25);

                //Обновляем цену товара: SKU = '0007', PRICE = 0.99
                updatePrice(connection,"0007", 0.99);

                //Выводим на экран стоимость товара с SKU 0007
                selectSKU(connection,"0007");

                //Удаляем товар: SKU = '0007';
                deleteSKU(connection,"0007");

                //Выводим на экран стоимость товара с SKU 0007
                selectSKU(connection,"0007");
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Connection FAILED!");
        }

        server.stop();
    }
    //Метод получения необходимого товара
    private static void selectSKU (Connection connection, String sku) {
        try (PreparedStatement preparedStatement = connection.prepareStatement ("SELECT price FROM CATALOG_ITEMS where sku = ?")) {
            preparedStatement.setString(1, sku);
            boolean isResult = preparedStatement.execute();
            if (isResult) {
                ResultSet resultSet = preparedStatement.getResultSet();
                if (resultSet.next()) {
                    System.out.println("Стоимость товара для SKU = " + sku + ", равна " + resultSet.getFloat("PRICE"));
                } else {
                    System.out.println("Не найден товар SKU = " + sku);
                }
            }
            connection.commit();
        } catch (SQLException e) {
        System.out.println("ERROR: Ошибка вывода SKU = " + sku + " на экран!");
        try {
            connection.rollback();
        } catch (SQLException ex){
            System.out.println("ERROR: Ошибка отката при выводе SKU = " + sku + " на экран!");
        }
    }
    }
    //Метод добавления нового товара
    private static void addNewItems(Connection connection, String sku , String name , String description , Double price ){

        try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO CATALOG_ITEMS VALUES (?,?,?,?)")) {
            preparedStatement.setString(1,sku);
            preparedStatement.setString(2,name);
            preparedStatement.setString(3,description);
            preparedStatement.setDouble(4,price);
            int result = preparedStatement.executeUpdate();
            if( result > 0 ){
                System.out.println("Добавлен новый товар: SKU = " + sku);
            } else {
                System.out.println("Не удалось добавить новый товар: SKU = " + sku);
            }
            connection.commit();
        }catch (SQLException e){
            System.out.println("ERROR: Ошибка добавления нового товара");
            try {
                connection.rollback();
            } catch (SQLException ex){
                System.out.println("ERROR: Ошибка отката при попытке добавления нового товара SKU = " + sku + "!");
            }
        }
    }
    //Метод обновления цены товара
    private static void updatePrice(Connection connection, String sku, Double newPrice){
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE CATALOG_ITEMS SET price = ? WHERE sku = ?")) {
            preparedStatement.setDouble(1,newPrice);
            preparedStatement.setString(2,sku);
            int result = preparedStatement.executeUpdate();
            if( result > 0 ){
                System.out.println("Успешное обновление цены товара для SKU = " + sku + ", новая цена равна " + newPrice);

            } else {
                System.out.println("Не удалось обновить цену товара для SKU = " + sku);
            }
            connection.commit();
        } catch (SQLException e){
            System.out.println("ERROR: Ошибка обновления цены товара");
            try {
                connection.rollback();
            } catch (SQLException ex){
                System.out.println("ERROR: Ошибка отката при попытке обновления цены товара SKU = " + sku + "!");
            }
        }
    }
    //Метод удаления товара
    private static void deleteSKU(Connection connection,String sku){
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM CATALOG_ITEMS where sku = ?")){
            preparedStatement.setString(1,sku);
            int result = preparedStatement.executeUpdate();
            if (result > 0) {
                System.out.println("Успешное удаление товара SKU = " + sku);
            } else {
                System.out.println("Не удалось удалить товар SKU = " + sku);
            }
            connection.commit();
        } catch (SQLException e){
            System.out.println("ERROR: Ошибка удаления товара");
            try {
                connection.rollback();
            } catch (SQLException ex){
                System.out.println("ERROR: Ошибка отката при попытке удаления товара SKU = " + sku + "!");
            }
        }
    }

    /**
     * Необходимо сконфигурировать DataSource для подключения к БД.
     * Строка подключения: jdbc:h2:tcp://localhost:9092/mem:testdb
     * Пользователь: sa
     * Пароль: sa
     */
    private static DataSource getH2DataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:tcp://localhost:9092/mem:testdb");
        dataSource.setUser("sa");
        dataSource.setPassword("sa");
        return dataSource;
    }
}