package com.nguyenthanhphuc.showroom.repositories;

import com.nguyenthanhphuc.showroom.models.GeneralInfo;
import com.nguyenthanhphuc.showroom.services.Product;
import com.nguyenthanhphuc.showroom.services.CompanyName;
import com.nguyenthanhphuc.showroom.services.Repo;
import com.nguyenthanhphuc.showroom.services.UpdateProductResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MySqlRepo implements Repo {
    private final String url;
    private final String username;
    private final String password;
    private static final String DEFAULT_SHORT_NAME = "Cedar Ridge";
    private static final String DEFAULT_LONG_NAME = "Cedar Ridge Custom Homes";
    private static final String DEFAULT_FEATURE_IMAGE_PATH = "/files/feature.jpg";
    private static final String DEFAULT_ABOUT_CONTENT = "Cedar Ridge Custom Homes designs and builds modern, energy-efficient residences across the region.\n"
            + "Our showroom highlights material selections, lighting, and smart-home options so clients can make confident, informed decisions.\n\n"
            + "We manage the full process from concept to completion, working with trusted architects and craftspeople.\n"
            + "Visit us to explore finish packages, compare layouts, and discuss your project timeline.";

    public MySqlRepo(
            @Value("${mysql.host}") String host,
            @Value("${mysql.port}") int port,
            @Value("${mysql.username}") String username,
            @Value("${mysql.password}") String password,
            @Value("${mysql.database}") String database) {
        this.url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        this.username = username;
        this.password = password;
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkLogin(String username, String password) {
        var sql = "SELECT username FROM users WHERE username = ? AND password = ?";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompanyName loadCompanyName() {
        var sql = "SELECT my_key, my_value FROM settings WHERE my_key = ? OR my_key = ?";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, "COMPANY_SHORT_NAME");
            ps.setString(2, "COMPANY_LONG_NAME");
            try (var rs = ps.executeQuery()) {
                var shortName = "";
                var longName = "";
                for (var i = 0; i < 2; i++) {
                    if (!rs.next()) {
                        throw new RuntimeException("Company name not configured correctly");
                    }
                    var key = rs.getString("my_key");
                    var value = rs.getString("my_value");
                    if (key.equals("COMPANY_SHORT_NAME")) {
                        shortName = value;
                    } else {
                        longName = value;
                    }
                }
                return new CompanyName(shortName, longName);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveCompanyName(CompanyName companyName) {
        var sql = """
                UPDATE settings
                SET my_value = CASE my_key
                    WHEN 'COMPANY_SHORT_NAME' THEN ?
                    WHEN 'COMPANY_LONG_NAME' THEN ?
                    ELSE my_value
                END
                WHERE my_key IN ('COMPANY_SHORT_NAME', 'COMPANY_LONG_NAME')
                """;
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, companyName.shortName());
            ps.setString(2, companyName.longName());
            if (ps.executeUpdate() < 2) {
                throw new RuntimeException("Company name not configured correctly");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String loadAboutContent() {
        var sql = "SELECT my_value FROM settings WHERE my_key = ?";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, "ABOUT_CONTENT");
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("my_value");
                }
            }
            return DEFAULT_ABOUT_CONTENT;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GeneralInfo getGeneralInfo() {
        ensureGeneralInfoTable();
        var sql = "SELECT short_name, long_name, feature_image_path FROM general_info WHERE id = 1";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql);
                var rs = ps.executeQuery()
        ) {
            if (rs.next()) {
                return new GeneralInfo(
                        rs.getString("short_name"),
                        rs.getString("long_name"),
                        rs.getString("feature_image_path")
                );
            }
            return new GeneralInfo(DEFAULT_SHORT_NAME, DEFAULT_LONG_NAME, DEFAULT_FEATURE_IMAGE_PATH);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateGeneralInfo(String shortName, String longName, String featureImagePath) {
        ensureGeneralInfoTable();
        var sql = "UPDATE general_info SET short_name = ?, long_name = ?, feature_image_path = ? WHERE id = 1";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, shortName);
            ps.setString(2, longName);
            ps.setString(3, featureImagePath);
            var updated = ps.executeUpdate();
            if (updated == 0) {
                insertDefaultGeneralInfo(conn, shortName, longName, featureImagePath);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveAboutContent(String aboutContent) {
        var updateSql = "UPDATE settings SET my_value = ? WHERE my_key = 'ABOUT_CONTENT'";
        var insertSql = "INSERT INTO settings (my_key, my_value) VALUES ('ABOUT_CONTENT', ?)";
        try (
                var conn = getConnection();
                var updatePs = conn.prepareStatement(updateSql)
        ) {
            updatePs.setString(1, aboutContent);
            if (updatePs.executeUpdate() == 0) {
                try (var insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, aboutContent);
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Product> loadProducts() {
        var sql = "SELECT id, name FROM products ORDER BY name ASC";
        try (
                var conn = getConnection();
                var stmt = conn.prepareStatement(sql);
                var rs = stmt.executeQuery()
        ) {
            var products = new ArrayList<Product>();
            while (rs.next()) {
                products.add(new Product(rs.getInt("id"), rs.getString("name")));
            }
            return products;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Product loadProductById(int id) {
        var sql = "SELECT id, name FROM products WHERE id = ?";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Product(rs.getInt("id"), rs.getString("name"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<String> loadProductName(int id) {
        var sql = "SELECT name FROM products WHERE id = ?";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("name"));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsProductName(String name) {
        var sql = "SELECT 1 FROM products WHERE name = ? LIMIT 1";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existsProductName(String name, int excludeId) {
        var sql = "SELECT 1 FROM products WHERE name = ? AND id <> ? LIMIT 1";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int insertProduct(String name) {
        var sql = "INSERT INTO products (name) VALUES (?)";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProductName(int id, String name) {
        var sql = "UPDATE products SET name = ? WHERE id = ?";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UpdateProductResult updateProduct(int id, String name) {
        var existsSql = "SELECT 1 FROM products WHERE id = ? LIMIT 1";
        var nameExistsSql = "SELECT 1 FROM products WHERE name = ? AND id <> ? LIMIT 1";
        var updateSql = "UPDATE products SET name = ? WHERE id = ?";
        try (
                var conn = getConnection();
                var existsPs = conn.prepareStatement(existsSql);
                var nameExistsPs = conn.prepareStatement(nameExistsSql);
                var updatePs = conn.prepareStatement(updateSql)
        ) {
            existsPs.setInt(1, id);
            try (var rs = existsPs.executeQuery()) {
                if (!rs.next()) {
                    return UpdateProductResult.NOT_FOUND;
                }
            }

            nameExistsPs.setString(1, name);
            nameExistsPs.setInt(2, id);
            try (var rs = nameExistsPs.executeQuery()) {
                if (rs.next()) {
                    return UpdateProductResult.NAME_EXISTED;
                }
            }

            updatePs.setString(1, name);
            updatePs.setInt(2, id);
            updatePs.executeUpdate();
            return UpdateProductResult.OK;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteProduct(int id) {
        var sql = "DELETE FROM products WHERE id = ?";
        try (
                var conn = getConnection();
                var ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureGeneralInfoTable() {
        var sql = """
                CREATE TABLE IF NOT EXISTS general_info (
                    id INT PRIMARY KEY,
                    short_name VARCHAR(50) NOT NULL,
                    long_name VARCHAR(100) NOT NULL,
                    feature_image_path VARCHAR(255) NOT NULL
                )
                """;
        try (
                var conn = getConnection();
                var stmt = conn.createStatement()
        ) {
            stmt.execute(sql);
            ensureDefaultGeneralInfoRow(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureDefaultGeneralInfoRow(Connection conn) throws SQLException {
        var countSql = "SELECT COUNT(*) FROM general_info";
        try (var stmt = conn.createStatement();
             var rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertDefaultGeneralInfo(conn, DEFAULT_SHORT_NAME, DEFAULT_LONG_NAME, DEFAULT_FEATURE_IMAGE_PATH);
            }
        }
    }

    private void insertDefaultGeneralInfo(Connection conn, String shortName, String longName, String featureImagePath) throws SQLException {
        var insertSql = "INSERT INTO general_info (id, short_name, long_name, feature_image_path) VALUES (1, ?, ?, ?)";
        try (var ps = conn.prepareStatement(insertSql)) {
            ps.setString(1, shortName);
            ps.setString(2, longName);
            ps.setString(3, featureImagePath);
            ps.executeUpdate();
        }
    }

}
