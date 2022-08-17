package com.cliff.reservation.db.repository;

import com.cliff.reservation.db.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = "select c from Customer c where c.fullName = :fullName and c.email = :email")
    Customer findCustomerByFullNameAndEmail(@Param("fullName") String fullname, @Param("email") String email);

}
