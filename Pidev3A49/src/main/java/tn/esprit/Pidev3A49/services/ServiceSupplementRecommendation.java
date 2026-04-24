package tn.esprit.Pidev3A49.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import tn.esprit.Pidev3A49.Models.Supplement;
import tn.esprit.Pidev3A49.Models.SupplementRecommendation;
import tn.esprit.Pidev3A49.utils.MyDataBase;
import tn.esprit.Pidev3A49.utils.SchemaInitializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ServiceSupplementRecommendation {

    private static final int DEFAULT_LIMIT = 6;
    private static final int MAX_LIMIT = 12;
    private static final int GEMINI_POOL_SIZE = 10;
    private static final double REORDER_WEIGHT = 0.50;
    private static final double CROSS_SELL_WEIGHT = 0.25;
    private static final double CATEGORY_WEIGHT = 0.25;
    private static final String DEFAULT_GEMINI_MODEL = "gemini-2.0-flash";
    private static final String GEMINI_ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private static final String REORDER_QUERY = """
            SELECT
                oi.supplement_id,
                SUM(oi.quantity * (1 / (1 + TIMESTAMPDIFF(DAY, o.created_at, NOW()) / 30.0))) AS weighted_score
            FROM %s oi
            JOIN %s o ON o.id = oi.order_id
            WHERE LOWER(o.email) = LOWER(?)
            GROUP BY oi.supplement_id
            """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_ITEM_TABLE, SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

    private static final String TOP_SELLING_QUERY = """
            SELECT
                oi.supplement_id,
                SUM(oi.quantity) AS sold_qty
            FROM %s oi
            JOIN %s o ON o.id = oi.order_id
            WHERE o.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
            GROUP BY oi.supplement_id
            ORDER BY sold_qty DESC
            LIMIT 30
            """.formatted(SchemaInitializer.SUPPLEMENT_ORDER_ITEM_TABLE, SchemaInitializer.SUPPLEMENT_ORDER_TABLE);

    private static final String USER_CATEGORY_SCORE_QUERY = """
            SELECT
                LOWER(TRIM(s.category)) AS category_key,
                SUM(oi.quantity * (1 / (1 + TIMESTAMPDIFF(DAY, o.created_at, NOW()) / 45.0))) AS weighted_score
            FROM %s oi
            JOIN %s o ON o.id = oi.order_id
            JOIN %s s ON s.id = oi.supplement_id
            WHERE LOWER(o.email) = LOWER(?)
              AND s.category IS NOT NULL
              AND TRIM(s.category) <> ''
            GROUP BY LOWER(TRIM(s.category))
            """.formatted(
            SchemaInitializer.SUPPLEMENT_ORDER_ITEM_TABLE,
            SchemaInitializer.SUPPLEMENT_ORDER_TABLE,
            SchemaInitializer.SUPPLEMENT_TABLE
    );

    private final Connection cnx;
    private final HttpClient httpClient;
    private final Gson gson;

    public ServiceSupplementRecommendation() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new IllegalStateException("Impossible de se connecter a MySQL.");
        }
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        gson = new Gson();
    }

    public List<SupplementRecommendation> recommendForEmail(String email, Set<Integer> excludedSupplementIds, int limit) {
        if (email == null || email.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit <= 0 ? DEFAULT_LIMIT : limit, MAX_LIMIT));
        Set<Integer> excludedIds = excludedSupplementIds == null ? Set.of() : excludedSupplementIds;

        LocalRecommendationContext context = buildLocalContext(email.trim(), excludedIds, Math.max(safeLimit, GEMINI_POOL_SIZE));
        if (context.candidates().isEmpty()) {
            return List.of();
        }

        List<SupplementRecommendation> aiRecommendations = requestGeminiRecommendations(email.trim(), context, safeLimit);
        if (!aiRecommendations.isEmpty()) {
            return aiRecommendations;
        }

        return context.candidates().stream()
                .limit(safeLimit)
                .toList();
    }

    private LocalRecommendationContext buildLocalContext(String email, Set<Integer> excludedIds, int poolLimit) {
        Map<Integer, Double> reorderScores = getReorderScores(email);
        List<Integer> purchasedIds = reorderScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(12)
                .toList();

        Map<String, Double> userCategoryScores = getUserCategoryScores(email);
        Map<Integer, Double> categoryScores = userCategoryScores.isEmpty()
                ? Map.of()
                : getCategoryCandidateScores(userCategoryScores, purchasedIds);

        Map<Integer, Double> crossSellScores = purchasedIds.isEmpty()
                ? Map.of()
                : getCrossSellScores(purchasedIds);

        Map<Integer, Double> fallbackScores = (reorderScores.isEmpty() && crossSellScores.isEmpty() && categoryScores.isEmpty())
                ? getTopSellingScores()
                : Map.of();

        Map<Integer, Double> mergedScores = mergeAndNormalizeScores(reorderScores, crossSellScores, categoryScores, fallbackScores);
        if (mergedScores.isEmpty()) {
            return new LocalRecommendationContext(List.of(), Map.of(), purchasedIds);
        }

        List<Integer> rankedIds = mergedScores.entrySet().stream()
                .filter(entry -> !excludedIds.contains(entry.getKey()))
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(poolLimit)
                .toList();

        if (rankedIds.isEmpty()) {
            return new LocalRecommendationContext(List.of(), Map.of(), purchasedIds);
        }

        Map<Integer, Supplement> availableSupplements = loadAvailableSupplementsByIds(rankedIds);
        if (availableSupplements.isEmpty()) {
            return new LocalRecommendationContext(List.of(), Map.of(), purchasedIds);
        }

        List<SupplementRecommendation> candidates = new ArrayList<>();
        for (Integer supplementId : rankedIds) {
            Supplement supplement = availableSupplements.get(supplementId);
            if (supplement == null) {
                continue;
            }

            double score = clampScore(mergedScores.getOrDefault(supplementId, 0.0));
            String source = resolveSource(supplementId, reorderScores, crossSellScores, categoryScores, fallbackScores);
            String reason = reasonFromSource(source);
            candidates.add(new SupplementRecommendation(supplementId, score, reason, source));
        }

        return new LocalRecommendationContext(candidates, availableSupplements, purchasedIds);
    }

    private Map<Integer, Double> getReorderScores(String email) {
        Map<Integer, Double> scores = new HashMap<>();
        try (PreparedStatement statement = cnx.prepareStatement(REORDER_QUERY)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    scores.put(
                            resultSet.getInt("supplement_id"),
                            resultSet.getDouble("weighted_score")
                    );
                }
            }
            return scores;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de calculer les suggestions reorder.", exception);
        }
    }

    private Map<Integer, Double> getCrossSellScores(List<Integer> purchasedIds) {
        if (purchasedIds == null || purchasedIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = purchasedIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String query = """
                SELECT
                    oi2.supplement_id,
                    SUM(oi2.quantity) AS cross_score
                FROM %s oi1
                JOIN %s oi2 ON oi1.order_id = oi2.order_id AND oi1.supplement_id <> oi2.supplement_id
                WHERE oi1.supplement_id IN (%s)
                  AND oi2.supplement_id NOT IN (%s)
                GROUP BY oi2.supplement_id
                ORDER BY cross_score DESC
                LIMIT 40
                """.formatted(
                SchemaInitializer.SUPPLEMENT_ORDER_ITEM_TABLE,
                SchemaInitializer.SUPPLEMENT_ORDER_ITEM_TABLE,
                placeholders,
                placeholders
        );

        Map<Integer, Double> scores = new HashMap<>();
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            int index = 1;
            for (Integer purchasedId : purchasedIds) {
                statement.setInt(index++, purchasedId);
            }
            for (Integer purchasedId : purchasedIds) {
                statement.setInt(index++, purchasedId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    scores.put(
                            resultSet.getInt("supplement_id"),
                            resultSet.getDouble("cross_score")
                    );
                }
            }
            return scores;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de calculer les suggestions cross-sell.", exception);
        }
    }

    private Map<Integer, Double> getTopSellingScores() {
        Map<Integer, Double> scores = new HashMap<>();
        try (PreparedStatement statement = cnx.prepareStatement(TOP_SELLING_QUERY);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                scores.put(
                        resultSet.getInt("supplement_id"),
                        resultSet.getDouble("sold_qty")
                );
            }
            return scores;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de calculer les suggestions fallback.", exception);
        }
    }

    private Map<String, Double> getUserCategoryScores(String email) {
        Map<String, Double> categoryScores = new HashMap<>();
        try (PreparedStatement statement = cnx.prepareStatement(USER_CATEGORY_SCORE_QUERY)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String categoryKey = resultSet.getString("category_key");
                    if (categoryKey == null || categoryKey.isBlank()) {
                        continue;
                    }
                    categoryScores.put(categoryKey, resultSet.getDouble("weighted_score"));
                }
            }
            return categoryScores;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de calculer les suggestions par categorie.", exception);
        }
    }

    private Map<Integer, Double> getCategoryCandidateScores(Map<String, Double> userCategoryScores, List<Integer> purchasedIds) {
        if (userCategoryScores == null || userCategoryScores.isEmpty()) {
            return Map.of();
        }

        List<String> categories = userCategoryScores.keySet().stream()
                .filter(category -> category != null && !category.isBlank())
                .toList();
        if (categories.isEmpty()) {
            return Map.of();
        }

        String categoryPlaceholders = categories.stream()
                .map(category -> "?")
                .collect(Collectors.joining(", "));

        StringBuilder queryBuilder = new StringBuilder("""
                SELECT id, LOWER(TRIM(category)) AS category_key
                FROM %s
                WHERE stock > 0
                  AND category IS NOT NULL
                  AND TRIM(category) <> ''
                  AND LOWER(TRIM(category)) IN (%s)
                """.formatted(SchemaInitializer.SUPPLEMENT_TABLE, categoryPlaceholders));

        List<Integer> safePurchasedIds = purchasedIds == null ? List.of() : purchasedIds.stream()
                .filter(id -> id != null && id > 0)
                .toList();
        if (!safePurchasedIds.isEmpty()) {
            String purchasedPlaceholders = safePurchasedIds.stream()
                    .map(id -> "?")
                    .collect(Collectors.joining(", "));
            queryBuilder.append(" AND id NOT IN (").append(purchasedPlaceholders).append(")");
        }

        Map<Integer, Double> scores = new HashMap<>();
        try (PreparedStatement statement = cnx.prepareStatement(queryBuilder.toString())) {
            int index = 1;
            for (String category : categories) {
                statement.setString(index++, category);
            }
            for (Integer purchasedId : safePurchasedIds) {
                statement.setInt(index++, purchasedId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int supplementId = resultSet.getInt("id");
                    String categoryKey = resultSet.getString("category_key");
                    double score = userCategoryScores.getOrDefault(categoryKey, 0.0);
                    if (score > 0.0) {
                        scores.put(supplementId, score);
                    }
                }
            }
            return scores;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de calculer les candidats par categorie.", exception);
        }
    }

    private Map<Integer, Supplement> loadAvailableSupplementsByIds(List<Integer> supplementIds) {
        if (supplementIds == null || supplementIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = supplementIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String query = """
                SELECT id, name, category, brand, price, stock, calories, description, image, created_at, updated_at
                FROM %s
                WHERE id IN (%s)
                  AND stock > 0
                """.formatted(SchemaInitializer.SUPPLEMENT_TABLE, placeholders);

        Map<Integer, Supplement> supplements = new HashMap<>();
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            int index = 1;
            for (Integer supplementId : supplementIds) {
                statement.setInt(index++, supplementId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Supplement supplement = new Supplement();
                    supplement.setId(resultSet.getInt("id"));
                    supplement.setName(resultSet.getString("name"));
                    supplement.setCategory(resultSet.getString("category"));
                    supplement.setBrand(resultSet.getString("brand"));
                    supplement.setPrice(resultSet.getBigDecimal("price"));
                    supplement.setStock(resultSet.getInt("stock"));

                    int calories = resultSet.getInt("calories");
                    supplement.setCalories(resultSet.wasNull() ? null : calories);

                    supplement.setDescription(resultSet.getString("description"));
                    supplement.setImage(resultSet.getString("image"));

                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    supplement.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

                    Timestamp updatedAt = resultSet.getTimestamp("updated_at");
                    supplement.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

                    supplements.put(supplement.getId(), supplement);
                }
            }
            return supplements;
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de charger les produits recommandes.", exception);
        }
    }

    private Map<Integer, Double> mergeAndNormalizeScores(
            Map<Integer, Double> reorderScores,
            Map<Integer, Double> crossSellScores,
            Map<Integer, Double> categoryScores,
            Map<Integer, Double> fallbackScores
    ) {
        Map<Integer, Double> merged = new HashMap<>();

        Map<Integer, Double> normalizedReorder = normalizeScores(reorderScores);
        Map<Integer, Double> normalizedCrossSell = normalizeScores(crossSellScores);
        Map<Integer, Double> normalizedCategory = normalizeScores(categoryScores);
        Map<Integer, Double> normalizedFallback = normalizeScores(fallbackScores);

        for (Map.Entry<Integer, Double> entry : normalizedReorder.entrySet()) {
            merged.merge(entry.getKey(), entry.getValue() * REORDER_WEIGHT, Double::sum);
        }
        for (Map.Entry<Integer, Double> entry : normalizedCrossSell.entrySet()) {
            merged.merge(entry.getKey(), entry.getValue() * CROSS_SELL_WEIGHT, Double::sum);
        }
        for (Map.Entry<Integer, Double> entry : normalizedCategory.entrySet()) {
            merged.merge(entry.getKey(), entry.getValue() * CATEGORY_WEIGHT, Double::sum);
        }

        if (merged.isEmpty()) {
            merged.putAll(normalizedFallback);
        }

        return merged;
    }

    private Map<Integer, Double> normalizeScores(Map<Integer, Double> rawScores) {
        if (rawScores == null || rawScores.isEmpty()) {
            return Map.of();
        }

        double maxScore = rawScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);

        if (maxScore <= 0.0) {
            return Map.of();
        }

        Map<Integer, Double> normalized = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : rawScores.entrySet()) {
            normalized.put(entry.getKey(), clampScore(entry.getValue() / maxScore));
        }
        return normalized;
    }

    private String resolveSource(
            int supplementId,
            Map<Integer, Double> reorderScores,
            Map<Integer, Double> crossSellScores,
            Map<Integer, Double> categoryScores,
            Map<Integer, Double> fallbackScores
    ) {
        boolean hasReorder = reorderScores.containsKey(supplementId);
        boolean hasCrossSell = crossSellScores.containsKey(supplementId);
        boolean hasCategory = categoryScores.containsKey(supplementId);
        int strategyCount = (hasReorder ? 1 : 0) + (hasCrossSell ? 1 : 0) + (hasCategory ? 1 : 0);

        if (strategyCount >= 2) {
            return "hybrid";
        }
        if (hasReorder) {
            return "reorder";
        }
        if (hasCrossSell) {
            return "cross_sell";
        }
        if (hasCategory) {
            return "category";
        }
        if (fallbackScores.containsKey(supplementId)) {
            return "popular";
        }
        return "local";
    }

    private String reasonFromSource(String source) {
        return switch (source) {
            case "hybrid" -> "Based on your previous orders and similar customer baskets.";
            case "reorder" -> "You bought this product before.";
            case "cross_sell" -> "Customers with similar orders also buy this.";
            case "category" -> "From categories you buy often.";
            case "popular" -> "Popular product in recent orders.";
            default -> "Recommended from your order history.";
        };
    }

    private List<SupplementRecommendation> requestGeminiRecommendations(
            String email,
            LocalRecommendationContext context,
            int limit
    ) {
        String apiKey = readConfig("fitopia.gemini.api.key", "GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return List.of();
        }

        String prompt = buildGeminiPrompt(email, context, limit);
        List<String> modelsToTry = resolveModelsToTry();

        for (String model : modelsToTry) {
            List<SupplementRecommendation> modelResponse =
                    requestGeminiWithModel(apiKey.trim(), model, prompt, context, limit);
            if (!modelResponse.isEmpty()) {
                return modelResponse;
            }
        }

        return List.of();
    }

    private List<SupplementRecommendation> requestGeminiWithModel(
            String apiKey,
            String model,
            String prompt,
            LocalRecommendationContext context,
            int limit
    ) {
        String endpoint = GEMINI_ENDPOINT_TEMPLATE.formatted(
                URLEncoder.encode(model, StandardCharsets.UTF_8),
                URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
        );

        JsonObject payload = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        payload.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.2);
        generationConfig.addProperty("responseMimeType", "application/json");
        generationConfig.addProperty("maxOutputTokens", 700);
        payload.add("generationConfig", generationConfig);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("Gemini model " + model + " failed with status " + response.statusCode() + ".");
                return List.of();
            }
            return parseGeminiResponse(response.body(), context, limit);
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            System.err.println("Gemini model " + model + " failed: " + exception.getMessage());
            return List.of();
        }
    }

    private List<String> resolveModelsToTry() {
        LinkedHashSet<String> models = new LinkedHashSet<>();

        String configuredModel = readConfig("fitopia.gemini.model", "GEMINI_MODEL");
        if (configuredModel != null && !configuredModel.isBlank()) {
            models.add(configuredModel.trim());
        }

        models.add(DEFAULT_GEMINI_MODEL);
        models.add("gemini-1.5-flash-latest");
        models.add("gemini-1.5-flash");

        return new ArrayList<>(models);
    }

    private String buildGeminiPrompt(String email, LocalRecommendationContext context, int limit) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a recommendation ranking engine for a supplement shop.\n");
        prompt.append("Task: rank candidate products for this user and provide short reasons.\n");
        prompt.append("User email: ").append(email).append("\n");
        prompt.append("Max recommendations: ").append(limit).append("\n");

        if (!context.purchasedSupplementIds().isEmpty()) {
            prompt.append("Previously purchased supplement IDs: ")
                    .append(context.purchasedSupplementIds())
                    .append("\n");
        } else {
            prompt.append("No previous purchases were found; local engine used popularity fallback.\n");
        }

        prompt.append("Candidate products:\n");
        for (SupplementRecommendation candidate : context.candidates()) {
            Supplement supplement = context.supplementsById().get(candidate.getSupplementId());
            if (supplement == null) {
                continue;
            }
            prompt.append("- id=").append(supplement.getId())
                    .append(" | name=").append(safeText(supplement.getName()))
                    .append(" | category=").append(safeText(supplement.getCategory()))
                    .append(" | brand=").append(safeText(supplement.getBrand()))
                    .append(" | price=").append(formatPrice(supplement.getPrice()))
                    .append(" | base_score=").append(String.format(Locale.ROOT, "%.4f", candidate.getScore()))
                    .append(" | base_reason=").append(safeText(candidate.getReason()))
                    .append("\n");
        }

        prompt.append("\nReturn ONLY valid JSON in this exact shape:\n");
        prompt.append("{\"recommendations\":[{\"supplement_id\":123,\"score\":0.91,\"reason\":\"short reason\"}]}\n");
        prompt.append("Rules:\n");
        prompt.append("- Only use supplement_id values from the candidate list.\n");
        prompt.append("- Keep score between 0 and 1.\n");
        prompt.append("- Keep reason concise (max 12 words).\n");
        prompt.append("- No markdown, no extra keys, no explanation outside JSON.\n");
        return prompt.toString();
    }

    private List<SupplementRecommendation> parseGeminiResponse(
            String responseBody,
            LocalRecommendationContext context,
            int limit
    ) {
        try {
            JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
            if (root.has("error")) {
                return List.of();
            }

            JsonArray candidates = root.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return List.of();
            }

            JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            JsonObject content = firstCandidate.getAsJsonObject("content");
            if (content == null) {
                return List.of();
            }

            JsonArray parts = content.getAsJsonArray("parts");
            if (parts == null || parts.isEmpty()) {
                return List.of();
            }

            JsonObject firstPart = parts.get(0).getAsJsonObject();
            String text = firstPart.has("text") ? firstPart.get("text").getAsString() : null;
            if (text == null || text.isBlank()) {
                return List.of();
            }

            return parseRecommendationPayload(text, context, limit);
        } catch (RuntimeException exception) {
            return List.of();
        }
    }

    private List<SupplementRecommendation> parseRecommendationPayload(
            String payloadText,
            LocalRecommendationContext context,
            int limit
    ) {
        String cleaned = stripCodeFence(payloadText);
        JsonElement parsed = JsonParser.parseString(cleaned);

        JsonArray recommendationsArray;
        if (parsed.isJsonObject() && parsed.getAsJsonObject().has("recommendations")) {
            recommendationsArray = parsed.getAsJsonObject().getAsJsonArray("recommendations");
        } else if (parsed.isJsonArray()) {
            recommendationsArray = parsed.getAsJsonArray();
        } else {
            return List.of();
        }

        Map<Integer, SupplementRecommendation> candidateById = context.candidates().stream()
                .collect(Collectors.toMap(SupplementRecommendation::getSupplementId, candidate -> candidate));

        List<SupplementRecommendation> finalRecommendations = new ArrayList<>();
        Set<Integer> usedIds = new LinkedHashSet<>();

        for (JsonElement recommendationElement : recommendationsArray) {
            if (!recommendationElement.isJsonObject()) {
                continue;
            }

            JsonObject recommendationObject = recommendationElement.getAsJsonObject();
            int supplementId = readInt(recommendationObject, "supplement_id", -1);
            if (supplementId <= 0 || usedIds.contains(supplementId)) {
                continue;
            }

            SupplementRecommendation baseCandidate = candidateById.get(supplementId);
            if (baseCandidate == null) {
                continue;
            }

            double score = readDouble(recommendationObject, "score", baseCandidate.getScore());
            String reason = readString(recommendationObject, "reason", baseCandidate.getReason());

            finalRecommendations.add(new SupplementRecommendation(
                    supplementId,
                    clampScore(score),
                    reason,
                    "gemini"
            ));
            usedIds.add(supplementId);

            if (finalRecommendations.size() >= limit) {
                break;
            }
        }

        if (finalRecommendations.size() < limit) {
            for (SupplementRecommendation candidate : context.candidates()) {
                if (usedIds.contains(candidate.getSupplementId())) {
                    continue;
                }
                finalRecommendations.add(candidate);
                if (finalRecommendations.size() >= limit) {
                    break;
                }
            }
        }

        return finalRecommendations;
    }

    private int readInt(JsonObject jsonObject, String key, int fallback) {
        if (!jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return jsonObject.get(key).getAsInt();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private double readDouble(JsonObject jsonObject, String key, double fallback) {
        if (!jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return jsonObject.get(key).getAsDouble();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private String readString(JsonObject jsonObject, String key, String fallback) {
        if (!jsonObject.has(key) || jsonObject.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            String value = jsonObject.get(key).getAsString();
            return value == null || value.isBlank() ? fallback : value.trim();
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    private String stripCodeFence(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int firstLineBreak = trimmed.indexOf('\n');
        if (firstLineBreak < 0) {
            return trimmed.replace("```", "").trim();
        }

        String withoutHeader = trimmed.substring(firstLineBreak + 1);
        int closingFence = withoutHeader.lastIndexOf("```");
        if (closingFence >= 0) {
            return withoutHeader.substring(0, closingFence).trim();
        }
        return withoutHeader.trim();
    }

    private String readConfig(String propertyKey, String envKey) {
        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        return null;
    }

    private double clampScore(double score) {
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(score, 1.0));
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0.00 DT";
        }
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString() + " DT";
    }

    private record LocalRecommendationContext(
            List<SupplementRecommendation> candidates,
            Map<Integer, Supplement> supplementsById,
            List<Integer> purchasedSupplementIds
    ) {
    }
}
