package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private Long userIdCounter = 1L;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        // Проверяем уникальность email
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            throw new IllegalStateException("Email already exists");
        }

        User user = new User();
        user.setId(userIdCounter++);
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        users.put(user.getId(), user);
        return mapToDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = users.get(userId);
        if (user == null) throw new IllegalArgumentException("User not found");
        if (userDto.getName() != null) user.setName(userDto.getName());
        if (userDto.getEmail() != null) {
            // Проверяем уникальность email при обновлении (кроме текущего пользователя)
            if (!user.getEmail().equals(userDto.getEmail()) &&
                    users.values().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
                throw new IllegalStateException("Email already exists");
            }
            user.setEmail(userDto.getEmail());
        }
        return mapToDto(user);
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = users.get(userId);
        if (user == null) throw new IllegalArgumentException("User not found");
        return mapToDto(user);
    }

    @Override
    public User getUserModel(Long userId) {
        User user = users.get(userId);
        if (user == null) throw new NotFoundException("User with id " + userId + " not found");
        return user;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}