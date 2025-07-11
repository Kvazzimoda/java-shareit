package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long itemIdCounter = 1L;

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(itemIdCounter++);
        }
        items.put(item.getId(), item);
        return item;
    }

    public Item findById(Long id) {
        return items.get(id);
    }

    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(i -> i.getOwnerId().equals(ownerId))
                .collect(Collectors.toList());
    }
}