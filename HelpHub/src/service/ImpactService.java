package service;

import model.*;
import java.util.*;
import java.util.stream.Collectors;

public class ImpactService {
    public String createReport(Collection<HelpRequest> requests, List<String> activityLines) {
        long total = requests.size();
        long completed = requests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count();
        long pending = requests.stream().filter(r -> r.getStatus() == RequestStatus.OPEN).count();
        long claimed = requests.stream().filter(r -> r.getStatus() == RequestStatus.CLAIMED).count();
        long cancelled = requests.stream().filter(r -> r.getStatus() == RequestStatus.CANCELLED).count();
        Map<Category, Long> byCategory = requests.stream().collect(
                Collectors.groupingBy(HelpRequest::getCategory, Collectors.counting()));
        int hours = 0;
        for (String line : activityLines) {
            String[] p = line.split(",", -1);
            if (p.length >= 4) {
                try { hours += Integer.parseInt(p[3]); }
                catch (NumberFormatException ignored) { }
            }
        }
        return "\n=== COMMUNITY IMPACT REPORT ===\n" +
                "Total requests: " + total + "\nCompleted: " + completed +
                "\nOpen/pending: " + pending + "\nClaimed: " + claimed +
                "\nCancelled: " + cancelled + "\nRecorded volunteer hours: " + hours +
                "\nRequests by category: " + byCategory + "\n";
    }
}
