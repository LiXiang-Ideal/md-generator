package com.mdgenerator.server.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MdBuilder单元测试
 */
class MdBuilderTest {

    private MdBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new MdBuilder();
    }

    @Test
    @DisplayName("测试添加一级标题")
    void testAddH1() {
        String result = builder.addH1("测试标题").build();
        assertTrue(result.contains("# 测试标题"));
    }

    @Test
    @DisplayName("测试添加多级标题")
    void testAddMultipleHeadings() {
        String result = builder.addH1("一级").addH2("二级").addH3("三级").build();
        assertTrue(result.contains("# 一级"));
        assertTrue(result.contains("## 二级"));
        assertTrue(result.contains("### 三级"));
    }

    @Test
    @DisplayName("测试添加段落")
    void testAddParagraph() {
        String result = builder.addParagraph("这是一个段落").build();
        assertTrue(result.contains("这是一个段落"));
        assertTrue(result.endsWith("\n"));
    }

    @Test
    @DisplayName("测试添加代码块")
    void testAddCodeBlock() {
        String result = builder.addCodeBlock("System.out.println()", "java").build();
        assertTrue(result.contains("```java"));
        assertTrue(result.contains("System.out.println()"));
        assertTrue(result.contains("```"));
    }

    @Test
    @DisplayName("测试添加表格")
    void testAddTable() {
        List<String> headers = Arrays.asList("姓名", "年龄");
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("张三", "25"),
                Arrays.asList("李四", "30")
        );

        String result = builder.addTable(headers, rows).build();
        assertTrue(result.contains("| 姓名 |"));
        assertTrue(result.contains("| 张三 |"));
        assertTrue(result.contains("---"));
    }

    @Test
    @DisplayName("测试添加无序列表")
    void testAddUnorderedList() {
        String result = builder.addUnorderedList("项目1", "项目2", "项目3").build();
        assertTrue(result.contains("- 项目1"));
        assertTrue(result.contains("- 项目2"));
        assertTrue(result.contains("- 项目3"));
    }

    @Test
    @DisplayName("测试添加有序列表")
    void testAddOrderedList() {
        String result = builder.addOrderedList("步骤1", "步骤2").build();
        assertTrue(result.contains("1. 步骤1"));
        assertTrue(result.contains("2. 步骤2"));
    }

    @Test
    @DisplayName("测试添加引用块")
    void testAddBlockQuote() {
        String result = builder.addBlockQuote("这是一段引用").build();
        assertTrue(result.contains("> 这是一段引用"));
    }

    @Test
    @DisplayName("测试添加链接")
    void testAddLink() {
        String result = builder.addLink("百度", "https://www.baidu.com").build();
        assertTrue(result.contains("[百度](https://www.baidu.com)"));
    }

    @Test
    @DisplayName("测试链式调用")
    void testChainedCalls() {
        String result = builder
                .addH1("文档标题")
                .addParagraph("文档描述")
                .addCodeBlock("code", "java")
                .addHorizontalRule()
                .build();

        assertTrue(result.contains("# 文档标题"));
        assertTrue(result.contains("文档描述"));
        assertTrue(result.contains("```java"));
        assertTrue(result.contains("---"));
    }

    @Test
    @DisplayName("测试null标题处理")
    void testNullTitle() {
        String result = builder.addH1(null).build();
        assertTrue(result.contains("# "));
    }

    @Test
    @DisplayName("测试表格空数据")
    void testEmptyTable() {
        assertThrows(IllegalArgumentException.class, () -> {
            builder.addTable(null, null);
        });
    }

    @Test
    @DisplayName("测试重置功能")
    void testReset() {
        builder.addH1("标题").addParagraph("内容");
        assertEquals(0, builder.reset().length());
    }

    @Test
    @DisplayName("测试性能：大表格生成")
    void testLargeTablePerformance() {
        List<String> headers = Arrays.asList("ID", "名称", "描述");
        List<List<String>> rows = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            rows.add(Arrays.asList(String.valueOf(i), "名称" + i, "描述" + i));
        }

        long start = System.currentTimeMillis();
        String result = builder.addTable(headers, rows).build();
        long elapsed = System.currentTimeMillis() - start;

        assertTrue(result.contains("| ID |"));
        assertTrue(elapsed < 1000, "大表格生成应该在1秒内完成，实际耗时: " + elapsed + "ms");
    }

    @Test
    @DisplayName("测试null段落处理")
    void testNullParagraph() {
        String result = builder.addParagraph(null).build();
        assertFalse(result.contains("null"));
    }

    @Test
    @DisplayName("测试null粗体处理")
    void testNullBold() {
        String result = builder.addBold(null).build();
        assertEquals("****", result);
    }

    @Test
    @DisplayName("测试null斜体处理")
    void testNullItalic() {
        String result = builder.addItalic(null).build();
        assertEquals("**", result);
    }

    @Test
    @DisplayName("测试null行内代码处理")
    void testNullInlineCode() {
        String result = builder.addInlineCode(null).build();
        assertEquals("``", result);
    }

    @Test
    @DisplayName("测试null删除线处理")
    void testNullStrikethrough() {
        String result = builder.addStrikethrough(null).build();
        assertEquals("~~~~", result);
    }

    @Test
    @DisplayName("测试任务列表")
    void testTaskList() {
        String result = builder.addTaskList(
            new String[]{"任务1", "任务2"},
            new boolean[]{true, false}
        ).build();
        assertTrue(result.contains("- [x] 任务1"));
        assertTrue(result.contains("- [ ] 任务2"));
    }

    @Test
    @DisplayName("测试添加注释")
    void testAddComment() {
        String result = builder.addComment("这是一个注释").build();
        assertTrue(result.contains("<!-- 这是一个注释 -->"));
    }

    @Test
    @DisplayName("测试添加折叠面板")
    void testAddCollapsible() {
        String result = builder.addCollapsible("点击展开", "隐藏内容").build();
        assertTrue(result.contains("<details>"));
        assertTrue(result.contains("<summary>点击展开</summary>"));
        assertTrue(result.contains("隐藏内容"));
        assertTrue(result.contains("</details>"));
    }

    @Test
    @DisplayName("测试添加目录")
    void testAddTableOfContents() {
        String[][] headings = {
            {"Introduction", "1"},
            {"Getting Started", "2"},
            {"Installation", "3"}
        };
        String result = builder.addTableOfContents(headings).build();
        assertTrue(result.contains("## 目录"));
        assertTrue(result.contains("[Introduction](#introduction)"));
        assertTrue(result.contains("[Getting Started](#getting-started)"));
        assertTrue(result.contains("[Installation](#installation)"));
    }

    @Test
    @DisplayName("测试添加图片")
    void testAddImage() {
        String result = builder.addImage("logo", "https://example.com/logo.png", "My Logo").build();
        assertTrue(result.contains("![logo](https://example.com/logo.png \"My Logo\")"));
    }

    @Test
    @DisplayName("测试添加Badge")
    void testAddBadge() {
        String result = builder.addBadge("build", "passing", "green").build();
        assertTrue(result.contains("![build](https://img.shields.io/badge/build-passing-green)"));
    }

    @Test
    @DisplayName("测试添加空行和换行")
    void testAddEmptyLineAndLineBreak() {
        String result = builder.addParagraph("line1").addLineBreak().addParagraph("line2").build();
        assertTrue(result.contains("line1"));
        assertTrue(result.contains("line2"));
    }

    @Test
    @DisplayName("测试appendRaw")
    void testAppendRaw() {
        String result = builder.addH1("Title").appendRaw("<!-- raw -->").build();
        assertTrue(result.contains("# Title"));
        assertTrue(result.contains("<!-- raw -->"));
    }

    @Test
    @DisplayName("测试saveToFile")
    void testSaveToFile() {
        builder.addH1("Test Document").addParagraph("Content here");
        String tempDir = System.getProperty("java.io.tmpdir");
        String filePath = builder.saveToFile(tempDir, "test_doc");
        assertTrue(filePath.endsWith(".md"));
        java.io.File file = new java.io.File(filePath);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);
        file.delete();
    }
}
