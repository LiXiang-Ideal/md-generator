package com.mdgenerator.server.controller;

import com.mdgenerator.server.apidoc.ApiDocGenerator;
import com.mdgenerator.server.apidoc.ApiInfo;
import com.mdgenerator.server.apidoc.ControllerParser;
import com.mdgenerator.server.config.PathValidator;
import com.mdgenerator.server.core.MdDocument;
import com.mdgenerator.server.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * API接口文档生成接口
 */
@RestController
@RequestMapping("/api/apidoc")
public class ApiDocController {

    /**
     * 扫描Controller源码目录生成API文档
     *
     * @param params 包含: sourceDir(源码目录路径), title(可选文档标题)
     */
    @PostMapping("/generate")
    public ApiResponse<Map<String, Object>> generateApiDoc(@RequestBody Map<String, String> params) {
        String sourceDir = params.get("sourceDir");
        String title = params.getOrDefault("title", "API接口文档");

        if (sourceDir == null || sourceDir.trim().isEmpty()) {
            return ApiResponse.error(400, "源码目录路径为必填项");
        }

        // 安全校验：防止路径遍历攻击
        String validatedPath = PathValidator.validateDirectory(sourceDir);

        ControllerParser parser = new ControllerParser();
        Map<String, List<ApiInfo>> controllerApis = parser.parseControllers(validatedPath);

        if (controllerApis.isEmpty()) {
            return ApiResponse.error(400, "未在源码目录中找到Controller类");
        }

        ApiDocGenerator docGenerator = new ApiDocGenerator().setLanguage(
            params.getOrDefault("language", "zh"));
        MdDocument document = docGenerator.generate(controllerApis, title);

        Map<String, Object> result = new HashMap<>();
        result.put("markdown", document.toString());
        result.put("controllerCount", controllerApis.size());

        List<Map<String, Object>> controllers = new ArrayList<>();
        for (Map.Entry<String, List<ApiInfo>> entry : controllerApis.entrySet()) {
            Map<String, Object> ctrl = new HashMap<>();
            ctrl.put("name", entry.getKey());
            List<ApiInfo> apis = entry.getValue();
            ctrl.put("tag", apis.isEmpty() ? "" : (apis.get(0).getTag() != null ? apis.get(0).getTag() : ""));
            ctrl.put("apiCount", apis.size());

            List<Map<String, Object>> apiList = new ArrayList<>();
            for (ApiInfo api : apis) {
                Map<String, Object> apiMap = new HashMap<>();
                apiMap.put("path", api.getPath());
                apiMap.put("httpMethod", api.getHttpMethod());
                apiMap.put("summary", api.getSummary());
                apiMap.put("controllerClass", api.getControllerClass());
                apiMap.put("methodName", api.getMethodName());
                apiMap.put("returnType", api.getReturnType());
                apiMap.put("deprecated", api.isDeprecated());

                List<Map<String, Object>> paramsList = new ArrayList<>();
                for (ApiInfo.ApiParam p : api.getParameters()) {
                    Map<String, Object> pm = new HashMap<>();
                    pm.put("name", p.getName());
                    pm.put("paramType", p.getParamType());
                    pm.put("dataType", p.getDataType());
                    pm.put("required", p.isRequired());
                    pm.put("description", p.getDescription());
                    pm.put("defaultValue", p.getDefaultValue());
                    paramsList.add(pm);
                }
                apiMap.put("parameters", paramsList);
                apiList.add(apiMap);
            }
            ctrl.put("apis", apiList);
            controllers.add(ctrl);
        }
        result.put("controllers", controllers);

        return ApiResponse.success(result);
    }
}
