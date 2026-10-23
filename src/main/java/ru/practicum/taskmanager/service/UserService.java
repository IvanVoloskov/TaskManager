package ru.practicum.taskmanager.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import ru.practicum.taskmanager.dto.UserDto;
import ru.practicum.taskmanager.dto.UserResponse;
import ru.practicum.taskmanager.exception.ConflictException;
import ru.practicum.taskmanager.exception.NotFoundException;
import ru.practicum.taskmanager.mapper.UserMapper;
import ru.practicum.taskmanager.model.User;
import ru.practicum.taskmanager.repository.TaskRepository;
import ru.practicum.taskmanager.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserMapper userMapper;

    public UserResponse create(UserDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Пользователь с email " + dto.email() + " уже существует");
        }
        User user = userRepository.save(userMapper.toEntity(dto));
        log.info("Создан пользователь id={}, email={}", user.getId(), user.getEmail());
        return userMapper.toResponse(user);
    }

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    public UserResponse getById(Long id) {
        return userMapper.toResponse(getEntity(id));
    }

    public User getEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    @CacheEvict(cacheNames = "tasks", allEntries = true)
    public UserResponse update(Long id, UserDto dto) {
        User user = getEntity(id);
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Пользователь с email " + dto.email() + " уже существует");
        }
        userMapper.updateEntity(user, dto);
        log.info("Обновлён пользователь id={}", id);
        return userMapper.toResponse(userRepository.save(user));
    }

    @CacheEvict(cacheNames = "tasks", allEntries = true)
    public void delete(Long id) {
        getEntity(id);
        if (taskRepository.existsByUserId(id)) {
            throw new ConflictException("Нельзя удалить пользователя id=" + id + ": у него есть задачи");
        }
        userRepository.deleteById(id);
        log.info("Удалён пользователь id={}", id);
    }
}