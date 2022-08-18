package com.cliff.reservation.controller;

import com.cliff.reservation.db.model.Reservation;
import com.cliff.reservation.model.ReservationAvailable;
import com.cliff.reservation.model.ReservationRequest;
import com.cliff.reservation.model.ReservationResponse;
import com.cliff.reservation.service.ReservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservation")
    public ReservationResponse saveReservation(@RequestBody ReservationRequest reservationRequest) {
        Map<String, String> errors = reservationService.validate(reservationRequest);
        if (!errors.isEmpty()) {
            return new ReservationResponse((errors));
        }
        Reservation reservation = reservationService.saveReservation(reservationRequest);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setId(reservation.getId());
        reservationResponse.setStatus("Success");
        return reservationResponse;
    }

    @PutMapping("/reservation")
    public ReservationResponse updateReservation(@RequestBody ReservationRequest reservationRequest) {
        Map<String, String> errors = reservationService.validate(reservationRequest);
        if (!errors.isEmpty()) {
            return new ReservationResponse((errors));
        }
        Reservation reservation = reservationService.updateReservation(reservationRequest);
        ReservationResponse reservationResponse = new ReservationResponse();
        reservationResponse.setId(reservation.getId());
        reservationResponse.setStatus("Success");
        return reservationResponse;
    }


    @GetMapping("/reservations/{startDate}")
    public ReservationAvailable checkAvailability(@PathVariable("startDate") @DateTimeFormat(pattern = "uuuu-MM-dd") LocalDate startDate,
                                                  @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "uuuu-MM-dd") LocalDate endDate) {
        if (endDate == null) {
            endDate = startDate.plusMonths(1);
        }
        return reservationService.checkAvailability(startDate, endDate);
    }

    @DeleteMapping("/reservation/{id}")
    public boolean deactivateReservation(@PathVariable("id") Long id) {
        return reservationService.deactivateReservation(id);
    }

}
