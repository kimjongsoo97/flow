package com.flow.backend.service;

public final class ExtensionNormalizer {

    private ExtensionNormalizer() {
    }

    // 앞뒤 공백, 앞쪽 점, 대소문자 차이를 제거해 같은 확장자로 맞춘다.
    public static String normalize(String extension) {
        if (extension == null) {
            return "";
        }
        String normalized = extension.trim().toLowerCase();
        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
