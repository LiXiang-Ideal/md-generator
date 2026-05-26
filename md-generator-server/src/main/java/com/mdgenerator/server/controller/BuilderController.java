package com.mdgenerator.server.controller;

import com.mdgenerator.server.core.MdBuilder;
import com.mdgenerator.server.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 可视化Markdown构建接口
 */
@RestController
@RequestMapping("/api/builder")
public class BuilderController {

    /**
     * 根据前端提交的元素列表生成Markdown
     */
    @PostMapping("/build")
    public ApiResponse<Map<String, String>> buildMarkdown(@RequestBody List<Map<String, Object>> elements) {
        MdBuilder builder = new MdBuilder();

        for (Map<String, Object> el : elements) {
            String type = (String) el.get("type");
            if (type == null) continue;

            switch (type) {
                case "heading": {
                    int level = ((Number) el.getOrDefault("level", 1)).intValue();
                    String content = (String) el.getOrDefault("content", "");
                    switch (level) {
                        case 1: builder.addH1(content); break;
                        case 2: builder.addH2(content); break;
                        case 3: builder.addH3(content); break;
                        case 4: builder.addH4(content); break;
                        case 5: builder.addH5(content); break;
                        default: builder.addH2(content);
                    }
                    break;
                }
                case "paragraph": {
                    builder.addParagraph((String) el.getOrDefault("content", ""));
                    break;
                }
                case "codeblock": {
                    builder.addCodeBlock(
                        (String) el.getOrDefault("content", ""),
                        (String) el.get("language")
                    );
                    break;
                }
                case "table": {
                    @SuppressWarnings("unchecked")
                    List<String> headers = (List<String>) el.get("headers");
                    @SuppressWarnings("unchecked")
                    List<List<String>> rows = (List<List<String>>) el.get("rows");
                    if (headers != null) {
                        builder.addTable(headers, rows != null ? rows : new ArrayList<>());
                    }
                    break;
                }
                case "blockquote": {
                    String content = (String) el.getOrDefault("content", "");
                    builder.addBlockQuote(content.split("\n"));
                    break;
                }
                case "unordered-list": {
                    @SuppressWarnings("unchecked")
                    List<String> items = (List<String>) el.get("items");
                    if (items != null) {
                        builder.addUnorderedList(items.toArray(new String[0]));
                    }
                    break;
                }
                case "ordered-list": {
                    @SuppressWarnings("unchecked")
                    List<String> items = (List<String>) el.get("items");
                    if (items != null) {
                        builder.addOrderedList(items.toArray(new String[0]));
                    }
                    break;
                }
                case "hr": {
                    builder.addHorizontalRule();
                    break;
                }
                case "badge": {
                    builder.addBadge(
                        (String) el.getOrDefault("label", ""),
                        (String) el.getOrDefault("message", ""),
                        (String) el.getOrDefault("color", "blue")
                    );
                    break;
                }
                case "link": {
                    builder.addLink(
                        (String) el.getOrDefault("text", ""),
                        (String) el.getOrDefault("url", "")
                    );
                    break;
                }
                case "image": {
                    builder.addImage(
                        (String) el.getOrDefault("altText", ""),
                        (String) el.getOrDefault("url", ""),
                        (String) el.get("title")
                    );
                    break;
                }
                case "bold": {
                    builder.addBold((String) el.getOrDefault("text", ""));
                    break;
                }
                case "italic": {
                    builder.addItalic((String) el.getOrDefault("text", ""));
                    break;
                }
                case "strikethrough": {
                    builder.addStrikethrough((String) el.getOrDefault("text", ""));
                    break;
                }
                case "inline-code": {
                    builder.addInlineCode((String) el.getOrDefault("text", ""));
                    break;
                }
                case "task-list": {
                    @SuppressWarnings("unchecked")
                    List<String> items = (List<String>) el.get("items");
                    @SuppressWarnings("unchecked")
                    List<Boolean> completions = (List<Boolean>) el.get("completions");
                    if (items != null) {
                        boolean[] compArr = null;
                        if (completions != null) {
                            compArr = new boolean[completions.size()];
                            for (int i = 0; i < completions.size(); i++) {
                                compArr[i] = Boolean.TRUE.equals(completions.get(i));
                            }
                        }
                        builder.addTaskList(items.toArray(new String[0]), compArr);
                    }
                    break;
                }
                case "comment": {
                    builder.addComment((String) el.getOrDefault("content", ""));
                    break;
                }
                case "collapsible": {
                    builder.addCollapsible(
                        (String) el.getOrDefault("summary", ""),
                        (String) el.getOrDefault("detail", "")
                    );
                    break;
                }
                case "toc": {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> headings = (List<Map<String, Object>>) el.get("headings");
                    if (headings != null) {
                        String[][] headingsArr = new String[headings.size()][2];
                        for (int i = 0; i < headings.size(); i++) {
                            Map<String, Object> h = headings.get(i);
                            headingsArr[i][0] = (String) h.getOrDefault("text", "");
                            headingsArr[i][1] = String.valueOf(h.getOrDefault("level", 1));
                        }
                        builder.addTableOfContents(headingsArr);
                    }
                    break;
                }
            }
        }

        Map<String, String> result = new HashMap<>();
        result.put("markdown", builder.build());

        return ApiResponse.success(result);
    }
}
