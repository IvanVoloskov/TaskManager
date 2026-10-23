package ru.practicum.taskmanager.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.taskmanager.dto.UserDto;
import ru.practicum.taskmanager.dto.UserResponse;
import ru.practicum.taskmanager.model.User;

@Component
public class UserMapper {

    public User toEntity(UserDto dto) {
        return new User(dto.name(), dto.email());
    }

    public void updateEntity(User user, UserDto dto) {
        user.setName(dto.name());
        user.setEmail(dto.email());
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}