package com.mdgenerator.server.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Markdown文档构建器
 * 
 * <p>提供链式调用的方式构建Markdown文档，支持所有常用的Markdown语法元素。</p>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * MdBuilder builder = new MdBuilder();
 * builder.addH1("数据库设计文档")
 *        .addTable("user_info", headers, rows)
 *        .addCodeBlock("SELECT * FROM user", "sql");
 * String markdown = builder.build();
 * }</pre>
 * 
 * <p>设计模式：采用Builder模式（建造者模式），通过链式调用逐步构建Markdown文档。
 * 内部使用StringBuilder进行高效字符串拼接，避免多次创建临时String对象。</p>
 * 
 * @author MD Generator Team
 * @version 1.0.0
 */
public class MdBuilder {

    /** 用于拼接Markdown内容的StringBuilder，线程不安全但单线程场景下性能最优 */
    private final StringBuilder content;

    /** 文档目录导航ID计数器，用于生成唯一的锚点ID */
    private int tocIdCounter;

    /**
     * 默认构造函数，初始化内部StringBuilder
     */
    public MdBuilder() {
        this.content = new StringBuilder();
        this.tocIdCounter = 0;
    }

    // ==================== 标题相关 ====================

    /**
     * 添加一级标题（对应HTML的h1标签）
     * 
     * @param title 标题文本，不允许为null（为null时将被替换为空字符串）
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addH1(String title) {
        return addHeading(title, 1);
    }

    /**
     * 添加二级标题（对应HTML的h2标签）
     * 
     * @param title 标题文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addH2(String title) {
        return addHeading(title, 2);
    }

    /**
     * 添加三级标题（对应HTML的h3标签）
     * 
     * @param title 标题文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addH3(String title) {
        return addHeading(title, 3);
    }

    /**
     * 添加四级标题（对应HTML的h4标签）
     * 
     * @param title 标题文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addH4(String title) {
        return addHeading(title, 4);
    }

    /**
     * 添加五级标题（对应HTML的h5标签）
     * 
     * @param title 标题文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addH5(String title) {
        return addHeading(title, 5);
    }

    /**
     * 统一的标题生成方法
     * 
     * <p>Markdown语法中，标题由N个#号加一个空格加标题文本组成。
     * 每个标题后会追加两个换行符以保证与后续内容有合适的间距。</p>
     * 
     * @param title 标题文本
     * @param level 标题级别（1-6），1为最高级别
     * @return 当前MdBuilder实例，支持链式调用
     */
    private MdBuilder addHeading(String title, int level) {
        String safeTitle = (title == null) ? "" : title;
        // 生成对应数量的#号
        StringBuilder sharp = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sharp.append("#");
        }
        content.append(sharp).append(" ").append(safeTitle).append("\n\n");
        return this;
    }

    // ==================== 文本段落 ====================

    /**
     * 添加普通文本段落
     * 
     * <p>段落末尾会自动追加两个换行符，使得后续内容与该段落保持一个空行的间距。</p>
     * 
     * @param text 段落文本内容
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addParagraph(String text) {
        content.append(text != null ? text : "").append("\n\n");
        return this;
    }

    /**
     * 添加粗体文本
     * 
     * <p>Markdown语法：使用两个星号包裹文本表示粗体</p>
     * 
     * @param text 需要加粗显示的文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addBold(String text) {
        String safe = text != null ? text : "";
        content.append("**").append(safe).append("**");
        return this;
    }

    /**
     * 添加斜体文本
     * 
     * <p>Markdown语法：使用一个星号包裹文本表示斜体</p>
     * 
     * @param text 需要斜体显示的文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addItalic(String text) {
        String safe = text != null ? text : "";
        content.append("*").append(safe).append("*");
        return this;
    }

    /**
     * 添加行内代码
     * 
     * <p>Markdown语法：使用反引号包裹文本表示行内代码样式</p>
     * 
     * @param text 代码文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addInlineCode(String text) {
        String safe = text != null ? text : "";
        content.append("`").append(safe).append("`");
        return this;
    }

    /**
     * 添加删除线文本
     * 
     * <p>Markdown语法：使用两个波浪号包裹文本表示删除线</p>
     * 
     * @param text 需要添加删除线的文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addStrikethrough(String text) {
        String safe = text != null ? text : "";
        content.append("~~").append(safe).append("~~");
        return this;
    }

    // ==================== 列表相关 ====================

    /**
     * 添加无序列表项
     * 
     * <p>Markdown语法：以 - 开头的行表示无序列表项</p>
     * 
     * @param items 列表项内容数组
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addUnorderedList(String... items) {
        for (String item : items) {
            content.append("- ").append(item).append("\n");
        }
        content.append("\n");
        return this;
    }

    /**
     * 添加有序列表项
     * 
     * <p>Markdown语法：以 数字. 开头的行表示有序列表，编号从1开始自动递增</p>
     * 
     * @param items 列表项内容数组
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addOrderedList(String... items) {
        for (int i = 0; i < items.length; i++) {
            content.append(i + 1).append(". ").append(items[i]).append("\n");
        }
        content.append("\n");
        return this;
    }

    /**
     * 添加任务列表（带勾选框的列表）
     * 
     * <p>Markdown语法：- [ ] 表示未完成，- [x] 表示已完成</p>
     * 
     * @param items 任务列表项数组
     * @param completions 对应的完成状态数组，true表示已完成
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addTaskList(String[] items, boolean[] completions) {
        for (int i = 0; i < items.length; i++) {
            String check = (completions != null && i < completions.length && completions[i]) ? "x" : " ";
            content.append("- [").append(check).append("] ").append(items[i]).append("\n");
        }
        content.append("\n");
        return this;
    }

    // ==================== 表格相关 ====================

    /**
     * 添加Markdown表格
     * 
     * <p>这是最核心的表格生成方法。Markdown表格由三部分组成：</p>
     * <ul>
     *   <li>表头行：由竖线分隔的列标题</li>
     *   <li>分隔行：由竖线和连字符组成，用于分隔表头和数据</li>
     *   <li>数据行：由竖线分隔的单元格内容</li>
     * </ul>
     * 
     * <p>该方法会自动计算每列的最大宽度，并根据最大宽度为分隔行生成合适数量的连字符。</p>
     * 
     * <p>注意：中文字符在终端中占用2个英文字符宽度，但在这里我们按字符数处理，
     * 因为Markdown渲染器会自动处理对齐。分隔行中的连字符仅作为语法标记，
     * 具体列宽由Markdown渲染器决定。</p>
     * 
     * <p>对齐方式：默认左对齐，如需居中对齐可在headers中使用特殊格式，
     * 该方法会自动根据headers判断对齐方式。</p>
     * 
     * @param headers 表头列名数组
     * @param rows 数据行，外层List表示行，内层List表示该行的各列数据
     * @return 当前MdBuilder实例，支持链式调用
     * @throws IllegalArgumentException 如果headers为null或为空
     */
    public MdBuilder addTable(List<String> headers, List<List<String>> rows) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("表头不能为空");
        }

        // 计算每列的最大宽度，用于对齐显示
        int colCount = headers.size();
        int[] colWidths = new int[colCount];

        // 第一遍：计算表头宽度
        for (int i = 0; i < colCount; i++) {
            colWidths[i] = headers.get(i).length();
        }

        // 第二遍：计算数据行宽度，更新最大宽度
        for (List<String> row : rows) {
            for (int i = 0; i < colCount && i < row.size(); i++) {
                // 使用Math.max保留该列目前的最大宽度
                colWidths[i] = Math.max(colWidths[i], row.get(i).length());
            }
        }

        // 第三遍：构建表格字符串
        // 表头行
        appendTableRow(headers, colWidths);

        // 分隔行（---左对齐，:---:居中对齐，---:右对齐）
        appendTableSeparator(colWidths);

        // 数据行
        for (List<String> row : rows) {
            appendTableRow(row, colWidths);
        }

        content.append("\n");
        return this;
    }

    /**
     * 使用二维数组添加表格（便捷方法）
     * 
     * <p>将二维数组转换为List格式后调用主表格生成方法。
     * 这是addTable(List, List)的简便版本，适用于已知数据不多的情况。</p>
     * 
     * @param headers 表头列名数组
     * @param rows 二维数组格式的数据，第一维是行，第二维是列
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addTable(String[] headers, String[][] rows) {
        List<String> headerList = Arrays.asList(headers);
        List<List<String>> rowList = new ArrayList<>();
        for (String[] row : rows) {
            rowList.add(Arrays.asList(row));
        }
        return addTable(headerList, rowList);
    }

    /**
     * 构建表格的一行（包括表头行和数据行）
     *
     * <p>每一行的格式为：| 值1 | 值2 | ... | 值N |</p>
     *
     * @param cells 该行的所有单元格内容
     * @param widths 各列的宽度数组
     */
    private void appendTableRow(List<String> cells, int[] widths) {
        content.append("|");
        for (int i = 0; i < widths.length; i++) {
            String cell = (i < cells.size()) ? cells.get(i) : "";
            int padding = widths[i] - cell.length();
            // 预分配空格数组，避免循环append
            char[] spaces = new char[padding + 2];
            spaces[0] = ' ';
            for (int j = 1; j <= padding; j++) spaces[j] = ' ';
            spaces[padding + 1] = ' ';
            content.append(" ").append(cell).append(spaces, 1, padding + 1).append("|");
        }
        content.append("\n");
    }

    /**
     * 构建表格的分隔行
     *
     * @param widths 各列的宽度数组
     */
    private void appendTableSeparator(int[] widths) {
        content.append("|");
        for (int width : widths) {
            char[] dashes = new char[width + 2];
            dashes[0] = ' ';
            for (int j = 1; j <= width; j++) dashes[j] = '-';
            dashes[width + 1] = ' ';
            content.append(dashes).append("|");
        }
        content.append("\n");
    }

    // ==================== 代码块 ====================

    /**
     * 添加代码块（带语言标识）
     * 
     * <p>Markdown语法：使用三个反引号包裹代码，可选指定语言用于语法高亮提示。
     * 常用的语言标识：java, xml, json, sql, bash, python, javascript等。</p>
     * 
     * <p>实现细节：代码块后追加两个换行符，确保与后续内容分隔。</p>
     * 
     * @param code 代码内容
     * @param language 编程语言标识（如"java"、"sql"、"json"），可为null
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addCodeBlock(String code, String language) {
        content.append("```").append(language != null ? language : "").append("\n");
        content.append(code).append("\n");
        content.append("```").append("\n\n");
        return this;
    }

    /**
     * 添加无语言标识的代码块
     * 
     * @param code 代码内容
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addCodeBlock(String code) {
        return addCodeBlock(code, null);
    }

    // ==================== 引用和分隔线 ====================

    /**
     * 添加引用块
     * 
     * <p>Markdown语法：以 > 开头的行表示引用块。
     * 支持多行引用，每行都以 > 开头。</p>
     * 
     * @param lines 引用文本行数组
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addBlockQuote(String... lines) {
        for (String line : lines) {
            content.append("> ").append(line).append("\n");
        }
        content.append("\n");
        return this;
    }

    /**
     * 添加水平分隔线
     * 
     * <p>Markdown语法：三个或更多的连字符、星号或下划线单独成行表示分隔线。</p>
     * 
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addHorizontalRule() {
        content.append("---\n\n");
        return this;
    }

    // ==================== 链接和图片 ====================

    /**
     * 添加超链接（Markdown内联格式）
     * 
     * <p>Markdown语法：[显示文本](URL)
     * 
     * @param text 链接的显示文本
     * @param url 链接的目标URL
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addLink(String text, String url) {
        content.append("[").append(text).append("](").append(url).append(")");
        return this;
    }

    /**
     * 添加图片
     * 
     * <p>Markdown语法：![alt文本](图片URL "可选标题")
     * 
     * @param altText 图片的替代文本（当图片无法显示时）
     * @param url 图片的URL路径
     * @param title 鼠标悬停时的提示文本，可为null
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addImage(String altText, String url, String title) {
        content.append("![").append(altText).append("](").append(url);
        if (title != null && !title.isEmpty()) {
            content.append(" \"").append(title).append("\"");
        }
        content.append(")\n\n");
        return this;
    }

    // ==================== 特殊元素 ====================

    /**
     * 添加注释（HTML注释格式，在Markdown渲染时不显示）
     * 
     * <p>Markdown本身没有注释语法，但支持内联HTML，因此可以使用HTML注释格式。
     * 这种注释在Markdown渲染为HTML时不会被显示，但在源码中可见。</p>
     * 
     * @param comment 注释内容
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addComment(String comment) {
        content.append("<!-- ").append(comment).append(" -->\n\n");
        return this;
    }

    /**
     * 添加折叠面板（HTML details标签）
     * 
     * <p>使用HTML的details/summary标签实现可折叠内容。
     * 在GitHub、GitLab等平台的Markdown渲染中支持此功能。</p>
     * 
     * @param summary 折叠面板的标题（点击展开前显示的内容）
     * @param detail 折叠面板的内容（展开后显示的内容）
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addCollapsible(String summary, String detail) {
        content.append("<details>\n");
        content.append("<summary>").append(summary).append("</summary>\n\n");
        content.append(detail).append("\n\n");
        content.append("</details>\n\n");
        return this;
    }

    /**
     * 添加Badge徽章
     * 
     * <p>Badge通常用于README中展示项目状态，如构建状态、版本号等。
     * 格式：![标签](https://img.shields.io/badge/标签-消息-颜色)</p>
     * 
     * @param label badge左侧标签文本
     * @param message badge右侧消息文本
     * @param color badge颜色（如"blue"、"green"、"red"、"orange"）
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addBadge(String label, String message, String color) {
        content.append("![").append(label).append("](https://img.shields.io/badge/");
        content.append(label).append("-").append(message).append("-").append(color).append(")");
        return this;
    }

    // ==================== 目录生成 ====================

    /**
     * 生成文档目录（Table of Contents）
     * 
     * <p>由于Markdown本身的限制，纯Markdown无法自动生成锚点链接目录。
     * 这里生成的是基于GitHub风格锚点的目录链接。</p>
     * 
     * <p>GitHub会自动为每个标题生成锚点ID，规则是：</p>
     * <ul>
     *   <li>所有字母转为小写</li>
     *   <li>空格转为连字符</li>
     *   <li>移除标点符号</li>
     * </ul>
     * 
     * @param headings 标题数组，每个元素是{标题文本, 级别}的数组
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addTableOfContents(String[][] headings) {
        content.append("## 目录\n\n");
        for (String[] heading : headings) {
            if (heading.length < 2) continue;
            String text = heading[0];
            int level = Integer.parseInt(heading[1]);

            // 生成GitHub风格的锚点链接
            String anchor = text.toLowerCase()
                .replaceAll("[^a-z0-9 \\-]", "")
                .replace(" ", "-");

            StringBuilder indent = new StringBuilder();
            for (int i = 0; i < level - 1; i++) {
                indent.append("  ");
            }
            content.append(indent).append("- [").append(text).append("](#").append(anchor).append(")\n");
        }
        content.append("\n");
        return this;
    }

    // ==================== 换行和间距 ====================

    /**
     * 添加一个空行
     * 
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addEmptyLine() {
        content.append("\n");
        return this;
    }

    /**
     * 添加换行符（Markdown软换行，等同于两个空格+换行）
     * 
     * <p>通常用于在同一段落内强制换行</p>
     * 
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder addLineBreak() {
        content.append("  \n");
        return this;
    }

    // ==================== 构建输出 ====================

    /**
     * 构建最终的Markdown字符串
     * 
     * <p>返回之前对内容做trim处理，去掉首尾多余的空白字符。
     * 这是Builder模式中的最终构建步骤。</p>
     * 
     * @return 完整的Markdown格式字符串
     */
    public String build() {
        return content.toString().trim();
    }

    /**
     * 追加原始文本到Markdown文档末尾
     * 
     * <p>用于直接追加不需要任何格式转换的原始内容。
     * 注意：该方法不会自动添加额外的换行符。</p>
     * 
     * @param raw 原始文本
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder appendRaw(String raw) {
        content.append(raw);
        return this;
    }

    /**
     * 获取当前构建内容的长度
     * 
     * @return 当前已构建的Markdown内容字符数
     */
    public int length() {
        return content.length();
    }

    /**
     * 清空当前构建内容，重新开始
     * 
     * @return 当前MdBuilder实例，支持链式调用
     */
    public MdBuilder reset() {
        content.setLength(0);
        tocIdCounter = 0;
        return this;
    }

    /**
     * 将当前构建内容写入文件并自动生成文件名
     *
     * @param basePath 保存目录路径
     * @param prefix 文件名前缀（会自动追加时间戳生成完整文件名）
     * @return 生成的实际文件路径
     */
    public String saveToFile(String basePath, String prefix) {
        // 生成带时间戳的文件名，避免覆盖已有文件
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new java.util.Date());
        String fileName = prefix + "_" + timestamp + ".md";
        java.io.File file = new java.io.File(basePath, fileName);

        // 确保父目录存在
        java.io.File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        // 写入文件（Java 8兼容写法：使用OutputStreamWriter指定UTF-8编码）
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
             java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(build());
        } catch (java.io.IOException e) {
            throw new RuntimeException("保存Markdown文件失败：" + e.getMessage(), e);
        }

        return file.getAbsolutePath();
    }
}