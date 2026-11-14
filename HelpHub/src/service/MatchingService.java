package service;

import model.HelpRequest;
import model.Volunteer;
import java.util.*;
import java.util.stream.Collectors;

public class MatchingService {
    public static class Match {
        private final Volunteer volunteer;
        private final int score;
        public Match(Volunteer volunteer, int score) { this.volunteer = volunteer; this.score = score; }
        public Volunteer getVolunteer() { return volunteer; }
        public int getScore() { return score; }
        @Override public String toString() { return volunteer.getName() + " (ID " + volunteer.getId() + ") — score " + score + "/100"; }
    }

    public List<Match> findMatches(HelpRequest request, Collection<Volunteer> volunteers) {
        return volunteers.stream()
                .filter(Volunteer::isAvailable)
                .map(v -> new Match(v, score(request, v)))
                .filter(m -> m.getScore() >= 20)
                .sorted(Comparator.comparingInt(Match::getScore).reversed()
                        .thenComparing(m -> m.getVolunteer().getName()))
                .collect(Collectors.toList());
    }

    private int score(HelpRequest request, Volunteer volunteer) {
        int score = 0;
        String needed = request.getCategory().name().toLowerCase();
        if (volunteer.hasSkill(needed) || volunteer.hasSkill("general")) score += 40;
        if (volunteer.isAvailable()) score += 25;
        if (volunteer.getArea().equalsIgnoreCase(request.getArea())) score += 20;
        score += Math.min(15, volunteer.getExperiencePoints());
        return score;
    }
}
