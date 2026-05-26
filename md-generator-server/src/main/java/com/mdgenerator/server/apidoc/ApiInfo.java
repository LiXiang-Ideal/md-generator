package com.mdgenerator.server.apidoc;

/**
 * API接口信息封装类
 * 
 * <p>封装一个HTTP API接口的完整信息，包括请求路径、HTTP方法、
 * 参数列表、返回值等。在接口文档生成过程中作为数据传输对象（DTO）使用。</p>
 * 
 * <p>支持的注解识别：</p>
 * <ul>
 *   <li>@RequestMapping / @GetMapping / @PostMapping / @PutMapping / @DeleteMapping</li>
 *   <li>@RequestParam / @PathVariable / @RequestBody</li>
 *   <li>@RequestHeader</li>
 *   <li>@ApiOperation（Swagger注解，用于获取接口描述）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * ApiInfo api = new ApiInfo();
 * api.setPath("/user/{id}");
 * api.setHttpMethod("GET");
 * api.setSummary("根据ID查询用户信息");
 * api.setControllerClass("UserController");
 * api.addParameter(new ApiParam("id", "路径参数", "Long", true, "用户ID"));
 * }</pre>
 * 
 * @author MD Generator Team
 * @version 1.0.0
 */
public class ApiInfo {

    /** 接口的访问路径（原始路径，如 "/user/{id}"） */
    private String path;

    /** HTTP请求方法（GET, POST, PUT, DELETE, PATCH等） */
    private String httpMethod;

    /** 接口描述/摘要信息 */
    private String summary;

    /** 接口详细说明 */
    private String description;

    /** 所属的Controller类名 */
    private String controllerClass;

    /** 处理该接口的Java方法名 */
    private String methodName;

    /** 接口的请求参数列表 */
    private java.util.List<ApiParam> parameters;

    /** 接口的返回值类型（Java完整类名） */
    private String returnType;

    /** 接口的返回值说明 */
    private String returnDescription;

    /** 接口所属的分组/标签（通常在Controller类的@RequestMapping中定义） */
    private String tag;

    /** 是否标记为已废弃（@Deprecated注解） */
    private boolean deprecated;

    /**
     * 请求参数信息内部类
     * 
     * <p>封装单个API参数的完整信息，支持路径参数（@PathVariable）、
     * 查询参数（@RequestParam）和请求体参数（@RequestBody）三种类型。</p>
     */
    public static class ApiParam {

        /** 参数名称（对应@RequestParam的value或变量名） */
        private String name;

        /** 参数类型标识：path/query/body/header */
        private String paramType;

        /** 参数的数据类型（Java类型，如"String"、"Long"） */
        private String dataType;

        /** 是否必须传递此参数 */
        private boolean required;

        /** 参数说明/描述信息 */
        private String description;

        /** 参数的默认值 */
        private String defaultValue;

        /**
         * 默认构造函数
         */
        public ApiParam() {
        }

        /**
         * 全参数构造函数
         * 
         * @param name 参数名称
         * @param paramType 参数类型（path/query/body/header）
         * @param dataType 数据类型
         * @param required 是否必须
         * @param description 参数说明
         */
        public ApiParam(String name, String paramType, String dataType, boolean required, String description) {
            this.name = name;
            this.paramType = paramType;
            this.dataType = dataType;
            this.required = required;
            this.description = description;
        }

        /**
         * 获取参数名称
         * @return 参数名
         */
        public String getName() {
            return name;
        }

        /**
         * 设置参数名称
         * @param name 参数名
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * 获取参数类型（path/query/body/header）
         * @return 参数类型标识
         */
        public String getParamType() {
            return paramType;
        }

        /**
         * 设置参数类型
         * @param paramType 参数类型标识
         */
        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        /**
         * 获取数据类型
         * @return Java数据类型字符串
         */
        public String getDataType() {
            return dataType;
        }

        /**
         * 设置数据类型
         * @param dataType Java数据类型字符串
         */
        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        /**
         * 是否必须参数
         * @return true表示必须传递
         */
        public boolean isRequired() {
            return required;
        }

        /**
         * 设置是否必须
         * @param required 是否必须
         */
        public void setRequired(boolean required) {
            this.required = required;
        }

        /**
         * 获取参数说明
         * @return 描述文本
         */
        public String getDescription() {
            return description;
        }

        /**
         * 设置参数说明
         * @param description 描述文本
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * 获取默认值
         * @return 默认值字符串
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

        @Override
        public String toString() {
            return "ApiParam{name='" + name + "', type='" + paramType + "', dataType='" + dataType + "'}";
        }
    }

    /**
     * 默认构造函数，初始化参数列表
     */
    public ApiInfo() {
        this.parameters = new java.util.ArrayList<>();
    }

    // ==================== Getter和Setter方法 ====================

    /**
     * 获取接口路径
     * @return 访问路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置接口路径
     * @param path 访问路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取HTTP方法
     * @return 如"GET"、"POST"
     */
    public String getHttpMethod() {
        return httpMethod;
    }

    /**
     * 设置HTTP方法
     * @param httpMethod 如"GET"、"POST"
     */
    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * 获取接口摘要
     * @return 摘要文本
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 设置接口摘要
     * @param summary 摘要文本
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 获取接口详细说明
     * @return 说明文本
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置接口详细说明
     * @param description 说明文本
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取Controller类名
     * @return 类名（不含包名）
     */
    public String getControllerClass() {
        return controllerClass;
    }

    /**
     * 设置Controller类名
     * @param controllerClass 类名
     */
    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    /**
     * 获取方法名
     * @return Java方法名
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 设置方法名
     * @param methodName Java方法名
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * 获取参数列表
     * @return 参数信息列表
     */
    public java.util.List<ApiParam> getParameters() {
        return parameters;
    }

    /**
     * 设置参数列表
     * @param parameters 参数信息列表
     */
    public void setParameters(java.util.List<ApiParam> parameters) {
        this.parameters = parameters;
    }

    /**
     * 添加一个参数
     * 
     * @param parameter 参数信息对象
     */
    public void addParameter(ApiParam parameter) {
        this.parameters.add(parameter);
    }

    /**
     * 获取返回值类型
     * @return Java完整类名
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * 设置返回值类型
     * @param returnType Java完整类名
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /**
     * 获取返回值说明
     * @return 说明文本
     */
    public String getReturnDescription() {
        return returnDescription;
    }

    /**
     * 设置返回值说明
     * @param returnDescription 说明文本
     */
    public void setReturnDescription(String returnDescription) {
        this.returnDescription = returnDescription;
    }

    /**
     * 获取接口标签/分组
     * @return 标签名称
     */
    public String getTag() {
        return tag;
    }

    /**
     * 设置接口标签/分组
     * @param tag 标签名称
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * 是否已废弃
     * @return true表示已废弃
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * 设置是否废弃
     * @param deprecated 是否废弃
     */
    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * 获取参数的完整描述信息
     * 
     * @return 包含所有参数信息的字符串
     */
    public String getParametersDescription() {
        if (parameters.isEmpty()) {
            return "无参数";
        }
        StringBuilder sb = new StringBuilder();
        for (ApiParam param : parameters) {
            sb.append(param.getName())
              .append("(").append(param.getParamType()).append(")")
              .append(": ").append(param.getDataType());
            if (param.isRequired()) {
                sb.append(" [必填]");
            }
            if (param.getDescription() != null && !param.getDescription().isEmpty()) {
                sb.append(" - ").append(param.getDescription());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return httpMethod + " " + path + " -> " + (summary != null ? summary : methodName);
    }
}