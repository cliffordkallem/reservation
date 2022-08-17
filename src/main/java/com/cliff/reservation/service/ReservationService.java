package com.cliff.reservation.service;

import com.cliff.reservation.db.model.Customer;
import com.cliff.reservation.db.model.Reservation;
import com.cliff.reservation.db.repository.CustomerRepository;
import com.cliff.reservation.db.repository.ReservationRepository;
import com.cliff.reservation.model.ReservationAvailable;
import com.cliff.reservation.model.ReservationDate;
import com.cliff.reservation.model.ReservationInfo;
import com.cliff.reservation.model.ReservationResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationService {
    private ReservationRepository reservationRepository;
    private CustomerRepository customerRepository;

    @Autowired
    public ReservationService(CustomerRepository customerRepository, ReservationRepository reservationRepository) {
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
    }
    @Transactional
    public Map<String, String> validate(ReservationInfo reservationInfo) {
        if (reservationInfo == null) {
            return Map.of("request", "request was empty");
        }
        Map<String, String> errors = new HashMap<>();
        if (StringUtils.isEmpty(reservationInfo.getFullName())) {
            errors.put("fullName", "full name is missing");
        }
        if (StringUtils.isEmpty(reservationInfo.getEmail())) {
            errors.put("email", "email is missing");
        }
        if (reservationInfo.getArrivalDate() == null) {
            errors.put("arrivalDate", "arrival date is missing");
        }
        if (reservationInfo.getDepartureDate() == null) {
            errors.put("departureDate", "departure date is missing");
        }
        if (reservationInfo.getDepartureDate() != null && reservationInfo.getArrivalDate() != null) {
            if (reservationInfo.getArrivalDate().isAfter(reservationInfo.getDepartureDate())) {
                errors.put("departureDate", "departure date is before arrival date");
            }
            if (reservationInfo.getArrivalDate().isBefore(LocalDate.now().plusDays(1))) {
                errors.put("arrivalDate", "arrival date must be more than one day in the future");
            }
            if (reservationInfo.getArrivalDate().isAfter(LocalDate.now().plusMonths(1))) {
                errors.put("arrivalDate", "arrival date must not be more than one month in the future");
            }
            if (!reservationRepository.findAnyReservationActiveDuringDate(reservationInfo.getArrivalDate(),
                                                                          reservationInfo.getDepartureDate())
                                      .isEmpty()) {
                errors.put("arrivalDate", "already existing reservation for date");
            }
            if (ChronoUnit.DAYS.between(reservationInfo.getArrivalDate(), reservationInfo.getDepartureDate()) > 3) {
                errors.put("departureDate", "total days of reservation is too long");
            }
        }
        return errors;
    }

    public ReservationAvailable checkAvailability(LocalDate startDate, LocalDate endDate) {

        List<Reservation> reservations = reservationRepository.findAnyReservationActiveDuringDate(startDate, endDate);
        List<Reservation> reservationList = new ArrayList<>(reservations);
        reservationList.sort(Comparator.comparing(Reservation::getArrivalDate));

        List<ReservationDate> reservationDates = new ArrayList<>();
        ReservationDate reservationDate = new ReservationDate();
        reservationDate.setStartDate(startDate);
        for (Reservation reservation: reservationList) {
            //first case
            if (reservation.getArrivalDate().isBefore(startDate) || reservation.getArrivalDate().isEqual(startDate)) {
                reservationDate.setStartDate(reservation.getDepartureDate());
                continue;
            }
            if (reservation.getArrivalDate().isAfter(startDate) && reservationDate.getStartDate() == null ) {
                reservationDate.setStartDate(startDate);
            }
            // after the first case
            reservationDate.setEndDate(reservation.getArrivalDate());
            reservationDates.add(reservationDate);
            reservationDate = new ReservationDate();
            reservationDate.setStartDate(reservation.getDepartureDate());

        }

        if ((reservationDate.getEndDate() == null && reservationDate.getStartDate().isBefore(endDate)) ||
            (reservationDate.getEndDate() != null && reservationDate.getEndDate().isAfter(endDate))) {

            reservationDate.setEndDate(endDate);
            reservationDates.add(reservationDate);
        }

        return new ReservationAvailable(reservationDates);
    }

    @Transactional
    public Reservation saveReservation(ReservationInfo reservationInfo) {

        Customer customer =
                customerRepository.findCustomerByFullNameAndEmail(reservationInfo.getFullName(),
                                                                  reservationInfo.getEmail());

        if (customer == null) {
            customer = new Customer();
            customer.setEmail(reservationInfo.getEmail());
            customer.setFullName(reservationInfo.getFullName());
            customerRepository.save(customer);
        }

        Reservation reservation = new Reservation();
        reservation.setArrivalDate(reservationInfo.getArrivalDate());
        reservation.setDepartureDate(reservationInfo.getDepartureDate());
        reservation.setActive(true);
        reservation.setCustomer(customer);
        customer.getReservationList().add(reservation);
        reservationRepository.save(reservation);


        return reservation;
    }
}
