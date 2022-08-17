package com.cliff.reservation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReservationAvailable {

    private List<ReservationDate> reservationDates = new ArrayList<>();

    public ReservationAvailable(List<ReservationDate> reservationDates) {
        this.reservationDates = reservationDates;
    }

    public List<ReservationDate> getReservationDates() {
        return reservationDates;
    }

    public void setReservationDates(List<ReservationDate> reservationDates) {
        this.reservationDates = reservationDates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservationAvailable that)) {
            return false;
        }
        return Objects.equals(reservationDates, that.reservationDates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationDates);
    }

    @Override
    public String toString() {
        return "ReservationAvailable{" +
                "reservationDates=" + reservationDates +
                '}';
    }
}
