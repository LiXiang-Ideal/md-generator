package com.mdgenerator.server.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.mdgenerator.server.common.DbTableInfo;
import com.mdgenerator.server.common.DbTableInfo.DbColumnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据库表结构读取器
 * 
 * <p>负责连接数据库并读取数据库中所有表的结构信息。
 * 支持MySQL、Oracle、PostgreSQL三种主流数据库。</p>
 * 
 * <p>核心功能：</p>
 * <ul>
 *   <li>建立数据库连接（通过JDBC）</li>
 *   <li>查询所有表的基本信息（表名、注释、引擎等）</li>
 *   <li>查询每张表的所有字段详细信息（字段名、类型、是否可空、默认值、注释等）</li>
 *   <li>查询主键信息</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * DbReader reader = new DbReader("jdbc:mysql://localhost:3306/mydb", "root", "password", "mysql");
 * List<DbTableInfo> tables = reader.readAllTables();
 * reader.close();
 * }</pre>
 * 
 * <p>设计说明：</p>
 * <ul>
 *   <li>数据库类型使用字符串枚举（"mysql"/"oracle"/"postgresql"），
 *      这样避免了依赖特定数据库驱动的类，保持代码的灵活性和可扩展性</li>
 *   <li>每次查询后都会关闭ResultSet和Statement，防止资源泄漏</li>
 *   <li>使用try-with-resources确保所有数据库资源被正确释放</li>
 * </ul>
 * 
 * @author MD Generator Team
 * @version 1.0.0
 */
public class DbReader {

    private static final Logger logger = LoggerFactory.getLogger(DbReader.class);

    /** JDBC数据库连接对象 */
    private Connection connection;

    /** 数据库类型标识（"mysql"、"oracle"、"postgresql"） */
    private String dbType;

    /** 数据库名称/服务名 */
    private String databaseName;

