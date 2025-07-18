package ru.practicum.shareit.item.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.constants.HeaderConstants;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@PathVariable Long itemId,
                                    @RequestHeader(HeaderConstants.USER_ID) Long userId,
                                    @Valid @RequestBody CommentDto commentDto) {
        return commentService.createComment(itemId, userId, commentDto);
    }
}
