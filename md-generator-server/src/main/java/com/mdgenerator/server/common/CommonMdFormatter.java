package com.mdgenerator.server.common;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mdgenerator.server.core.MdBuilder;

/**
 * 通用数据→Markdown格式化工具
 * 
 * <p>提供将各种常见数据格式转换为Markdown表示的功能。
 * 支持JSON数据、CSV数据、List/Map集合、SQL查询结果等。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>JSON字符串→Markdown表格/代码块</li>
 *   <li>List&lt;Map&gt;→Markdown表格</li>
 *   <li>CSV文本→Markdown表格</li>
 *   <li>二维数组→Markdown表格</li>
 *   <li>SQL SELECT结果→Markdown表格</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * String json = "[{\"name\":\"张三\",\"age\":25},{\"name\":\"李四\",\"age\":30}]";
 * String md = CommonMdFormatter.jsonToTable(json);
 * }</pre>
 * 
 * <p>设计说明：
 * 所有方法均为静态方法，作为工具类直接调用。
 * 内部使用Jackson进行JSON解析，使用MdBuilder进行Markdown构建。</p>
 * 
 * @author MD Generator Team
 * @version 1.0.0
 */
public final class CommonMdFormatter {

    /** Jackson ObjectMapper实例，线程安全，使用static final单例模式 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 私有构造函数，防止实例化工具类
     */
    private CommonMdFormatter() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将JSON数组字符串转换为Markdown表格
     * 
     * <p>支持两种JSON格式：</p>
     * <ul>
     *   <li>JSON数组：[{"key1": "val1"}, {"key2": "val2"}]</li>
     *   <li>JSON对象（会被包裹为单行表格）</li>
     * </ul>
     * 
     * <p>表格列名会自动从JSON对象的key中提取。
     * 所有值都会转为String类型后填充到表格中。</p>
     * 
     * <p>示例输入：</p>
     * <pre>{@code
     * [{"name": "张三", "age": 25}, {"name": "李四", "age": 30}]
     * }</pre>
     * 
     * <p>输出：</p>
     * <pre>
     * | name | age |
     * | ---- | --- |
     * | 张三  | 25  |
     * | 李四  | 30  |
     * </pre>
     * 
     * @param json JSON数组字符串
     * @return Markdown表格格式的字符串
     * @throws RuntimeException 如果JSON解析失败或数据为空
     */
    public static String jsonToTable(String json) {
        try {
            // 去除BOM头（UTF-8 BOM: 0xEF 0xBB 0xBF），兼容Windows记事本保存的UTF-8文件
            if (json != null && !json.isEmpty() && json.charAt(0) == '\uFEFF') {
                json = json.substring(1);
            }
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);

            // 收集所有唯一的键名作为表头
            Set<String> allKeys = new LinkedHashSet<>();

            if (rootNode.isArray()) {
                // 遍历数组，收集所有可能的键
                ArrayNode arrayNode = (ArrayNode) rootNode;
                for (JsonNode item : arrayNode) {
                    if (item.isObject()) {
                        ObjectNode objNode = (ObjectNode) item;
                        objNode.fieldNames().forEachRemaining(allKeys::add);
                    }
                }

                List<String> headers = new ArrayList<>(allKeys);
                List<List<String>> rows = new ArrayList<>();

                // 为每个数组元素构建一行数据
                for (JsonNode item : arrayNode) {
                    List<String> row = new ArrayList<>();
                    for (String key : allKeys) {
                        JsonNode valueNode = item.get(key);
                        if (valueNode != null && !valueNode.isNull()) {
                            // 处理字符串值（去掉引号），其他类型保持原样
                            row.add(valueNode.isTextual() ? valueNode.asText() : valueNode.toString());
                        } else {
                            row.add("-");
                        }
                    }
                    rows.add(row);
                }

                // 使用MdBuilder构建表格
                MdBuilder builder = new MdBuilder();
                builder.addTable(headers, rows);
                return builder.build();

            } else if (rootNode.isObject()) {
                // 单个JSON对象 → 转为键值对表格
                ObjectNode objNode = (ObjectNode) rootNode;
                List<String> headers = Arrays.asList("字段", "值");
                List<List<String>> rows = new ArrayList<>();

                Iterator<String> fieldNames = objNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode value = objNode.get(fieldName);
                    String valueStr = value.isTextual() ? value.asText() : value.toString();
                    rows.add(Arrays.asList(fieldName, valueStr));
                }

                MdBuilder builder = new MdBuilder();
                builder.addTable(headers, rows);
                return builder.build();
            }

