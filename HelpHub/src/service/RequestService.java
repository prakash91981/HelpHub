package service;

import exceptions.InvalidRequestException;
import model.*;
import repository.FileRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestService {
    private final Map<Integer, HelpRequest> requests = new HashMap<Integer, HelpRequest>();
    private final Map<Integer, Volunteer> volunteers = new HashMap<Integer, Volunteer>();
    private final FileRepository repository;
    private final AtomicInteger nextRequestId = new AtomicInteger(1);
    private final AtomicInteger nextVolunteerId = new AtomicInteger(1);
    private final MatchingService matchingService = new MatchingService();

    public RequestService(FileRepository repository) throws IOException {
        this.repository = repository;
        for (HelpRequest request : repository.loadRequests()) {
            requests.put(request.getId(), request);
            nextRequestId.set(Math.max(nextRequestId.get(), request.getId() + 1));
        }
    }

    public synchronized Volunteer addVolunteer(String name, String area, Set<String> skills,
                                               boolean available, int experience) {
        Volunteer v = new Volunteer(nextVolunteerId.getAndIncrement(), name, area, skills, available, experience);
        volunteers.put(v.getId(), v);
        return v;
    }

    public synchronized HelpRequest createRequest(int requesterId, String description, Category category,
            Priority priority, LocalDate requiredBy, String area, int quantity, String details)
            throws InvalidRequestException {
        try {
            int id = nextRequestId.getAndIncrement();
            HelpRequest r;
            switch (category) {
                case BOOK: r = new BookRequest(id, requesterId, description, priority, requiredBy, area, quantity, details); break;
                case STATIONERY: r = new StationeryRequest(id, requesterId, description, priority, requiredBy, area, quantity, details); break;
                case TUTORING: r = new TutoringRequest(id, requesterId, description, priority, requiredBy, area, quantity, details); break;
                case VOLUNTEER: r = new VolunteerTaskRequest(id, requesterId, description, priority, requiredBy, area, quantity, details); break;
                case CIVIC: r = new CivicRequest(id, requesterId, description, priority, requiredBy, area, quantity, details); break;
                case FOOD: r = new FoodRequest(id, requesterId, description, priority, requiredBy, area, quantity, details); break;
                default: throw new InvalidRequestException("Unsupported category.");
            }
            requests.put(id, r);
            persist();
            record(r.getId(), "CREATED", 0);
            return r;
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException(ex.getMessage());
        } catch (IOException ex) {
            throw new InvalidRequestException("Could not save request: " + ex.getMessage());
        }
    }

    public synchronized List<HelpRequest> getRequests() {
        List<HelpRequest> result = new ArrayList<HelpRequest>(requests.values());
        result.sort(Comparator.comparingInt(HelpRequest::getId));
        return result;
    }

    public synchronized List<HelpRequest> getOpenRequests() {
        List<HelpRequest> result = new ArrayList<HelpRequest>();
        for (HelpRequest r : requests.values()) if (r.getStatus() == RequestStatus.OPEN) result.add(r);
        result.sort(Comparator.comparing(HelpRequest::getRequiredBy));
        return result;
    }

    public synchronized List<Volunteer> getVolunteers() {
        return new ArrayList<Volunteer>(volunteers.values());
    }

    public synchronized HelpRequest getRequest(int id) {
        return requests.get(id);
    }

    public synchronized Volunteer getVolunteer(int id) {
        return volunteers.get(id);
    }

    public List<MatchingService.Match> findMatches(int requestId) throws InvalidRequestException {
        HelpRequest r = getRequest(requestId);
        if (r == null) throw new InvalidRequestException("Request ID not found.");
        return matchingService.findMatches(r, getVolunteers());
    }

    public synchronized boolean claimRequest(int requestId, int volunteerId) throws InvalidRequestException {
        HelpRequest r = requests.get(requestId);
        Volunteer v = volunteers.get(volunteerId);
        if (r == null) throw new InvalidRequestException("Request ID not found.");
        if (v == null) throw new InvalidRequestException("Volunteer ID not found.");
        if (!v.isAvailable()) throw new InvalidRequestException("Volunteer is not available.");
        if (!r.claim(volunteerId)) return false;
        try {
            persist();
            record(requestId, "CLAIMED_BY_" + volunteerId, 0);
            return true;
        } catch (IOException ex) {
            r.cancel(); // fail closed if persistence fails; in-memory state will not remain claimable
            throw new InvalidRequestException("Could not save claim: " + ex.getMessage());
        }
    }

    public synchronized boolean completeRequest(int requestId, int hours) throws InvalidRequestException {
        HelpRequest r = requests.get(requestId);
        if (r == null) throw new InvalidRequestException("Request ID not found.");
        if (hours < 0) throw new InvalidRequestException("Hours cannot be negative.");
        if (!r.complete()) return false;
        try {
            persist();
            record(requestId, "COMPLETED", hours);
            return true;
        } catch (IOException ex) {
            throw new InvalidRequestException("Could not save completion: " + ex.getMessage());
        }
    }

    public synchronized boolean cancelRequest(int requestId) throws InvalidRequestException {
        HelpRequest r = requests.get(requestId);
        if (r == null) throw new InvalidRequestException("Request ID not found.");
        if (!r.cancel()) return false;
        try {
            persist();
            record(requestId, "CANCELLED", 0);
            return true;
        } catch (IOException ex) {
            throw new InvalidRequestException("Could not save cancellation: " + ex.getMessage());
        }
    }

    public synchronized String impactReport() throws IOException {
        return new ImpactService().createReport(requests.values(), repository.loadActivityLines());
    }

    private void persist() throws IOException {
        repository.saveRequests(requests.values());
    }

    private void record(int requestId, String action, int hours) throws IOException {
        repository.appendActivity(new ActivityRecord(requestId, action, hours));
    }
}
