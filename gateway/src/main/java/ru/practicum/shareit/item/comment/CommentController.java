package ru.practicum.shareit.item.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import static ru.practicum.shareit.constants.HeaderConstants.USER_ID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class CommentController {

    private final CommentClient commentClient;

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable Long itemId,
                                                @RequestHeader(USER_ID) Long userId,
                                                @Valid @RequestBody CommentDto commentDto) {
        return commentClient.createComment(userId, itemId, commentDto);
    }
}
