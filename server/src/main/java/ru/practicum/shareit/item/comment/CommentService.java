package ru.practicum.shareit.item.comment;

import ru.practicum.shareit.item.comment.commentDto.CommentDto;

public interface CommentService {
    CommentDto createComment(Long itemId, Long userId, CommentDto commentDto);
}