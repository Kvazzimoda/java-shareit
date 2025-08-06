package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ItemRequestResponseDto {
    private Long id;
    @NotBlank
    private String description;
    private Long requesterId;
    private LocalDateTime created;
    private List<ItemResponse> items; // Список ответов

    @Data
    public static class ItemResponse {
        private Long itemId;
        private String name;
        private Long ownerId;
    }
}