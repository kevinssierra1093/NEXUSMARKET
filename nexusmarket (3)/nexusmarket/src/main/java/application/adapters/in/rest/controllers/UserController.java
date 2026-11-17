package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.ParticipantRestMapper;
import application.adapters.in.rest.mappers.RestReferences;
import application.adapters.in.rest.requests.ChangePasswordRequest;
import application.adapters.in.rest.requests.ChangeStatusRequest;
import application.adapters.in.rest.requests.RegisterParticipantUserRequest;
import application.adapters.in.rest.requests.RegisterStaffUserRequest;
import application.adapters.in.rest.responses.UserResponse;
import application.domain.services.user.ChangeUserPasswordService;
import application.domain.services.user.ChangeUserStatusService;
import application.domain.services.user.ConsultUserService;
import application.domain.services.user.RegisterParticipantUserService;
import application.domain.services.user.RegisterStaffUserService;
import application.domain.valueobjects.UserStatus;
import application.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static application.adapters.shared.ValueObjectResolver.fromCode;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Transactional
public class UserController {

    private final AuthenticatedUserResolver authenticatedUser;
    private final RegisterStaffUserService registerStaffUserService;
    private final RegisterParticipantUserService registerParticipantUserService;
    private final ConsultUserService consultUserService;
    private final ChangeUserStatusService changeUserStatusService;
    private final ChangeUserPasswordService changeUserPasswordService;

    /** ADMINISTRATOR: creates LOGISTICS_OPERATOR, ADMINISTRATOR or SUPERVISOR users. */
    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerStaff(@Valid @RequestBody RegisterStaffUserRequest request) {
        return ParticipantRestMapper.toResponse(
                registerStaffUserService.execute(authenticatedUser.resolve(), ParticipantRestMapper.toStaffUser(request)));
    }

    /** System access of an existing buyer or seller (typically the seller incorporated by the administrator). */
    @PostMapping("/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerParticipant(@Valid @RequestBody RegisterParticipantUserRequest request) {
        return ParticipantRestMapper.toResponse(
                registerParticipantUserService.execute(ParticipantRestMapper.toParticipantUser(request)));
    }

    @GetMapping
    public List<UserResponse> consultAll() {
        return consultUserService.executeAll(authenticatedUser.resolve()).stream()
                .map(ParticipantRestMapper::toResponse).toList();
    }

    @GetMapping("/me")
    public UserResponse consultMe() {
        var requestingUser = authenticatedUser.resolve();
        return ParticipantRestMapper.toResponse(consultUserService.execute(requestingUser, requestingUser));
    }

    @GetMapping("/{userId}")
    public UserResponse consult(@PathVariable Integer userId) {
        return ParticipantRestMapper.toResponse(
                consultUserService.execute(authenticatedUser.resolve(), RestReferences.user(userId)));
    }

    @PatchMapping("/{userId}/status")
    public UserResponse changeStatus(@PathVariable Integer userId, @Valid @RequestBody ChangeStatusRequest request) {
        return ParticipantRestMapper.toResponse(changeUserStatusService.execute(authenticatedUser.resolve(),
                RestReferences.user(userId), fromCode(UserStatus.class, request.status())));
    }

    @PatchMapping("/{userId}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable Integer userId, @Valid @RequestBody ChangePasswordRequest request) {
        changeUserPasswordService.execute(authenticatedUser.resolve(), RestReferences.user(userId), request.newPassword());
    }
}
