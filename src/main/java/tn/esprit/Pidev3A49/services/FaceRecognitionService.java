package tn.esprit.Pidev3A49.services;

import tn.esprit.Pidev3A49.Models.FitopiaUser;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class FaceRecognitionService {
    private static final int HASH_SIZE = 16;
    private static final int COMPARISON_SIZE = 64;
    private static final double GENERAL_MATCH_SCORE = 0.62;
    private static final double STRICT_MATCH_SCORE = 0.65;
    private static final double MAX_MEAN_PIXEL_DIFF = 26.0;

    public Optional<FitopiaUser> findBestMatch(BufferedImage capturedImage, List<FitopiaUser> users) {
        return findBestMatchResult(capturedImage, users).map(MatchResult::user);
    }

    public Optional<MatchResult> findBestMatchResult(BufferedImage capturedImage, List<FitopiaUser> users) {
        return users.stream()
                .filter(FitopiaUser::isFaceIdEnabled)
                .filter(user -> user.getFaceImagePath() != null && !user.getFaceImagePath().isBlank())
                .map(user -> compareWithStoredPath(capturedImage, user, user.getFaceImagePath()).orElse(null))
                .filter(result -> result != null && result.confidence() >= GENERAL_MATCH_SCORE)
                .max(Comparator.comparingDouble(MatchResult::confidence));
    }

    public boolean matchesUser(BufferedImage capturedImage, FitopiaUser user) {
        return compareWithUser(capturedImage, user)
                .map(this::isStrictMatch)
                .orElse(false);
    }

    public boolean isStrictMatch(MatchResult result) {
        return result != null && result.confidence() >= STRICT_MATCH_SCORE;
    }

    public Optional<MatchResult> compareWithUser(BufferedImage capturedImage, FitopiaUser user) {
        if (user == null || !user.isFaceIdEnabled() || user.getFaceImagePath() == null || user.getFaceImagePath().isBlank()) {
            return Optional.empty();
        }
        return compareWithStoredPath(capturedImage, user, user.getFaceImagePath());
    }

    public void saveFaceImage(BufferedImage image, Path output) throws IOException {
        Files.createDirectories(output.getParent());
        ImageIO.write(image, "PNG", output.toFile());
    }

    private Optional<MatchResult> compareWithStoredPath(BufferedImage capturedImage, FitopiaUser user, String storedImagePath) {
        try {
            File file = new File(storedImagePath);
            if (!file.exists()) {
                return Optional.empty();
            }

            BufferedImage storedImage = ImageIO.read(file);
            if (storedImage == null) {
                return Optional.empty();
            }

            BufferedImage normalizedCaptured = normalizeForComparison(capturedImage);
            BufferedImage normalizedStored = normalizeForComparison(storedImage);

            double hashSimilarity = computeHashSimilarity(normalizedCaptured, normalizedStored);
            double pixelSimilarity = computePixelSimilarity(normalizedCaptured, normalizedStored);
            double histogramSimilarity = computeHistogramSimilarity(normalizedCaptured, normalizedStored);
            double gradientSimilarity = computeGradientSimilarity(normalizedCaptured, normalizedStored);

            double confidence = (hashSimilarity * 0.35)
                    + (pixelSimilarity * 0.25)
                    + (histogramSimilarity * 0.20)
                    + (gradientSimilarity * 0.20);

            int distance = (int) Math.round((1.0 - confidence) * 100.0);
            return Optional.of(new MatchResult(
                    user,
                    distance,
                    clamp(confidence),
                    clamp(hashSimilarity),
                    clamp(pixelSimilarity),
                    clamp(histogramSimilarity),
                    clamp(gradientSimilarity)
            ));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private BufferedImage normalizeForComparison(BufferedImage source) {
        BufferedImage cropped = cropCenterRegion(source);
        BufferedImage normalized = new BufferedImage(COMPARISON_SIZE, COMPARISON_SIZE, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = normalized.createGraphics();
        Image scaled = cropped.getScaledInstance(COMPARISON_SIZE, COMPARISON_SIZE, Image.SCALE_SMOOTH);
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();
        return normalized;
    }

    private BufferedImage cropCenterRegion(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        int cropWidth = Math.max(1, (int) Math.round(width * 0.48));
        int cropHeight = Math.max(1, (int) Math.round(height * 0.64));
        int x = Math.max(0, (width - cropWidth) / 2);
        int y = Math.max(0, (int) Math.round(height * 0.16));

        if (y + cropHeight > height) {
            y = Math.max(0, height - cropHeight);
        }

        return source.getSubimage(x, y, Math.min(cropWidth, width - x), Math.min(cropHeight, height - y));
    }

    private double computeHashSimilarity(BufferedImage first, BufferedImage second) {
        String firstHash = computeAverageHash(first);
        String secondHash = computeAverageHash(second);
        int distance = hammingDistance(firstHash, secondHash);
        return 1.0 - (distance / (double) firstHash.length());
    }

    private String computeAverageHash(BufferedImage source) {
        BufferedImage resized = new BufferedImage(HASH_SIZE, HASH_SIZE, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = resized.createGraphics();
        Image scaled = source.getScaledInstance(HASH_SIZE, HASH_SIZE, Image.SCALE_SMOOTH);
        graphics.drawImage(scaled, 0, 0, null);
        graphics.dispose();

        int[] pixels = new int[HASH_SIZE * HASH_SIZE];
        int total = 0;
        for (int y = 0; y < HASH_SIZE; y++) {
            for (int x = 0; x < HASH_SIZE; x++) {
                int rgb = resized.getRGB(x, y) & 0xFF;
                pixels[(y * HASH_SIZE) + x] = rgb;
                total += rgb;
            }
        }

        int average = total / pixels.length;
        StringBuilder hash = new StringBuilder(pixels.length);
        for (int pixel : pixels) {
            hash.append(pixel >= average ? '1' : '0');
        }
        return hash.toString();
    }

    private double computePixelSimilarity(BufferedImage first, BufferedImage second) {
        double meanPixelDiff = computeMeanPixelDifference(first, second);
        if (meanPixelDiff >= MAX_MEAN_PIXEL_DIFF) {
            return 0.0;
        }
        return 1.0 - (meanPixelDiff / MAX_MEAN_PIXEL_DIFF);
    }

    private double computeMeanPixelDifference(BufferedImage first, BufferedImage second) {
        long total = 0;
        int width = Math.min(first.getWidth(), second.getWidth());
        int height = Math.min(first.getHeight(), second.getHeight());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int firstPixel = first.getRGB(x, y) & 0xFF;
                int secondPixel = second.getRGB(x, y) & 0xFF;
                total += Math.abs(firstPixel - secondPixel);
            }
        }

        return total / (double) (width * height);
    }

    private double computeHistogramSimilarity(BufferedImage first, BufferedImage second) {
        double[] firstHistogram = buildHistogram(first);
        double[] secondHistogram = buildHistogram(second);
        double diff = 0.0;

        for (int i = 0; i < firstHistogram.length; i++) {
            diff += Math.abs(firstHistogram[i] - secondHistogram[i]);
        }

        return clamp(1.0 - (diff / 2.0));
    }

    private double[] buildHistogram(BufferedImage image) {
        double[] histogram = new double[16];
        int width = image.getWidth();
        int height = image.getHeight();
        double totalPixels = width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                histogram[Math.min(histogram.length - 1, pixel / 16)]++;
            }
        }

        for (int i = 0; i < histogram.length; i++) {
            histogram[i] /= totalPixels;
        }
        return histogram;
    }

    private double computeGradientSimilarity(BufferedImage first, BufferedImage second) {
        double totalDiff = 0.0;
        int width = Math.min(first.getWidth(), second.getWidth());
        int height = Math.min(first.getHeight(), second.getHeight());
        int samples = 0;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int firstGradient = gradientAt(first, x, y);
                int secondGradient = gradientAt(second, x, y);
                totalDiff += Math.abs(firstGradient - secondGradient);
                samples++;
            }
        }

        if (samples == 0) {
            return 0.0;
        }

        double meanGradientDiff = totalDiff / samples;
        return clamp(1.0 - (meanGradientDiff / 120.0));
    }

    private int gradientAt(BufferedImage image, int x, int y) {
        int left = image.getRGB(x - 1, y) & 0xFF;
        int right = image.getRGB(x + 1, y) & 0xFF;
        int top = image.getRGB(x, y - 1) & 0xFF;
        int bottom = image.getRGB(x, y + 1) & 0xFF;
        return Math.abs(right - left) + Math.abs(bottom - top);
    }

    private int hammingDistance(String first, String second) {
        int distance = 0;
        for (int i = 0; i < Math.min(first.length(), second.length()); i++) {
            if (first.charAt(i) != second.charAt(i)) {
                distance++;
            }
        }
        return distance + Math.abs(first.length() - second.length());
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public record MatchResult(
            FitopiaUser user,
            int distance,
            double confidence,
            double hashSimilarity,
            double pixelSimilarity,
            double histogramSimilarity,
            double gradientSimilarity
    ) {
    }
}
