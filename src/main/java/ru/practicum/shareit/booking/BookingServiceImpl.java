package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final ItemService itemService;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    @Override
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = userService.getUserModel(userId);
        Item item = itemService.getItemModel(bookingDto.getItemId(), userId);

        if (!item.getAvailable()) {
            throw new IllegalStateException("Item is not available");
        }

        if (bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Booking booking = new Booking();
        booking.setItemId(bookingDto.getItemId());
        booking.setBookerId(userId);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        bookingRepository.save(booking);

        // Закрываем доступ к вещи
        item.setAvailable(false);
        ItemDto updatedItemDto = ItemMapper.toDto(item);
        ItemUpdateDto itemUpdateDto = ItemMapper.toUpdateDto(updatedItemDto);
        itemService.updateItem(item.getOwnerId(), item.getId(), itemUpdateDto);

        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }

        Item item = itemService.getItemModel(booking.getItemId(), ownerId);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new SecurityException("Only owner can approve booking");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        if (approved && LocalDateTime.now().isAfter(booking.getEnd())) {
            item.setAvailable(true);
            ItemDto updatedItemDto = ItemMapper.toDto(item);
            ItemUpdateDto itemUpdateDto = ItemMapper.toUpdateDto(updatedItemDto);
            itemService.updateItem(ownerId, item.getId(), itemUpdateDto);
        }

        bookingRepository.save(booking); // сохраняем обновлённый статус
        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId);
        if (booking == null ||
                (!booking.getBookerId().equals(userId) &&
                        !itemService.getItemModel(booking.getItemId(), userId).getOwnerId().equals(userId))) {
            throw new SecurityException("Access denied");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        List<Booking> userBookings = bookingRepository.findByBookerId(userId);
        return filterBookingsByState(userBookings, state);
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        List<Booking> ownerBookings = bookingRepository.findByOwnerId(userId, itemService);
        return filterBookingsByState(ownerBookings, state);
    }

    private List<BookingDto> filterBookingsByState(List<Booking> bookings, String state) {
        // пока заглушка — можно фильтровать по статусу, времени и т.д.
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }
}