    /**
     * 构造函数：建立数据库连接
     * 
     * <p>根据传入的URL、用户名、密码和数据库类型建立JDBC连接。
     * 连接建立时会自动调用DriverManager获取连接对象。</p>
     * 
     * @param jdbcUrl JDBC连接URL，如"jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=Asia/Shanghai"
     * @param username 数据库用户名
     * @param password 数据库密码
     * @param dbType 数据库类型标识（"mysql"、"oracle"、"postgresql"）
     * @throws RuntimeException 如果建立连接失败（SQLException）
     */
    public DbReader(String jdbcUrl, String username, String password, String dbType) {
        this.dbType = dbType != null ? dbType.toLowerCase() : "mysql";
        try {
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
            // 从URL中提取数据库名称，用于日志和数据源标识
            this.databaseName = extractDatabaseName(jdbcUrl);
            logger.info("数据库连接成功，类型：{}，数据库名称：{}", this.dbType, this.databaseName);
            logger.debug("JDBC URL：{}", jdbcUrl);
        } catch (SQLException e) {
            logger.error("数据库连接失败：{}", e.getMessage(), e);
            throw new RuntimeException("数据库连接失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从JDBC URL中提取数据库名称
     * 
     * <p>处理不同数据库的URL格式：</p>
     * <ul>
     *   <li>MySQL: jdbc:mysql://host:port/dbname?params → dbname</li>
     *   <li>Oracle: jdbc:oracle:thin:@host:port:sid → sid</li>
     *   <li>PostgreSQL: jdbc:postgresql://host:port/dbname → dbname</li>
     * </ul>
     * 
     * @param jdbcUrl JDBC连接URL
     * @return 数据库名称
     */
    private String extractDatabaseName(String jdbcUrl) {
        try {
            if ("mysql".equals(dbType)) {
                // MySQL URL格式: jdbc:mysql://host:port/dbname?params
                // 去掉 jdbc:mysql:// 前缀
                String withoutPrefix = jdbcUrl.substring(jdbcUrl.indexOf("://") + 3);
                // 找到第一个 / 后面的数据库名
                int slashIndex = withoutPrefix.indexOf('/');
                if (slashIndex == -1) {
                    return "unknown";
                }
                String afterSlash = withoutPrefix.substring(slashIndex + 1);
                int questionIndex = afterSlash.indexOf('?');
                if (questionIndex == -1) {
                    return afterSlash;
                }
                return afterSlash.substring(0, questionIndex);
            } else if ("oracle".equals(dbType)) {
                // Oracle URL格式: jdbc:oracle:thin:@host:port:sid 或 jdbc:oracle:thin:@host:port/serviceName
                String afterAt = jdbcUrl.substring(jdbcUrl.lastIndexOf('@') + 1);
                String[] parts = afterAt.split(":");
                if (parts.length >= 3) {
                    return parts[2].split("/")[0];
                }
            } else if ("postgresql".equals(dbType)) {
                return extractDatabaseName(jdbcUrl.replace("postgresql", "mysql"));
            }
        } catch (Exception e) {
            // 如果解析失败，返回"unknown"
        }
        return "unknown";
    }

    /**
     * 读取数据库中所有表的结构信息
     *
     * <p>这是数据库文档生成的核心方法，执行步骤：</p>
     * <ol>
     *   <li>查询information_schema获取所有基础表（非视图）的元信息</li>
     *   <li>对每张表，查询其所有字段的完整信息</li>
     *   <li>查询每张表的主键信息并标记对应字段</li>
     * </ol>
     *
     * @return 包含所有表信息的列表，按表名字典序排列
     * @throws RuntimeException 如果查询过程中发生SQL异常
     */
    public List<DbTableInfo> readAllTables() {
        List<DbTableInfo> tableList = new ArrayList<>();

        // MySQL查询SQL：从information_schema.TABLES获取表基本信息
        if ("mysql".equals(dbType)) {
            String sql = "SELECT TABLE_NAME, TABLE_COMMENT, ENGINE, TABLE_COLLATION, TABLE_TYPE "
                       + "FROM information_schema.TABLES "
                       + "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE' "
                       + "ORDER BY TABLE_NAME";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, databaseName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DbTableInfo tableInfo = new DbTableInfo();
                        tableInfo.setTableName(rs.getString("TABLE_NAME"));
                        tableInfo.setTableComment(rs.getString("TABLE_COMMENT"));
                        tableInfo.setEngine(rs.getString("ENGINE"));
                        tableInfo.setCharset(rs.getString("TABLE_COLLATION"));
                        tableInfo.setTableType(rs.getString("TABLE_TYPE"));

                        tableInfo.setColumns(readColumns(tableInfo.getTableName()));
                        markPrimaryKeys(tableInfo);

                        tableList.add(tableInfo);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("查询表信息失败：" + e.getMessage(), e);
            }
        } else {
            readTablesByMetaData(tableList);
        }

        return tableList;
    }

    /**
     * 通过JDBC DatabaseMetaData读取表信息（通用方式）
     * 
     * <p>这是information_schema方式的备选方案，适用于非MySQL数据库。
     * DatabaseMetaData是JDBC标准API，所有JDBC驱动都必须实现。</p>
     * 
     * @param tableList 用于填充结果的表信息列表
     */
    private void readTablesByMetaData(List<DbTableInfo> tableList) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            // 参数：catalog, schemaPattern, tableNamePattern, types[]
            try (ResultSet rs = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    DbTableInfo tableInfo = new DbTableInfo();
                    tableInfo.setTableName(rs.getString("TABLE_NAME"));
                    tableInfo.setTableComment(rs.getString("REMARKS"));
                    tableInfo.setTableType(rs.getString("TABLE_TYPE"));

                    // 读取字段信息
                    tableInfo.setColumns(readColumnsByMetaData(metaData, tableInfo.getTableName()));

                    // 标记主键
                    markPrimaryKeysByMetaData(metaData, tableInfo);

                    tableList.add(tableInfo);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("通过MetaData查询表信息失败：" + e.getMessage(), e);
        }
    }

    /**
     * 读取指定表的所有字段信息（MySQL版本，通过information_schema）
     *
     * @param tableName 表名
     * @return 字段信息列表，按ordinal_position排序
     */
    private List<DbColumnInfo> readColumns(String tableName) {
        List<DbColumnInfo> columns = new ArrayList<>();

        String sql = "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, "
                   + "COLUMN_COMMENT, EXTRA, ORDINAL_POSITION "
                   + "FROM information_schema.COLUMNS "
                   + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? "
                   + "ORDER BY ORDINAL_POSITION";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, databaseName);
            stmt.setString(2, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DbColumnInfo column = new DbColumnInfo();
                    column.setColumnName(rs.getString("COLUMN_NAME"));
                    column.setColumnType(rs.getString("COLUMN_TYPE"));
                    column.setIsNullable(rs.getString("IS_NULLABLE"));
                    column.setDefaultValue(rs.getString("COLUMN_DEFAULT"));
                    column.setColumnComment(rs.getString("COLUMN_COMMENT"));

                    String extra = rs.getString("EXTRA");
                    column.setAutoIncrement(extra != null && extra.toLowerCase().contains("auto_increment"));
                    column.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));

