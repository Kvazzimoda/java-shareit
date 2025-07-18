package ru.practicum.shareit.item.comment;

public interface CommentService {
    CommentDto createComment(Long itemId, Long userId, CommentDto commentDto);
}