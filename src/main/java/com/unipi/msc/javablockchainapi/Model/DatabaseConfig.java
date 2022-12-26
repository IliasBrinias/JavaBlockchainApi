package com.unipi.msc.javablockchainapi.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DatabaseConfig {
    private static final String DATABASE_URL = "jdbc:sqlite:"+System.getProperty("user.dir")+"/database.sqlite";
    private static Random r;
    public static void createDB(){
        createNewDatabase();
        createTables();
        List<Product> productList = getProducts();
        if (!productList.isEmpty()) return;
        add_products();
        add_prices();
    }

    private static List<Product> getProducts() {
        List<Product> productList = new ArrayList<>();
        String query = "SELECT * FROM product";
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                productList.add(new Product(rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productList;
    }
    public static List<ProductPrice> getData() {
        List<ProductPrice> productPriceList = new ArrayList<>();
        String query = "SELECT * FROM product_price INNER JOIN product on product_price.product_id = product.product_id";
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while(rs.next()){
                productPriceList.add(new ProductPrice(
                        rs.getInt("product_price_id"),
                        new Product(rs.getInt("product_id"),rs.getString("name"),rs.getString("description"),rs.getString("category")),
                        rs.getDouble("price"),
                        rs.getLong("date")
                        )
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productPriceList;
    }

    private static void createNewDatabase() {
        try {
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void createTables(){
        String product_table = """
                CREATE TABLE IF NOT EXISTS product (
                 product_id INTEGER PRIMARY KEY AUTOINCREMENT,
                 name TEXT NOT NULL,
                 description TEXT,
                 category TEXT
                );
                """;
        String productPrice_table= """
                CREATE TABLE IF NOT EXISTS product_price (
                 product_price_id integer  PRIMARY KEY AUTOINCREMENT,
                 product_id INTEGER,
                 date INTEGER,
                 price REAL,
                 FOREIGN KEY(product_id) REFERENCES product(id)
                );
                """;
        try{
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();
            stmt.execute(product_table);
            stmt.execute(productPrice_table);
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void add_products() {
        String insert_product = """
                INSERT INTO product (name, description, category) VALUES 
                ('Xiaomi Redmi Note 10',
                'Το XIAOMI Redmi Note 10 ξεπερνά πλέον τα καθιερωμένα με αστραπιαία ταχύτητα χάρη στην έκδοση 5G! Αρχικά, πρόκειται για ένα πανέμορφο Smartphone, ελαφρύ και λείο για ν’ αφήνει υπέροχη αίσθηση όταν το κρατάς και, παράλληλα, να μην σε κουράζει ακόμα και μετά από πολύωρη χρήση. Επιπρόσθετα, ο αισθητήρας ανίχνευσης δακτυλικού αποτυπώματος τοποθετείται στο πλάι για ακριβές και γρήγορο ξεκλείδωμα με ένα απαλό άγγιγμα.',
                'SmartPhones'),
                ('TOYOTA Yaris Hybrid 2022',
                'Προορισμένο για τους γρήγορους δρόμους της πόλης και του αυτοκινητόδρομου, η νέα γενιά Yaris είναι απόλυτα ready-to-go, γεμάτη ενέργεια, συνδυάζοντας την πλήρως υβριδική ηλεκτρική τεχνολογία με τις συμπαγείς διαστάσεις και την αιχμηρή σχεδίασή.',
                'Car');
              """;
        try{
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();
            stmt.execute(insert_product);
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void add_prices(){
        r = new Random();
        List<Product> productList = getProducts();

        String product_price_value = """
                    INSERT INTO product_price(product_id, date, price) VALUES (?,?,?)
                """;
        try{
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement statement = conn.prepareStatement(product_price_value);

            for (Product p : productList) {
                for (int i=0;i<4;i++){
                    statement.setInt(1, p.getId());
                    statement.setDouble(2, new Date().getTime()+ (long) i *r.nextInt(0,20));
                    if (p.getProductCategory().equals(ProductCategory.SmartPhones.toString())){
                        statement.setDouble(3, BigDecimal.valueOf(r.nextDouble(200,250))
                                                                     .setScale(2, RoundingMode.HALF_UP)
                                                                     .doubleValue());
                    }else {
                        statement.setDouble(3, BigDecimal.valueOf(r.nextDouble(20000,25000))
                                                                     .setScale(2, RoundingMode.HALF_UP)
                                                                     .doubleValue());
                    }
                    statement.addBatch();
                }
            }
            statement.executeBatch();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
