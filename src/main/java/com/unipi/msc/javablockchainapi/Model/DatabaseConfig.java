package com.unipi.msc.javablockchainapi.Model;

import com.unipi.msc.javablockchainapi.Constants.Constant;
import com.unipi.msc.javablockchainapi.Controllers.Request.AddProductRequest;
import org.sqlite.SQLiteConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DatabaseConfig {
    private static final String DATABASE_URL = "jdbc:sqlite:"+System.getProperty("user.dir")+"/database.sqlite";
    public static final String DRIVER = "org.sqlite.JDBC";
    private static Connection getConnection() throws ClassNotFoundException {
        Class.forName(DRIVER);
        Connection connection = null;
        try {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true);
            connection = DriverManager.getConnection(DATABASE_URL,config.toProperties());
        } catch (SQLException ignore) {}
        return connection;
    }
    public static void createDB(){
        createNewDatabase();
        createTables();
        List<Product> productList = getProducts();
        if (!productList.isEmpty()) return;
        add_products();
        add_prices();
    }

    public static List<Product> getProducts() {
        List<Product> productList = new ArrayList<>();
        String query = "SELECT * FROM product";
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                productList.add(new Product(rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return productList;
    }
    public static List<ProductPrice> getData() {
        List<ProductPrice> productPriceList = new ArrayList<>();
        String query = "SELECT * FROM product_price INNER JOIN product on product_price.product_id = product.id";
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                productPriceList.add(new ProductPrice(
                        rs.getInt("product_price_id"),
                        new Product(rs.getInt("id"),rs.getString("name"),rs.getString("description"),rs.getString("category")),
                        rs.getDouble("price"),
                        rs.getLong("date")
                    )
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return productPriceList;
    }

    private static void createNewDatabase() {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void createTables(){
        String product_table = """
                CREATE TABLE IF NOT EXISTS product (
                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                 name TEXT NOT NULL,
                 description TEXT,
                 category TEXT
                );
                """;
        String productPrice_table= """
                CREATE TABLE IF NOT EXISTS product_price (
                 product_price_id INTEGER PRIMARY KEY AUTOINCREMENT,
                 product_id INTEGER,
                 date INTEGER,
                 price REAL,
                 FOREIGN KEY (product_id) REFERENCES product(id)
                );
                """;
        try{
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(product_table);
            stmt.execute(productPrice_table);
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void add_products() {
        String insert_product = """
                INSERT INTO product (name, description, category) VALUES 
                ('Xiaomi Redmi Note 10',
                '???? XIAOMI Redmi Note 10 ?????????????? ?????????? ???? ?????????????????????? ???? ???????????????????? ???????????????? ???????? ???????? ???????????? 5G! ????????????, ?????????????????? ?????? ?????? ?????????????????? Smartphone, ???????????? ?????? ???????? ?????? ????? ???????????? ?????????????? ?????????????? ???????? ???? ???????????? ??????, ??????????????????, ???? ?????? ???? ???????????????? ?????????? ?????? ???????? ?????? ?????????????? ??????????. ??????????????????????, ?? ???????????????????? ???????????????????? ???????????????????? ???????????????????????? ???????????????????????? ?????? ???????? ?????? ?????????????? ?????? ?????????????? ???????????????????? ???? ?????? ?????????? ??????????????.',
                'SmartPhones'),
                ('TOYOTA Yaris Hybrid 2022',
                '?????????????????????? ?????? ???????? ?????????????????? ?????????????? ?????? ?????????? ?????? ?????? ????????????????????????????????, ?? ?????? ?????????? Yaris ?????????? ?????????????? ready-to-go, ???????????? ????????????????, ???????????????????????? ?????? ???????????? ???????????????? ?????????????????? ???????????????????? ???? ?????? ?????????????????? ???????????????????? ?????? ?????? ?????????????? ????????????????.',
                'Car');
              """;
        try{
            Connection conn = getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(insert_product);
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private static void add_prices(){
        Random r = new Random();
        List<Product> productList = getProducts();

        String product_price_value = """
                    INSERT INTO product_price(product_id, date, price) VALUES (?,?,?)
                """;
        try{
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(product_price_value);
            for (Product p : productList) {
                for (int i=0;i<4;i++){
                    statement.setInt(1, p.getId());
                    statement.setDouble(2, new Date().getTime()+ i * 10);
                    if (p.getProductCategory().equals(Constant.SMARTPHONES)){
                        statement.setDouble(3, BigDecimal.valueOf(r.nextDouble(200,250))
                                                             .setScale(2, RoundingMode.HALF_UP)
                                                             .doubleValue()
                            );
                    }else {
                        statement.setDouble(3, BigDecimal.valueOf(r.nextDouble(20000,25000))
                                                             .setScale(2, RoundingMode.HALF_UP)
                                                             .doubleValue()
                            );
                    }
                    statement.addBatch();
                }
            }
            statement.executeBatch();
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public static Integer addPrice(Integer productId, double price, Long timestamp) {
        String product_price_value = """
                    INSERT INTO product_price(product_id, date, price) VALUES (?,?,?)
                """;
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(product_price_value);
            statement.setInt(1, productId);
            statement.setDouble(2, timestamp);
            statement.setDouble(3, price);
            statement.execute();
            conn.close();
        }catch (SQLException e){
            return e.getErrorCode();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return 0;
    }

    public static ProductPrice getLastData() {
        ProductPrice productPrice = null;
        String query = "SELECT * FROM product_price " +
                "INNER JOIN product on product_price.product_id = product.id " +
                "ORDER BY product_price_id DESC " +
                "LIMIT 1;";
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                productPrice = new ProductPrice(
                        rs.getInt("product_price_id"),
                        new Product(rs.getInt("id"),rs.getString("name"),rs.getString("description"),rs.getString("category")),
                        rs.getDouble("price"),
                        rs.getLong("date")
                    );
            }
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return productPrice;
    }

    public static Integer addPrices(List<ProductPrice> productPriceList) {
        String product_price_value = """
                    INSERT INTO product_price(product_id, date, price) VALUES (?,?,?)
                """;
        Connection conn = null;
        try{
            conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(product_price_value);
            conn.setAutoCommit(false);
            for (ProductPrice p : productPriceList) {
                statement.setInt(1, p.getProduct().getId());
                statement.setDouble(2, p.getTimestamp());
                statement.setDouble(3, p.getPrice());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return e.getErrorCode();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return 0;
    }

    public static List<ProductPrice> getLastData(int size) {
        List<ProductPrice> productPriceList = new ArrayList<>();
        String query = "SELECT * FROM product_price " +
                        "INNER JOIN product on product_price.product_id = product.id " +
                        "ORDER BY product_price_id DESC " +
                        "LIMIT (?);";
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1,size);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                productPriceList.add(new ProductPrice(
                        rs.getInt("product_price_id"),
                        new Product(rs.getInt("id"),rs.getString("name"),rs.getString("description"),rs.getString("category")),
                        rs.getDouble("price"),
                        rs.getLong("date")
                    )
                );
            }
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return productPriceList;
    }

    public static void addProducts(List<AddProductRequest> requestList) {
        String product_price_value = """
                    INSERT INTO product(name, description, category) VALUES (?,?,?)
                """;
        try{
            Connection conn = getConnection();
            conn.setAutoCommit(false);
            PreparedStatement statement = conn.prepareStatement(product_price_value);
            for (AddProductRequest request : requestList) {
                statement.setString(1, request.getName());
                statement.setString(2, request.getDsc());
                statement.setString(3, request.getCategory());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
           e.printStackTrace();
        }
    }

    public static void addProduct(AddProductRequest request) {
        String product_price_value = """
                    INSERT INTO product(name, description, category) VALUES (?,?,?)
                """;
        try{
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(product_price_value);
            statement.setString(1, request.getName());
            statement.setString(2, request.getDsc());
            statement.setString(3, request.getCategory());
            statement.execute();
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Product getProduct(int id) {
        createDB();
        Product product = null;
        String query = "SELECT * FROM product WHERE id = (?);";
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1,id);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                product = new Product(rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("category")
                );
            }
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return product;
    }

    public static List<Product> searchProducts(String category, String name) {
        List<Product> productList = new ArrayList<>();
        String query = """
                    SELECT *
                    FROM product
                    WHERE category LIKE '%'||(?)||'%' AND
                          name     LIKE '%'||(?)||'%'
                    ORDER BY id;
                    """;
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,category);
            statement.setString(2,name);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("category")
                    )
                );
            }
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return productList;
    }

    public static int getProductPriceCount() {
        int rows = 0;
        String query = """
                    SELECT COUNT(*) AS recordCount FROM product_price
                    """;
        try {
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                rows = rs.getInt("recordCount");
            }
            conn.close();
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }
}
