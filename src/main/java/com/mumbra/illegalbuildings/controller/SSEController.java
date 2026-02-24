package com.mumbra.illegalbuildings.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@Component
@RequestMapping("/api/sse")
@CrossOrigin(origins = "*")
public class SSEController {
    
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    
    @GetMapping(value = "/building-updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamBuildingUpdates(
            javax.servlet.http.HttpServletResponse response) {
        
        // Set proper headers for SSE
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Cache-Control");
        System.out.println("New SSE connection requested");
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        
        emitters.add(emitter);
        System.out.println("Total SSE connections: " + emitters.size());
        
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            System.out.println("SSE connection completed. Remaining connections: " + emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            System.out.println("SSE connection timed out. Remaining connections: " + emitters.size());
        });
        emitter.onError((ex) -> {
            emitters.remove(emitter);
            System.out.println("SSE connection error: " + ex.getMessage() + ". Remaining connections: " + emitters.size());
        });
        
        try {
            // Send initial connection message
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected to building updates"));
            System.out.println("Sent initial connection message");
        } catch (IOException e) {
            System.err.println("Failed to send initial connection message: " + e.getMessage());
            emitters.remove(emitter);
        }
        
        return emitter;
    }
    
    public void notifyBuildingApproved(String buildingName, Long buildingId) {
        String message = String.format("{\"type\":\"approved\",\"buildingName\":\"%s\",\"buildingId\":%d,\"timestamp\":%d}", 
                                     buildingName, buildingId, System.currentTimeMillis());
        
        System.out.println("Sending building approved notification to " + emitters.size() + " clients: " + message);
        
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("building-approved")
                        .data(message));
                return false;
            } catch (IOException e) {
                System.err.println("Failed to send notification to client: " + e.getMessage());
                return true;
            }
        });
        
        System.out.println("Notification sent. Active connections: " + emitters.size());
    }
    
    public void notifyBuildingRejected(String buildingName, Long buildingId) {
        String message = String.format("{\"type\":\"rejected\",\"buildingName\":\"%s\",\"buildingId\":%d,\"timestamp\":%d}", 
                                     buildingName, buildingId, System.currentTimeMillis());
        
        System.out.println("Sending building rejected notification to " + emitters.size() + " clients: " + message);
        
        emitters.removeIf(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("building-rejected")
                        .data(message));
                return false;
            } catch (IOException e) {
                System.err.println("Failed to send rejection notification to client: " + e.getMessage());
                return true;
            }
        });
        
        System.out.println("Rejection notification sent. Active connections: " + emitters.size());
    }
    
    @GetMapping("/test-notification")
    public String testNotification() {
        System.out.println("Test notification requested");
        notifyBuildingApproved("Test Building", 999L);
        return "Test notification sent to " + emitters.size() + " clients";
    }
    
    @GetMapping("/health")
    public String health() {
        return "SSE Controller is running. Active connections: " + emitters.size();
    }
}