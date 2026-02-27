package com.openclaw.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileReaderUtils {

    public static String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            return readPdf(filePath);
        } else if (fileName.endsWith(".docx")) {
            return readDocx(filePath);
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".md") || 
                   fileName.endsWith(".csv") || fileName.endsWith(".json") ||
                   fileName.endsWith(".xml") || fileName.endsWith(".html")) {
            return readTextFile(filePath);
        } else {
            throw new IOException("不支持的文件格式: " + fileName);
        }
    }

    public static String readPdf(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public static String readDocx(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                content.append(paragraph.getText()).append("\n");
            }
        }
        return content.toString();
    }

    public static String readTextFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(new File(filePath).toPath()), StandardCharsets.UTF_8);
    }

    public static boolean isImageFile(String filePath) {
        String lowerPath = filePath.toLowerCase();
        return lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg") || 
               lowerPath.endsWith(".png") || lowerPath.endsWith(".gif") || 
               lowerPath.endsWith(".bmp") || lowerPath.endsWith(".webp");
    }

    public static boolean isSupportedDocument(String filePath) {
        String lowerPath = filePath.toLowerCase();
        return lowerPath.endsWith(".pdf") || lowerPath.endsWith(".docx") || 
               lowerPath.endsWith(".txt") || lowerPath.endsWith(".md") || 
               lowerPath.endsWith(".csv") || lowerPath.endsWith(".json") || 
               lowerPath.endsWith(".xml") || lowerPath.endsWith(".html");
    }
}
