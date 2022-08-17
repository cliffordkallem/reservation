package com.cliff.reservation.model;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationDate {
    private LocalDate startDate;
    private LocalDate endDate;

    public ReservationDate() {
    }

    public ReservationDate(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservationDate that)) {
            return false;
        }
        return Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public String toString() {
        return "ReservationDate{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
