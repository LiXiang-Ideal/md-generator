package com.mdgenerator.server.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonMdFormatter单元测试
 */
class CommonMdFormatterTest {

    @Test
    @DisplayName("测试JSON数组转表格")
    void testJsonToTableArray() {
        String json = "[{\"name\":\"张三\",\"age\":25},{\"name\":\"李四\",\"age\":30}]";
        String result = CommonMdFormatter.jsonToTable(json);

        assertTrue(result.contains("| name | age |"));
        assertTrue(result.contains("| 张三 | 25 |"));
        assertTrue(result.contains("| 李四 | 30 |"));
    }

    @Test
    @DisplayName("测试JSON对象转表格")
    void testJsonToTableObject() {
        String json = "{\"name\":\"王五\",\"city\":\"北京\"}";
        String result = CommonMdFormatter.jsonToTable(json);

        assertTrue(result.contains("| 字段 | 值 |"));
        assertTrue(result.contains("| name | 王五 |"));
        assertTrue(result.contains("| city | 北京 |"));
    }

    @Test
    @DisplayName("测试JSON转代码块")
    void testJsonToCodeBlock() {
        String json = "{\"test\":123}";
        String result = CommonMdFormatter.jsonToCodeBlock(json);

        assertTrue(result.contains("```json"));
        assertTrue(result.contains("\"test\": 123"));
        assertTrue(result.contains("```"));
    }

    @Test
    @DisplayName("测试ListMap转表格")
    void testListMapToTable() {
        List<Map<String, Object>> data = Arrays.asList(
                Map.of("id", 1, "name", "A"),
                Map.of("id", 2, "name", "B")
        );

        String result = CommonMdFormatter.listMapToTable(data);
        assertTrue(result.contains("| id | name |"));
        assertTrue(result.contains("| 1 | A |"));
        assertTrue(result.contains("| 2 | B |"));
    }

    @Test
    @DisplayName("测试CSV转表格")
    void testCsvToTable() {
        String csv = "姓名,年龄\n张三,25\n李四,30";
        String result = CommonMdFormatter.csvToTable(csv);

        assertTrue(result.contains("| 姓名 | 年龄 |"));
        assertTrue(result.contains("| 张三 | 25 |"));
    }

    @Test
    @DisplayName("测试CSV带引号字段")
    void testCsvWithQuotes() {
        String csv = "姓名,备注\n\"张三\",\"北京,海淀\"";
        String result = CommonMdFormatter.csvToTable(csv);

        assertTrue(result.contains("| 张三 | 北京,海淀 |"));
    }

    @Test
    @DisplayName("测试二维数组转表格")
    void testArrayToTable() {
        String[] headers = {"ID", "值"};
        String[][] data = {{"1", "A"}, {"2", "B"}};

        String result = CommonMdFormatter.arrayToTable(headers, data);
        assertTrue(result.contains("| ID | 值 |"));
        assertTrue(result.contains("| 1 | A |"));
    }

    @Test
    @DisplayName("测试空列表抛出异常")
    void testEmptyListMapThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            CommonMdFormatter.listMapToTable(null);
        });
    }

    @Test
    @DisplayName("测试空CSV返回空")
    void testEmptyCsvReturnsEmpty() {
        String result = CommonMdFormatter.csvToTable("");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试Key-Value转表格")
    void testKeyValueToTable() {
        Map<String, String> kv = new HashMap<>();
        kv.put("port", "8080");
        kv.put("host", "localhost");

        String result = CommonMdFormatter.keyValueToTable(kv);
        assertTrue(result.contains("| Name | Value |"));
        assertTrue(result.contains("| port | 8080 |"));
    }

    @Test
    @DisplayName("测试JSON带BOM头")
    void testJsonWithBOM() {
        String jsonWithBOM = "﻿{\"test\":123}";
        assertDoesNotThrow(() -> {
            CommonMdFormatter.jsonToTable(jsonWithBOM);
        });
    }

    @Test
    @DisplayName("测试非法JSON抛出异常")
    void testInvalidJsonThrowsException() {
        String invalidJson = "{invalid json";
        assertThrows(RuntimeException.class, () -> {
            CommonMdFormatter.jsonToTable(invalidJson);
        });
    }
}