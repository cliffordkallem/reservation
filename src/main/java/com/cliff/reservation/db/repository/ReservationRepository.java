package com.cliff.reservation.db.repository;

import com.cliff.reservation.db.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query(value = "select r from Reservation r where r.customer.id = :customerId and r.active = true")
    List<Reservation> findReservationsByCustomerId(@Param("customerId") Long customerId);

    @Query(value = "select r from Reservation r where r.arrivalDate > :arrivalDate and r.arrivalDate < :departureDate and r.active = true")
    List<Reservation> findAnyReservationActiveDuringDate(@Param("arrivalDate") LocalDate arrivalDate, @Param("departureDate") LocalDate departureDate);
}
