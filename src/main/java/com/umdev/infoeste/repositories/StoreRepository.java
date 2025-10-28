package com.umdev.infoeste.repositories;

import com.umdev.infoeste.entities.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {
    Optional<Store> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT s FROM Store s WHERE " +
           "(:q IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(s.city) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Store> findStoresWithSearch(@Param("q") String query, Pageable pageable);
}
