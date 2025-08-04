package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private ItemService itemService;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;



    @Test
    void createBooking_WithValidData_ReturnsBookingDto() {
        // Arrange
        Long userId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        User booker = new User();
        booker.setId(userId);

        Item item = new Item();
        item.setId(1L);
        item.setAvailable(true);
        User owner = new User();
        owner.setId(2L);
        item.setOwner(owner);

        when(userService.getUserModel(userId)).thenReturn(booker);
        when(itemService.getItemModel(1L, userId)).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BookingDto result = bookingService.createBooking(userId, bookingDto);

        // Assert
        assertNotNull(result);
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(itemService).updateItem(eq(2L), eq(1L), any(ItemUpdateDto.class));
    }

    @Test
    void createBooking_WhenItemNotAvailable_ThrowsBadRequestException() {
        // Arrange
        Long userId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        Item item = new Item();
        item.setAvailable(false);

        when(userService.getUserModel(userId)).thenReturn(new User());
        when(itemService.getItemModel(1L, userId)).thenReturn(item);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> bookingService.createBooking(userId, bookingDto));
    }

    @Test
    void createBooking_WithStartInPast_ThrowsWrongDateValidationException() {
        // Arrange
        Long userId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        Item item = new Item();
        item.setAvailable(true);

        when(userService.getUserModel(userId)).thenReturn(new User());
        when(itemService.getItemModel(1L, userId)).thenReturn(item);

        // Act & Assert
        assertThrows(WrongDateValidationException.class, () -> bookingService.createBooking(userId, bookingDto));
    }

    @Test
    void createBooking_WithEndBeforeStart_ThrowsWrongDateValidationException() {
        // Arrange
        Long userId = 1L;
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));

        Item item = new Item();
        item.setAvailable(true);

        when(userService.getUserModel(userId)).thenReturn(new User());
        when(itemService.getItemModel(1L, userId)).thenReturn(item);

        // Act & Assert
        assertThrows(WrongDateValidationException.class, () -> bookingService.createBooking(userId, bookingDto));
    }

    @Test
    void createBooking_WithEqualStartAndEnd_ThrowsWrongDateValidationException() {
        // Arrange
        Long userId = 1L;
        Long itemId = 1L;
        LocalDateTime now = LocalDateTime.now().plusDays(1);

        // Создаем BookingDto с ОДИНАКОВЫМИ датами
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(itemId);
        bookingDto.setStart(now);
        bookingDto.setEnd(now);  // Полностью одинаковые даты

        // Подготовка минимально необходимых данных
        User owner = new User(2L, "Owner", "owner@mail.com");
        User booker = new User(userId, "Booker", "booker@mail.com");

        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);

        when(userService.getUserModel(userId)).thenReturn(booker);
        when(itemService.getItemModel(itemId, userId)).thenReturn(item);

        // Act & Assert
        Exception exception = assertThrows(
                Exception.class,  // Ловим любое исключение
                () -> bookingService.createBooking(userId, bookingDto)
        );

        // Проверяем тип исключения
        assertTrue(exception instanceof WrongDateValidationException,
                "Expected WrongDateValidationException but got " + exception.getClass());

        // Проверяем текст исключения (варианты)
        String message = exception.getMessage();
        assertTrue(message.contains("cannot be equal") || message.contains("must be after"),
                "Unexpected exception message: " + message);
    }

    @Test
    void approveBooking_WhenApproved_ReturnsApprovedBooking() {
        // Arrange
        Long bookingId = 1L;
        Long ownerId = 2L;
        Long bookerId = 3L;
        LocalDateTime now = LocalDateTime.now();

        // Создаем пользователя
        User booker = new User();
        booker.setId(bookerId);

        // Создаем владельца
        User owner = new User();
        owner.setId(ownerId);

        // Создаем предмет
        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        item.setAvailable(false);

        // Создаем бронирование с корректными датами
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(now.plusDays(1));  // Начало в будущем
        booking.setEnd(now.plusDays(2));    // Конец в будущем
        booking.setItem(item);
        booking.setBooker(booker);  // Не забываем установить booker

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        // Act
        BookingDto result = bookingService.approveBooking(bookingId, ownerId, true);

        // Assert
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        assertFalse(item.getAvailable(), "Предмет должен остаться недоступным, так как" +
                " бронирование еще не началось");
    }

    @Test
    void approveBooking_WhenRejected_ReturnsRejectedBooking() {
        // Arrange
        Long bookingId = 1L;
        Long ownerId = 2L;
        Long bookerId = 3L;

        // Создаем пользователя, который делает бронирование
        User booker = new User();
        booker.setId(bookerId);
        booker.setName("Booker Name");

        // Создаем владельца вещи
        User owner = new User();
        owner.setId(ownerId);
        owner.setName("Owner Name");

        // Создаем предмет
        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);

        // Создаем бронирование
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BookingDto result = bookingService.approveBooking(bookingId, ownerId, false);

        // Assert
        assertEquals(BookingStatus.REJECTED, result.getStatus());
        assertEquals(bookerId, result.getBooker().getId());

        // Проверяем, что информация о вещи сохранилась правильно
        assertNotNull(result.getItem());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(item.getName(), result.getItem().getName());
    }

    @Test
    void approveBooking_WhenNotOwner_ThrowsForbiddenException() {
        // Arrange
        Long bookingId = 1L;
        Long ownerId = 2L;
        Long wrongOwnerId = 3L;
        Booking booking = new Booking();

        Item item = new Item();
        User owner = new User();
        owner.setId(ownerId);
        item.setOwner(owner);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> bookingService.approveBooking(bookingId, wrongOwnerId, true));
    }

    @Test
    void approveBooking_WhenBookingNotFound_ThrowsNotFoundException() {
        // Arrange
        Long bookingId = 1L;
        Long ownerId = 2L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(bookingId, ownerId, true));
    }

    @Test
    void approveBooking_WhenItemOrOwnerNotFound_ThrowsNotFoundException() {
        // Arrange
        Long bookingId = 1L;
        Long ownerId = 2L;
        Booking booking = new Booking();
        booking.setItem(null); // No item set

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bookingService.approveBooking(bookingId, ownerId, true));
    }

    @Test
    void getBooking_WhenBookingExists_ReturnsBookingDto() {
        // Arrange
        Long bookingId = 1L;
        Long userId = 2L;
        Booking booking = new Booking();

        User booker = new User();
        booker.setId(userId);
        booking.setBooker(booker);

        Item item = new Item();
        User owner = new User();
        owner.setId(userId);
        item.setOwner(owner);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act
        BookingDto result = bookingService.getBooking(bookingId, userId);

        // Assert
        assertNotNull(result);
    }

    @Test
    void getBooking_WhenNotBookerOrOwner_ThrowsForbiddenException() {
        // Arrange
        Long bookingId = 1L;
        Long userId = 2L;
        Long wrongUserId = 3L;
        Booking booking = new Booking();

        User booker = new User();
        booker.setId(userId);
        booking.setBooker(booker);

        Item item = new Item();
        User owner = new User();
        owner.setId(userId);
        item.setOwner(owner);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> bookingService.getBooking(bookingId, wrongUserId));
    }

    @Test
    void getBooking_WhenBookingNotFound_ThrowsNotFoundException() {
        // Arrange
        Long bookingId = 1L;
        Long userId = 2L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bookingService.getBooking(bookingId, userId));
    }

    @Test
    void getUserBookings_WithAllState_ReturnsAllBookings() {
        // Arrange
        Long userId = 1L;
        String state = "ALL";
        LocalDateTime now = LocalDateTime.now();

        // Создаем тестовые данные
        User user = new User();
        user.setId(userId);

        Item item1 = new Item();
        item1.setId(1L);
        Item item2 = new Item();
        item2.setId(2L);

        // Явно создаем два разных бронирования
        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStart(now.plusDays(1));
        booking1.setEnd(now.plusDays(2));
        booking1.setItem(item1);
        booking1.setBooker(user);
        booking1.setStatus(BookingStatus.WAITING);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStart(now.plusDays(3));
        booking2.setEnd(now.plusDays(4));
        booking2.setItem(item2);
        booking2.setBooker(user);
        booking2.setStatus(BookingStatus.APPROVED);

        // Явно мокаем возвращаемые значения
        when(userService.getUserModel(userId)).thenReturn(user);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        // Диагностика
        System.out.println("Returned bookings count: " + result.size());
        result.forEach(dto -> System.out.println("Booking id: " + dto.getId()));

        // Assert
        assertEquals(2, result.size(), "Должны вернуться все бронирования без фильтрации");

        // Проверяем, что вернулись именно те бронирования, которые мы создали
        List<Long> returnedIds = result.stream().map(BookingDto::getId).toList();
        assertTrue(returnedIds.contains(booking1.getId()));
        assertTrue(returnedIds.contains(booking2.getId()));
    }

    @Test
    void getUserBookings_WithCurrentState_ReturnsCurrentBookings() {
        // Arrange
        Long userId = 1L;
        String state = "CURRENT";

        // Создаем необходимые объекты
        User user = new User();
        user.setId(userId);

        Item item = new Item();
        item.setId(1L);
        item.setOwner(user); // Владелец вещи

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(1));
        booking.setItem(item); // Устанавливаем вещь
        booking.setBooker(user); // Устанавливаем пользователя, который бронирует

        when(userService.getUserModel(userId)).thenReturn(user);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(booking));

        // Act
        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void getUserBookings_WithPastState_ReturnsPastBookings() {
        // Arrange
        Long userId = 1L;
        String state = "PAST";
        LocalDateTime now = LocalDateTime.now();

        // Создаем пользователя
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        // Создаем предмет
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAvailable(true);

        // Создаем завершенное бронирование (в прошлом)
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now.minusDays(2));
        booking.setEnd(now.minusDays(1));  // Завершено
        booking.setStatus(BookingStatus.APPROVED);
        booking.setItem(item);  // Устанавливаем предмет
        booking.setBooker(user);  // Устанавливаем пользователя

        when(userService.getUserModel(userId)).thenReturn(user);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(booking));

        // Act
        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        // Assert
        assertEquals(1, result.size(), "Должно вернуться 1 завершенное бронирование");
        assertEquals(booking.getId(), result.getFirst().getId());
        assertEquals(item.getId(), result.getFirst().getItemId());
        assertEquals(userId, result.getFirst().getBooker().getId());
    }

    @Test
    void getUserBookings_WithFutureState_ReturnsFutureBookings() {
        // Arrange
        Long userId = 1L;
        String state = "FUTURE";
        LocalDateTime now = LocalDateTime.now();

        // Создаем пользователя
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        // Создаем предмет
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAvailable(true);

        // Создаем будущее бронирование
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now.plusDays(1));  // Начало в будущем
        booking.setEnd(now.plusDays(2));    // Конец в будущем
        booking.setStatus(BookingStatus.APPROVED);
        booking.setItem(item);        // Устанавливаем предмет
        booking.setBooker(user);      // Устанавливаем пользователя

        when(userService.getUserModel(userId)).thenReturn(user);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(booking));

        // Act
        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        // Assert
        assertEquals(1, result.size(), "Должно вернуться 1 будущее бронирование");
        assertEquals(booking.getId(), result.getFirst().getId());
        assertEquals(item.getId(), result.getFirst().getItemId());
        assertEquals(userId, result.getFirst().getBooker().getId());
    }

    @Test
    void getUserBookings_WithWaitingState_ReturnsWaitingBookings() {
        // Arrange
        Long userId = 1L;
        String state = "WAITING";
        LocalDateTime now = LocalDateTime.now();

        // Создаем пользователя
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        // Создаем предмет
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAvailable(true);

        // Создаем бронирование в статусе WAITING
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(2));
        booking.setStatus(BookingStatus.WAITING); // Устанавливаем нужный статус
        booking.setItem(item); // Устанавливаем предмет
        booking.setBooker(user); // Устанавливаем пользователя

        when(userService.getUserModel(userId)).thenReturn(user);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(booking));

        // Act
        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        // Assert
        assertEquals(1, result.size(), "Должно вернуться 1 бронирование в статусе WAITING");
        assertEquals(BookingStatus.WAITING, result.getFirst().getStatus());
        assertEquals(item.getId(), result.getFirst().getItemId());
        assertEquals(userId, result.getFirst().getBooker().getId());
    }

    @Test
    void getUserBookings_WithRejectedState_ReturnsRejectedBookings() {
        // Arrange
        Long userId = 1L;
        String state = "REJECTED";
        LocalDateTime now = LocalDateTime.now();

        // Создаем пользователя
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        // Создаем предмет
        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setAvailable(true);

        // Создаем отклоненное бронирование
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now.plusDays(1));
        booking.setEnd(now.plusDays(2));
        booking.setStatus(BookingStatus.REJECTED); // Устанавливаем статус REJECTED
        booking.setItem(item); // Устанавливаем предмет
        booking.setBooker(user); // Устанавливаем пользователя

        when(userService.getUserModel(userId)).thenReturn(user);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(booking));

        // Act
        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        // Assert
        assertEquals(1, result.size(), "Должно вернуться 1 отклоненное бронирование");
        assertEquals(BookingStatus.REJECTED, result.getFirst().getStatus());
        assertEquals(item.getId(), result.getFirst().getItemId());
        assertEquals(userId, result.getFirst().getBooker().getId());
    }

    @Test
    void getUserBookings_WithInvalidState_ThrowsIllegalArgumentException() {
        // Arrange
        Long userId = 1L;
        String state = "INVALID";

        when(userService.getUserModel(userId)).thenReturn(new User());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookingService.getUserBookings(userId, state));
    }

    @Test
    void getOwnerBookings_WithAllState_ReturnsAllBookings() {
        // Arrange
        Long userId = 1L;
        String state = "ALL";
        LocalDateTime now = LocalDateTime.now();

        // Создаем владельца
        User owner = new User();
        owner.setId(userId);
        owner.setName("Owner");

        // Создаем пользователя, который бронирует
        User booker = new User();
        booker.setId(2L);
        booker.setName("Booker");

        // Создаем предметы
        Item item1 = new Item();
        item1.setId(1L);
        item1.setOwner(owner);

        Item item2 = new Item();
        item2.setId(2L);
        item2.setOwner(owner);

        // Создаем бронирования
        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setStart(now.plusDays(1));
        booking1.setEnd(now.plusDays(2));
        booking1.setStatus(BookingStatus.APPROVED);
        booking1.setItem(item1);
        booking1.setBooker(booker);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStart(now.plusDays(3));
        booking2.setEnd(now.plusDays(4));
        booking2.setStatus(BookingStatus.WAITING);
        booking2.setItem(item2);
        booking2.setBooker(booker);

        when(userService.getUserModel(userId)).thenReturn(owner);
        when(bookingRepository.findByItemOwnerId(userId)).thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingDto> result = bookingService.getOwnerBookings(userId, state);

        // Assert
        assertEquals(2, result.size(), "Должны вернуться все бронирования владельца");

        // Проверяем сортировку по дате (новые сначала)
        assertTrue(result.get(0).getStart().isAfter(result.get(1).getStart()));

        // Проверяем содержимое
        assertEquals(booking2.getId(), result.get(0).getId());
        assertEquals(booking1.getId(), result.get(1).getId());
    }

    @Test
    void getOwnerBookings_WithCurrentState_ReturnsCurrentBookings() {
        // Arrange
        Long userId = 1L;
        String state = "CURRENT";
        LocalDateTime now = LocalDateTime.now();

        // Создаем владельца
        User owner = new User();
        owner.setId(userId);
        owner.setName("Owner");

        // Создаем пользователя, который бронирует
        User booker = new User();
        booker.setId(2L);
        booker.setName("Booker");

        // Создаем предмет
        Item item = new Item();
        item.setId(1L);
        item.setOwner(owner);
        item.setName("Test Item");
        item.setAvailable(true);

        // Создаем текущее бронирование (началось в прошлом, заканчивается в будущем)
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(now.minusDays(1));  // Началось вчера
        booking.setEnd(now.plusDays(1));    // Заканчивается завтра
        booking.setStatus(BookingStatus.APPROVED);
        booking.setItem(item);        // Устанавливаем предмет
        booking.setBooker(booker);    // Устанавливаем пользователя

        when(userService.getUserModel(userId)).thenReturn(owner);
        when(bookingRepository.findByItemOwnerId(userId)).thenReturn(List.of(booking));

        // Act
        List<BookingDto> result = bookingService.getOwnerBookings(userId, state);

        // Assert
        assertEquals(1, result.size(), "Должно вернуться 1 текущее бронирование");
        assertEquals(booking.getId(), result.getFirst().getId());
        assertEquals(item.getId(), result.getFirst().getItemId());
        assertEquals(booker.getId(), result.getFirst().getBooker().getId());
        assertTrue(result.getFirst().getStart().isBefore(now), "Начало бронирования должно быть в прошлом");
        assertTrue(result.getFirst().getEnd().isAfter(now), "Окончание бронирования должно быть в будущем");
    }

    @Test
    void getUserBookings_shouldReturnAllBookings_whenStateAll() {
        Long userId = 1L;
        User user = new User(userId, "Booker", "booker@example.com");
        Booking booking = createBooking(userId, 2L, BookingStatus.APPROVED,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(userService.getUserModel(userId)).thenReturn(user);
        when(bookingRepository.findByBookerId(userId)).thenReturn(List.of(booking));

        List<BookingDto> result = bookingService.getUserBookings(userId, "ALL");

        assertEquals(1, result.size());
        assertEquals(booking.getId(), result.get(0).getId());
    }

    @Test
    void getOwnerBookings_shouldReturnWaitingBookings() {
        Long userId = 1L;
        User owner = new User(userId, "Owner", "owner@example.com");
        Booking waitingBooking = createBooking(2L, userId, BookingStatus.WAITING,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        when(userService.getUserModel(userId)).thenReturn(owner);
        when(bookingRepository.findByItemOwnerId(userId)).thenReturn(List.of(waitingBooking));

        List<BookingDto> result = bookingService.getOwnerBookings(userId, "WAITING");

        assertEquals(1, result.size());
        assertEquals(BookingStatus.WAITING, result.get(0).getStatus());
    }

    @Test
    void getUserBookings_shouldThrow_whenUnknownState() {
        Long userId = 1L;
        when(userService.getUserModel(userId)).thenReturn(new User(userId, "Test", "test@test.com"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                bookingService.getUserBookings(userId, "INVALID_STATE")
        );

        assertTrue(ex.getMessage().contains("Unknown state"));
    }

    @Test
    void approveBooking_WhenApprovedAfterEnd_ShouldMakeItemAvailable() {
        // Arrange
        Long bookingId = 1L;
        Long ownerId = 2L;
        Long bookerId = 3L;
        LocalDateTime pastEnd = LocalDateTime.now().minusDays(1);

        User booker = new User(bookerId, "Booker", "booker@mail.com");
        User owner = new User(ownerId, "Owner", "owner@mail.com");

        // Создаем Item с правильным конструктором
        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Description");
        item.setAvailable(false);
        item.setOwner(owner);
        item.setRequest(null); // или реальный ItemRequest, если требуется

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setStart(pastEnd.minusDays(1));
        booking.setEnd(pastEnd);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act
        BookingDto result = bookingService.approveBooking(bookingId, ownerId, true);

        // Assert
        assertEquals(BookingStatus.APPROVED, result.getStatus());
        assertTrue(item.getAvailable());
        verify(itemService).updateItem(eq(ownerId), eq(item.getId()), any(ItemUpdateDto.class));
    }

    @Test
    void getBooking_WhenNotOwnerOrBooker_ThrowsForbiddenException() {
        // Arrange
        Long bookingId = 1L;
        Long userId = 2L;
        Long ownerId = 3L;
        Long bookerId = 4L;

        User owner = new User(ownerId, "Owner", "owner@mail.com");
        User booker = new User(bookerId, "Booker", "booker@mail.com");

        // Создаем Item с правильным конструктором
        Item item = new Item();
        item.setId(1L);
        item.setName("Item");
        item.setDescription("Desc");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(null);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItem(item);
        booking.setBooker(booker);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(ForbiddenException.class, () ->
                bookingService.getBooking(bookingId, userId));
    }

    private Booking createBooking(Long bookerId, Long ownerId, BookingStatus status,
                                  LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setId(new Random().nextLong());
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(status);

        User booker = new User(bookerId, "Booker", "booker@example.com");
        User owner = new User(ownerId, "Owner", "owner@example.com");

        Item item = new Item();
        item.setId(1L);
        item.setName("Item 1");
        item.setAvailable(true);
        item.setOwner(owner);

        booking.setBooker(booker);
        booking.setItem(item);
        return booking;
    }

}