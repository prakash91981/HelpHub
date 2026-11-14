package repository;

import model.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FileRepository {
    private final Path dataDirectory;
    private final Path requestsFile;
    private final Path activitiesFile;

    public FileRepository(String directory) throws IOException {
        dataDirectory = Paths.get(directory);
        Files.createDirectories(dataDirectory);
        requestsFile = dataDirectory.resolve("requests.csv");
        activitiesFile = dataDirectory.resolve("activities.csv");
        if (!Files.exists(requestsFile)) Files.createFile(requestsFile);
        if (!Files.exists(activitiesFile)) Files.createFile(activitiesFile);
    }

    public synchronized void saveRequests(Collection<HelpRequest> requests) throws IOException {
        List<String> lines = requests.stream().map(HelpRequest::toCsv).collect(Collectors.toList());
        atomicWrite(requestsFile, lines);
    }

    public synchronized List<String> loadRequestLines() throws IOException {
        return Files.readAllLines(requestsFile, StandardCharsets.UTF_8);
    }

    public synchronized void appendActivity(ActivityRecord record) throws IOException {
        Files.write(activitiesFile, Collections.singletonList(record.toCsv()),
                StandardCharsets.UTF_8, StandardOpenOption.APPEND);
    }

    public synchronized List<String> loadActivityLines() throws IOException {
        return Files.readAllLines(activitiesFile, StandardCharsets.UTF_8);
    }

    private void atomicWrite(Path target, List<String> lines) throws IOException {
        Path temp = target.resolveSibling(target.getFileName().toString() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // CSV is intentionally simple: commas in user text are replaced before saving.
    public List<HelpRequest> loadRequests() throws IOException {
        List<HelpRequest> requests = new ArrayList<HelpRequest>();
        for (String line : loadRequestLines()) {
            if (line.trim().isEmpty()) continue;
            try {
                String[] p = line.split(",", -1);
                if (p.length != 11) throw new IllegalArgumentException("Expected 11 fields");
                int id = Integer.parseInt(p[0]);
                int requester = Integer.parseInt(p[1]);
                Category category = Category.valueOf(p[2]);
                Priority priority = Priority.valueOf(p[3]);
                LocalDate date = LocalDate.parse(p[4]);
                int quantity = Integer.parseInt(p[5]);
                RequestStatus status = RequestStatus.valueOf(p[6]);
                Integer volunteerId = p[7].isEmpty() ? null : Integer.valueOf(p[7]);
                String area = p[8], description = p[9], details = p[10];
                HelpRequest r;
                String specific = details.contains("=") ? details.substring(details.indexOf('=') + 1) : details;
                switch (category) {
                    case BOOK:
                        r = new BookRequest(id, requester, description, priority, date, area, quantity, specific); break;
                    case STATIONERY:
                        r = new StationeryRequest(id, requester, description, priority, date, area, quantity, specific); break;
                    case TUTORING:
                        r = new TutoringRequest(id, requester, description, priority, date, area, quantity, specific); break;
                    case VOLUNTEER:
                        r = new VolunteerTaskRequest(id, requester, description, priority, date, area, quantity, specific); break;
                    case CIVIC:
                        r = new CivicRequest(id, requester, description, priority, date, area, quantity, specific); break;
                    case FOOD:
                        r = new FoodRequest(id, requester, description, priority, date, area, quantity, specific); break;
                    default: throw new IllegalArgumentException("Unknown category");
                }
                // Restore state through permitted transitions.
                if (status == RequestStatus.CLAIMED && volunteerId != null) r.claim(volunteerId);
                else if (status == RequestStatus.COMPLETED) {
                    if (volunteerId != null) r.claim(volunteerId);
                    r.complete();
                } else if (status == RequestStatus.CANCELLED) r.cancel();
                requests.add(r);
            } catch (RuntimeException ex) {
                System.err.println("Skipping malformed request record: " + ex.getMessage());
            }
        }
        return requests;
    }
}
