package com.cliff.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cliff.reservation.db.model.Customer;
import com.cliff.reservation.db.model.Reservation;
import com.cliff.reservation.db.repository.CustomerRepository;
import com.cliff.reservation.db.repository.ReservationRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReservationRepositoryTest {
    @Autowired CustomerRepository customerRepository;
    @Autowired ReservationRepository reservationRepository;

    @Test
    void findAnyReservationActiveDuringDate_startAndEndOverlapping() {
        Customer customer = new Customer();
        customer.setEmail("bob@bob.com");
        customer.setFullName("bob face");
        customerRepository.save(customer);
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setDepartureDate(LocalDate.now().plusDays(15));
        reservation.setArrivalDate(LocalDate.now().plusDays(12));
        reservation.setActive(true);
        reservationRepository.save(reservation);

        Reservation reservation2 = new Reservation();
        reservation2.setCustomer(customer);
        reservation2.setDepartureDate(LocalDate.now().plusMonths(1).plusDays(1));
        reservation2.setArrivalDate(LocalDate.now().plusDays(28));
        reservation2.setActive(true);
        reservationRepository.save(reservation2);

        Reservation reservation3 = new Reservation();
        reservation3.setCustomer(customer);
        reservation3.setDepartureDate(LocalDate.now().minusDays(1));
        reservation3.setArrivalDate(LocalDate.now().plusDays(2));
        reservation3.setActive(true);
        reservationRepository.save(reservation3);

        List<Reservation> reservations = reservationRepository.findAnyReservationActiveDuringDate(LocalDate.now(), LocalDate.now().plusMonths(1));
        assertEquals(reservations.size(), 3);
    }

    @Test
    void findAnyReservationActiveDuringDate_outsideDateRange() {
        Customer customer = new Customer();
        customer.setEmail("bob@bob.com");
        customer.setFullName("bob face");
        customerRepository.save(customer);
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setDepartureDate(LocalDate.now().plusMonths(15));
        reservation.setArrivalDate(LocalDate.now().plusMonths(12));
        reservation.setActive(true);
        reservationRepository.save(reservation);


        List<Reservation> reservations = reservationRepository.findAnyReservationActiveDuringDate(LocalDate.now(), LocalDate.now().plusMonths(1));
        assertEquals(reservations.size(), 0);
    }
}
