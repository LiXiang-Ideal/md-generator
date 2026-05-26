package com.mdgenerator.server.config;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 路径安全验证工具
 *
 * <p>防止路径遍历攻击（Path Traversal），确保用户传入的路径参数是合法的、
 * 在允许的目录范围内访问。</p>
 *
 * @author MD Generator Team
 * @version 1.0.0
 */
public final class PathValidator {

    private PathValidator() {}

    /** 允许访问的根目录白名单（可配置） */
    private static final Set<String> ALLOWED_ROOTS = ConcurrentHashMap.newKeySet();

    static {
        ALLOWED_ROOTS.addAll(Arrays.asList(
                System.getProperty("user.home"),
                System.getProperty("java.io.tmpdir"),
                System.getProperty("user.dir")
        ));
    }

    /**
     * 验证路径是否合法且在允许范围内
     *
     * @param pathStr 用户传入的路径字符串
     * @return 规范化后的绝对路径
     * @throws InvalidPathException 如果路径不合法或超出允许范围
     */
    public static String validatePath(String pathStr) {
        if (pathStr == null || pathStr.trim().isEmpty()) {
            throw new InvalidPathException(pathStr, "路径不能为空");
        }

        // 规范化路径，消除 .. 和 . 等相对路径元素
        Path path;
        try {
            path = Paths.get(pathStr).toAbsolutePath().normalize();
        } catch (InvalidPathException e) {
            throw new InvalidPathException(pathStr, "路径格式不合法");
        }

        String absolutePath = path.toString();

        // 检查是否包含可疑字符
        if (pathStr.contains("\0") || pathStr.contains("..")) {
            throw new InvalidPathException(pathStr, "路径包含非法字符");
        }

        // 检查是否在允许的根目录下
        boolean isAllowed = ALLOWED_ROOTS.stream()
                .anyMatch(root -> absolutePath.startsWith(root));

        if (!isAllowed) {
            throw new InvalidPathException(pathStr, "路径超出允许访问范围");
        }

        // 检查路径是否实际存在（可选，根据业务需要）
        File file = path.toFile();
        if (!file.exists()) {
            throw new InvalidPathException(pathStr, "指定的路径不存在");
        }

        return absolutePath;
    }

    /**
     * 验证目录路径是否合法
     *
     * @param pathStr 用户传入的目录路径
     * @return 规范化后的绝对路径
     * @throws InvalidPathException 如果路径不合法或不是目录
     */
    public static String validateDirectory(String pathStr) {
        String absolutePath = validatePath(pathStr);
        File dir = new File(absolutePath);
        if (!dir.isDirectory()) {
            throw new InvalidPathException(pathStr, "指定的路径不是目录");
        }
        return absolutePath;
    }

    /**
     * 添加允许的根目录
     *
     * @param rootPath 允许访问的根目录路径
     */
    public static void addAllowedRoot(String rootPath) {
        if (rootPath != null && !rootPath.trim().isEmpty()) {
            ALLOWED_ROOTS.add(Paths.get(rootPath).toAbsolutePath().normalize().toString());
        }
    }
}
