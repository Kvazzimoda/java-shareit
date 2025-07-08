package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingDto bookingDto);
    BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved);
    BookingDto getBooking(Long bookingId, Long userId);
    List<BookingDto> getUserBookings(Long userId, String state);
    List<BookingDto> getOwnerBookings(Long userId, String state);
}