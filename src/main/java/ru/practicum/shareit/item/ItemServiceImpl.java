package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentMapper;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = userService.getUserModel(userId);
        if (user == null) {
            throw new NotFoundException("User with id " + userId + " not found");
        }
        Item item = ItemMapper.toItem(itemDto, user);
        item = itemRepository.save(item);
        return ItemMapper.toDto(item);
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can update item");
        }
        if (itemUpdateDto.getName() != null) item.setName(itemUpdateDto.getName());
        if (itemUpdateDto.getDescription() != null) item.setDescription(itemUpdateDto.getDescription());
        if (itemUpdateDto.getAvailable() != null) item.setAvailable(itemUpdateDto.getAvailable());
        item = itemRepository.save(item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        ItemDto itemDto = ItemMapper.toDto(item);
        if (item.getOwner().getId().equals(userId)) {
            itemDto.setLastBooking(bookingRepository.findByItemId(itemId).stream()
                    .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                    .max(Comparator.comparing(Booking::getEnd))
                    .map(BookingMapper::toDto)
                    .orElse(null));
            itemDto.setNextBooking(bookingRepository.findByItemId(itemId).stream()
                    .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                    .min(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::toDto)
                    .orElse(null));
        }
        itemDto.setComments(commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList()));
        return itemDto;
    }

    @Override
    public Item getItemModel(Long itemId, Long userId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
    }

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        return itemRepository.findByOwnerId(userId).stream()
                .map(item -> {
                    ItemDto itemDto = ItemMapper.toDto(item);
                    itemDto.setLastBooking(bookingRepository.findByItemId(item.getId()).stream()
                            .filter(b -> b.getEnd().isBefore(LocalDateTime.now()))
                            .max(Comparator.comparing(Booking::getEnd))
                            .map(BookingMapper::toDto)
                            .orElse(null));
                    itemDto.setNextBooking(bookingRepository.findByItemId(item.getId()).stream()
                            .filter(b -> b.getStart().isAfter(LocalDateTime.now()))
                            .min(Comparator.comparing(Booking::getStart))
                            .map(BookingMapper::toDto)
                            .orElse(null));
                    itemDto.setComments(commentRepository.findByItemId(item.getId()).stream()
                            .map(CommentMapper::toDto)
                            .collect(Collectors.toList()));
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String searchText = "%" + text + "%";
        return itemRepository.searchAvailableByNameOrDescription(searchText)
                .stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}