package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.comment.commentDto.CommentDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        // Given
        User author = new User();
        author.setId(1L);
        author.setName("John Doe");

        Item item = new Item();
        item.setId(10L);

        Comment comment = new Comment();
        comment.setId(100L);
        comment.setText("Great item!");
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.of(2023, 1, 1, 12, 0));

        // When
        CommentDto dto = CommentMapper.toDto(comment);

        // Then
        assertNotNull(dto);
        assertEquals(comment.getId(), dto.getId());
        assertEquals(comment.getText(), dto.getText());
        assertEquals(comment.getItem().getId(), dto.getItemId());
        assertEquals(comment.getAuthor().getId(), dto.getAuthorId());
        assertEquals(comment.getAuthor().getName(), dto.getAuthorName());
        assertEquals(comment.getCreated(), dto.getCreated());
    }

    @Test
    void toComment_shouldMapAllFieldsCorrectly() {
        // Given
        CommentDto dto = new CommentDto();
        dto.setId(100L);
        dto.setText("Great item!");
        dto.setCreated(LocalDateTime.of(2023, 1, 1, 12, 0));

        // When
        Comment comment = CommentMapper.toComment(dto);

        // Then
        assertNotNull(comment);
        assertEquals(dto.getId(), comment.getId());
        assertEquals(dto.getText(), comment.getText());
        assertEquals(dto.getCreated(), comment.getCreated());
        assertNull(comment.getItem()); // Эти поля не устанавливаются в этом методе
        assertNull(comment.getAuthor());
    }

    @Test
    void toComment_shouldHandleNullValues() {
        // Given
        CommentDto dto = new CommentDto(); // Все поля null

        // When
        Comment comment = CommentMapper.toComment(dto);

        // Then
        assertNotNull(comment);
        assertNull(comment.getId());
        assertNull(comment.getText());
        assertNull(comment.getCreated());
        assertNull(comment.getItem());
        assertNull(comment.getAuthor());
    }
}