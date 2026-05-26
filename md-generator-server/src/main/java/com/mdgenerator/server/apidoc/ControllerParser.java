package com.mdgenerator.server.apidoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

import com.mdgenerator.server.apidoc.ApiInfo.ApiParam;

/**
 * Java Controller源码解析器
 * 
 * <p>负责扫描指定目录下的Java源文件，识别Spring MVC Controller类，
 * 并从中提取API接口信息（路径、HTTP方法、参数、返回值等）。</p>
 * 
 * <p>解析过程：</p>
 * <ol>
 *   <li>递归扫描指定目录下的所有.java文件</li>
 *   <li>使用JavaParser解析每个文件为AST（抽象语法树）</li>
 *   <li>识别标记了@RestController或@Controller注解的类</li>
 *   <li>提取类级别的@RequestMapping路径（作为基础路径前缀）</li>
 *   <li>遍历类中所有public方法，识别带有@RequestMapping/@GetMapping/@PostMapping等注解的方法</li>
 *   <li>从方法注解中提取HTTP方法和路径</li>
 *   <li>从方法参数中提取参数信息（@RequestParam、@PathVariable、@RequestBody等）</li>
 *   <li>收集所有接口信息，按Controller分组返回</li>
 * </ol>
 * 
 * <p>注意：</p>
 * <ul>
 *   <li>仅解析源码中的注解信息，不分析实际业务逻辑</li>
 *   <li>不依赖编译环境，直接解析.java源文件文本</li>
 *   <li>对于@RequestBody类型的参数，dataType显示为参数类的简单名称</li>
 * </ul>
 * 
 * @author MD Generator Team
 * @version 1.0.0
 */
public class ControllerParser {

    /**
     * 扫描指定目录下的所有Java源文件，解析Controller类中的API接口信息
     * 
     * <p>这是Controller解析的入口方法。它会递归扫描源码目录，
     * 找到所有Java文件，解析注解，提取API接口信息。</p>
     * 
     * @param sourceDir 源码目录路径（如"src/main/java/com/example/controller"）
     * @return 按Controller分组的API信息Map，key是Controller类名，value是该Controller下的接口列表
     * @throws RuntimeException 如果扫描或解析过程中发生错误
     */
    public Map<String, List<ApiInfo>> parseControllers(String sourceDir) {
        Map<String, List<ApiInfo>> controllerApis = new LinkedHashMap<>();
        File dir = new File(sourceDir);

        if (!dir.exists()) {
            throw new RuntimeException("源码目录不存在：" + sourceDir);
        }

        // 递归扫描目录下的所有.java文件
        List<File> javaFiles = new ArrayList<>();
        collectJavaFiles(dir, javaFiles);

        // 对每个Java文件进行解析
        for (File javaFile : javaFiles) {
            try {
                parseJavaFile(javaFile, controllerApis);
            } catch (Exception e) {
                // 单个文件解析失败不应中断整个扫描过程，打印警告后继续
                System.err.println("警告：解析文件失败 " + javaFile.getAbsolutePath() + "：" + e.getMessage());
            }
        }

        return controllerApis;
    }

