package com.mdgenerator.server.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.InvalidPathException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PathValidator单元测试
 */
class PathValidatorTest {

    @BeforeEach
    void setUp() {
        // 添加测试用临时目录到白名单
        String tempDir = System.getProperty("java.io.tmpdir");
        PathValidator.addAllowedRoot(tempDir);
    }

    @Test
    @DisplayName("测试合法路径验证通过")
    void testValidPath() {
        String tempDir = System.getProperty("java.io.tmpdir");
        assertDoesNotThrow(() -> {
            String result = PathValidator.validatePath(tempDir);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("测试路径遍历攻击被拒绝")
    void testPathTraversalRejected() {
        assertThrows(InvalidPathException.class, () -> {
            PathValidator.validatePath("/etc/passwd");
        });

        assertThrows(InvalidPathException.class, () -> {
            PathValidator.validatePath("../sensitive/file.txt");
        });
    }

    @Test
    @DisplayName("测试空路径抛出异常")
    void testEmptyPathThrowsException() {
        assertThrows(InvalidPathException.class, () -> {
            PathValidator.validatePath("");
        });

        assertThrows(InvalidPathException.class, () -> {
            PathValidator.validatePath(null);
        });
    }

    @Test
    @DisplayName("测试目录验证")
    void testDirectoryValidation() {
        String tempDir = System.getProperty("java.io.tmpdir");
        assertDoesNotThrow(() -> {
            String result = PathValidator.validateDirectory(tempDir);
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("测试文件路径作为目录验证失败")
    void testFileAsDirectoryFails() {
        // 创建一个临时文件
        String tempDir = System.getProperty("java.io.tmpdir");
        java.nio.file.Path tempFile = java.nio.file.Paths.get(tempDir, "test_file_" + System.currentTimeMillis());
        try {
            java.nio.file.Files.createFile(tempFile);

            assertThrows(InvalidPathException.class, () -> {
                PathValidator.validateDirectory(tempFile.toString());
            });
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("测试包含特殊字符的路径被拒绝")
    void testPathWithSpecialCharactersRejected() {
        assertThrows(InvalidPathException.class, () -> {
            PathValidator.validatePath("/path/with\0null");
        });
    }
}