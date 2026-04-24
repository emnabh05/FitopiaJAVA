package tn.esprit.Pidev3A49.services.security;

public class ApiAuthorizationService {
    private final JwtService jwtService;

    public ApiAuthorizationService() {
        this(new JwtService());
    }

    public ApiAuthorizationService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public JwtClaims requireAuthenticated(String authorizationHeader) {
        return jwtService.validate(extractBearerToken(authorizationHeader));
    }

    public JwtClaims requireAdmin(String authorizationHeader) {
        JwtClaims claims = requireAuthenticated(authorizationHeader);
        if (!isAdminRole(claims.role())) {
            throw new RuntimeException("Acces refuse: role admin requis.");
        }
        return claims;
    }

    public JwtClaims requireSelfOrAdmin(String authorizationHeader, int targetUserId) {
        JwtClaims claims = requireAuthenticated(authorizationHeader);
        if (isAdminRole(claims.role()) || claims.userId() == targetUserId) {
            return claims;
        }
        throw new RuntimeException("Acces refuse: token non autorise pour cette ressource.");
    }

    public long getAccessTokenExpirySeconds() {
        return jwtService.getExpirySeconds();
    }

    private String extractBearerToken(String authorizationHeader) {
        String value = authorizationHeader == null ? "" : authorizationHeader.trim();
        if (value.isBlank()) {
            throw new RuntimeException("Header Authorization manquant.");
        }
        if (!value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new RuntimeException("Header Authorization invalide. Format attendu: Bearer <token>.");
        }
        String token = value.substring(7).trim();
        if (token.isBlank()) {
            throw new RuntimeException("Token JWT manquant.");
        }
        return token;
    }

    private boolean isAdminRole(String role) {
        return "admin".equalsIgnoreCase(role);
    }
}
