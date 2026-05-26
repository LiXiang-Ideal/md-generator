package com.mdgenerator.server.controller;

import com.mdgenerator.server.common.CommonMdFormatter;
import com.mdgenerator.server.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 格式转换接口（JSON/CSV → Markdown）
 */
@RestController
@RequestMapping("/api/convert")
public class ConvertController {

    /**
     * JSON转Markdown表格或代码块
     */
    @PostMapping("/json")
    public ApiResponse<Map<String, String>> convertJson(@RequestBody Map<String, String> params) {
        String jsonData = params.get("data");
        String format = params.getOrDefault("format", "table");

        if (jsonData == null || jsonData.trim().isEmpty()) {
            return ApiResponse.error(400, "JSON数据不能为空");
        }

        String markdown;
        if ("code".equalsIgnoreCase(format)) {
            markdown = CommonMdFormatter.jsonToCodeBlock(jsonData);
        } else {
            markdown = CommonMdFormatter.jsonToTable(jsonData);
        }

        Map<String, String> result = new HashMap<>();
        result.put("markdown", markdown);
        result.put("format", format);

        return ApiResponse.success(result);
    }

    /**
     * CSV转Markdown表格
     */
    @PostMapping("/csv")
    public ApiResponse<Map<String, String>> convertCsv(@RequestBody Map<String, String> params) {
        String csvData = params.get("data");
        String delimiter = params.getOrDefault("delimiter", ",");

        if (csvData == null || csvData.trim().isEmpty()) {
            return ApiResponse.error(400, "CSV数据不能为空");
        }

        if ("\\t".equals(delimiter)) {
            delimiter = "\t";
        }

        String markdown = CommonMdFormatter.csvToTable(csvData, delimiter);

        Map<String, String> result = new HashMap<>();
        result.put("markdown", markdown);

        return ApiResponse.success(result);
    }

    /**
     * 手动创建表格
     */
    @PostMapping("/table")
    public ApiResponse<Map<String, String>> createTable(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<String> headers = (List<String>) params.get("headers");
        @SuppressWarnings("unchecked")
        List<List<String>> rows = (List<List<String>>) params.get("rows");

        if (headers == null || headers.isEmpty()) {
            return ApiResponse.error(400, "表头不能为空");
        }

        String[] h = headers.toArray(new String[0]);
        String[][] d = new String[rows != null ? rows.size() : 0][];
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                d[i] = rows.get(i).toArray(new String[0]);
            }
        }

        String markdown = CommonMdFormatter.arrayToTable(h, d);

        Map<String, String> result = new HashMap<>();
        result.put("markdown", markdown);

        return ApiResponse.success(result);
    }

    /**
     * 批量生成KV对表格
     */
    @PostMapping("/kv-table")
    public ApiResponse<Map<String, String>> createKvTable(@RequestBody Map<String, String> params) {
        Map<String, String> kvMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!"markdown".equals(entry.getKey())) {
                kvMap.put(entry.getKey(), entry.getValue());
            }
        }

        String markdown = CommonMdFormatter.keyValueToTable(kvMap);

        Map<String, String> result = new HashMap<>();
        result.put("markdown", markdown);

        return ApiResponse.success(result);
    }
}
