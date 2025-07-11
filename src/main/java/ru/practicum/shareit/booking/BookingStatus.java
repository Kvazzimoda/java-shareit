package ru.practicum.shareit.booking;

import lombok.Getter;

@Getter
public enum BookingStatus {
    WAITING("Ожидает подтверждения"),
    APPROVED("Подтверждено"),
    REJECTED("Отклонено"),
    CANCELED("Отменено");

    private final String description;

    BookingStatus(String description) {
        this.description = description;
    }

}
