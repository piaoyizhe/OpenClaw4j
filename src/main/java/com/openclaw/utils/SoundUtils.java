package com.openclaw.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SoundUtils {

    private static boolean soundEnabled = true;

    public static void main(String[] args) {
        System.out.println("=== 音效测试工具 ===\n");
        System.out.println("可用系统音效：");
        
        String[] sounds = {
            "Pop", "Tink", "Basso", "Blow", "Bottle",
            "Frog", "Funk", "Glass", "Hero", "Morse",
            "Ping", "Pop", "Purr", "Sosumi", "Submarine",
            "Tink"
        };
//        String[] sounds = {
//                 "Blow", "Bottle",
//                "Frog", "Funk", "Glass"
//        };
        
        for (int i = 0; i < sounds.length; i++) {
            System.out.println((i + 1) + ". " + sounds[i]);
        }
        
        System.out.println("\n播放示例：");
        System.out.println("  SoundUtils.playCompleteSound();  // 播放完成音效");
        System.out.println("  SoundUtils.playBeepSound();      // 播放提示音");
        System.out.println("  SoundUtils.playSound(\"Pop\");     // 播放指定音效");
        System.out.println("  SoundUtils.speak(\"你好\");        // 语音播报");
        
        System.out.println("\n正在播放所有音效测试...\n");
        
        for (String sound : sounds) {
            System.out.print("播放 " + sound + " ... ");
            playSound(sound);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            System.out.println("OK");
        }
        
        System.out.println("\n测试完成！请告诉我你喜欢的音效名称。");
    }

    public static void playCompleteSound() {
        if (!soundEnabled) {
            return;
        }
        try {
            ProcessBuilder pb = new ProcessBuilder("afplay", "/System/Library/Sounds/Glass.aiff");
            pb.start();
        } catch (Exception e) {
            System.err.println("播放提示音失败: " + e.getMessage());
        }
    }

    public static void playBeepSound() {
        if (!soundEnabled) {
            return;
        }
        try {
            ProcessBuilder pb = new ProcessBuilder("afplay", "/System/Library/Sounds/Tink.aiff");
            pb.start();
        } catch (Exception e) {
            System.err.println("播放提示音失败: " + e.getMessage());
        }
    }

    public static void playSound(String soundName) {
        if (!soundEnabled) {
            return;
        }
        try {
            String soundPath = "/System/Library/Sounds/" + soundName + ".aiff";
            ProcessBuilder pb = new ProcessBuilder("afplay", soundPath);
            pb.start();
        } catch (Exception e) {
            System.err.println("播放提示音失败: " + e.getMessage());
        }
    }

    public static void speak(String text) {
        try {
            ProcessBuilder pb = new ProcessBuilder("say", text);
            pb.start();
        } catch (Exception e) {
            System.err.println("语音播放失败: " + e.getMessage());
        }
    }

    public static void setSoundEnabled(boolean enabled) {
        soundEnabled = enabled;
    }

    public static boolean isSoundEnabled() {
        return soundEnabled;
    }
}
