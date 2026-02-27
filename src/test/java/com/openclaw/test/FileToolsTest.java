package com.openclaw.test;

import com.openclaw.utils.FileReaderUtils;
import com.openclaw.utils.CompressionUtils;

public class FileToolsTest {

    public static void main(String[] args) {
        System.out.println("=== 文件工具单元测试 ===\n");
        
        int passed = 0;
        int failed = 0;
        
        try {
            testTextFileRead();
            System.out.println("✓ testTextFileRead - 通过");
            passed++;
        } catch (Exception e) {
            System.out.println("✗ testTextFileRead - 失败: " + e.getMessage());
            failed++;
        }
        
        try {
            testIsImageFile();
            System.out.println("✓ testIsImageFile - 通过");
            passed++;
        } catch (Exception e) {
            System.out.println("✗ testIsImageFile - 失败: " + e.getMessage());
            failed++;
        }
        
        try {
            testIsSupportedDocument();
            System.out.println("✓ testIsSupportedDocument - 通过");
            passed++;
        } catch (Exception e) {
            System.out.println("✗ testIsSupportedDocument - 失败: " + e.getMessage());
            failed++;
        }
        
        try {
            testCompressionUtilsExists();
            System.out.println("✓ testCompressionUtilsExists - 通过");
            passed++;
        } catch (Exception e) {
            System.out.println("✗ testCompressionUtilsExists - 失败: " + e.getMessage());
            failed++;
        }
        
        System.out.println("\n=== 测试完成: " + passed + " 通过, " + failed + " 失败 ===");
    }
    
    private static void testTextFileRead() throws Exception {
        String testContent = "Hello World\nTest Content";
        java.io.File tempFile = java.io.File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        java.nio.file.Files.write(tempFile.toPath(), testContent.getBytes());
        
        String result = FileReaderUtils.readFile(tempFile.getAbsolutePath());
        if (!result.contains("Hello World")) {
            throw new Exception("文件内容不匹配");
        }
    }

    private static void testIsImageFile() {
        if (!FileReaderUtils.isImageFile("test.jpg")) throw new RuntimeException("jpg应该是图片");
        if (!FileReaderUtils.isImageFile("test.png")) throw new RuntimeException("png应该是图片");
        if (!FileReaderUtils.isImageFile("test.jpeg")) throw new RuntimeException("jpeg应该是图片");
        if (FileReaderUtils.isImageFile("test.txt")) throw new RuntimeException("txt不应该是图片");
        if (FileReaderUtils.isImageFile("test.pdf")) throw new RuntimeException("pdf不应该是图片");
    }

    private static void testIsSupportedDocument() {
        if (!FileReaderUtils.isSupportedDocument("test.pdf")) throw new RuntimeException("pdf应该是支持的");
        if (!FileReaderUtils.isSupportedDocument("test.docx")) throw new RuntimeException("docx应该是支持的");
        if (!FileReaderUtils.isSupportedDocument("test.txt")) throw new RuntimeException("txt应该是支持的");
        if (!FileReaderUtils.isSupportedDocument("test.md")) throw new RuntimeException("md应该是支持的");
        if (FileReaderUtils.isSupportedDocument("test.exe")) throw new RuntimeException("exe不应该支持");
    }

    private static void testCompressionUtilsExists() {
        if (CompressionUtils.class == null) throw new RuntimeException("CompressionUtils不存在");
    }
}
