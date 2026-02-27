package com.openclaw.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CompressionUtils {

    public static String compress(String sourcePath, String zipPath) {
        try {
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                return "错误：源文件或目录不存在: " + sourcePath;
            }

            List<String> command = new ArrayList<>();
            command.add("zip");
            command.add("-r");
            command.add(zipPath);
            
            if (sourceFile.isDirectory()) {
                File parentDir = sourceFile.getParentFile();
                String dirName = sourceFile.getName();
                command.add(dirName);
                return executeCommand(command, parentDir);
            } else {
                command.add(sourceFile.getName());
                return executeCommand(command, sourceFile.getParentFile());
            }
        } catch (Exception e) {
            return "压缩失败: " + e.getMessage();
        }
    }

    public static String decompress(String zipPath, String destPath, boolean overwrite) {
        try {
            File zipFile = new File(zipPath);
            if (!zipFile.exists()) {
                return "错误：压缩文件不存在: " + zipPath;
            }

            File destDir = new File(destPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            List<String> command = new ArrayList<>();
            command.add("unzip");
            if (overwrite) {
                command.add("-o");
            }
            command.add(zipPath);
            command.add("-d");
            command.add(destPath);

            return executeCommand(command, destDir);
        } catch (Exception e) {
            return "解压失败: " + e.getMessage();
        }
    }

    public static String listZipContents(String zipPath) {
        try {
            File zipFile = new File(zipPath);
            if (!zipFile.exists()) {
                return "错误：压缩文件不存在: " + zipPath;
            }

            List<String> command = new ArrayList<>();
            command.add("unzip");
            command.add("-l");
            command.add(zipPath);

            return executeCommand(command, zipFile.getParentFile());
        } catch (Exception e) {
            return "列出压缩文件内容失败: " + e.getMessage();
        }
    }

    private static String executeCommand(List<String> command, File workingDir) {
        StringBuilder result = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            if (workingDir != null) {
                pb.directory(workingDir);
            }
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "操作成功！\n" + result.toString();
            } else {
                return "操作失败，退出码: " + exitCode + "\n" + result.toString();
            }
        } catch (Exception e) {
            return "执行命令失败: " + e.getMessage();
        }
    }

    public static boolean isZipAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("zip", "-h");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0 || exitCode == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isUnzipAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("unzip", "-h");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0 || exitCode == 1;
        } catch (Exception e) {
            return false;
        }
    }
}
