package com.mdgenerator.server.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class BuilderControllerTest {

    private final BuilderController controller = new BuilderController();

    @Test
    @DisplayName("测试构建基本元素列表")
    void testBuildBasicElements() {
        List<Map<String, Object>> elements = new ArrayList<>();

        Map<String, Object> heading = new HashMap<>();
        heading.put("type", "heading");
        heading.put("level", 1);
        heading.put("content", "Title");
        elements.add(heading);

        Map<String, Object> paragraph = new HashMap<>();
        paragraph.put("type", "paragraph");
        paragraph.put("content", "Hello World");
        elements.add(paragraph);

        var result = controller.buildMarkdown(elements);
        assertNotNull(result);
        String md = result.get("markdown");
        assertTrue(md.contains("# Title"));
        assertTrue(md.contains("Hello World"));
    }

    @Test
    @DisplayName("测试构建strikethrough元素")
    void testBuildStrikethrough() {
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> el = new HashMap<>();
        el.put("type", "strikethrough");
        el.put("text", "deleted");
        elements.add(el);

        var result = controller.buildMarkdown(elements);
        String md = result.get("markdown");
        assertTrue(md.contains("~~deleted~~"));
    }

    @Test
    @DisplayName("测试构建inline-code元素")
    void testBuildInlineCode() {
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> el = new HashMap<>();
        el.put("type", "inline-code");
        el.put("text", "var x = 1");
        elements.add(el);

        var result = controller.buildMarkdown(elements);
        String md = result.get("markdown");
        assertTrue(md.contains("`var x = 1`"));
    }

    @Test
    @DisplayName("测试构建task-list元素")
    void testBuildTaskList() {
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> el = new HashMap<>();
        el.put("type", "task-list");
        el.put("items", Arrays.asList("Task1", "Task2"));
        el.put("completions", Arrays.asList(true, false));
        elements.add(el);

        var result = controller.buildMarkdown(elements);
        String md = result.get("markdown");
        assertTrue(md.contains("- [x] Task1"));
        assertTrue(md.contains("- [ ] Task2"));
    }

    @Test
    @DisplayName("测试构建comment元素")
    void testBuildComment() {
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> el = new HashMap<>();
        el.put("type", "comment");
        el.put("content", "hidden note");
        elements.add(el);

        var result = controller.buildMarkdown(elements);
        String md = result.get("markdown");
        assertTrue(md.contains("<!-- hidden note -->"));
    }

    @Test
    @DisplayName("测试构建collapsible元素")
    void testBuildCollapsible() {
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> el = new HashMap<>();
        el.put("type", "collapsible");
        el.put("summary", "Click me");
        el.put("detail", "Hidden content");
        elements.add(el);

        var result = controller.buildMarkdown(elements);
        String md = result.get("markdown");
        assertTrue(md.contains("<details>"));
        assertTrue(md.contains("<summary>Click me</summary>"));
        assertTrue(md.contains("Hidden content"));
        assertTrue(md.contains("</details>"));
    }

    @Test
    @DisplayName("测试构建toc元素")
    void testBuildToc() {
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> el = new HashMap<>();
        el.put("type", "toc");
        List<Map<String, Object>> headings = new ArrayList<>();
        headings.add(Map.of("text", "Intro", "level", 1));
        headings.add(Map.of("text", "Setup", "level", 2));
        el.put("headings", headings);
        elements.add(el);

        var result = controller.buildMarkdown(elements);
        String md = result.get("markdown");
        assertTrue(md.contains("## 目录"));
        assertTrue(md.contains("[Intro](#intro)"));
        assertTrue(md.contains("[Setup](#setup)"));
    }

    @Test
    @DisplayName("测试未知type被忽略")
    void testUnknownTypeIgnored() {
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> el = new HashMap<>();
        el.put("type", "unknown-type");
        el.put("content", "something");
        elements.add(el);

        var result = controller.buildMarkdown(elements);
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试null type被跳过")
    void testNullTypeSkipped() {
        List<Map<String, Object>> elements = new ArrayList<>();
        Map<String, Object> el = new HashMap<>();
        el.put("content", "no type");
        elements.add(el);

        var result = controller.buildMarkdown(elements);
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试空元素列表")
    void testEmptyElements() {
        List<Map<String, Object>> elements = new ArrayList<>();
        var result = controller.buildMarkdown(elements);
        String md = result.get("markdown");
        assertTrue(md.isEmpty());
    }
}
