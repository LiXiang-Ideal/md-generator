package com.mdgenerator.server.core;

import java.util.*;

/**
 * Markdown文档内容封装类
 * 
 * <p>封装了完整的Markdown文档内容，包括元数据（标题、作者、日期等）
 * 和文档正文内容。作为MdBuilder构建结果的载体。</p>
 * 
 * <p>设计模式：这是一个简单的POJO（Plain Old Java Object），
 * 用于在模块间传递已构建完成的Markdown文档。</p>
 * 
 * @author MD Generator Team
 * @version 1.0.0
 */
public class MdDocument {

    /** 文档标题（通常作为一级标题出现在文档开头） */
    private String title;

    /** 文档作者 */
    private String author;

    /** 文档创建日期（格式：yyyy-MM-dd） */
    private String date;

    /** 文档版本号（如"1.0.0"） */
    private String version;

    /** 文档正文内容（完整的Markdown格式字符串） */
    private String content;

    /** 文档生成的唯一标识（UUID格式） */
    private String docId;

    /**
     * 默认构造函数
     * 
     * <p>初始化文档ID为随机UUID，日期为当前日期</p>
     */
    public MdDocument() {
        this.docId = UUID.randomUUID().toString();
        // 设置默认日期为今天
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        this.date = sdf.format(new java.util.Date());
    }

    /**
     * 带标题的构造函数
     * 
     * @param title 文档标题
     */
    public MdDocument(String title) {
        this();
        this.title = title;
    }

    /**
     * 带标题和内容的构造函数
     * 
     * @param title 文档标题
     * @param content 文档正文内容（Markdown格式）
     */
    public MdDocument(String title, String content) {
        this(title);
        this.content = content;
    }

    // ==================== Getter方法 ====================

    /**
     * 获取文档标题
     * 
     * @return 文档标题字符串，未设置时返回null
     */
    public String getTitle() {
        return title;
    }

    /**
     * 获取文档作者
     * 
     * @return 作者名称，未设置时返回null
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 获取文档日期
     * 
     * @return 日期字符串（格式：yyyy-MM-dd）
     */
    public String getDate() {
        return date;
    }

    /**
     * 获取文档版本号
     * 
     * @return 版本号字符串
     */
    public String getVersion() {
        return version;
    }

    /**
     * 获取文档正文内容
     * 
     * @return 完整的Markdown格式内容字符串
     */
    public String getContent() {
        return content;
    }

    /**
     * 获取文档唯一标识
     * 
     * @return UUID格式的文档ID
     */
    public String getDocId() {
        return docId;
    }

    // ==================== Setter方法（支持链式调用） ====================

    /**
     * 设置文档标题
     * 
     * @param title 标题文本
     * @return 当前MdDocument实例，支持链式调用
     */
    public MdDocument setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置文档作者
     * 
     * @param author 作者名称
     * @return 当前MdDocument实例，支持链式调用
     */
    public MdDocument setAuthor(String author) {
        this.author = author;
        return this;
    }

    /**
     * 设置文档日期
     * 
     * @param date 日期字符串（建议格式：yyyy-MM-dd）
     * @return 当前MdDocument实例，支持链式调用
     */
    public MdDocument setDate(String date) {
        this.date = date;
        return this;
    }

    /**
     * 设置文档版本号
     * 
     * @param version 版本号（如"1.0.0"）
     * @return 当前MdDocument实例，支持链式调用
     */
    public MdDocument setVersion(String version) {
        this.version = version;
        return this;
    }

    /**
     * 设置文档正文内容
     * 
     * @param content Markdown格式的正文内容
     * @return 当前MdDocument实例，支持链式调用
     */
    public MdDocument setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * 获取文档的完整摘要信息
     * 
     * <p>返回包含标题、作者、日期、版本和ID的综合信息字符串</p>
     * 
     * @return 格式化的摘要信息
     */
    public String getSummary() {
        return String.format(
            "文档摘要: [ID=%s] 标题=%s, 作者=%s, 日期=%s, 版本=%s",
            docId, title != null ? title : "未设置",
            author != null ? author : "未设置",
            date, version != null ? version : "未设置"
        );
    }

    /**
     * 重写toString方法，返回完整文档内容
     * 
     * @return 包含头部元信息和正文的完整文档字符串
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("title: ").append(title != null ? title : "").append("\n");
        sb.append("author: ").append(author != null ? author : "").append("\n");
        sb.append("date: ").append(date).append("\n");
        sb.append("version: ").append(version != null ? version : "").append("\n");
        sb.append("id: ").append(docId).append("\n");
        sb.append("---\n\n");
        sb.append(content != null ? content : "");
        return sb.toString();
    }

    /**
     * 判断文档是否包含有效内容
     * 
     * @return 如果content不为null且非空，返回true
     */
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }
}