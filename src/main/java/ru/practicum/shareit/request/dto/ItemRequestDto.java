package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "Description cannot be blank")
    private String description;
    private Long requesterId;
    private LocalDateTime created;
}