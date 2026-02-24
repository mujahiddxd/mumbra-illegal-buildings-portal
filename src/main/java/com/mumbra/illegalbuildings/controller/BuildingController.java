package com.mumbra.illegalbuildings.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mumbra.illegalbuildings.model.Building;
import com.mumbra.illegalbuildings.service.BuildingService;

@RestController
@RequestMapping("/api/buildings")
@CrossOrigin(origins = "*")
public class BuildingController {

    private final BuildingService buildingService;
    private final SSEController sseController;

    
    public BuildingController(BuildingService buildingService, SSEController sseController) {
        this.buildingService = buildingService;
        this.sseController = sseController;
    }

    @GetMapping
    public List<Building> getAllBuildings() {
        // Only return verified buildings for public view
        List<Building> verifiedBuildings = buildingService.getBuildingsByStatus("VERIFIED");
        System.out.println("Public API returning " + verifiedBuildings.size() + " verified buildings");
        return verifiedBuildings;
    }

    @GetMapping("/all")
    public List<Building> getAllBuildingsForAdmin() {
        // Return all buildings for admin panel
        return buildingService.getAllBuildings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Building> getBuildingById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.getBuildingById(id));
    }

    @GetMapping("/severity/{severity}")
    public List<Building> getBuildingsBySeverity(@PathVariable String severity) {
        return buildingService.getBuildingsBySeverity(severity);
    }

    @GetMapping("/search")
    public List<Building> searchBuildings(@RequestParam String query) {
        return buildingService.searchBuildings(query);
    }

    @GetMapping("/filter")
    public List<Building> getBuildingsWithFilters(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String violationType,
            @RequestParam(required = false) String wardNumber) {
        return buildingService.getBuildingsWithFilters(severity, violationType, wardNumber);
    }

    @GetMapping("/ward/{wardNumber}")
    public List<Building> getBuildingsByWard(@PathVariable String wardNumber) {
        return buildingService.getBuildingsByWard(wardNumber);
    }

    @GetMapping("/violation-type/{violationType}")
    public List<Building> getBuildingsByViolationType(@PathVariable String violationType) {
        return buildingService.getBuildingsByViolationType(violationType);
    }

    @PostMapping
    public Building createBuilding(@RequestBody Building building) {
        return buildingService.saveBuilding(building);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Building> updateBuilding(@PathVariable Long id, @RequestBody Building buildingDetails) {
        Building building = buildingService.getBuildingById(id);
        building.setName(buildingDetails.getName());
        building.setAddress(buildingDetails.getAddress());
        building.setLocation(buildingDetails.getLocation());
        building.setWardNumber(buildingDetails.getWardNumber());
        building.setFloors(buildingDetails.getFloors());
        building.setCategory(buildingDetails.getCategory());
        building.setViolationType(buildingDetails.getViolationType());
        building.setLatitude(buildingDetails.getLatitude());
        building.setLongitude(buildingDetails.getLongitude());
        building.setSeverity(buildingDetails.getSeverity());
        building.setStatus(buildingDetails.getStatus());
        building.setDescription(buildingDetails.getDescription());
        building.setImageUrl(buildingDetails.getImageUrl());
        
        final Building updatedBuilding = buildingService.saveBuilding(building);
        return ResponseEntity.ok(updatedBuilding);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBuilding(@PathVariable Long id) {
        buildingService.deleteBuilding(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Building> approveBuilding(@PathVariable Long id) {
        System.out.println("Approving building with ID: " + id);
        
        Building building = buildingService.getBuildingById(id);
        System.out.println("Found building: " + building.getName() + " with status: " + building.getStatus());
        
        building.setStatus("VERIFIED");
        Building updatedBuilding = buildingService.saveBuilding(building);
        System.out.println("Updated building status to: " + updatedBuilding.getStatus());
        
        // Add to CSV file
        try {
            addBuildingToCSV(updatedBuilding);
            System.out.println("Successfully added building to CSV: " + building.getName());
        } catch (Exception e) {
            System.err.println("Failed to add building to CSV: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Notify all connected clients via SSE
        try {
            sseController.notifyBuildingApproved(updatedBuilding.getName(), updatedBuilding.getId());
            System.out.println("Sent SSE notification for approved building: " + updatedBuilding.getName());
        } catch (Exception e) {
            System.err.println("Failed to send SSE notification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(updatedBuilding);
    }
    
    private void addBuildingToCSV(Building building) throws IOException {
        // Format: ID,Ward No.,Building Name,Location,Floors,Category,Image URL,Area,Latitude,Longitude,Severity
        String csvLine = String.format("%d,%s,\"%s\",\"%s\",%s,%s,\"%s\",%s,%.6f,%.6f,%s\n",
            building.getId(),
            building.getWardNumber() != null ? building.getWardNumber() : "16",
            building.getName() != null ? building.getName().replace("\"", "\"\"") : "Unknown Building",
            building.getLocation() != null ? building.getLocation().replace("\"", "\"\"") : "Unknown Location",
            building.getFloors() != null ? building.getFloors() : "0",
            building.getCategory() != null ? building.getCategory() : "C2B",
            building.getImageUrl() != null ? building.getImageUrl().replace("\"", "\"\"") : "/images/buildings/default-building.jpg",
            "1000", // Default area
            building.getLatitude() != 0.0 ? building.getLatitude() : 19.1750,
            building.getLongitude() != 0.0 ? building.getLongitude() : 73.0100,
            building.getSeverity() != null ? building.getSeverity() : "MEDIUM"
        );
        
        // Try multiple possible paths for the CSV file
        String[] possiblePaths = {
            "src/main/resources/static/buildings.csv",
            "target/classes/static/buildings.csv",
            "classes/static/buildings.csv"
        };
        
        boolean written = false;
        for (String pathStr : possiblePaths) {
            try {
                Path csvPath = Paths.get(pathStr);
                if (Files.exists(csvPath)) {
                    Files.write(csvPath, csvLine.getBytes(), StandardOpenOption.APPEND);
                    System.out.println("Successfully wrote to: " + pathStr);
                    written = true;
                    break;
                }
            } catch (Exception e) {
                System.err.println("Failed to write to " + pathStr + ": " + e.getMessage());
            }
        }
        
        if (!written) {
            // If none of the paths work, create the file in the source directory
            Path csvPath = Paths.get("src/main/resources/static/buildings.csv");
            Files.write(csvPath, csvLine.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
            System.out.println("Created/wrote to source file: " + csvPath.toString());
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Building> rejectBuilding(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Building building = buildingService.getBuildingById(id);
        building.setStatus("REJECTED");
        // You can store the rejection reason in description or create a new field
        String reason = request.get("reason");
        if (reason != null) {
            building.setDescription(building.getDescription() + " [REJECTED: " + reason + "]");
        }
        Building updatedBuilding = buildingService.saveBuilding(building);
        
        // Notify all connected clients via SSE
        try {
            sseController.notifyBuildingRejected(updatedBuilding.getName(), updatedBuilding.getId());
            System.out.println("Sent SSE notification for rejected building: " + updatedBuilding.getName());
        } catch (Exception e) {
            System.err.println("Failed to send SSE notification: " + e.getMessage());
        }
        
        return ResponseEntity.ok(updatedBuilding);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        List<Building> allBuildings = buildingService.getAllBuildings();
        List<Building> verifiedBuildings = buildingService.getBuildingsByStatus("VERIFIED");
        
        String response = String.format("API Test - Total buildings: %d, Verified buildings: %d", 
                                       allBuildings.size(), verifiedBuildings.size());
        System.out.println(response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-sse")
    public ResponseEntity<String> testSSE() {
        try {
            sseController.notifyBuildingApproved("Test Building", 999L);
            return ResponseEntity.ok("SSE test notification sent");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("SSE test failed: " + e.getMessage());
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> exportVerifiedBuildingsToCSV() {
        List<Building> verifiedBuildings = buildingService.getBuildingsByStatus("VERIFIED");
        StringBuilder csv = new StringBuilder();
        
        // CSV Header
        csv.append("Name,Address,Location,Ward Number,Floors,Category,Violation Type,Latitude,Longitude,Severity,Status,Description,Image URL,Reported Date\n");
        
        // CSV Data
        for (Building building : verifiedBuildings) {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.6f,%.6f,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                building.getName(),
                building.getAddress(),
                building.getLocation(),
                building.getWardNumber(),
                building.getFloors(),
                building.getCategory(),
                building.getViolationType(),
                building.getLatitude(),
                building.getLongitude(),
                building.getSeverity(),
                building.getStatus(),
                building.getDescription().replace("\"", "\"\""), // Escape quotes
                building.getImageUrl(),
                building.getReportedDate()
            ));
        }
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=\"verified_buildings.csv\"")
                .body(csv.toString());
    }
}
