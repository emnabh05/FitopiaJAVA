package tn.esprit.Pidev3A49.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OpenFoodFactsService {
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v2/product/";
    private static final String USER_AGENT = "Fitopia-JavaApp/1.0";
    
    private final OkHttpClient httpClient;

    public OpenFoodFactsService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public ProductInfo getProductInfo(String barcode) throws IOException {
        String url = BASE_URL + barcode + ".json";
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", USER_AGENT)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Code HTTP: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Corps de la réponse vide");
            }
            String responseBody = body.string();
            JSONObject json = new JSONObject(responseBody);
            
            if (json.getInt("status") != 1) {
                throw new IOException("Produit non trouvé dans OpenFoodFacts");
            }

            JSONObject product = json.getJSONObject("product");
            return parseProductInfo(product, barcode);
        }
    }

    private ProductInfo parseProductInfo(JSONObject product, String barcode) {
        ProductInfo info = new ProductInfo();
        info.setBarcode(barcode);
        
        // Nom du produit
        if (product.has("product_name")) {
            info.setName(product.getString("product_name"));
        } else if (product.has("product_name_fr")) {
            info.setName(product.getString("product_name_fr"));
        } else {
            info.setName("Produit inconnu");
        }

        // Marque
        if (product.has("brands")) {
            info.setBrand(product.getString("brands"));
        } else if (product.has("brand")) {
            info.setBrand(product.getString("brand"));
        }

        // Catégories
        if (product.has("categories")) {
            info.setCategories(product.getString("categories"));
        } else if (product.has("categories_fr")) {
            info.setCategories(product.getString("categories_fr"));
        }

        // Nutri-Score - Essayer TOUS les champs possibles avec débogage
        String nutriScore = "N/A";
        
        // Debug: afficher les champs disponibles (Désactivé pour ne pas spammer la console)
        // System.out.println("Champs disponibles dans le produit: " + product.keySet());
        
        if (product.has("nutriscore_grade")) {
            nutriScore = product.getString("nutriscore_grade").toUpperCase();
            System.out.println("Nutri-Score trouvé dans nutriscore_grade: " + nutriScore);
        } else if (product.has("nutriscore")) {
            nutriScore = product.getString("nutriscore").toUpperCase();
            System.out.println("Nutri-Score trouvé dans nutriscore: " + nutriScore);
        } else if (product.has("nutrition_grades")) {
            String grades = product.getString("nutrition_grades");
            if (!grades.isEmpty() && !grades.equals("unknown") && !grades.equals("null")) {
                nutriScore = grades.split(",")[0].trim().toUpperCase();
                System.out.println("Nutri-Score trouvé dans nutrition_grades: " + nutriScore);
            }
        } else if (product.has("nutrition_grade")) {
            nutriScore = product.getString("nutrition_grade").toUpperCase();
            System.out.println("Nutri-Score trouvé dans nutrition_grade: " + nutriScore);
        }
        
        // Si toujours N/A, essayer de calculer basé sur les nutriments
        if (nutriScore.equals("N/A") && product.has("nutriments")) {
            JSONObject nutriments = product.getJSONObject("nutriments");
            System.out.println("Tentative de calcul du Nutri-Score depuis les nutriments...");
            nutriScore = calculateNutriScoreFromNutriments(nutriments);
            System.out.println("Nutri-Score calculé: " + nutriScore);
        }
        
        // Si toujours N/A, essayer une estimation basique
        if (nutriScore.equals("N/A")) {
            nutriScore = estimateBasicNutriScore(product);
            System.out.println("Nutri-Score estimé: " + nutriScore);
        }
        
        info.setNutriScore(nutriScore);

        // Calories pour 100g
        if (product.has("nutriments")) {
            JSONObject nutriments = product.getJSONObject("nutriments");
            if (nutriments.has("energy-kcal_100g")) {
                info.setCaloriesPer100g(nutriments.getDouble("energy-kcal_100g"));
            } else if (nutriments.has("energy_100g")) {
                // Convertir de kJ à kcal si nécessaire
                double energyKJ = nutriments.getDouble("energy_100g");
                info.setCaloriesPer100g(energyKJ / 4.184);
            }
        }

        // Protéines
        if (product.has("nutriments")) {
            JSONObject nutriments = product.getJSONObject("nutriments");
            if (nutriments.has("proteins_100g")) {
                info.setProteinesPer100g(nutriments.getDouble("proteins_100g"));
            }
        }

        // Glucides
        if (product.has("nutriments")) {
            JSONObject nutriments = product.getJSONObject("nutriments");
            if (nutriments.has("carbohydrates_100g")) {
                info.setCarbohydratesPer100g(nutriments.getDouble("carbohydrates_100g"));
            }
        }

        // Lipides
        if (product.has("nutriments")) {
            JSONObject nutriments = product.getJSONObject("nutriments");
            if (nutriments.has("fat_100g")) {
                info.setLipidesPer100g(nutriments.getDouble("fat_100g"));
            }
        }

        // Catégories
        if (product.has("categories")) {
            info.setCategories(product.getString("categories"));
        }

        // Ingrédients
        if (product.has("ingredients_text")) {
            info.setIngredients(product.getString("ingredients_text"));
        }

        return info;
    }

    public boolean isCompatibleWithRegime(ProductInfo product, String regimeType) {
        if (product.getNutriScore().equals("E")) {
            return false; // Nutri-Score E est généralement incompatible avec les régimes
        }

        // Logique supplémentaire selon le type de régime
        switch (regimeType.toLowerCase()) {
            case "perte de poids":
                return product.getCaloriesPer100g() <= 400; // Limite de calories pour régime minceur
            case "prise de masse":
                return product.getProteinsPer100g() >= 10; // Minimum de protéines
            case "equilibré":
                return product.getNutriScore().equals("A") || product.getNutriScore().equals("B");
            default:
                return true;
        }
    }

    public String getCompatibilityMessage(ProductInfo product, String regimeType) {
        if (product.getNutriScore().equals("E")) {
            return String.format("Attention : Nutri-Score %s, incompatible avec votre régime actuel.", 
                    product.getNutriScore());
        }

        if (!isCompatibleWithRegime(product, regimeType)) {
            return String.format("Produit %s - Nutri-Score %s. Modération recommandée pour votre régime %s.", 
                    product.getName(), product.getNutriScore(), regimeType);
        }

        return String.format("Produit %s - Nutri-Score %s. Compatible avec votre régime %s.", 
                product.getName(), product.getNutriScore(), regimeType);
    }

    public static class ProductInfo {
        private String barcode;
        private String name;
        private String nutriScore;
        private String brand;
        private String categories;
        private String ingredients;
        private double caloriesPer100g;
        private double proteinsPer100g;
        private double carbohydratesPer100g;
        private double lipidesPer100g;

        public ProductInfo() {
            this.caloriesPer100g = 0;
            this.proteinsPer100g = 0;
            this.carbohydratesPer100g = 0;
            this.lipidesPer100g = 0;
        }

        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getNutriScore() { return nutriScore; }
        public void setNutriScore(String nutriScore) { this.nutriScore = nutriScore; }

        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }

        public double getCaloriesPer100g() { return caloriesPer100g; }
        public void setCaloriesPer100g(double caloriesPer100g) { this.caloriesPer100g = caloriesPer100g; }

        public double getProteinsPer100g() { return proteinsPer100g; }
        public void setProteinesPer100g(double proteinsPer100g) { this.proteinsPer100g = proteinsPer100g; }

        public double getCarbohydratesPer100g() { return carbohydratesPer100g; }
        public void setCarbohydratesPer100g(double carbohydratesPer100g) { this.carbohydratesPer100g = carbohydratesPer100g; }

        public double getLipidesPer100g() { return lipidesPer100g; }
        public void setLipidesPer100g(double lipidesPer100g) { this.lipidesPer100g = lipidesPer100g; }

        public String getCategories() { return categories; }
        public void setCategories(String categories) { this.categories = categories; }

        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }

        @Override
        public String toString() {
            return String.format("%s - Nutri-Score: %s - %.0f kcal/100g", 
                    name, nutriScore, caloriesPer100g);
        }
    }
    
    private String calculateNutriScoreFromNutriments(JSONObject nutriments) {
        // Calcul simplifié du Nutri-Score basé sur les nutriments disponibles
        // C'est une approximation car le vrai calcul est complexe
        try {
            double calories = 0;
            double sugars = 0;
            double saturatedFat = 0;
            double sodium = 0;
            double fiber = 0;
            double proteins = 0;
            
            // Récupérer les valeurs
            if (nutriments.has("energy-kcal_100g")) {
                calories = nutriments.getDouble("energy-kcal_100g");
            }
            if (nutriments.has("sugars_100g")) {
                sugars = nutriments.getDouble("sugars_100g");
            }
            if (nutriments.has("saturated-fat_100g")) {
                saturatedFat = nutriments.getDouble("saturated-fat_100g");
            }
            if (nutriments.has("sodium_100g")) {
                sodium = nutriments.getDouble("sodium_100g");
            }
            if (nutriments.has("fiber_100g")) {
                fiber = nutriments.getDouble("fiber_100g");
            }
            if (nutriments.has("proteins_100g")) {
                proteins = nutriments.getDouble("proteins_100g");
            }
            
            // Calcul simple du Nutri-Score
            int score = 0;
            
            // Points négatifs
            if (calories > 335) score += 10;
            else if (calories > 270) score += 6;
            else if (calories > 200) score += 2;
            
            if (sugars > 13.5) score += 10;
            else if (sugars > 9) score += 6;
            else if (sugars > 4.5) score += 2;
            
            if (saturatedFat > 6) score += 10;
            else if (saturatedFat > 4.5) score += 6;
            else if (saturatedFat > 3) score += 2;
            
            if (sodium > 0.9) score += 10;
            else if (sodium > 0.6) score += 6;
            else if (sodium > 0.3) score += 2;
            
            // Points positifs
            if (fiber > 4.7) score -= 5;
            else if (fiber > 3.5) score -= 3;
            else if (fiber > 2.8) score -= 1;
            
            if (proteins > 8) score -= 5;
            else if (proteins > 6.4) score -= 3;
            else if (proteins > 4.8) score -= 1;
            
            // Convertir le score en lettre
            if (score <= -1) return "A";
            else if (score <= 2) return "B";
            else if (score <= 10) return "C";
            else if (score <= 18) return "D";
            else return "E";
            
        } catch (Exception e) {
            System.err.println("Erreur calcul Nutri-Score: " + e.getMessage());
            return "C";
        }
    }
    
    private String estimateBasicNutriScore(JSONObject product) {
        // Estimation très basique basée sur le nom du produit et les catégories
        try {
            String productName = product.has("product_name") ? product.getString("product_name").toLowerCase() : "";
            String categories = product.has("categories") ? product.getString("categories").toLowerCase() : "";
            
            // Mots-clés pour produits sains
            if (productName.contains("eau") || productName.contains("water") || 
                productName.contains("légume") || productName.contains("fruit") ||
                productName.contains("vegetable") || productName.contains("salade") ||
                categories.contains("fruits") || categories.contains("vegetables")) {
                return "A";
            }
            
            // Mots-clés pour produits modérément sains
            if (productName.contains("yaourt") || productName.contains("yogurt") ||
                productName.contains("lait") || productName.contains("milk") ||
                productName.contains("pain") || productName.contains("bread")) {
                return "B";
            }
            
            // Mots-clés pour produits moins sains
            if (productName.contains("biscuit") || productName.contains("cookie") ||
                productName.contains("chocolat") || productName.contains("chocolate") ||
                productName.contains("soda") || productName.contains("coca") ||
                categories.contains("sugary") || categories.contains("confectionery")) {
                return "D";
            }
            
            // Mots-clés pour produits très mauvais
            if (productName.contains("frites") || productName.contains("chips") ||
                productName.contains("burger") || productName.contains("pizza") ||
                categories.contains("fast-foods") || categories.contains("processed")) {
                return "E";
            }
            
            // Par défaut, retourner C
            return "C";
            
        } catch (Exception e) {
            System.err.println("Erreur estimation Nutri-Score: " + e.getMessage());
            return "C";
        }
    }
}
