package com.mdgenerator.server.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseControllerTest {

    @Test
    @DisplayName("测试SessionInfo构造函数初始化时间戳")
    void testSessionInfoInitialization() throws Exception {
        Class<?> sessionInfoClass = Class.forName(
            "com.mdgenerator.server.controller.DatabaseController$SessionInfo");
        Constructor<?> constructor = sessionInfoClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object info = constructor.newInstance();

        long before = System.currentTimeMillis() - 1000;
        Method getCreatedAt = sessionInfoClass.getDeclaredField("createdAt");
        getCreatedAt.setAccessible(true);
        long createdAt = (long) getCreatedAt.get(info);

        Method getLastAccessedAt = sessionInfoClass.getDeclaredField("lastAccessedAt");
        getLastAccessedAt.setAccessible(true);
        long lastAccessedAt = (long) getLastAccessedAt.get(info);

        assertTrue(createdAt >= before, "createdAt should be recent");
        assertEquals(createdAt, lastAccessedAt, "lastAccessedAt should equal createdAt initially");
    }

    @Test
    @DisplayName("测试SessionInfo.touch()更新lastAccessedAt")
    void testSessionInfoTouch() throws Exception {
        Class<?> sessionInfoClass = Class.forName(
            "com.mdgenerator.server.controller.DatabaseController$SessionInfo");
        Constructor<?> constructor = sessionInfoClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object info = constructor.newInstance();

        Method getCreatedAt = sessionInfoClass.getDeclaredField("createdAt");
        getCreatedAt.setAccessible(true);
        long createdAt = (long) getCreatedAt.get(info);

        Thread.sleep(10);

        Method touchMethod = sessionInfoClass.getDeclaredMethod("touch");
        touchMethod.setAccessible(true);
        touchMethod.invoke(info);

        Method getLastAccessedAt = sessionInfoClass.getDeclaredField("lastAccessedAt");
        getLastAccessedAt.setAccessible(true);
        long lastAccessedAt = (long) getLastAccessedAt.get(info);

        assertTrue(lastAccessedAt > createdAt, "lastAccessedAt should be updated after touch()");
    }

    @Test
    @DisplayName("测试buildJdbcUrl支持多种数据库")
    void testBuildJdbcUrl() throws Exception {
        DatabaseController controller = new DatabaseController();
        Method method = DatabaseController.class.getDeclaredMethod("buildJdbcUrl", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        String mysqlUrl = (String) method.invoke(controller, "mysql", "localhost", "3306", "testdb");
        assertTrue(mysqlUrl.startsWith("jdbc:mysql://"));
        assertTrue(mysqlUrl.contains("testdb"));

        String pgUrl = (String) method.invoke(controller, "postgresql", "localhost", "5432", "testdb");
        assertTrue(pgUrl.startsWith("jdbc:postgresql://"));

        String oracleUrl = (String) method.invoke(controller, "oracle", "localhost", "1521", "testdb");
        assertTrue(oracleUrl.startsWith("jdbc:oracle:thin:@"));

        String sqlServerUrl = (String) method.invoke(controller, "sqlserver", "localhost", "1433", "testdb");
        assertTrue(sqlServerUrl.startsWith("jdbc:sqlserver://"));
    }

    @Test
    @DisplayName("测试getDefaultPort返回正确端口")
    void testGetDefaultPort() throws Exception {
        DatabaseController controller = new DatabaseController();
        Method method = DatabaseController.class.getDeclaredMethod("getDefaultPort", String.class);
        method.setAccessible(true);

        assertEquals("3306", method.invoke(controller, "mysql"));
        assertEquals("5432", method.invoke(controller, "postgresql"));
        assertEquals("1521", method.invoke(controller, "oracle"));
        assertEquals("1433", method.invoke(controller, "sqlserver"));
    }

    @Test
    @DisplayName("测试isValidIdentifier拒绝非法字符")
    void testIsValidIdentifier() throws Exception {
        DatabaseController controller = new DatabaseController();
        Method method = DatabaseController.class.getDeclaredMethod("isValidIdentifier", String.class);
        method.setAccessible(true);

        assertTrue((boolean) method.invoke(controller, "my_database"));
        assertTrue((boolean) method.invoke(controller, "MyDB123"));
        assertFalse((boolean) method.invoke(controller, "my-database"));
        assertFalse((boolean) method.invoke(controller, "db;DROP TABLE"));
        assertFalse((boolean) method.invoke(controller, (String) null));
        assertFalse((boolean) method.invoke(controller, ""));
    }

    @Test
    @DisplayName("测试buildJdbcUrl不支持的数据库类型抛异常")
    void testBuildJdbcUrlUnsupportedType() throws Exception {
        DatabaseController controller = new DatabaseController();
        Method method = DatabaseController.class.getDeclaredMethod("buildJdbcUrl", String.class, String.class, String.class, String.class);
        method.setAccessible(true);

        assertThrows(java.lang.reflect.InvocationTargetException.class, () -> {
            method.invoke(controller, "redis", "localhost", "6379", "testdb");
        });
    }
}
