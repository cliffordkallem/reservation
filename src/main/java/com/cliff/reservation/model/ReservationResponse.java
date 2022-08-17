package com.cliff.reservation.model;

import java.util.Map;
import java.util.Objects;

public class ReservationResponse {
    private Long id;
    private String status;
    private Map<String, String> errors;

    public ReservationResponse() {
    }

    public ReservationResponse(Map<String, String> errors) {
        this.errors = errors;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservationResponse that)) {
            return false;
        }
        return Objects.equals(id, that.id) &&
                Objects.equals(status, that.status) &&
                Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, errors);
    }

    @Override
    public String toString() {
        return "ReservationResponse{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", errors=" + errors +
                '}';
    }
}
