package com.mumbra.illegalbuildings.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity // 👉 This annotation marks the class as a JPA entity, which means it will be mapped to a database table.
@Table(name = "buildings") // 👉 This annotation specifies the name of the database table.
public class Building {
    
    @Id // 👉 This annotation marks the field as the primary key of the entity.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // This annotation specifies the generation strategy for the primary key.(auto)
    private Long id;
    
    private String name;
    @Column(length = 512)
    private String address;
    @Column(length = 512)
    private String location;
    private String wardNumber;
    private String floors;
    private String category;
    private String violationType; 
    private double latitude;
    private double longitude;
    private String severity; // HIGH, MEDIUM, LOW
    private String status;   // PENDING, VERIFIED, UNDER_REVIEW, RESOLVED
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    private LocalDateTime reportedDate;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public String getFloors() {
        return floors;
    }

    public void setFloors(String floors) {
        this.floors = floors;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDateTime reportedDate) {
        this.reportedDate = reportedDate;
    }
}
