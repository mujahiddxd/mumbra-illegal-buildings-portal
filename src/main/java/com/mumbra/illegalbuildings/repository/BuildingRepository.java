package com.mumbra.illegalbuildings.repository;

import com.mumbra.illegalbuildings.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    
    List<Building> findBySeverity(String severity);
    List<Building> findByStatus(String status);
    List<Building> findByAddressContainingIgnoreCase(String address);
    List<Building> findByLocationContainingIgnoreCase(String location);
    List<Building> findByWardNumber(String wardNumber);
    List<Building> findByViolationType(String violationType);
    List<Building> findByCategory(String category);
    
    @Query("SELECT b FROM Building b WHERE " +
           "LOWER(b.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.address) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Building> searchBuildings(@Param("query") String query);
    
    @Query("SELECT b FROM Building b WHERE " +
           "(:severity IS NULL OR b.severity = :severity) AND " +
           "(:violationType IS NULL OR b.violationType = :violationType) AND " +
           "(:wardNumber IS NULL OR b.wardNumber = :wardNumber)")
    List<Building> findBuildingsWithFilters(@Param("severity") String severity, 
                                           @Param("violationType") String violationType,
                                           @Param("wardNumber") String wardNumber);
}
