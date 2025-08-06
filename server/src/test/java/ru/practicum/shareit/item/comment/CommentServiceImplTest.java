package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.comment.commentDto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void createComment_shouldCreateCommentSuccessfully() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        Item item = new Item();
        item.setId(itemId);

        User author = new User();
        author.setId(userId);

        CommentDto inputDto = new CommentDto();
        inputDto.setText("Great item!");

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setEnd(LocalDateTime.now().minusDays(1));

        Comment savedComment = new Comment();
        savedComment.setText("Great item!");
        savedComment.setItem(item);
        savedComment.setAuthor(author);
        savedComment.setCreated(LocalDateTime.now());

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.getUserModel(userId)).thenReturn(author);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(booking));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // When
        CommentDto result = commentService.createComment(itemId, userId, inputDto);

        // Then
        assertNotNull(result);
        assertEquals(inputDto.getText(), result.getText());

        verify(itemRepository).findById(itemId);
        verify(userService).getUserModel(userId);
        verify(bookingRepository).findByBookerId(userId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_shouldThrowWhenItemNotFound() {
        // Given
        Long itemId = 99L;
        Long userId = 1L;
        CommentDto inputDto = new CommentDto();
        inputDto.setText("Test comment");

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class,
                () -> commentService.createComment(itemId, userId, inputDto));

        verify(itemRepository).findById(itemId);
        verifyNoInteractions(userService, bookingRepository, commentRepository);
    }

    @Test
    void createComment_shouldThrowWhenTextIsBlank() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;
        CommentDto inputDto = new CommentDto();
        inputDto.setText(" ");

        // Подготавливаем моки для зависимостей, которые будут вызваны
        Item item = new Item();
        item.setId(itemId);

        User user = new User();
        user.setId(userId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.getUserModel(userId)).thenReturn(user);

        // When & Then
        assertThrows(BadRequestException.class,
                () -> commentService.createComment(itemId, userId, inputDto));

        // Проверяем, что дошли до валидации текста
        verify(itemRepository).findById(itemId);
        verify(userService).getUserModel(userId);
        verifyNoInteractions(bookingRepository, commentRepository);
    }

    @Test
    void createComment_shouldThrowWhenNoValidBookings() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        Item item = new Item();
        item.setId(itemId);

        User author = new User();
        author.setId(userId);

        CommentDto inputDto = new CommentDto();
        inputDto.setText("Great item!");

        // Текущее бронирование (еще не завершено)
        Booking currentBooking = new Booking();
        currentBooking.setItem(item);
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.getUserModel(userId)).thenReturn(author);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(currentBooking));

        // When & Then
        assertThrows(BadRequestException.class,
                () -> commentService.createComment(itemId, userId, inputDto));

        verify(itemRepository).findById(itemId);
        verify(userService).getUserModel(userId);
        verify(bookingRepository).findByBookerId(userId);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void createComment_shouldThrowWhenNoBookingsAtAll() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        Item item = new Item();
        item.setId(itemId);

        User author = new User();
        author.setId(userId);

        CommentDto inputDto = new CommentDto();
        inputDto.setText("Great item!");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.getUserModel(userId)).thenReturn(author);
        when(bookingRepository.findByBookerId(userId)).thenReturn(Collections.emptyList());

        // When & Then
        assertThrows(BadRequestException.class,
                () -> commentService.createComment(itemId, userId, inputDto));

        verify(itemRepository).findById(itemId);
        verify(userService).getUserModel(userId);
        verify(bookingRepository).findByBookerId(userId);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void createComment_shouldThrowWhenUserNotFound() {
        // Given
        Long itemId = 1L;
        Long userId = 99L;
        CommentDto inputDto = new CommentDto();
        inputDto.setText("Test comment");

        Item item = new Item();
        item.setId(itemId); // Добавляем установку ID

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.getUserModel(userId)).thenThrow(new NotFoundException("User not found"));

        // When & Then
        assertThrows(NotFoundException.class,
                () -> commentService.createComment(itemId, userId, inputDto));

        verify(itemRepository).findById(itemId);
        verify(userService).getUserModel(userId);
        verifyNoInteractions(bookingRepository, commentRepository);
    }

    @Test
    void createComment_shouldCheckOnlyFinishedBookings() {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        Item item = new Item();
        item.setId(itemId);

        User author = new User();
        author.setId(userId);
        author.setName("Test User"); // Добавляем обязательное поле

        CommentDto inputDto = new CommentDto();
        inputDto.setText("Great item!");

        // Прошлое бронирование (валидное)
        Booking pastBooking = new Booking();
        pastBooking.setItem(item);
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));

        // Текущее бронирование (невалидное)
        Booking currentBooking = new Booking();
        currentBooking.setItem(item);
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(userService.getUserModel(userId)).thenReturn(author);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(pastBooking, currentBooking));

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setText(inputDto.getText());
        savedComment.setItem(item);
        savedComment.setAuthor(author);
        savedComment.setCreated(LocalDateTime.now());

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // When
        CommentDto result = commentService.createComment(itemId, userId, inputDto);

        // Then
        assertNotNull(result);
        assertEquals(inputDto.getText(), result.getText());
        assertEquals(author.getName(), result.getAuthorName());
    }
}