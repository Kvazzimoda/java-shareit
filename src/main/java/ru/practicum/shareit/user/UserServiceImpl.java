package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getUserModel(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userRepository.findByEmail(userDto.getEmail()).ifPresent(existing -> {
            throw new IllegalStateException("Email already in use");
        });

        User user = UserMapper.toUser(userDto);
        User saved = userRepository.save(user);
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existing = getUserModel(userId);

        if (userDto.getEmail() != null && !userDto.getEmail().equalsIgnoreCase(existing.getEmail())) {
            userRepository.findByEmail(userDto.getEmail()).ifPresent(otherUser -> {
                if (!otherUser.getId().equals(userId)) {
                    throw new IllegalStateException("Email already in use");
                }
            });
            existing.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }

        User saved = userRepository.save(existing);
        return UserMapper.toDto(saved);
    }

    @Override
    public UserDto getUser(Long userId) {
        return UserMapper.toDto(getUserModel(userId));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }
}