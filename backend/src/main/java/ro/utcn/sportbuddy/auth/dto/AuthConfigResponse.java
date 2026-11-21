package ro.utcn.sportbuddy.auth.dto;

// Ce trebuie să știe pagina de autentificare înainte de conectare.
// googleClientId gol: Google nu e configurat, iar butonul folosește conectarea simulată.
// mailConfigured false: linkurile din emailuri apar doar în consola backend-ului.
public record AuthConfigResponse(String googleClientId, boolean mailConfigured) {}
