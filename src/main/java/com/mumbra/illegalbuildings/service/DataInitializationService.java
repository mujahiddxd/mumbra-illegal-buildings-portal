package com.mumbra.illegalbuildings.service;

import com.mumbra.illegalbuildings.model.Building;
import com.mumbra.illegalbuildings.repository.BuildingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private BuildingRepository buildingRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (buildingRepository.count() > 0) {
            return; // Data already initialized
        }

        // Prefer CSV if present; only fallback when CSV file is missing
        if (csvExists("buildings.csv")) {
            int imported = importFromCsv("buildings.csv");
            System.out.println("CSV import attempted; rows imported: " + imported);
            return;
        } else {
            System.out.println("CSV not found on classpath, seeding with sample data.");
            initializeBuildingsData();
        }
    }

    private boolean csvExists(String classpathCsv) {
        try {
            return new ClassPathResource(classpathCsv).exists();
        } catch (Exception e) {
            return false;
        }
    }

    private int importFromCsv(String classpathCsv) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathCsv);
            if (!resource.exists()) {
                return 0;
            }
            InputStream inputStream = resource.getInputStream();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String header = reader.readLine(); // read header
                if (header == null) {
                    return 0;
                }
                String[] headerCols = parseCsvLine(header);
                List<Building> toSave = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    String[] parts = parseCsvLine(line);
                    Building b = null;
                    if (parts.length >= 13 || equalsIgnoreCase(headerCols[0], "name")) {
                        // Full schema
                        if (parts.length < 13) {
                            continue;
                        }
                        b = new Building();
                        b.setName(parts[0]);
                        b.setAddress(parts[1]);
                        b.setLocation(parts[2]);
                        b.setWardNumber(parts[3]);
                        b.setFloors(parts[4]);
                        b.setCategory(parts[5]);
                        b.setViolationType(parts[6]);
                        b.setLatitude(safeParseDouble(parts[7]));
                        b.setLongitude(safeParseDouble(parts[8]));
                        b.setSeverity(parts[9]);
                        b.setStatus(parts[10]);
                        b.setDescription(parts[11]);
                        String image = parts[12];
                        if (image == null || image.trim().isEmpty()) {
                            image = "/images/buildings/default-building.jpg";
                        }
                        b.setImageUrl(image);
                    } else if (parts.length >= 7 && equalsIgnoreCase(headerCols[0], "ID")) {
                        // Minimal schema with possible commas inside Location.
                        // Expected logical columns:
                        // [0]=ID, [1]=Ward No., [2]=Building Name,
                        // [3..catIdx-2]=Location (variable), [catIdx-1]=Floors, [catIdx]=Category,
                        // After category: image URL (may contain commas), optional area, and latitude/longitude.
                        b = new Building();
                        String name = parts[2];
                        String ward = parts[1];

                        // Find category index (assume 'C2B' for now)
                        int catIdx = -1;
                        for (int i = parts.length - 1; i >= 0; i--) {
                            if (equalsIgnoreCase(parts[i], "C2B")) {
                                catIdx = i;
                                break;
                            }
                        }
                        // Fallback: use old positional assumption if not found
                        if (catIdx == -1 || catIdx < 5) {
                            catIdx = parts.length - 2;
                        }

                        String floorsStr = parts[Math.max(4, catIdx - 1)];
                        String category = parts[catIdx];

                        // Build location between index 3 and catIdx-2
                        StringBuilder locBuilder = new StringBuilder();
                        for (int i = 3; i <= catIdx - 2; i++) {
                            if (parts[i] == null || parts[i].trim().isEmpty()) {
                                continue;
                            }
                            if (locBuilder.length() > 0) locBuilder.append(", ");
                            locBuilder.append(parts[i].trim());
                        }
                        String location = locBuilder.toString();
                        // Determine latitude/longitude possibly in last fields
                        Double lat = null;
                        Double lng = null;
                        String last = parts[parts.length - 1];
                        // Case A: last token is "lat, lon"
                        if (last != null && last.contains(",")) {
                            String[] ll = last.split("\\s*,\\s*");
                            if (ll.length >= 2) {
                                lat = tryParseDouble(ll[0]);
                                lng = tryParseDouble(ll[1]);
                            }
                        }
                        // Case B: last two tokens are numeric doubles
                        if (lat == null || lng == null) {
                            if (parts.length - (catIdx + 1) >= 2) {
                                Double maybeLng = tryParseDouble(parts[parts.length - 1]);
                                Double maybeLat = tryParseDouble(parts[parts.length - 2]);
                                if (maybeLat != null && maybeLng != null) {
                                    lat = maybeLat;
                                    lng = maybeLng;
                                }
                            }
                        }
                        // Build image URL from tokens after category up to before lat/lng and optional area
                        int endForImage = parts.length; // exclusive
                        if (lat != null && lng != null) {
                            endForImage = parts.length - 2; // assume last two are lat/lng or last one is "lat,lon"
                            if (last != null && last.contains(",")) {
                                endForImage = parts.length - 1;
                            }
                        }
                        // If the token before endForImage is a pure number (area), skip it
                        if (endForImage - 1 > catIdx + 0) {
                            Double maybeArea = tryParseDouble(parts[endForImage - 1]);
                            if (maybeArea != null) {
                                endForImage -= 1;
                            }
                        }
                        StringBuilder imgBuilder = new StringBuilder();
                        for (int i = catIdx + 1; i < endForImage; i++) {
                            if (imgBuilder.length() > 0) imgBuilder.append(',');
                            imgBuilder.append(parts[i]);
                        }
                        String image = imgBuilder.toString();

                        b.setName(name);
                        b.setAddress(location);
                        b.setLocation(location);
                        b.setWardNumber(ward);
                        b.setFloors(floorsStr);
                        b.setCategory(category);
                        b.setViolationType("unauthorized");
                        // Set lat/lng if provided, else 0
                        b.setLatitude(lat != null ? lat : 0.0);
                        b.setLongitude(lng != null ? lng : 0.0);
                        // Heuristic severity by floors
                        int floorCount = (int) safeParseDouble(floorsStr);
                        String severity = floorCount >= 9 ? "HIGH" : (floorCount >= 6 ? "MEDIUM" : "LOW");
                        b.setSeverity(severity);
                        b.setStatus("PENDING");
                        b.setDescription("Seeded from CSV");
                        if (image == null || image.trim().isEmpty()) {
                            image = "/images/buildings/default-building.jpg";
                        }
                        b.setImageUrl(image);
                    }

                    if (b != null) {
                        b.setReportedDate(LocalDateTime.now().minusDays((long) (Math.random() * 365)));
                        toSave.add(b);
                    }
                }
                if (!toSave.isEmpty()) {
                    buildingRepository.saveAll(toSave);
                }
                return toSave.size();
            }
        } catch (Exception e) {
            // If anything goes wrong, fallback will handle it
            return 0;
        }
    }

    // Basic CSV parser supporting simple commas and quoted fields
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString());
        return tokens.toArray(new String[0]);
    }

    private boolean equalsIgnoreCase(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }

    private double safeParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return 0.0;
        }
    }

    private Double tryParseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private void initializeBuildingsData() {
        List<Building> buildings = Arrays.asList(
            // Data from your first image (building area data)
            createBuilding("Farida Mansion", "Mumbra Devi Road", "Mumbra Devi Road", "16", "7", "C2B", "unauthorized", 19.1750, 73.0100, "HIGH", "VERIFIED", "Five floors added beyond approved plan. Safety exits non-compliant with regulations.", "https://via.placeholder.com/400x300/ff6b6b/ffffff?text=Farida+Mansion"),
            createBuilding("Diamond Park", "Aashiyana Theater Complex", "Aashiyana Theater Complex, Mumbra", "16", "7", "C2B", "unauthorized", 19.1755, 73.0105, "HIGH", "VERIFIED", "Seven floors constructed without proper permits. Structural safety concerns identified.", "https://via.placeholder.com/400x300/ffa726/ffffff?text=Diamond+Park"),
            createBuilding("Mohammad Fa Building", "Jeevan Bagh", "Jeevan Bagh", "16", "0", "C2B", "unauthorized", 19.1760, 73.0110, "HIGH", "PENDING", "Construction halted due to zero floor approval. Building incomplete and unsafe.", "https://via.placeholder.com/400x300/ef5350/ffffff?text=Mohammad+Fa"),
            
            // Data from your second image (illegal buildings table)
            createBuilding("Babu Street B Wing", "Mumbra Devi Road", "Mumbra Devi Road", "16", "6", "C2B", "unauthorized", 19.1752, 73.0102, "MEDIUM", "VERIFIED", "Six floors constructed without proper authorization.", "https://via.placeholder.com/400x300/66bb6a/ffffff?text=Babu+Street"),
            createBuilding("Dilshad Building No. 5", "Jeevan Bagh", "Jeevan Bagh", "16", "5", "C2B", "unauthorized", 19.1758, 73.0108, "MEDIUM", "PENDING", "Five floors built beyond approved plan.", "/images/buildings/dilshad-5.jpg"),
            createBuilding("Dilshad Building No. 3", "Jeevan Bagh", "Jeevan Bagh", "16", "7", "C2B", "unauthorized", 19.1759, 73.0109, "HIGH", "PENDING", "Seven floors constructed without proper permits.", "/images/buildings/dilshad-3.jpg"),
            createBuilding("Dilshad Building No. 4-B", "Jeevan Bagh", "Jeevan Bagh", "16", "7", "C2B", "unauthorized", 19.1761, 73.0111, "HIGH", "UNDER_REVIEW", "Seven floors built beyond approved plan.", "/images/buildings/dilshad-4b.jpg"),
            createBuilding("Dilshad Building No. 2", "Jeevan Bagh", "Jeevan Bagh", "16", "6", "C2B", "unauthorized", 19.1757, 73.0107, "MEDIUM", "UNDER_REVIEW", "Six floors constructed without proper authorization.", "/images/buildings/dilshad-2.jpg"),
            createBuilding("Jubilee Apartments A & B Wing", "Aashiyana Theater Complex", "Aashiyana Theater Complex, Mumbra", "16", "8", "C2B", "unauthorized", 19.1753, 73.0103, "HIGH", "UNDER_REVIEW", "Eight floors constructed without proper permits.", "/images/buildings/jubilee-apartments.jpg"),
            createBuilding("Meena Mahal", "Jeevan Bagh", "Jeevan Bagh", "16", "7", "C2B", "unauthorized", 19.1762, 73.0112, "HIGH", "UNDER_REVIEW", "Seven floors built beyond approved plan.", "/images/buildings/meena-mahal.jpg"),
            createBuilding("Navi Imarat Bada Imambada", "Nusrat Imambada Compound", "Nusrat Imambada Compound, Retiwadi", "16", "4", "C2B", "unauthorized", 19.1754, 73.0104, "MEDIUM", "UNDER_REVIEW", "Four floors constructed without proper authorization.", "/images/buildings/navi-imarat.jpg"),
            createBuilding("Rehmat Manzil", "Retiwadi", "Retiwadi", "16", "6", "C2B", "unauthorized", 19.1755, 73.0105, "MEDIUM", "UNDER_REVIEW", "Six floors built beyond approved plan.", "/images/buildings/rehmat-manzil.jpg"),
            createBuilding("Noorani Building", "Retiwadi", "Retiwadi", "16", "5", "C2B", "unauthorized", 19.1756, 73.0106, "MEDIUM", "UNDER_REVIEW", "Five floors constructed without proper permits.", "/images/buildings/noorani-building.jpg"),
            createBuilding("Almas Apartments", "Naigaon", "Naigaon, Mumbra", "16", "7", "C2B", "unauthorized", 19.1763, 73.0113, "HIGH", "UNDER_REVIEW", "Seven floors built beyond approved plan.", "/images/buildings/almas-apartments.jpg"),
            createBuilding("Hina Palace", "Aashiyana Theater Complex", "Aashiyana Theater Complex, Mumbra", "16", "8", "C2B", "unauthorized", 19.1757, 73.0107, "HIGH", "UNDER_REVIEW", "Eight floors constructed without proper authorization.", "/images/buildings/hina-palace.jpg"),
            createBuilding("Haji Karim Building", "Jeevan Bagh", "Jeevan Bagh", "16", "6", "C2B", "unauthorized", 19.1758, 73.0108, "MEDIUM", "UNDER_REVIEW", "Six floors built beyond approved plan.", "/images/buildings/haji-karim.jpg"),
            createBuilding("Gulshan Terrace", "Mumbra Devi Road", "Mumbra Devi Road", "16", "7", "C2B", "unauthorized", 19.1759, 73.0109, "HIGH", "UNDER_REVIEW", "Seven floors constructed without proper permits.", "/images/buildings/gulshan-terrace.jpg"),
            createBuilding("Asiya Manzil", "Baida Colony", "Baida Colony, Mumbra", "16", "4", "C2B", "unauthorized", 19.1760, 73.0110, "MEDIUM", "UNDER_REVIEW", "Four floors built beyond approved plan.", "/images/buildings/asiya-manzil.jpg"),
            createBuilding("Rizwan Palace", "Naigaon", "Naigaon, Mumbra", "16", "8", "C2B", "unauthorized", 19.1761, 73.0111, "HIGH", "UNDER_REVIEW", "Eight floors constructed without proper authorization.", "/images/buildings/rizwan-palace.jpg"),
            createBuilding("Heena Tower A & B", "Aashiyana Theater Complex", "Aashiyana Theater Complex, Mumbra", "16", "10", "C2B", "unauthorized", 19.1762, 73.0112, "HIGH", "UNDER_REVIEW", "Ten floors built beyond approved plan. Major safety concerns.", "/images/buildings/heena-tower.jpg"),
            createBuilding("Zeenat Apartments", "Retiwadi", "Retiwadi", "16", "5", "C2B", "unauthorized", 19.1763, 73.0113, "MEDIUM", "UNDER_REVIEW", "Five floors constructed without proper permits.", "/images/buildings/zeenat-apartments.jpg"),
            createBuilding("Al-Falah Building", "Mumbra Bazar Peth", "Mumbra Bazar Peth, Mumbra", "16", "6", "C2B", "unauthorized", 19.1764, 73.0114, "MEDIUM", "UNDER_REVIEW", "Six floors built beyond approved plan.", "/images/buildings/al-falah.jpg"),
            createBuilding("Mehboob Manzil", "Naigaon", "Naigaon, Mumbra", "16", "6", "C2B", "unauthorized", 19.1765, 73.0115, "MEDIUM", "UNDER_REVIEW", "Six floors constructed without proper authorization.", "/images/buildings/mehboob-manzil.jpg"),
            createBuilding("Ayesha Complex", "Mumbra Devi Road", "Mumbra Devi Road", "16", "8", "C2B", "unauthorized", 19.1766, 73.0116, "HIGH", "UNDER_REVIEW", "Eight floors built beyond approved plan.", "/images/buildings/ayesha-complex.jpg"),
            createBuilding("Bismillah Tower", "Retiwadi", "Retiwadi", "16", "12", "C2B", "unauthorized", 19.1767, 73.0117, "HIGH", "UNDER_REVIEW", "Twelve floors constructed without proper permits. Major violation.", "https://via.placeholder.com/400x300/ab47bc/ffffff?text=Bismillah+Tower"),
            createBuilding("Yasmeen Apartments", "Jeevan Bagh", "Jeevan Bagh", "16", "7", "C2B", "unauthorized", 19.1768, 73.0118, "HIGH", "UNDER_REVIEW", "Seven floors built beyond approved plan.", "/images/buildings/yasmeen-apartments.jpg"),
            createBuilding("Kareem Complex A & B", "Baida Colony", "Baida Colony, Mumbra", "16", "6", "C2B", "unauthorized", 19.1769, 73.0119, "MEDIUM", "UNDER_REVIEW", "Six floors constructed without proper authorization.", "/images/buildings/kareem-complex.jpg"),
            createBuilding("Heena Residency", "Naigaon", "Naigaon, Mumbra", "16", "9", "C2B", "unauthorized", 19.1770, 73.0120, "HIGH", "UNDER_REVIEW", "Nine floors built beyond approved plan.", "/images/buildings/heena-residency.jpg"),
            createBuilding("Noorani Manzil", "Mumbra Bazar Peth", "Mumbra Bazar Peth, Mumbra", "16", "5", "C2B", "unauthorized", 19.1771, 73.0121, "MEDIUM", "UNDER_REVIEW", "Five floors constructed without proper permits.", "/images/buildings/noorani-manzil.jpg"),
            createBuilding("Gulzar Apartments", "Jeevan Bagh", "Jeevan Bagh", "16", "7", "C2B", "unauthorized", 19.1772, 73.0122, "HIGH", "UNDER_REVIEW", "Seven floors built beyond approved plan.", "/images/buildings/gulzar-apartments.jpg"),
            createBuilding("Shaheen Tower", "Retiwadi", "Retiwadi", "16", "8", "C2B", "unauthorized", 19.1773, 73.0123, "HIGH", "UNDER_REVIEW", "Eight floors constructed without proper authorization.", "/images/buildings/shaheen-tower.jpg"),
            createBuilding("Fatima Complex", "Naigaon", "Naigaon, Mumbra", "16", "5", "C2B", "unauthorized", 19.1774, 73.0124, "MEDIUM", "UNDER_REVIEW", "Five floors built beyond approved plan.", "/images/buildings/fatima-complex.jpg"),
            createBuilding("Sajid Manzil", "Jeevan Bagh", "Jeevan Bagh", "16", "7", "C2B", "unauthorized", 19.1775, 73.0125, "HIGH", "UNDER_REVIEW", "Seven floors constructed without proper permits.", "/images/buildings/sajid-manzil.jpg"),
            createBuilding("Imran Palace", "Retiwadi", "Retiwadi", "16", "6", "C2B", "unauthorized", 19.1776, 73.0126, "MEDIUM", "UNDER_REVIEW", "Six floors built beyond approved plan.", "/images/buildings/imran-palace.jpg"),
            createBuilding("Al-Barkat Apartments", "Baida Colony", "Baida Colony, Mumbra", "16", "8", "C2B", "unauthorized", 19.1777, 73.0127, "HIGH", "UNDER_REVIEW", "Eight floors constructed without proper authorization.", "/images/buildings/al-barkat.jpg"),
            createBuilding("Yasir Residency", "Aashiyana Theater Complex", "Aashiyana Theater Complex, Mumbra", "16", "7", "C2B", "unauthorized", 19.1778, 73.0128, "HIGH", "UNDER_REVIEW", "Seven floors built beyond approved plan.", "/images/buildings/yasir-residency.jpg"),
            createBuilding("Hoor Manzil", "Mumbra Devi Road", "Mumbra Devi Road", "16", "6", "C2B", "unauthorized", 19.1779, 73.0129, "MEDIUM", "UNDER_REVIEW", "Six floors constructed without proper permits.", "/images/buildings/hoor-manzil.jpg"),
            createBuilding("Aman Tower", "Naigaon", "Naigaon, Mumbra", "16", "12", "C2B", "unauthorized", 19.1780, 73.0130, "HIGH", "UNDER_REVIEW", "Twelve floors built beyond approved plan. Major safety violation.", "https://via.placeholder.com/400x300/ff7043/ffffff?text=Aman+Tower")
        );

        buildingRepository.saveAll(buildings);
        System.out.println("Initialized " + buildings.size() + " illegal buildings in the database");
    }

    private Building createBuilding(String name, String address, String location, String wardNumber, 
                                  String floors, String category, String violationType, 
                                  double latitude, double longitude, String severity, String status, 
                                  String description, String imageUrl) {
        Building building = new Building();
        building.setName(name);
        building.setAddress(address);
        building.setLocation(location);
        building.setWardNumber(wardNumber);
        building.setFloors(floors);
        building.setCategory(category);
        building.setViolationType(violationType);
        building.setLatitude(latitude);
        building.setLongitude(longitude);
        building.setSeverity(severity);
        building.setStatus(status);
        building.setDescription(description);
        building.setImageUrl(imageUrl);
        building.setReportedDate(LocalDateTime.now().minusDays((long) (Math.random() * 365)));
        return building;
    }
}
