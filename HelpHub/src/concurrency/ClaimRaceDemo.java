package concurrency;

import model.*;

public class ClaimRaceDemo {
    public static void run() throws InterruptedException {
        final HelpRequest request = new BookRequest(999, 1, "Thread demo request",
                Priority.HIGH, java.time.LocalDate.now().plusDays(1),
                "Demo Area", 1, "Demo");
        Runnable first = () -> System.out.println("Volunteer 101 claim result: " + request.claim(101));
        Runnable second = () -> System.out.println("Volunteer 102 claim result: " + request.claim(102));
        Thread t1 = new Thread(first, "Volunteer-101");
        Thread t2 = new Thread(second, "Volunteer-102");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("Final status: " + request.getStatus() +
                ", winning volunteer ID: " + request.getVolunteerId());
        System.out.println("Expected: exactly one claim returns true.");
    }
}
