package application.adapters.in.rest.responses;

/** Never exposes the password hash. participantType: BUYER, SELLER or null for staff. */
public record UserResponse(
        Integer userId,
        String username,
        String identification,
        String fullName,
        String email,
        String phoneNumber,
        String address,
        String role,
        String status,
        String participantType,
        String participantIdentification) {
}
