package app;

import repository.FileRepository;
import service.RequestService;

import java.awt.EventQueue;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            final RequestService service = new RequestService(new FileRepository("data"));
            EventQueue.invokeLater(() -> new HelpHubGUI(service));
        } catch (IOException ex) {
            System.err.println("Could not initialize HelpHub data files: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
