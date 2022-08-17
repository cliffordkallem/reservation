package com.cliff.reservation.service;

import com.cliff.reservation.db.model.Reservation;
import com.cliff.reservation.db.repository.ReservationRepository;
import com.cliff.reservation.model.ReservationAvailable;
import com.cliff.reservation.model.ReservationInfo;
import com.cliff.reservation.utils.LocalDateDeserializer;
import com.cliff.reservation.utils.LocalDateSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @InjectMocks ReservationService reservationService;

    @Spy ReservationRepository reservationRepository;


    @Test
    void test() throws JsonProcessingException {

        JavaTimeModule module = new JavaTimeModule();
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(module);
        String s = objectMapper.writeValueAsString(LocalDate.now());

        System.out.println("here");
    }

    @Test
    void empty_object() {
        ReservationInfo reservationInfo = new ReservationInfo();
        Map<String, String> errors = reservationService.validate(reservationInfo);
        assertNotNull(errors.get("fullName"));
        assertNotNull(errors.get("email"));
        assertNotNull(errors.get("arrivalDate"));
        assertNotNull(errors.get("departureDate"));
    }

    @Test
    void verify_max_reservationDays() {
        ReservationInfo reservationInfo = new ReservationInfo();
        reservationInfo.setFullName("bob mcface");
        reservationInfo.setEmail("bob@bob.com");
        reservationInfo.setArrivalDate(LocalDate.now());
        reservationInfo.setDepartureDate(LocalDate.now().plusDays(10));
        Map<String, String> errors = reservationService.validate(reservationInfo);
        assertNotNull(errors.get("departureDate"));
        assertEquals(errors.get("departureDate"), "total days of reservation is too long");
    }

    @Test
    void departureDate_before_arrivalDate() {
        ReservationInfo reservationInfo = new ReservationInfo();
        reservationInfo.setFullName("bob mcface");
        reservationInfo.setEmail("bob@bob.com");
        reservationInfo.setArrivalDate(LocalDate.now());
        reservationInfo.setDepartureDate(LocalDate.now().minusDays(10));
        Map<String, String> errors = reservationService.validate(reservationInfo);
        assertNotNull(errors.get("departureDate"));
        assertEquals(errors.get("departureDate"), "departure date is before arrival date");
    }

    @Test
    void arrivalDate_today() {
        ReservationInfo reservationInfo = new ReservationInfo();
        reservationInfo.setFullName("bob mcface");
        reservationInfo.setEmail("bob@bob.com");
        reservationInfo.setArrivalDate(LocalDate.now());
        reservationInfo.setDepartureDate(LocalDate.now().plusDays(3));
        Map<String, String> errors = reservationService.validate(reservationInfo);
        assertNotNull(errors.get("arrivalDate"));
        assertEquals(errors.get("arrivalDate"), "arrival date must be more than one day in the future");
    }

    @Test
    void arrivalDate_twoMonth_future() {
        ReservationInfo reservationInfo = new ReservationInfo();
        reservationInfo.setFullName("bob mcface");
        reservationInfo.setEmail("bob@bob.com");
        reservationInfo.setArrivalDate(LocalDate.now().plusMonths(2));
        reservationInfo.setDepartureDate(LocalDate.now().plusMonths(2).plusDays(2));
        Map<String, String> errors = reservationService.validate(reservationInfo);
        assertNotNull(errors.get("arrivalDate"));
        assertEquals(errors.get("arrivalDate"), "arrival date must not be more than one month in the future");
    }

    @Test
    void overlapping_dates() {
        ReservationInfo reservationInfo = new ReservationInfo();
        reservationInfo.setFullName("bob mcface");
        reservationInfo.setEmail("bob@bob.com");
        reservationInfo.setArrivalDate(LocalDate.now().plusMonths(2));
        reservationInfo.setDepartureDate(LocalDate.now().plusMonths(2).plusDays(2));
        when(reservationRepository.findAnyReservationActiveDuringDate(any(), any())).thenReturn(List.of(new Reservation()));
        Map<String, String> errors = reservationService.validate(reservationInfo);
        assertNotNull(errors.get("arrivalDate"));
        assertEquals(errors.get("arrivalDate"), "already existing reservation for date");
    }


    @Test
    void checkAvailability_overlapping_dates() {
        Reservation reservation = new Reservation();
        reservation.setDepartureDate(LocalDate.now().plusDays(15));
        reservation.setArrivalDate(LocalDate.now().plusDays(12));

        when(reservationRepository.findAnyReservationActiveDuringDate(any(), any())).thenReturn(List.of(reservation));
        ReservationAvailable reservationAvailable = reservationService.checkAvailability(LocalDate.now(), LocalDate.now().plusMonths(1));
        assertEquals(reservationAvailable.getReservationDates().size(), 2);
    }

    @Test
    void checkAvailability_overlapping_dates_split_dates() {
        Reservation reservation = new Reservation();
        reservation.setDepartureDate(LocalDate.now().plusDays(15));
        reservation.setArrivalDate(LocalDate.now().plusDays(12));

        Reservation reservation2 = new Reservation();
        reservation2.setDepartureDate(LocalDate.now().plusDays(25));
        reservation2.setArrivalDate(LocalDate.now().plusDays(22));

        when(reservationRepository.findAnyReservationActiveDuringDate(any(), any())).thenReturn(List.of(reservation, reservation2));
        ReservationAvailable reservationAvailable = reservationService.checkAvailability(LocalDate.now(), LocalDate.now().plusMonths(1));
        assertEquals(reservationAvailable.getReservationDates().size(), 3);
    }

    @Test
    void checkAvailability_overlapping_dates_split_date_override_start_date() {
        Reservation reservation = new Reservation();
        reservation.setDepartureDate(LocalDate.now().plusDays(3));
        reservation.setArrivalDate(LocalDate.now().minusDays(1));

        Reservation reservation2 = new Reservation();
        reservation2.setDepartureDate(LocalDate.now().plusDays(25));
        reservation2.setArrivalDate(LocalDate.now().plusDays(22));

        when(reservationRepository.findAnyReservationActiveDuringDate(any(), any())).thenReturn(List.of(reservation, reservation2));
        ReservationAvailable reservationAvailable = reservationService.checkAvailability(LocalDate.now(), LocalDate.now().plusMonths(1));
        assertEquals(reservationAvailable.getReservationDates().size(), 2);
    }

    @Test
    void checkAvailability_overlapping_dates_split_date_override_end_date() {
        Reservation reservation = new Reservation();
        reservation.setDepartureDate(LocalDate.now().plusDays(15));
        reservation.setArrivalDate(LocalDate.now().plusDays(12));

        Reservation reservation2 = new Reservation();
        reservation2.setDepartureDate(LocalDate.now().plusMonths(1).plusDays(1));
        reservation2.setArrivalDate(LocalDate.now().plusDays(28));

        when(reservationRepository.findAnyReservationActiveDuringDate(any(), any())).thenReturn(List.of(reservation, reservation2));
        ReservationAvailable reservationAvailable = reservationService.checkAvailability(LocalDate.now(), LocalDate.now().plusMonths(1));
        assertEquals(reservationAvailable.getReservationDates().size(), 2);
    }

    @Test
    void checkAvailability_no_existing_reservation() {
        when(reservationRepository.findAnyReservationActiveDuringDate(any(), any())).thenReturn(List.of());
        ReservationAvailable reservationAvailable = reservationService.checkAvailability(LocalDate.now(), LocalDate.now().plusMonths(1));
        assertEquals(reservationAvailable.getReservationDates().size(), 1);
    }
}
