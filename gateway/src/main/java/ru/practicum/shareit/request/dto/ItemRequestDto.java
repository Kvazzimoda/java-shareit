package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "Description cannot be blank")
    private String description;
    private Long requesterId;
    private LocalDateTime created;
}