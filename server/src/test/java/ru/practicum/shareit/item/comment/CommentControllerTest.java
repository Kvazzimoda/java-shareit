package ru.practicum.shareit.item.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.commentDto.CommentDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.constants.HeaderConstants.USER_ID;

@WebMvcTest(controllers = CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void createComment_shouldReturnCreatedComment() throws Exception {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        CommentDto inputDto = new CommentDto();
        inputDto.setText("Great item!");

        CommentDto outputDto = new CommentDto();
        outputDto.setId(1L);
        outputDto.setText("Great item!");
        outputDto.setAuthorName("John Doe");
        outputDto.setCreated(LocalDateTime.now());

        when(commentService.createComment(eq(itemId), eq(userId), any(CommentDto.class)))
                .thenReturn(outputDto);

        // When & Then
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("John Doe"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void createComment_shouldReturnBadRequestWhenTextIsBlank() throws Exception {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        CommentDto invalidDto = new CommentDto();
        invalidDto.setText(" "); // Пустой текст

        when(commentService.createComment(eq(itemId), eq(userId), any(CommentDto.class)))
                .thenThrow(new BadRequestException("Comment text cannot be empty"));

        // When & Then
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_shouldReturnNotFoundWhenItemNotExists() throws Exception {
        // Given
        Long itemId = 99L;
        Long userId = 1L;

        CommentDto inputDto = new CommentDto();
        inputDto.setText("Test comment");

        when(commentService.createComment(eq(itemId), eq(userId), any(CommentDto.class)))
                .thenThrow(new NotFoundException("Item not found"));

        // When & Then
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_shouldReturnBadRequestWhenNoValidBookings() throws Exception {
        // Given
        Long itemId = 1L;
        Long userId = 1L;

        CommentDto inputDto = new CommentDto();
        inputDto.setText("Test comment");

        when(commentService.createComment(eq(itemId), eq(userId), any(CommentDto.class)))
                .thenThrow(new BadRequestException("User has not booked this item"));

        // When & Then
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isBadRequest());
    }
}