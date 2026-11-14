package model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Volunteer extends User {
    private final Set<String> skills;
    private final boolean available;
    private final int experiencePoints;

    public Volunteer(int id, String name, String area, Set<String> skills,
                     boolean available, int experiencePoints) {
        super(id, name, area);
        if (experiencePoints < 0) {
            throw new IllegalArgumentException("Experience points cannot be negative.");
        }
        this.skills = new HashSet<String>();
        if (skills != null) {
            for (String skill : skills) {
                if (skill != null && !skill.trim().isEmpty()) {
                    this.skills.add(skill.trim().toLowerCase());
                }
            }
        }
        this.available = available;
        this.experiencePoints = experiencePoints;
    }

    public Set<String> getSkills() {
        return Collections.unmodifiableSet(skills);
    }
    public boolean isAvailable() { return available; }
    public int getExperiencePoints() { return experiencePoints; }

    public boolean hasSkill(String skill) {
        return skills.contains(skill.toLowerCase().trim());
    }

    @Override
    public String toString() {
        return "Volunteer{id=" + getId() + ", name='" + getName() +
                "', area='" + getArea() + "', skills=" + skills +
                ", available=" + available + ", experience=" + experiencePoints + "}";
    }
}
