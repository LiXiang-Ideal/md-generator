package com.mdgenerator.server.controller;

import com.mdgenerator.server.common.DbTableInfo;
import com.mdgenerator.server.core.MdDocument;
import com.mdgenerator.server.database.DbDocGenerator;
import com.mdgenerator.server.database.DbReader;
import com.mdgenerator.server.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据库文档生成接口
 */
@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    /** 会话存储：key=sessionId, value=DbReader */
    private final ConcurrentHashMap<String, DbReader> sessions = new ConcurrentHashMap<>();

    /** 会话元信息 */
    private final ConcurrentHashMap<String, SessionInfo> sessionInfo = new ConcurrentHashMap<>();

    private static class SessionInfo {
        String dbName;
        String dbType;
        long createdAt;
        volatile long lastAccessedAt;

        SessionInfo() {
            long now = System.currentTimeMillis();
            this.createdAt = now;
            this.lastAccessedAt = now;
        }

        void touch() {
            this.lastAccessedAt = System.currentTimeMillis();
        }
    }

    /**
     * 连接数据库并返回所有表的简要列表
     */
    @PostMapping("/connect")
    public ApiResponse<Map<String, Object>> connectAndListTables(@RequestBody Map<String, String> params) {
        String dbType = params.getOrDefault("dbType", "mysql");
        String host = params.getOrDefault("host", "localhost");
        String port = params.getOrDefault("port", getDefaultPort(dbType));
        String dbName = params.get("dbName");
        String username = params.getOrDefault("username", "root");
        String password = params.get("password");

        if (dbName == null || dbName.trim().isEmpty()) {
            return ApiResponse.error(400, "数据库名称为必填项");
        }
        if (password == null) {
            return ApiResponse.error(400, "密码为必填项");
        }

        // 参数校验：防止SQL注入
        if (!isValidIdentifier(dbName) || !isValidIdentifier(username)) {
            return ApiResponse.error(400, "数据库名称或用户名包含非法字符");
        }

        String jdbcUrl = buildJdbcUrl(dbType, host, port, dbName);
        DbReader reader;
        try {
            reader = new DbReader(jdbcUrl, username, password, dbType);
        } catch (Exception e) {
            logger.warn("数据库连接失败: {}", e.getMessage());
            return ApiResponse.error("连接失败: " + e.getMessage());
        }

        try {
            logger.info("开始查询表列表，数据库：{}, JDBC URL：{}", dbName, jdbcUrl);
            logger.info("当前连接的数据库：{}", reader.getDatabaseName());

            List<DbTableInfo> tables = reader.readTableSummaries();
            logger.info("查询到 {} 张表", tables.size());

            // 生成会话ID
            String sessionId = UUID.randomUUID().toString().replace("-", "");
            sessions.put(sessionId, reader);
            SessionInfo info = new SessionInfo();
            info.dbName = dbName;
            info.dbType = dbType;
            sessionInfo.put(sessionId, info);

            List<Map<String, Object>> result = tables.stream().map(t -> {
                Map<String, Object> m = new HashMap<>();
                m.put("tableName", t.getTableName());
                m.put("tableComment", t.getTableComment() != null ? t.getTableComment() : "");
                m.put("engine", t.getEngine());
                m.put("charset", t.getCharset());
                m.put("checked", false);
                return m;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("tables", result);
            response.put("dbType", dbType);
            response.put("dbName", dbName);

            return ApiResponse.success("已连接，共 " + tables.size() + " 张表", response);

        } catch (Exception e) {
            closeReader(reader);
            return ApiResponse.error("查询表列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据勾选的表生成文档
     */
    @PostMapping("/generate")
    public ApiResponse<Map<String, String>> generateSelectedTables(@RequestBody Map<String, Object> params) {
        String sessionId = (String) params.get("sessionId");
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ApiResponse.error(400, "会话ID为必填项，请先连接数据库");
        }

        DbReader reader = sessions.get(sessionId);
        if (reader == null) {
            return ApiResponse.error(400, "会话已过期，请重新连接");
        }

        SessionInfo info = sessionInfo.get(sessionId);
        if (info == null) {
            return ApiResponse.error(400, "会话信息丢失");
        }
        info.touch();

        @SuppressWarnings("unchecked")
        List<String> selectedTables = (List<String>) params.get("tables");
        if (selectedTables == null || selectedTables.isEmpty()) {
            return ApiResponse.error(400, "请至少选择一张表");
        }

        // 表名安全校验
        for (String tableName : selectedTables) {
            if (!isValidIdentifier(tableName)) {
                return ApiResponse.error(400, "表名包含非法字符: " + tableName);
            }
        }

        try {
            List<DbTableInfo> tables = reader.readTables(selectedTables);

            String lang = params.get("language") != null ? (String) params.get("language") : "zh";
            DbDocGenerator docGenerator = new DbDocGenerator().setLanguage(lang);
            MdDocument document = docGenerator.generate(tables, info.dbName, info.dbType);

            Map<String, String> result = new HashMap<>();
            result.put("markdown", document.toString());
            result.put("tableCount", String.valueOf(tables.size()));
            result.put("dbName", info.dbName);

            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("生成文档失败: {}", e.getMessage(), e);
            return ApiResponse.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 断开连接
     */
    @PostMapping("/disconnect")
    public ApiResponse<String> disconnect(@RequestBody Map<String, String> params) {
        String sessionId = params.get("sessionId");
        if (sessionId != null) {
            DbReader reader = sessions.remove(sessionId);
            sessionInfo.remove(sessionId);
            if (reader != null) {
                closeReader(reader);
            }
        }
        return ApiResponse.success("已断开连接");
    }

    /**
     * 清理过期会话（超过30分钟未使用）
     */
    @GetMapping("/cleanup")
    public ApiResponse<String> cleanupExpiredSessions() {
        long expireTime = System.currentTimeMillis() - 30 * 60 * 1000;
        int cleaned = 0;
        Iterator<Map.Entry<String, SessionInfo>> it = sessionInfo.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, SessionInfo> entry = it.next();
            if (entry.getValue().lastAccessedAt < expireTime) {
                String sessionId = entry.getKey();
                DbReader reader = sessions.remove(sessionId);
                it.remove();
                if (reader != null) {
                    closeReader(reader);
                }
                cleaned++;
            }
        }
        return ApiResponse.success("已清理 " + cleaned + " 个过期会话");
    }

    /**
     * 应用关闭时清理所有连接
     */
    @PreDestroy
    public void destroy() {
        for (DbReader reader : sessions.values()) {
            closeReader(reader);
        }
        sessions.clear();
        sessionInfo.clear();
        logger.info("所有数据库连接已关闭");
    }

    private void closeReader(DbReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * 构建JDBC URL（支持多种数据库）
     */
    private String buildJdbcUrl(String dbType, String host, String port, String dbName) {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                        + "?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
            case "postgresql":
                return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
            case "oracle":
                return "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
            case "sqlserver":
                return "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + dbName;
            default:
                throw new IllegalArgumentException("不支持的数据库类型: " + dbType);
        }
    }

    /**
     * 获取默认端口
     */
    private String getDefaultPort(String dbType) {
        switch (dbType.toLowerCase()) {
            case "mysql": return "3306";
            case "postgresql": return "5432";
            case "oracle": return "1521";
            case "sqlserver": return "1433";
            default: return "3306";
        }
    }

    /**
     * 校验标识符是否合法（防止SQL注入）
     * 只允许字母、数字、下划线
     */
    private boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) return false;
        return identifier.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }
}
