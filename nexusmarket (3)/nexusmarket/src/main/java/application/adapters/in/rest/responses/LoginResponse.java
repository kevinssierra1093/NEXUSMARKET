package application.adapters.in.rest.responses;

public record LoginResponse(
        String token,
        String tokenType) {

    public static LoginResponse bearer(String token) {
        return new LoginResponse(token, "Bearer");
    }
}
