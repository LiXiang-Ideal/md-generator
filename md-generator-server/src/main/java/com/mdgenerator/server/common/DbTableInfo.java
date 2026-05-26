package com.mdgenerator.server.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库表结构信息封装类
 * 
 * <p>用于存储单张数据库表的完整结构信息，包括表名、备注、
 * 字段列表等。在数据库文档生成过程中作为数据传输对象（DTO）使用。</p>
 * 
 * <p>字段信息的内部类TableColumn包含了一个字段（列）的所有属性：
 * 字段名、数据类型、是否可为空、默认值、备注等。</p>
 * 
 * @author MD Generator Team
 * @version 1.0.0
 */
public class DbTableInfo {

    /** 表名（物理表名，如 "user_info"） */
    private String tableName;

    /** 表注释/备注（创建表时指定的COMMENT内容） */
    private String tableComment;

    /** 表的字符集编码（如 "utf8mb4"） */
    private String charset;

    /** 表的存储引擎（如 "InnoDB"、"MyISAM"） */
    private String engine;

    /** 是否为基础表（BASE TABLE）或视图（VIEW） */
    private String tableType;

    /** 该表的字段列表 */
    private List<DbColumnInfo> columns;

    /**
     * 数据库字段信息内部类
     * 
     * <p>对应JDBC中ResultSetMetaData的一列（字段）信息，
     * 包含字段名、类型、注释等完整属性。</p>
     */
    public static class DbColumnInfo {

        /** 字段名（物理列名，如 "user_id"） */
        private String columnName;

        /** 字段类型（如 "VARCHAR(50)"、"INT(11)"、"DATETIME"） */
        private String columnType;

        /** 是否为NULL，YES表示可为空，NO表示不可为空 */
        private String isNullable;

        /** 字段默认值 */
        private String defaultValue;

        /** 字段注释/备注（创建表时指定的COMMENT内容） */
        private String columnComment;

        /** 是否为主键字段 */
        private boolean primaryKey;

        /** 是否自动递增 */
        private boolean autoIncrement;

        /** 字段在表中的顺序位置（从1开始） */
        private int ordinalPosition;

        /**
         * 获取字段名
         * @return 物理列名
         */
        public String getColumnName() {
            return columnName;
        }

        /**
         * 设置字段名
         * @param columnName 物理列名
         */
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        /**
         * 获取字段类型（包含长度/精度信息）
         * @return 完整的类型字符串，如"VARCHAR(50)"
         */
        public String getColumnType() {
            return columnType;
        }

        /**
         * 设置字段类型
         * @param columnType 类型字符串
         */
        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }

        /**
         * 获取是否可为空
         * @return "YES"表示可为空，"NO"表示不可为空，null表示未知
         */
        public String getIsNullable() {
            return isNullable;
        }

        /**
         * 设置是否可为空
         * @param isNullable "YES"或"NO"
         */
        public void setIsNullable(String isNullable) {
            this.isNullable = isNullable;
        }

        /**
         * 获取默认值
         * @return 字段的默认值表达式，null表示无默认值
         */
        public String getDefaultValue() {
            return defaultValue;
        }

        /**
         * 设置默认值
         * @param defaultValue 默认值字符串
         */
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * 获取字段注释
         * @return COMMENT内容
         */
        public String getColumnComment() {
            return columnComment;
        }

        /**
         * 设置字段注释
         * @param columnComment COMMENT内容
         */
        public void setColumnComment(String columnComment) {
            this.columnComment = columnComment;
        }

        /**
         * 是否为主键
         * @return true表示是主键字段
         */
        public boolean isPrimaryKey() {
            return primaryKey;
        }

        /**
         * 设置是否为主键
         * @param primaryKey 是否主键
         */
        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }

        /**
         * 是否自动递增
         * @return true表示是自增字段
         */
        public boolean isAutoIncrement() {
            return autoIncrement;
        }

        /**
         * 设置是否自动递增
         * @param autoIncrement 是否自增
         */
        public void setAutoIncrement(boolean autoIncrement) {
            this.autoIncrement = autoIncrement;
        }

        /**
         * 获取字段在表中的顺序位置
         * @return 顺序号（从1开始）
         */
        public int getOrdinalPosition() {
            return ordinalPosition;
        }

        /**
         * 设置字段在表中的顺序位置
         * @param ordinalPosition 顺序号
         */
        public void setOrdinalPosition(int ordinalPosition) {
            this.ordinalPosition = ordinalPosition;
        }
    }

    /**
     * 默认构造函数，初始化columns列表
     */
    public DbTableInfo() {
        this.columns = new ArrayList<>();
    }

    /**
     * 带表名的构造函数
     * 
     * @param tableName 表名
     */
    public DbTableInfo(String tableName) {
        this();
        this.tableName = tableName;
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取表名
     * @return 物理表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 设置表名
     * @param tableName 物理表名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 获取表注释
     * @return 表的COMMENT内容
     */
    public String getTableComment() {
        return tableComment;
    }

    /**
     * 设置表注释
     * @param tableComment 表的COMMENT内容
     */
    public void setTableComment(String tableComment) {
        this.tableComment = tableComment;
    }

    /**
     * 获取字符集编码
     * @return 如"utf8mb4"
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 设置字符集编码
     * @param charset 字符集名称
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * 获取存储引擎
     * @return 如"InnoDB"
     */
    public String getEngine() {
        return engine;
    }

    /**
     * 设置存储引擎
     * @param engine 引擎名称
     */
    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * 获取表类型（BASE TABLE / VIEW）
     * @return 表类型字符串
     */
    public String getTableType() {
        return tableType;
    }

    /**
     * 设置表类型
     * @param tableType 表类型
     */
    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    /**
     * 获取字段列表
     * @return 字段信息列表
     */
    public List<DbColumnInfo> getColumns() {
        return columns;
    }

    /**
     * 设置字段列表
     * @param columns 字段信息列表
     */
    public void setColumns(List<DbColumnInfo> columns) {
        this.columns = columns;
    }

    /**
     * 添加一个字段信息到列表中
     * 
     * @param column 字段信息对象
     */
    public void addColumn(DbColumnInfo column) {
        this.columns.add(column);
    }

    /**
     * 获取字段数量
     * 
     * @return 该表的字段总数
     */
    public int getColumnCount() {
        return columns != null ? columns.size() : 0;
    }

    /**
     * 获取表的显示名称
     * 
     * <p>优先返回表注释（更有可读性），如果没有注释则返回物理表名</p>
     * 
     * @return 表的显示名称
     */
    public String getDisplayName() {
        return (tableComment != null && !tableComment.isEmpty()) ? tableComment : tableName;
    }

    @Override
    public String toString() {
        return "DbTableInfo{"
            + "tableName='" + tableName + '\''
            + ", tableComment='" + tableComment + '\''
            + ", columns=" + getColumnCount() + "个字段"
            + '}';
    }
}