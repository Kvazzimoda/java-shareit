package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
public class ItemRequest {
    private Long id;
    @NotBlank(message = "Description cannot be blank")
    private String description;
    private Long requesterId;
    private LocalDateTime created;
}