    /**
     * 递归收集目录下所有.java源文件
     * 
     * <p>使用递归深度优先遍历目录树，收集所有以.java结尾的文件。
     * 不会进入隐藏目录（以.开头的目录）。</p>
     * 
     * @param dir 目录文件对象
     * @param javaFiles 用于收集结果的列表
     */
    private void collectJavaFiles(File dir, List<File> javaFiles) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory() && !file.getName().startsWith(".")) {
                // 递归处理子目录
                collectJavaFiles(file, javaFiles);
            } else if (file.getName().endsWith(".java")) {
                // 收集.java源文件
                javaFiles.add(file);
            }
        }
    }

    /**
     * 解析单个Java源文件，提取其中的Controller类及其接口信息
     * 
     * <p>使用JavaParser将Java源文件解析为AST（抽象语法树），
     * 然后遍历AST中的类型声明和成员方法，提取API信息。</p>
     * 
     * <p>JavaParser是一种源码级别的解析工具，它不需要编译环境，
     * 可以直接解析.java源文件文本。AST以树形结构表示源代码的所有元素：
     * 包声明、导入、类声明、方法声明、注解、参数等。</p>
     * 
     * @param javaFile Java源文件对象
     * @param controllerApis 用于收集结果的Map
     */
    private void parseJavaFile(File javaFile, Map<String, List<ApiInfo>> controllerApis) throws FileNotFoundException {
        // 使用JavaParser解析Java源文件为CompilationUnit（编译单元）
        CompilationUnit cu = StaticJavaParser.parse(javaFile);

        // 解析包名，用于后续生成完整的路径信息参考
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        // 遍历编译单元中所有的类型声明（类、接口、枚举）
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            // 检查类是否标注了@RestController或@Controller注解
            if (isControllerClass(classDecl)) {
                // 解析该Controller类中的所有API接口
                List<ApiInfo> apis = parseControllerClass(classDecl, packageName);
                if (!apis.isEmpty()) {
                    controllerApis.put(classDecl.getNameAsString(), apis);
                }
            }
        });
    }

    /**
     * 判断一个类声明是否是一个Spring MVC Controller
     * 
     * <p>判断标准：检查类上是否标注了以下任一注解</p>
     * <ul>
     *   <li>@RestController</li>
     *   <li>@Controller</li>
     * </ul>
     * 
     * @param classDecl 类声明节点
     * @return 如果是Controller类返回true
     */
    private boolean isControllerClass(ClassOrInterfaceDeclaration classDecl) {
        return classDecl.isAnnotationPresent("RestController")
            || classDecl.isAnnotationPresent("Controller");
    }

    /**
     * 解析一个Controller类，提取其所有API接口信息
     * 
     * <p>处理步骤：</p>
     * <ol>
     *   <li>获取类级别的@RequestMapping路径（作为基础路径前缀）</li>
     *   <li>遍历类中的所有方法，识别带有HTTP方法映射注解的方法</li>
     *   <li>提取每个方法的路径、HTTP方法、参数、返回值等完整信息</li>
     * </ol>
     * 
     * @param classDecl 类声明节点
     * @param packageName 包名
     * @return 该Controller中所有API接口的列表
     */
    private List<ApiInfo> parseControllerClass(ClassOrInterfaceDeclaration classDecl, String packageName) {
        List<ApiInfo> apis = new ArrayList<>();

        // 获取类级别的基础路径前缀
        // 例如：@RequestMapping("/api/user") → "/api/user"
        String basePath = getRequestMappingPath(classDecl);

        // 遍历类中声明的所有方法
        for (MethodDeclaration method : classDecl.getMethods()) {
            // 解析方法上的HTTP映射注解
            ApiInfo api = parseApiMethod(method, basePath, classDecl.getNameAsString());
            if (api != null) {
                apis.add(api);
            }
        }

        return apis;
    }

    /**
     * 解析单个方法，提取其API接口信息
     * 
     * <p>首先检查方法上是否有Spring MVC映射注解（@RequestMapping @GetMapping等），
     * 如果有则提取完整信息；如果没有则返回null，表示该方法不是API接口。</p>
     * 
     * @param method 方法声明节点
     * @param basePath Controller类的基础路径
     * @param controllerName Controller类名
     * @return API信息对象，如果不是接口方法则返回null
     */
    private ApiInfo parseApiMethod(MethodDeclaration method, String basePath, String controllerName) {
        ApiInfo api = new ApiInfo();
        api.setControllerClass(controllerName);
        api.setMethodName(method.getNameAsString());

        // 提取HTTP方法和路径信息
        String httpMethod = extractHttpMethod(method);
        String methodPath = getRequestMappingPath(method);

        // 如果方法没有HTTP映射注解，说明它不是API接口，返回null
        if (httpMethod == null && methodPath == null) {
            return null;
        }

        api.setHttpMethod(httpMethod != null ? httpMethod : "GET");

        // 拼接完整路径：Controller基础路径 + 方法路径
        String fullPath = buildFullPath(basePath, methodPath);
        api.setPath(fullPath);

        // 检查方法是否被@Deprecated标记
        api.setDeprecated(method.isAnnotationPresent(Deprecated.class));

        // 提取接口描述信息
        // 优先使用@ApiOperation注解的值，其次使用方法上的JavaDoc注释
        api.setSummary(extractApiSummary(method));

        // 解析方法参数（@RequestParam, @PathVariable, @RequestBody等）
        parseMethodParameters(method, api);

        // 解析返回值类型
        api.setReturnType(method.getType().asString());

        return api;
    }

    /**
     * 从方法注解中提取HTTP方法类型
     * 
     * <p>支持的Spring MVC映射注解：</p>
     * <ul>
     *   <li>@GetMapping → "GET"</li>
     *   <li>@PostMapping → "POST"</li>
     *   <li>@PutMapping → "PUT"</li>
     *   <li>@DeleteMapping → "DELETE"</li>
     *   <li>@PatchMapping → "PATCH"</li>
     *   <li>@RequestMapping(method = ...) → 提取method属性值</li>
     * </ul>
     * 
     * @param method 方法声明节点
     * @return HTTP方法字符串，如果无法确定则返回null
     */
    private String extractHttpMethod(MethodDeclaration method) {
        // 检查各个简化注解
        if (method.isAnnotationPresent("GetMapping")) return "GET";
        if (method.isAnnotationPresent("PostMapping")) return "POST";
        if (method.isAnnotationPresent("PutMapping")) return "PUT";
        if (method.isAnnotationPresent("DeleteMapping")) return "DELETE";
        if (method.isAnnotationPresent("PatchMapping")) return "PATCH";

        // 处理@RequestMapping注解，它的method属性指定了HTTP方法
        if (method.isAnnotationPresent("RequestMapping")) {
            return method.getAnnotationByName("RequestMapping")
                    .flatMap(ann -> {
                        // 获取method属性值
                        // 例如：@RequestMapping(value = "/user", method = RequestMethod.POST)
                        if (ann.isNormalAnnotationExpr()) {
                            return getAnnotationMemberValue(ann.asNormalAnnotationExpr(), "method");
                        }
                        return java.util.Optional.empty();
                    })
                    .orElse("GET"); // 默认为GET
        }

        return null;
    }

    /**
     * 从类或方法声明中提取@RequestMapping路径
     * 
     * <p>处理@RequestMapping注解的value或path属性。</p>
     * 
     * @param node 类声明节点或方法声明节点
     * @return 映射路径字符串，如果没有映射注解则返回null
     */
    private String getRequestMappingPath(NodeWithAnnotations<?> node) {
        String[] annotationNames = {"RequestMapping", "GetMapping", "PostMapping",
                                     "PutMapping", "DeleteMapping", "PatchMapping"};

        for (String annName : annotationNames) {
            if (node.isAnnotationPresent(annName)) {
                return node.getAnnotationByName(annName)
                        .flatMap(ann -> {
                            if (ann.isNormalAnnotationExpr()) {
                                // 尝试从value属性获取路径
                                java.util.Optional<String> value = getAnnotationMemberValue(
                                        ann.asNormalAnnotationExpr(), "value");
                                if (value.isPresent()) return value;

                                // 尝试从path属性获取路径
                                return getAnnotationMemberValue(
                                        ann.asNormalAnnotationExpr(), "path");
                            }
                            if (ann.isSingleMemberAnnotationExpr()) {
                                // 处理单一成员注解，如 @GetMapping("/user")
                                return java.util.Optional.of(
                                        ann.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString());
                            }
                            if (ann.isMarkerAnnotationExpr()) {
                                // Marker注解，没有参数（如 @GetMapping 没有写路径）
                                return java.util.Optional.of("");
                            }
                            return java.util.Optional.empty();
                        })
                        .orElse("");
            }
        }
        return null;
    }

    /**
     * 从NormalAnnotationExpr中获取指定成员的值
     * 
     * <p>JavaParser将注解表达式分为三种类型：</p>
     * <ul>
     *   <li>MarkerAnnotationExpr: @Override（无参数）</li>
     *   <li>SingleMemberAnnotationExpr: @GetMapping("/path")（单一值）</li>
     *   <li>NormalAnnotationExpr: @RequestMapping(value="/path", method=POST)（键值对形式）</li>
     * </ul>
     * 
     * <p>本方法处理NormalAnnotationExpr类型，查找指定名称的成员并返回其值。</p>
     * 
     * @param ann 注解表达式节点
     * @param memberName 成员名称
     * @return 成员值的Optional包装
     */
    private java.util.Optional<String> getAnnotationMemberValue(NormalAnnotationExpr ann, String memberName) {
        return ann.getPairs().stream()
                .filter(pair -> pair.getNameAsString().equals(memberName))
                .findFirst()
                .map(pair -> {
                    if (pair.getValue().isStringLiteralExpr()) {
                        // 值是字符串，如 value = "/user"
                        return pair.getValue().asStringLiteralExpr().asString();
                    } else if (pair.getValue().isFieldAccessExpr()) {
                        // 值是字段访问表达式，如 method = RequestMethod.POST
                        FieldAccessExpr fae = pair.getValue().asFieldAccessExpr();
                        return fae.getNameAsString();
                    }
                    return pair.getValue().toString();
                });
    }

    /**
     * 拼接Controller基础路径和方法路径，生成完整接口路径
     * 
     * <p>处理规则：</p>
     * <ul>
     *   <li>如果方法路径是绝对路径（以/开头），则直接使用</li>
     *   <li>否则，基路径 + "/" + 方法路径</li>
     *   <li>确保路径以/开头</li>
     * </ul>
     * 
     * @param basePath Controller基础路径
     * @param methodPath 方法级别的映射路径
     * @return 完整的接口访问路径
     */
    private String buildFullPath(String basePath, String methodPath) {
        // 如果方法路径是绝对路径（以/开头），直接使用它
        if (methodPath != null && methodPath.startsWith("/")) {
            return methodPath;
        }

        StringBuilder fullPath = new StringBuilder();

        // 添加Controller基础路径，确保以/开头
        if (basePath != null && !basePath.isEmpty()) {
            if (basePath.startsWith("/")) {
                fullPath.append(basePath);
            } else {
                fullPath.append("/").append(basePath);
            }
        }

        // 添加方法路径
        if (methodPath != null && !methodPath.isEmpty()) {
            if (!fullPath.toString().endsWith("/") && !methodPath.startsWith("/")) {
                fullPath.append("/");
            }
            fullPath.append(methodPath);
        }

        String result = fullPath.toString();
        // 确保最终路径以/开头
        return result.isEmpty() ? "/" : result;
    }

    /**
     * 提取API接口的描述信息
     * 
     * <p>优先级：</p>
     * <ol>
     *   <li>@ApiOperation注解的value属性（Swagger注解）</li>
     *   <li>@Operation注解的summary属性（Swagger 3 / OpenAPI注解）</li>
     *   <li>方法的JavaDoc注释（如果有的话）</li>
     *   <li>方法名（作为兜底）</li>
     * </ol>
     * 
     * @param method 方法声明节点
     * @return 接口描述字符串
     */
    private String extractApiSummary(MethodDeclaration method) {
        // 优先使用Swagger的@ApiOperation注解
        if (method.isAnnotationPresent("ApiOperation")) {
            return method.getAnnotationByName("ApiOperation")
                    .flatMap(ann -> {
                        if (ann.isNormalAnnotationExpr()) {
                            return getAnnotationMemberValue(ann.asNormalAnnotationExpr(), "value");
                        }
                        if (ann.isSingleMemberAnnotationExpr()) {
                            return java.util.Optional.of(
                                    ann.asSingleMemberAnnotationExpr().getMemberValue().toString());
                        }
                        return java.util.Optional.empty();
                    })
                    .orElse(method.getNameAsString());
        }

        // 其次使用Swagger 3 / OpenAPI的@Operation注解
        if (method.isAnnotationPresent("Operation")) {
            return method.getAnnotationByName("Operation")
                    .flatMap(ann -> {
                        if (ann.isNormalAnnotationExpr()) {
                            return getAnnotationMemberValue(ann.asNormalAnnotationExpr(), "summary");
                        }
                        return java.util.Optional.empty();
                    })
                    .orElse(method.getNameAsString());
        }

        // 兜底：使用方法名
        return method.getNameAsString();
    }

    /**
     * 解析方法的请求参数列表
     * 
     * <p>遍历方法的每个参数，识别Spring MVC参数注解：</p>
     * <ul>
     *   <li>@RequestParam → 查询参数（query类型），默认必须</li>
     *   <li>@PathVariable → 路径参数（path类型），默认必须</li>
     *   <li>@RequestBody → 请求体参数（body类型），默认必须</li>
     *   <li>@RequestHeader → 请求头参数（header类型）</li>
     * </ul>
     * 
     * <p>如果参数没有任何参数注解，则根据类型判断：</p>
     * <ul>
     *   <li>HttpServletRequest / HttpServletResponse / HttpSession等 → 忽略（框架注入参数）</li>
     *   <li>其他 → 作为query类型参数</li>
     * </ul>
     * 
     * @param method 方法声明节点
     * @param api API信息对象，用于填充参数列表
     */
    private void parseMethodParameters(MethodDeclaration method, ApiInfo api) {
        for (Parameter param : method.getParameters()) {
            String paramName = param.getNameAsString();
            String paramType = param.getType().asString();

            // 忽略框架注入的参数类型（HttpServletRequest等），这些不是API的业务参数
            if (isFrameworkParameter(paramType)) {
                continue;
            }

            ApiParam apiParam = new ApiParam();
            apiParam.setName(paramName);
            apiParam.setDataType(paramType);

            // 按照注解优先级判断参数类型
            if (param.isAnnotationPresent("RequestParam")) {
                // @RequestParam → query参数
                apiParam.setParamType("query");
                apiParam.setRequired(true); // 默认必须
                // 提取注解的value属性作为参数名（如果指定了的话）
                extractRequestParamDetail(param, apiParam);

            } else if (param.isAnnotationPresent("PathVariable")) {
                // @PathVariable → path参数
                apiParam.setParamType("path");
                apiParam.setRequired(true);
                // 提取注解的value属性
                param.getAnnotationByName("PathVariable")
                        .flatMap(ann -> {
                            if (ann.isNormalAnnotationExpr()) {
                                return getAnnotationMemberValue(ann.asNormalAnnotationExpr(), "value");
                            }
                            if (ann.isSingleMemberAnnotationExpr()) {
                                return java.util.Optional.of(
                                        ann.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString());
                            }
                            return java.util.Optional.empty();
                        })
                        .ifPresent(apiParam::setName);

            } else if (param.isAnnotationPresent("RequestBody")) {
                // @RequestBody → body参数，使用参数的类名作为dataType
                apiParam.setParamType("body");
                apiParam.setRequired(true);

            } else if (param.isAnnotationPresent("RequestHeader")) {
                // @RequestHeader → header参数
                apiParam.setParamType("header");
                apiParam.setRequired(true);

            } else {
                // 默认为query参数
                apiParam.setParamType("query");
                apiParam.setRequired(false);
            }

            api.addParameter(apiParam);
        }
    }

    /**
     * 提取@RequestParam注解的详细信息
     * 
     * <p>从注解中提取value（参数名）、required（是否必须）、
     * defaultValue（默认值）等属性。</p>
     * 
     * @param param 方法参数节点
     * @param apiParam API参数对象
     */
    private void extractRequestParamDetail(Parameter param, ApiParam apiParam) {
        param.getAnnotationByName("RequestParam").ifPresent(ann -> {
            if (ann.isNormalAnnotationExpr()) {
                NormalAnnotationExpr nae = ann.asNormalAnnotationExpr();

                // 提取value属性作为参数名
                getAnnotationMemberValue(nae, "value").ifPresent(apiParam::setName);

                // 提取required属性（是否需要）
                nae.getPairs().stream()
                        .filter(pair -> pair.getNameAsString().equals("required"))
                        .findFirst()
                        .ifPresent(pair -> {
                            String val = pair.getValue().toString();
                            apiParam.setRequired(!"false".equals(val));
                        });

                // 提取defaultValue属性
                getAnnotationMemberValue(nae, "defaultValue").ifPresent(apiParam::setDefaultValue);

            } else if (ann.isSingleMemberAnnotationExpr()) {
                // @RequestParam("paramName") 简写形式
                apiParam.setName(
                        ann.asSingleMemberAnnotationExpr().getMemberValue().asStringLiteralExpr().asString());
            }
        });
    }

    /**
     * 判断参数类型是否是Spring框架自动注入的参数
     * 
     * <p>以下类型的参数会被Spring MVC框架自动注入，不需要在API文档中展示：</p>
     * <ul>
     *   <li>HttpServletRequest</li>
     *   <li>HttpServletResponse</li>
     *   <li>HttpSession</li>
     *   <li>Model / ModelMap / ModelAndView</li>
     *   <li>BindingResult / Errors</li>
     *   <li>Principal</li>
     * </ul>
     * 
     * @param paramType 参数类型全限定名
     * @return 如果是框架注入参数返回true
     */
    private boolean isFrameworkParameter(String paramType) {
        return paramType.contains("HttpServletRequest")
            || paramType.contains("HttpServletResponse")
            || paramType.contains("HttpSession")
            || paramType.contains("ModelAndView")
            || paramType.contains("ModelMap")
            || paramType.equals("Model")
            || paramType.contains("BindingResult")
            || paramType.contains("Errors")
            || paramType.contains("Principal");
    }
}