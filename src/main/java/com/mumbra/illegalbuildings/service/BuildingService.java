package com.mumbra.illegalbuildings.service;

import com.mumbra.illegalbuildings.model.Building;
import com.mumbra.illegalbuildings.repository.BuildingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }

    public Building getBuildingById(Long id) {
        return buildingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Building not found: " + id));
    }

    public List<Building> getBuildingsBySeverity(String severity) {
        return buildingRepository.findBySeverity(severity);
    }

    public List<Building> searchBuildings(String query) {
        return buildingRepository.searchBuildings(query);
    }

    public List<Building> getBuildingsWithFilters(String severity, String violationType, String wardNumber) {
        return buildingRepository.findBuildingsWithFilters(severity, violationType, wardNumber);
    }

    public List<Building> getBuildingsByWard(String wardNumber) {
        return buildingRepository.findByWardNumber(wardNumber);
    }

    public List<Building> getBuildingsByViolationType(String violationType) {
        return buildingRepository.findByViolationType(violationType);
    }

    public List<Building> getBuildingsByLocation(String location) {
        return buildingRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<Building> getBuildingsByCategory(String category) {
        return buildingRepository.findByCategory(category);
    }

    public Building saveBuilding(Building building) {
        return buildingRepository.save(building);
    }

    public void deleteBuilding(Long id) {
        buildingRepository.deleteById(id);
    }

    public List<Building> getBuildingsByStatus(String status) {
        return buildingRepository.findByStatus(status);
    }
}
