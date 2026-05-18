package com.example.user.config;

import com.example.user.util.CipherUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DatabaseInitializer {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Value("${default-admin.username:admin}")
    private String defaultUsername;

    @Value("${default-admin.password:admin123}")
    private String defaultPassword;

    @Value("${default-admin.real-name:超级管理员}")
    private String defaultRealName;

    @Value("${default-admin.id-card:110101199001011234}")
    private String defaultIdCard;

    @Value("${default-admin.phone:13800138000}")
    private String defaultPhone;

    @Value("${default-admin.email:admin@example.com}")
    private String defaultEmail;

    @Value("${default-admin.role:SUPER_ADMIN}")
    private String defaultRole;

    @Value("${test-data.enabled:false}")
    private boolean testDataEnabled;

    public DatabaseInitializer(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            if (!tableExists("sys_user")) {
                executeSchemaScript();
                
                if (testDataEnabled) {
                    loadTestData();
                } else {
                    createDefaultAdmin();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    private void executeSchemaScript() throws IOException {
        ClassPathResource resource = new ClassPathResource("db/schema.sql");
        List<String> statements = parseSqlScript(resource);
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                if (!sql.trim().isEmpty()) {
                    statement.execute(sql);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("执行SQL脚本失败: db/schema.sql", e);
        }
    }

    private List<String> parseSqlScript(ClassPathResource resource) throws IOException {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                    continue;
                }
                
                currentStatement.append(line).append("\n");
                
                if (trimmedLine.endsWith(";")) {
                    String sql = currentStatement.toString().trim();
                    if (sql.endsWith(";")) {
                        sql = sql.substring(0, sql.length() - 1).trim();
                    }
                    if (!sql.isEmpty()) {
                        statements.add(sql);
                    }
                    currentStatement = new StringBuilder();
                }
            }
            
            String lastSql = currentStatement.toString().trim();
            if (lastSql.endsWith(";")) {
                lastSql = lastSql.substring(0, lastSql.length() - 1).trim();
            }
            if (!lastSql.isEmpty()) {
                statements.add(lastSql);
            }
        }
        
        return statements;
    }

    private void loadTestData() {
        try {
            String testPassword = CipherUtil.encrypt("Test@123");
            String defaultPassword = CipherUtil.encrypt(this.defaultPassword);
            
            try (Connection conn = dataSource.getConnection()) {
                String sql = "INSERT INTO sys_user (username, password, real_name, id_card, phone, email, status, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    insertUser(ps, "admin", defaultPassword, defaultRealName, defaultIdCard, defaultPhone, defaultEmail, 0, defaultRole);
                    insertUser(ps, "superadmin", testPassword, "超级管理员", "110101199001011234", "13800000001", "superadmin@test.com", 0, "SUPER_ADMIN");
                    insertUser(ps, "admin01", testPassword, "管理员01", "110101199001011235", "13800010001", "admin01@test.com", 0, "ADMIN");
                    insertUser(ps, "admin02", testPassword, "管理员02", "110101199001011236", "13800010002", "admin02@test.com", 0, "ADMIN");
                    insertUser(ps, "admin03", testPassword, "管理员03", "110101199001011237", "13800010003", "admin03@test.com", 0, "ADMIN");
                    insertUser(ps, "user01", testPassword, "用户01", "110101199001011238", "13900000001", "user01@test.com", 0, "USER");
                    insertUser(ps, "user02", testPassword, "用户02", "110101199001011239", "13900000002", "user02@test.com", 0, "USER");
                    insertUser(ps, "user03", testPassword, "用户03", "110101199001011240", "13900000003", "user03@test.com", 0, "USER");
                    insertUser(ps, "user04", testPassword, "用户04", "110101199001011241", "13900000004", "user04@test.com", 0, "USER");
                    insertUser(ps, "user05", testPassword, "用户05", "110101199001011242", "13900000005", "user05@test.com", 0, "USER");
                    insertUser(ps, "user06", testPassword, "用户06", "110101199001011243", "13900000006", "user06@test.com", 0, "USER");
                    insertUser(ps, "user07", testPassword, "用户07", "110101199001011244", "13900000007", "user07@test.com", 0, "USER");
                    insertUser(ps, "user08", testPassword, "用户08", "110101199001011245", "13900000008", "user08@test.com", 0, "USER");
                    insertUser(ps, "user09", testPassword, "用户09", "110101199001011246", "13900000009", "user09@test.com", 0, "USER");
                    insertUser(ps, "user10", testPassword, "用户10", "110101199001011247", "13900000010", "user10@test.com", 0, "USER");
                }
            }
            
            System.out.println("测试数据已加载:");
            System.out.println("  - 默认管理员: admin / " + this.defaultPassword + " (" + defaultRole + ")");
            System.out.println("  - 超级管理员: superadmin / Test@123");
            System.out.println("  - 管理员: admin01-03 / Test@123");
            System.out.println("  - 普通用户: user01-10 / Test@123");
        } catch (Exception e) {
            System.err.println("加载测试数据失败: " + e.getMessage());
            e.printStackTrace();
            createDefaultAdmin();
        }
    }

    private void insertUser(PreparedStatement ps, String username, String password, 
                           String realName, String idCard, String phone, 
                           String email, int status, String role) throws Exception {
        ps.setString(1, username);
        ps.setString(2, password);
        ps.setString(3, realName);
        ps.setString(4, CipherUtil.encrypt(idCard));
        ps.setString(5, phone);
        ps.setString(6, email);
        ps.setInt(7, status);
        ps.setString(8, role);
        ps.executeUpdate();
    }

    private void createDefaultAdmin() {
        String encryptedPassword = CipherUtil.encrypt(defaultPassword);
        String encryptedIdCard = CipherUtil.encrypt(defaultIdCard);
        
        jdbcTemplate.update(
            "INSERT INTO sys_user (username, password, real_name, id_card, phone, email, status, role) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            defaultUsername, encryptedPassword, defaultRealName, encryptedIdCard, 
            defaultPhone, defaultEmail, 0, defaultRole
        );
        
        System.out.println("默认超级管理员账户已创建:");
        System.out.println("  用户名: " + defaultUsername);
        System.out.println("  密码: " + defaultPassword);
        System.out.println("  角色: " + defaultRole);
    }

    private boolean tableExists(String tableName) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'"
            );
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}
