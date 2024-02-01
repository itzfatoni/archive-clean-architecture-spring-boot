package br.com.elwgomes.registerapi.infra.user.controller.impl;

import br.com.elwgomes.registerapi.core.user.exception.UserAlreadyExistsException;
import br.com.elwgomes.registerapi.core.user.exception.UserNotFoundException;
import br.com.elwgomes.registerapi.core.user.usecase.command.DeleteUserCommand;
import br.com.elwgomes.registerapi.core.user.usecase.command.GetAllUsersCommand;
import br.com.elwgomes.registerapi.core.user.usecase.command.SaveUserCommand;
import br.com.elwgomes.registerapi.infra.user.controller.UserResource;
import br.com.elwgomes.registerapi.infra.user.controller.response.UserResponse;
import br.com.elwgomes.registerapi.core.user.dto.UserDTO;
import br.com.elwgomes.registerapi.infra.user.persistence.entity.UserEntity;
import br.com.elwgomes.registerapi.infra.user.persistence.mapper.UserRepositoryMapperImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserResourceImpl implements UserResource {

    private final GetAllUsersCommand getAllUsersCommand;
    private final SaveUserCommand saveUserCommand;
    private final DeleteUserCommand deleteUserCommand;

    private final UserRepositoryMapperImpl mapper;

    @Override
    @GetMapping
    public UserResponse<Collection<UserDTO>> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse("");
        if ("ROLE_ADMIN".equals(role)) {
            return new UserResponse<>("success", String.valueOf(HttpStatus.OK), "OK", getAllUsersCommand.execute().stream().map(mapper::mapDomainToDtoFullDetails).collect(Collectors.toList()));
        }
        return new UserResponse<>("success", String.valueOf(HttpStatus.OK), "OK", getAllUsersCommand.execute().stream().map(mapper::mapDomainToDto).collect(Collectors.toList()));
    }

    @Override
    @PostMapping
    public UserResponse<Boolean> saveUser(@RequestBody @Valid UserEntity entity) throws Exception {
        try {
            saveUserCommand.execute(mapper.mapToDomain(entity));
        } catch (UserAlreadyExistsException e) {
            throw new Exception(e);
        }
        return new UserResponse<>("success", String.valueOf(HttpStatus.OK), "New user has been created.");
    }

    @Override
    @DeleteMapping("{id}")
    public UserResponse<Boolean> deleteUser(@PathVariable("id") UUID id) throws UserNotFoundException {
        try {
            deleteUserCommand.execute(id);
        } catch (UserNotFoundException exception) {
            throw new UserNotFoundException(exception.getMessage());
        }

        return new UserResponse<>("no content", String.valueOf(HttpStatus.NO_CONTENT), "User has been deleted.");

    }

}