            return "";

        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将JSON字符串格式化为Markdown代码块（带语法高亮）
     * 
     * <p>对JSON进行美化排版（缩进2空格），然后放入带json语言标识的代码块中。
     * 这比表格更适合展示复杂的嵌套JSON结构。</p>
     * 
     * @param json JSON字符串（可以是非格式化的单行JSON）
     * @return 包含美化JSON的Markdown代码块
     * @throws RuntimeException 如果JSON解析失败
     */
    public static String jsonToCodeBlock(String json) {
        try {
            // 去除BOM头
            if (json != null && !json.isEmpty() && json.charAt(0) == '\uFEFF') {
                json = json.substring(1);
            }
            // 先解析再美化输出，确保JSON格式正确且美观
            Object parsed = OBJECT_MAPPER.readValue(json, Object.class);
            String prettyJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(parsed);

            MdBuilder builder = new MdBuilder();
            builder.addCodeBlock(prettyJson, "json");
            return builder.build();

        } catch (Exception e) {
            throw new RuntimeException("JSON格式化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将List&lt;Map&gt;集合转换为Markdown表格
     * 
     * <p>适用于从数据库中查询出的结果集（通常以List&lt;Map&gt;形式返回）。
     * 表头从第一行Map的key集合中提取，保持插入顺序。</p>
     * 
     * <p>示例：</p>
     * <pre>{@code
     * List<Map<String, Object>> data = jdbcTemplate.queryForList("SELECT * FROM user");
     * String md = CommonMdFormatter.listMapToTable(data);
     * }</pre>
     * 
     * @param dataList 数据列表，每个Map表示一行数据
     * @return Markdown表格格式的字符串
     * @throws IllegalArgumentException 如果数据列表为null或为空
     */
    public static String listMapToTable(List<Map<String, Object>> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("数据列表不能为空");
        }

        // 从第一行提取表头键名（保持插入顺序用LinkedHashSet去重）
        Set<String> keySet = new LinkedHashSet<>(dataList.get(0).keySet());
        List<String> headers = new ArrayList<>(keySet);

        // 构建数据行
        List<List<String>> rows = new ArrayList<>();
        for (Map<String, Object> row : dataList) {
            List<String> rowData = new ArrayList<>();
            for (String key : headers) {
                Object value = row.get(key);
                // null值转为"-"显示
                rowData.add(value != null ? value.toString() : "-");
            }
            rows.add(rowData);
        }

        MdBuilder builder = new MdBuilder();
        builder.addTable(headers, rows);
        return builder.build();
    }

    /**
     * 将CSV格式文本转换为Markdown表格
     * 
     * <p>CSV（Comma-Separated Values）是一种常见的数据交换格式。
     * 第一行作为表头，后续行作为数据行。</p>
     * 
     * <p>支持的处理：</p>
     * <ul>
     *   <li>逗号分隔（默认）</li>
     *   <li>制表符分隔（通过delimiter参数指定）</li>
     *   <li>带引号的字段值（引号内的逗号不会作为分隔符）</li>
     * </ul>
     * 
     * @param csv CSV格式的文本
     * @param delimiter 分隔符（如","、"\t"、"|"等）
     * @return Markdown表格格式的字符串
     */
    public static String csvToTable(String csv, String delimiter) {
        if (csv == null || csv.trim().isEmpty()) {
            return "";
        }

        // 按行分割
        String[] lines = csv.trim().split("\\r?\\n");
        if (lines.length == 0) {
            return "";
        }

        List<List<String>> rows = new ArrayList<>();

        for (String line : lines) {
            // 解析CSV行（处理引号包裹的字段）
            List<String> cells = parseCsvLine(line, delimiter);
            rows.add(cells);
        }

        // 第一行作为表头
        List<String> headers = rows.remove(0);

        MdBuilder builder = new MdBuilder();
        builder.addTable(headers, rows);
        return builder.build();
    }

    /**
     * 使用默认逗号分隔符转换CSV
     * 
     * @param csv CSV格式文本
     * @return Markdown表格格式的字符串
     */
    public static String csvToTable(String csv) {
        return csvToTable(csv, ",");
    }

    /**
     * 解析CSV的一行数据
     * 
     * <p>处理引号内的分隔符（不会将引号内的逗号作为分隔符）。
     * 例如：`"Zhang, San",25,Beijing` 会被正确解析为3个字段。</p>
     * 
     * @param line CSV的一行文本
     * @param delimiter 分隔符
     * @return 解析后的字段列表
     */
    private static List<String> parseCsvLine(String line, String delimiter) {
        List<String> cells = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // 遇到引号，切换引号状态
                inQuotes = !inQuotes;
            } else if (ch == delimiter.charAt(0) && !inQuotes) {
                // 分隔符（不在引号内）
                cells.add(currentCell.toString().trim());
                currentCell.setLength(0);
            } else {
                // 普通字符
                currentCell.append(ch);
            }
        }
        // 添加最后一个字段
        cells.add(currentCell.toString().trim());

        return cells;
    }

