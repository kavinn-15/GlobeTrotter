package com.example.loginapp.repository;

import com.example.loginapp.entity.Trip;
import com.example.loginapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Trip}. Hibernate is the underlying
 * JPA provider; Spring Data generates the implementation at runtime.
 */
public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByOwnerOrderByStartDateDesc(User owner);

    List<Trip> findAllByOrderByCreatedAtDesc();
}
