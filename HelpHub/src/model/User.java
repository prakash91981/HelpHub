package model;

import java.util.Objects;

public class User {
    private final int id;
    private final String name;
    private final String area;

    public User(int id, String name, String area) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (area == null || area.trim().isEmpty()) {
            throw new IllegalArgumentException("Area cannot be empty.");
        }
        this.id = id;
        this.name = name.trim();
        this.area = area.trim();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getArea() { return area; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof User)) return false;
        User user = (User) other;
        return id == user.id;
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', area='" + area + "'}";
    }
}