    /**
     * 将二维字符串数组转换为Markdown表格
     * 
     * <p>这是最基础的表格生成方法，适用于所有以二维数组表示的数据。
     * 第一维是行，第二维是列。</p>
     * 
     * @param headers 表头数组
     * @param data 二维数据数组
     * @return Markdown表格格式的字符串
     */
    public static String arrayToTable(String[] headers, String[][] data) {
        MdBuilder builder = new MdBuilder();
        builder.addTable(headers, data);
        return builder.build();
    }

    /**
     * 将Java对象（POJO）格式化为一列两行的键值对表格
     * 
     * <p>使用Jackson将对象转为Map，然后生成"字段名-值"的表格。
     * 适用于展示单个对象的属性。</p>
     * 
     * @param object 任意Java对象
     * @return Markdown表格格式的字符串
     */
    @SuppressWarnings("unchecked")
    public static String objectToTable(Object object) {
        if (object == null) {
            return "";
        }

        try {
            // 将对象转为Map
            Map<String, Object> map = OBJECT_MAPPER.convertValue(object, Map.class);

            List<String> headers = Arrays.asList("属性", "值");
            List<List<String>> rows = new ArrayList<>();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object value = entry.getValue();
                rows.add(Arrays.asList(
                    entry.getKey(),
                    value != null ? value.toString() : "null"
                ));
            }

            MdBuilder builder = new MdBuilder();
            builder.addTable(headers, rows);
            return builder.build();

        } catch (Exception e) {
            throw new RuntimeException("对象转表格失败：" + e.getMessage(), e);
        }
    }

    /**
     * 生成键值对列表（无表头表格）
     * 
     * <p>适用于配置信息、环境变量等键值对数据的展示。
     * 格式为两列表格：键 | 值。</p>
     * 
     * @param kvPairs 键值对Map
     * @return Markdown表格格式的字符串
     */
    public static String keyValueToTable(Map<String, String> kvPairs) {
        if (kvPairs == null || kvPairs.isEmpty()) {
            return "";
        }

        List<String> headers = Arrays.asList("Name", "Value");
        List<List<String>> rows = new ArrayList<>();

        for (Map.Entry<String, String> entry : kvPairs.entrySet()) {
            rows.add(Arrays.asList(entry.getKey(), entry.getValue()));
        }

        MdBuilder builder = new MdBuilder();
        builder.addTable(headers, rows);
        return builder.build();
    }

    /**
     * 生成统计摘要表格
     * 
     * <p>适用于快速生成项目统计、性能指标等摘要信息。</p>
     * 
     * @param items 摘要项列表（键值对）
     * @return Markdown表格格式的字符串
     */
    public static String summaryTable(List<Map.Entry<String, String>> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }

        List<String> headers = Arrays.asList("指标", "数值");
        List<List<String>> rows = new ArrayList<>();

        for (Map.Entry<String, String> item : items) {
            rows.add(Arrays.asList(item.getKey(), item.getValue()));
        }

        MdBuilder builder = new MdBuilder();
        builder.addTable(headers, rows);
        return builder.build();
    }

    /**
     * 生成简单的单列表格（用于罗列清单项）
     * 
     * @param header 列名
     * @param items 清单项列表
     * @return Markdown表格格式的字符串
     */
    public static String singleColumnTable(String header, List<String> items) {
        List<String> headers = Arrays.asList(header);
        List<List<String>> rows = new ArrayList<>();

        for (String item : items) {
            rows.add(Arrays.asList(item));
        }

        MdBuilder builder = new MdBuilder();
        builder.addTable(headers, rows);
        return builder.build();
    }
}