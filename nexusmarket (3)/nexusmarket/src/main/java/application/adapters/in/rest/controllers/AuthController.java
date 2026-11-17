package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.ParticipantRestMapper;
import application.adapters.in.rest.requests.LoginRequest;
import application.adapters.in.rest.requests.RegisterBuyerRequest;
import application.adapters.in.rest.responses.LoginResponse;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.services.buyer.RegisterBuyerService;
import application.domain.services.user.LoginService;
import application.domain.services.user.RegisterParticipantUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Public endpoints (no token required): login and buyer self-registration. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Transactional
public class AuthController {

    private final LoginService loginService;
    private final RegisterBuyerService registerBuyerService;
    private final RegisterParticipantUserService registerParticipantUserService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        User credentials = new User();
        credentials.setUsername(request.username());
        credentials.setPassword(request.password());
        return LoginResponse.bearer(loginService.execute(credentials));
    }

    /** Buyer + user in one transaction: if the user cannot be created, the buyer is rolled back. */
    @PostMapping("/register-buyer")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerBuyer(@Valid @RequestBody RegisterBuyerRequest request) {
        Buyer buyer = registerBuyerService.execute(ParticipantRestMapper.toBuyer(request));
        User user = registerParticipantUserService.execute(ParticipantRestMapper.toBuyerUser(request, buyer));
        return ParticipantRestMapper.toResponse(user);
    }
}
