package ru.practicum.shareit.item.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        // 1. Проверка существования предмета
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        User author = userService.getUserModel(userId);
        // 3. Валидация данных комментария
        if (commentDto == null || commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new BadRequestException("Comment text cannot be empty");
        }
        // 4. Проверка, что пользователь бронировал этот предмет
        boolean hasBooking = bookingRepository.findByBookerId(userId).stream()
                .anyMatch(b -> b.getItem().getId().equals(itemId) && b.getEnd().isBefore(LocalDateTime.now()));
        if (!hasBooking) {
            throw new BadRequestException("User has not booked this item or booking is not approved");
        }
        // 5. Создание и сохранение комментария
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        return CommentMapper.toDto(comment);
    }
}