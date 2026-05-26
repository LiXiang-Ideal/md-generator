package com.mdgenerator.server.common;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件操作工具类
 * 
 * <p>提供文件读写、目录创建、路径处理等通用文件操作功能。
 * 所有方法均为静态方法，可直接通过类名调用。</p>
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>统一使用UTF-8编码，确保跨平台兼容性</li>
 *   <li>自动创建不存在的父目录，简化调用方逻辑</li>
 *   <li>所有IO异常都会转换为RuntimeException向上抛出，简化调用方异常处理</li>
 * </ul>
 * 
 * @author MD Generator Team
 * @version 1.0.0
 */
public final class FileUtil {

    /**
     * 私有构造函数，防止实例化工具类
     */
    private FileUtil() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    /**
     * 将字符串内容写入文件
     * 
     * <p>该方法会自动创建不存在的父目录，并使用UTF-8编码写入。</p>
     * 
     * @param filePath 目标文件的绝对路径
     * @param content 需要写入的文本内容
     * @throws RuntimeException 如果写入过程中发生IO异常
     */
    public static void writeStringToFile(String filePath, String content) {
        File file = new File(filePath);

        // 确保父目录存在，如果不存在则递归创建
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new RuntimeException("无法创建目录：" + parentDir.getAbsolutePath());
            }
        }

        // 使用try-with-resources确保流被正确关闭
        // Java 8的FileWriter不支持直接传入Charset，使用OutputStreamWriter包装
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
             java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
            writer.write(content);
            writer.flush(); // 强制刷新缓冲区，确保内容写入磁盘
        } catch (IOException e) {
            throw new RuntimeException("写入文件失败：" + filePath + "，原因：" + e.getMessage(), e);
        }
    }

    /**
     * 读取文本文件内容
     * 
     * <p>使用NIO的Files.readAllBytes读取完整文件内容，适用于中小型文本文件。</p>
     * 
     * @param filePath 文件路径
     * @return 文件的完整文本内容
     * @throws RuntimeException 如果文件不存在或读取失败
     */
    public static String readFileToString(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new RuntimeException("文件不存在：" + filePath);
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes, StandardCharsets.UTF_8);
            // 自动去除UTF-8 BOM头（Windows记事本保存UTF-8文件时会添加BOM）
            if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
                content = content.substring(1);
            }
            return content;
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败：" + filePath + "，原因：" + e.getMessage(), e);
        }
    }

    /**
     * 生成带时间戳的输出文件名
     * 
     * <p>文件名格式：基础名称_yyyyMMdd_HHmmss.md</p>
     * <p>例如：readme_20240521_143052.md</p>
     * 
     * <p>时间戳的作用是防止多次生成时覆盖之前的文件，
     * 用户可以通过文件名中的时间戳区分不同的生成版本。</p>
     * 
     * @param baseName 文件的基础名称（如"database_doc"、"api_doc"、"readme"）
     * @return 带时间戳的完整文件名
     */
    public static String generateTimestampFileName(String baseName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return baseName + "_" + timestamp + ".md";
    }

    /**
     * 判断文件是否存在
     * 
     * @param filePath 文件路径
     * @return 如果文件存在返回true，否则返回false
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 创建目录（递归创建所有不存在的父目录）
     * 
     * @param dirPath 目录路径
     * @return 如果创建成功返回true
     */
    public static boolean createDirectories(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            return dir.mkdirs();
        }
        return true;
    }

    /**
     * 获取文件名（不含扩展名）
     * 
     * <p>例如输入 "database_doc.md" 返回 "database_doc"</p>
     * 
     * @param fileName 完整文件名
     * @return 不含扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return fileName;
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    /**
     * 将文件路径转换为跨平台格式
     * 
     * <p>将路径中的反斜杠(\)替换为正斜杠(/)或系统默认分隔符</p>
     * 
     * @param filePath 原始路径
     * @return 规范化后的路径
     */
    public static String normalizePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        // 将所有反斜杠替换为正斜杠
        return filePath.replace("\\", "/");
    }
}