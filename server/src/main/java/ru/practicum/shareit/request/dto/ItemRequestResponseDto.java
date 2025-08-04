package ru.practicum.shareit.request.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestResponseDto {
    private Long id;
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