                    columns.add(column);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询字段信息失败（表：" + tableName + "）：" + e.getMessage(), e);
        }

        return columns;
    }

    /**
     * 通过DatabaseMetaData读取字段信息（通用方式）
     * 
     * @param metaData DatabaseMetaData对象
     * @param tableName 表名
     * @return 字段信息列表
     */
    private List<DbColumnInfo> readColumnsByMetaData(DatabaseMetaData metaData, String tableName) {
        List<DbColumnInfo> columns = new ArrayList<>();
        try (ResultSet rs = metaData.getColumns(databaseName, null, tableName, "%")) {
            while (rs.next()) {
                DbColumnInfo column = new DbColumnInfo();
                column.setColumnName(rs.getString("COLUMN_NAME"));
                // 拼接类型：如 "VARCHAR(50)"
                String typeName = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                if (columnSize > 0) {
                    column.setColumnType(typeName + "(" + columnSize + ")");
                } else {
                    column.setColumnType(typeName);
                }
                column.setIsNullable("YES".equals(rs.getString("IS_NULLABLE")) ? "YES" : "NO");
                column.setDefaultValue(rs.getString("COLUMN_DEF"));
                column.setColumnComment(rs.getString("REMARKS"));
                column.setAutoIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT")));
                column.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));

                columns.add(column);
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询字段信息失败（表：" + tableName + "）：" + e.getMessage(), e);
        }
        return columns;
    }

    /**
     * 标记表中的主键字段（MySQL版本，通过information_schema）
     *
     * @param tableInfo 需要标记主键的表信息对象
     */
    private void markPrimaryKeys(DbTableInfo tableInfo) {
        String sql = "SELECT COLUMN_NAME FROM information_schema.KEY_COLUMN_USAGE "
                   + "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND CONSTRAINT_NAME = 'PRIMARY'";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, databaseName);
            stmt.setString(2, tableInfo.getTableName());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String pkColumn = rs.getString("COLUMN_NAME");
                    for (DbColumnInfo column : tableInfo.getColumns()) {
                        if (column.getColumnName().equals(pkColumn)) {
                            column.setPrimaryKey(true);
                            break;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询主键信息失败：" + e.getMessage(), e);
        }
    }

    /**
     * 通过DatabaseMetaData标记主键字段（通用方式）
     * 
     * @param metaData DatabaseMetaData对象
     * @param tableInfo 需要标记主键的表信息对象
     */
    private void markPrimaryKeysByMetaData(DatabaseMetaData metaData, DbTableInfo tableInfo) {
        try (ResultSet rs = metaData.getPrimaryKeys(databaseName, null, tableInfo.getTableName())) {
            while (rs.next()) {
                String pkColumn = rs.getString("COLUMN_NAME");
                for (DbColumnInfo column : tableInfo.getColumns()) {
                    if (column.getColumnName().equals(pkColumn)) {
                        column.setPrimaryKey(true);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询主键信息失败：" + e.getMessage(), e);
        }
    }

    /**
     * 读取指定数据库中的指定表列表
     * 
     * <p>如果传入的表名列表为空或null，则返回所有表的信息。</p>
     * 
     * @param tableNames 需要读取的表名列表，null或空列表表示读取所有表
     * @return 表信息列表
     */
    public List<DbTableInfo> readTables(List<String> tableNames) {
        if (tableNames == null || tableNames.isEmpty()) {
            // 表名列表为空，读取所有表
            return readAllTables();
        }

        List<DbTableInfo> allTables = readAllTables();
        List<DbTableInfo> result = new ArrayList<>();

        // 过滤出指定表名的表信息
        for (String name : tableNames) {
            for (DbTableInfo table : allTables) {
                if (table.getTableName().equalsIgnoreCase(name)) {
                    result.add(table);
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称字符串
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * 获取数据库中所有表的简要信息（只含表名和注释，不含字段详情）
     */
    public List<DbTableInfo> readTableSummaries() {
        List<DbTableInfo> list = new ArrayList<>();
        logger.info("开始查询表列表，数据库类型：{}，数据库名称：{}", dbType, databaseName);

        if ("mysql".equals(dbType)) {
            // MySQL 使用 information_schema 查询
            String sql = "SELECT TABLE_NAME, TABLE_COMMENT, ENGINE, TABLE_COLLATION, TABLE_TYPE "
                    + "FROM information_schema.TABLES "
                    + "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE' "
                    + "ORDER BY TABLE_NAME";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, databaseName);
                logger.debug("执行SQL：{}，参数：{}", sql, databaseName);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        DbTableInfo t = new DbTableInfo();
                        t.setTableName(rs.getString("TABLE_NAME"));
                        t.setTableComment(rs.getString("TABLE_COMMENT"));
                        t.setEngine(rs.getString("ENGINE"));
                        t.setCharset(rs.getString("TABLE_COLLATION"));
                        t.setTableType(rs.getString("TABLE_TYPE"));
                        list.add(t);
                        logger.debug("找到表：{}", t.getTableName());
                    }
                }
            } catch (SQLException e) {
                logger.error("查询表列表失败：{}", e.getMessage(), e);
                throw new RuntimeException("查询表列表失败：" + e.getMessage(), e);
            }
        } else {
            // 其他数据库使用 DatabaseMetaData 通用方式
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                try (ResultSet rs = metaData.getTables(databaseName, null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        DbTableInfo t = new DbTableInfo();
                        t.setTableName(rs.getString("TABLE_NAME"));
                        t.setTableComment(rs.getString("REMARKS"));
                        t.setTableType(rs.getString("TABLE_TYPE"));
                        list.add(t);
                    }
                }
            } catch (SQLException e) {
                logger.error("查询表列表失败：{}", e.getMessage(), e);
                throw new RuntimeException("查询表列表失败：" + e.getMessage(), e);
            }
        }

        logger.info("查询完成，共找到 {} 张表", list.size());
        return list;
    }

    /**
     * 关闭数据库连接
     * 
     * <p>使用完后必须调用此方法释放数据库连接资源。
     * 建议在finally块中调用或在try-with-resources中使用。</p>
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.warn("关闭数据库连接时发生异常：{}", e.getMessage());
            }
        }
    }
}