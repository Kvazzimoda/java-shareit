package ru.practicum.shareit.item.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.commentDto.CommentDto;

import static ru.practicum.shareit.constants.HeaderConstants.USER_ID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@PathVariable Long itemId,
                                    @RequestHeader(USER_ID) Long userId,
                                    @RequestBody CommentDto commentDto) {
        return commentService.createComment(itemId, userId, commentDto);
    }
}