package com.flow.backend.service;

public final class ExtensionNormalizer {

    private ExtensionNormalizer() {
    }

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
