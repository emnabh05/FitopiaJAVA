package tn.esprit.Pidev3A49.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class SupplementImageStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp");
    private static final String DIRECTORY_NAME = "supplement-images";

    private SupplementImageStorage() {
    }

    public static String store(File sourceFile) {
        validateImageFile(sourceFile);

        try {
            Files.createDirectories(resolveStorageDirectory());

            String extension = extractExtension(sourceFile.getName());
            String storedFileName = UUID.randomUUID() + extension;
            Files.copy(
                    sourceFile.toPath(),
                    resolveStorageDirectory().resolve(storedFileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            return storedFileName;
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de sauvegarder l'image du supplement.", exception);
        }
    }

    public static Path resolveImagePath(String imageName) {
        return resolveStorageDirectory().resolve(imageName);
    }

    public static void deleteImage(String imageName) {
        if (imageName == null || imageName.isBlank()) {
            return;
        }

        try {
            Files.deleteIfExists(resolveImagePath(imageName));
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible de supprimer l'image du supplement.", exception);
        }
    }

    public static void validateImageFile(File sourceFile) {
        if (sourceFile == null || !sourceFile.isFile()) {
            throw new IllegalArgumentException("Selectionne un fichier image valide.");
        }

        String extension = extractExtension(sourceFile.getName());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Formats acceptes: PNG, JPG, JPEG, GIF, WEBP.");
        }
    }

    private static Path resolveStorageDirectory() {
        return Path.of(System.getProperty("user.dir")).resolve(DIRECTORY_NAME);
    }

    private static String extractExtension(String fileName) {
        int separatorIndex = fileName.lastIndexOf('.');
        if (separatorIndex < 0) {
            return "";
        }

        return fileName.substring(separatorIndex).toLowerCase(Locale.ROOT);
    }
}
