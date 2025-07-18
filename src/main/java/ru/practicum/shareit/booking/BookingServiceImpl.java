package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.WrongDateValidationException;
import ru.practicum.shareit.item.ItemService;
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
            throw new BadRequestException("Item is not available for booking");
        }

        // Валидация дат
        LocalDateTime now = LocalDateTime.now();
        if (bookingDto.getStart().isBefore(now)) {
            throw new WrongDateValidationException("Start date must be in the future");
        }
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new WrongDateValidationException("End date must be after start date");
        }
        if (bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new WrongDateValidationException("Start and end dates cannot be equal");
        }

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setStatus(BookingStatus.WAITING);

        bookingRepository.save(booking);

        // Закрываем доступ к вещи
        item.setAvailable(false);
        ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
        itemUpdateDto.setAvailable(false);
        itemService.updateItem(item.getOwner().getId(), item.getId(), itemUpdateDto);

        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        Item item = booking.getItem();
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only owner can approve booking");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        if (approved && LocalDateTime.now().isAfter(booking.getEnd())) {
            item.setAvailable(true);
            ItemUpdateDto itemUpdateDto = new ItemUpdateDto();
            itemUpdateDto.setAvailable(true);
            itemService.updateItem(ownerId, item.getId(), itemUpdateDto);
        }

        bookingRepository.save(booking);
        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        userService.getUserModel(userId); // Проверка существования пользователя
        List<Booking> userBookings = bookingRepository.findByBookerId(userId);
        return filterBookingsByState(userBookings, state);
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        userService.getUserModel(userId); // Проверка существования пользователя
        List<Booking> ownerBookings = bookingRepository.findByItemOwnerId(userId);
        return filterBookingsByState(ownerBookings, state);
    }

    private List<BookingDto> filterBookingsByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state.toUpperCase()) {
            case "CURRENT" -> bookings.stream()
                    .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case "PAST" -> bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case "FUTURE" -> bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case "WAITING" -> bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.WAITING)
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            case "REJECTED" -> bookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toList());
            default -> bookings.stream()
                    .map(BookingMapper::toDto)
                    .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                    .collect(Collectors.toList());
        };
    }
}