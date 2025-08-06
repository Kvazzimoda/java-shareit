package ru.practicum.shareit.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserModel_shouldReturnUserWhenExists() {
        // Given
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // When
        User actualUser = userService.getUserModel(userId);

        // Then
        assertEquals(expectedUser, actualUser);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserModel_shouldThrowNotFoundExceptionWhenUserNotExists() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.getUserModel(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void createUser_shouldSaveNewUser() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        UserDto result = userService.createUser(userDto);

        // Then
        assertNotNull(result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowExceptionWhenEmailExists() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setEmail("existing@example.com");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("existing@example.com");

        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(IllegalStateException.class, () -> userService.createUser(userDto));
        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_shouldUpdateNameAndEmail() {
        // Given
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");
        updateDto.setEmail("new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDto result = userService.updateUser(userId, updateDto);

        // Then
        assertEquals(userId, result.getId());
        assertEquals(updateDto.getName(), result.getName());
        assertEquals(updateDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmail(updateDto.getEmail());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_shouldUpdateOnlyNameWhenEmailNotChanged() {
        // Given
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("same@example.com");

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");
        updateDto.setEmail("same@example.com"); // Email не меняется

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDto result = userService.updateUser(userId, updateDto);

        // Then
        assertEquals(userId, result.getId());
        assertEquals(updateDto.getName(), result.getName());
        assertEquals(existingUser.getEmail(), result.getEmail()); // Email остался прежним
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_shouldThrowExceptionWhenNewEmailExistsForOtherUser() {
        // Given
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setEmail("old@example.com");

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        UserDto updateDto = new UserDto();
        updateDto.setEmail("other@example.com"); // Email другого пользователя

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail(updateDto.getEmail())).thenReturn(Optional.of(otherUser));

        // When & Then
        assertThrows(IllegalStateException.class, () -> userService.updateUser(userId, updateDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByEmail(updateDto.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUser_shouldReturnUserDto() {
        // Given
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserDto result = userService.getUser(userId);

        // Then
        assertEquals(userId, result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void deleteUser_shouldCallRepositoryDelete() {
        // Given
        Long userId = 1L;
        doNothing().when(userRepository).deleteById(userId);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void getAllUsers_shouldReturnListOfUserDtos() {
        // Given
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // When
        List<UserDto> result = userService.getAllUsers();

        // Then
        assertEquals(2, result.size());
        assertEquals(user1.getId(), result.get(0).getId());
        assertEquals(user2.getId(), result.get(1).getId());
        verify(userRepository, times(1)).findAll();
    }
}