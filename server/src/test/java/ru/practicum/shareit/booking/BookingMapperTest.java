package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingMapperTest {

    @Test
    void toDto_WithValidBooking_MapsAllFieldsCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(2));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.APPROVED);

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        try (MockedStatic<UserMapper> userMapperMock = mockStatic(UserMapper.class);
             MockedStatic<ItemMapper> itemMapperMock = mockStatic(ItemMapper.class)) {
            userMapperMock.when(() -> UserMapper.toDto(user)).thenReturn(userDto);
            itemMapperMock.when(() -> ItemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            BookingDto result = BookingMapper.toDto(booking);

            // Assert
            assertNotNull(result);
            assertEquals(booking.getId(), result.getId());
            assertEquals(booking.getItem().getId(), result.getItemId());
            assertEquals(booking.getBooker().getId(), result.getBookerId());
            assertEquals(booking.getStart(), result.getStart());
            assertEquals(booking.getEnd(), result.getEnd());
            assertEquals(booking.getStatus(), result.getStatus());
            assertEquals(userDto, result.getBooker());
            assertEquals(itemDto, result.getItem());

            userMapperMock.verify(() -> UserMapper.toDto(user), times(1));
            itemMapperMock.verify(() -> ItemMapper.toDto(item), times(1));
        }
    }

    @Test
    void toDto_WithNullStartAndEnd_MapsFieldsCorrectly() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(null); // null start
        booking.setEnd(null);   // null end
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        try (MockedStatic<UserMapper> userMapperMock = mockStatic(UserMapper.class);
             MockedStatic<ItemMapper> itemMapperMock = mockStatic(ItemMapper.class)) {
            userMapperMock.when(() -> UserMapper.toDto(user)).thenReturn(userDto);
            itemMapperMock.when(() -> ItemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            BookingDto result = BookingMapper.toDto(booking);

            // Assert
            assertNotNull(result);
            assertEquals(booking.getId(), result.getId());
            assertEquals(booking.getItem().getId(), result.getItemId());
            assertEquals(booking.getBooker().getId(), result.getBookerId());
            assertNull(result.getStart());
            assertNull(result.getEnd());
            assertEquals(booking.getStatus(), result.getStatus());
            assertEquals(userDto, result.getBooker());
            assertEquals(itemDto, result.getItem());

            userMapperMock.verify(() -> UserMapper.toDto(user), times(1));
            itemMapperMock.verify(() -> ItemMapper.toDto(item), times(1));
        }
    }

    @Test
    void toDto_WithNullStatus_MapsFieldsCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(user);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(2));
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(null); // null status

        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        try (MockedStatic<UserMapper> userMapperMock = mockStatic(UserMapper.class);
             MockedStatic<ItemMapper> itemMapperMock = mockStatic(ItemMapper.class)) {
            userMapperMock.when(() -> UserMapper.toDto(user)).thenReturn(userDto);
            itemMapperMock.when(() -> ItemMapper.toDto(item)).thenReturn(itemDto);

            // Act
            BookingDto result = BookingMapper.toDto(booking);

            // Assert
            assertNotNull(result);
            assertEquals(booking.getId(), result.getId());
            assertEquals(booking.getItem().getId(), result.getItemId());
            assertEquals(booking.getBooker().getId(), result.getBookerId());
            assertEquals(booking.getStart(), result.getStart());
            assertEquals(booking.getEnd(), result.getEnd());
            assertNull(result.getStatus());
            assertEquals(userDto, result.getBooker());
            assertEquals(itemDto, result.getItem());

            userMapperMock.verify(() -> UserMapper.toDto(user), times(1));
            itemMapperMock.verify(() -> ItemMapper.toDto(item), times(1));
        }
    }

    @Test
    void toDto_WithNullBooking_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> BookingMapper.toDto(null));
    }

    @Test
    void toBooking_WithValidBookingDto_MapsAllFieldsCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(now.plusDays(1));
        dto.setEnd(now.plusDays(2));
        dto.setStatus(BookingStatus.APPROVED);

        // Act
        Booking result = BookingMapper.toBooking(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getStart(), result.getStart());
        assertEquals(dto.getEnd(), result.getEnd());
        assertEquals(dto.getStatus(), result.getStatus());
        assertNull(result.getItem()); // item не маппится
        assertNull(result.getBooker()); // booker не маппится
    }

    @Test
    void toBooking_WithNullStartAndEnd_MapsFieldsCorrectly() {
        // Arrange
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(null);
        dto.setEnd(null);
        dto.setStatus(BookingStatus.WAITING);

        // Act
        Booking result = BookingMapper.toBooking(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertNull(result.getStart());
        assertNull(result.getEnd());
        assertEquals(dto.getStatus(), result.getStatus());
        assertNull(result.getItem());
        assertNull(result.getBooker());
    }

    @Test
    void toBooking_WithNullStatus_MapsFieldsCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(now.plusDays(1));
        dto.setEnd(now.plusDays(2));
        dto.setStatus(null);

        // Act
        Booking result = BookingMapper.toBooking(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getStart(), result.getStart());
        assertEquals(dto.getEnd(), result.getEnd());
        assertNull(result.getStatus());
        assertNull(result.getItem());
        assertNull(result.getBooker());
    }

    @Test
    void toBooking_WithNullBookingDto_ThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> BookingMapper.toBooking(null));
    }
}