package ru.practicum.shareit.booking;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class BookingRepository {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private Long idCounter = 1L;

    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            booking.setId(idCounter++);
        }
        bookings.put(booking.getId(), booking);
        return booking;
    }

    public Booking findById(Long id) {
        return bookings.get(id);
    }

    public List<Booking> findAll() {
        return new ArrayList<>(bookings.values());
    }

    public List<Booking> findByBookerId(Long bookerId) {
        return bookings.values().stream()
                .filter(b -> b.getBookerId().equals(bookerId))
                .collect(Collectors.toList());
    }

    public List<Booking> findByOwnerId(Long ownerId, ItemService itemService) {
        return bookings.values().stream()
                .filter(b -> {
                    Item item = itemService.getItemModel(b.getItemId(), ownerId);
                    return item.getOwnerId().equals(ownerId);
                })
                .collect(Collectors.toList());
    }
}