package com.cliff.reservation.service;

import com.cliff.reservation.db.model.Customer;
import com.cliff.reservation.db.model.Reservation;
import com.cliff.reservation.db.repository.CustomerRepository;
import com.cliff.reservation.db.repository.ReservationRepository;
import com.cliff.reservation.model.ReservationAvailable;
import com.cliff.reservation.model.ReservationDate;
import com.cliff.reservation.model.ReservationRequest;
import com.cliff.reservation.model.ReservationResponse;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private ReservationRepository reservationRepository;
    private CustomerRepository customerRepository;

    @Autowired
    public ReservationService(CustomerRepository customerRepository, ReservationRepository reservationRepository) {
        this.customerRepository = customerRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Map<String, String> validate(ReservationRequest reservationRequest) {
        if (reservationRequest == null) {
            return Map.of("request", "request was empty");
        }
        Map<String, String> errors = new HashMap<>();
        if (reservationRequest.getId() != null) {
            Reservation reservation = safeGetById(reservationRequest.getId());
            if ( reservation == null) {
                errors.put("id", "id given does not match existing reservations ids");
            }
        }

        if (reservationRequest.getId() == null) {
            if (StringUtils.isEmpty(reservationRequest.getFullName())) {
                errors.put("fullName", "full name is missing");
            }
            if (StringUtils.isEmpty(reservationRequest.getEmail())) {
                errors.put("email", "email is missing");
            }
        }
        if (reservationRequest.getArrivalDate() == null) {
            errors.put("arrivalDate", "arrival date is missing");
        }
        if (reservationRequest.getDepartureDate() == null) {
            errors.put("departureDate", "departure date is missing");
        }
        if (reservationRequest.getDepartureDate() != null && reservationRequest.getArrivalDate() != null) {
            if (reservationRequest.getArrivalDate().isAfter(reservationRequest.getDepartureDate())) {
                errors.put("departureDate", "departure date is before arrival date");
            }
            if (reservationRequest.getArrivalDate().isBefore(LocalDate.now().plusDays(1))) {
                errors.put("arrivalDate", "arrival date must be more than one day in the future");
            }
            if (reservationRequest.getArrivalDate().isAfter(LocalDate.now().plusMonths(1))) {
                errors.put("arrivalDate", "arrival date must not be more than one month in the future");
            }

            List<Reservation> reservations =
                reservationRepository.findAnyReservationActiveDuringDate(reservationRequest.getArrivalDate(),
                                                                         reservationRequest.getDepartureDate())
                    .stream()
                    .filter(reservation -> reservationRequest.getId() == null ||
                            !reservation.getId().equals(reservationRequest.getId()))
                    .toList();
            if (!reservations.isEmpty()) {
                errors.put("arrivalDate", "already existing reservation for date");
            }
            if (ChronoUnit.DAYS.between(reservationRequest.getArrivalDate(),
                                        reservationRequest.getDepartureDate()) > 3) {
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
    public Reservation updateReservation(ReservationRequest reservationRequest) {
        Reservation reservation = reservationRepository.getReferenceById(reservationRequest.getId());
        reservation.setArrivalDate(reservationRequest.getArrivalDate());
        reservation.setDepartureDate(reservationRequest.getDepartureDate());
        reservationRepository.save(reservation);
        return reservation;
    }

    @Transactional
    public boolean deactivateReservation(Long id) {
        Reservation reservation = safeGetById(id);
        if (reservation != null) {
            reservation.setActive(false);
            reservationRepository.save(reservation);
            return true;
        }
        return false;
    }

    @Transactional
    public Reservation saveReservation(ReservationRequest reservationRequest) {

        Customer customer =
                customerRepository.findCustomerByFullNameAndEmail(reservationRequest.getFullName(),
                                                                  reservationRequest.getEmail());

        if (customer == null) {
            customer = new Customer();
            customer.setEmail(reservationRequest.getEmail());
            customer.setFullName(reservationRequest.getFullName());
            customerRepository.save(customer);
        }

        Reservation reservation = new Reservation();
        reservation.setArrivalDate(reservationRequest.getArrivalDate());
        reservation.setDepartureDate(reservationRequest.getDepartureDate());
        reservation.setActive(true);
        reservation.setCustomer(customer);
        customer.getReservationList().add(reservation);
        reservationRepository.save(reservation);


        return reservation;
    }

    @Transactional
    public Reservation safeGetById(Long id) {
        Reservation reservation = null;
        try {
            reservation = reservationRepository.getReferenceById(id);
        } catch (EntityNotFoundException e ) {
            log.error("Nothing found for id: {}", id);
        }
        return reservation;
    }
}
