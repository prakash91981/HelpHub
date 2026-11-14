package app;

import concurrency.ClaimRaceDemo;
import exceptions.InvalidRequestException;
import model.*;
import service.MatchingService;
import service.RequestService;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/**
 * HelpHub desktop interface built with Java AWT.
 * Business rules and CSV persistence remain in RequestService/FileRepository.
 */
public class HelpHubGUI extends Frame {
    private final RequestService service;
    private final CardLayout cardLayout = new CardLayout();
    private final Panel content = new Panel(cardLayout);
    private final java.awt.List requestList = new java.awt.List(14, false);
    private final java.awt.List volunteerList = new java.awt.List(14, false);
    private final TextArea reportArea = new TextArea("", 12, 70, TextArea.SCROLLBARS_VERTICAL_ONLY);
    private final Label totalValue = new Label("0");
    private final Label openValue = new Label("0");
    private final Label volunteerValue = new Label("0");
    private final Label completedValue = new Label("0");

    private static final Color NAVY = new Color(27, 47, 78);
    private static final Color BLUE = new Color(44, 105, 173);
    private static final Color BACKGROUND = new Color(245, 247, 250);

    public HelpHubGUI(RequestService service) {
        super("HelpHub | Community Help & Resource Share");
        this.service = service;
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        refreshAll();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private Panel buildHeader() {
        Panel header = new Panel(new BorderLayout());
        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(100, 76));
        Label brand = new Label("  HelpHub");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 25));
        Label subtitle = new Label("Community Help & Resource Share     ");
        subtitle.setForeground(new Color(220, 230, 242));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        header.add(brand, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private Panel buildSidebar() {
        Panel side = new Panel(new GridLayout(0, 1, 6, 8));
        side.setBackground(new Color(233, 239, 247));
        side.setPreferredSize(new Dimension(205, 100));
        side.setFont(new Font("SansSerif", Font.BOLD, 13));
        side.setLayout(new GridLayout(0, 1, 6, 8));
        side.add(navButton("Dashboard", "dashboard"));
        side.add(navButton("Help Requests", "requests"));
        side.add(navButton("Volunteers", "volunteers"));
        side.add(navButton("Impact Report", "impact"));
        side.add(navButton("Thread Demo", "threads"));
        side.add(navButton("Exit", "exit"));
        return side;
    }

    private Button navButton(String text, String action) {
        Button b = new Button(text);
        b.setBackground(Color.WHITE);
        b.setForeground(NAVY);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.addActionListener(e -> {
            if ("exit".equals(action)) {
                dispose();
                System.exit(0);
            } else if ("threads".equals(action)) {
                runThreadDemo();
            } else {
                if ("impact".equals(action)) refreshReport();
                if ("requests".equals(action)) refreshRequests();
                if ("volunteers".equals(action)) refreshVolunteers();
                cardLayout.show(content, action);
            }
        });
        return b;
    }

    private Panel buildContent() {
        content.setBackground(BACKGROUND);
        content.add(buildDashboard(), "dashboard");
        content.add(buildRequestsPage(), "requests");
        content.add(buildVolunteersPage(), "volunteers");
        content.add(buildImpactPage(), "impact");
        return content;
    }

    private Panel page(String title, String description) {
        Panel p = new Panel(new BorderLayout(12, 12));
        p.setBackground(BACKGROUND);
        p.setName(title);
        Panel heading = new Panel(new GridLayout(2, 1));
        heading.setBackground(BACKGROUND);
        Label t = new Label(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 24));
        t.setForeground(NAVY);
        Label d = new Label(description);
        d.setForeground(new Color(90, 103, 120));
        heading.add(t);
        heading.add(d);
        p.add(heading, BorderLayout.NORTH);
        return p;
    }

    private Panel buildDashboard() {
        Panel p = page("Dashboard", "A simple overview of community support activity.");
        Panel body = new Panel(new BorderLayout(12, 18));
        body.setBackground(BACKGROUND);
        Panel stats = new Panel(new GridLayout(1, 4, 12, 12));
        stats.setBackground(BACKGROUND);
        stats.add(statCard("Total requests", totalValue));
        stats.add(statCard("Open requests", openValue));
        stats.add(statCard("Volunteers", volunteerValue));
        stats.add(statCard("Completed", completedValue));
        Panel actions = new Panel(new GridLayout(2, 2, 12, 12));
        actions.setBackground(BACKGROUND);
        actions.add(actionCard("Create a help request", "Add a new community request.", e -> showCreateRequest()));
        actions.add(actionCard("Register volunteer", "Add skills and availability.", e -> showAddVolunteer()));
        actions.add(actionCard("View requests", "Review, claim, complete or cancel.", e -> {
            refreshRequests(); cardLayout.show(content, "requests");
        }));
        actions.add(actionCard("Impact report", "See completed work and volunteer hours.", e -> {
            refreshReport(); cardLayout.show(content, "impact");
        }));
        body.add(stats, BorderLayout.NORTH);
        body.add(actions, BorderLayout.CENTER);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private Panel statCard(String caption, Label value) {
        Panel card = new Panel(new GridLayout(2, 1, 4, 4));
        card.setBackground(Color.WHITE);
        Label c = new Label(caption, Label.CENTER);
        c.setForeground(new Color(85, 99, 117));
        value.setAlignment(Label.CENTER);
        value.setFont(new Font("SansSerif", Font.BOLD, 28));
        value.setForeground(BLUE);
        card.add(c);
        card.add(value);
        return card;
    }

    private Panel actionCard(String title, String description, ActionListener listener) {
        Panel card = new Panel(new GridLayout(3, 1, 4, 4));
        card.setBackground(Color.WHITE);
        Label t = new Label(title, Label.CENTER);
        t.setFont(new Font("SansSerif", Font.BOLD, 15));
        Label d = new Label(description, Label.CENTER);
        d.setForeground(new Color(90, 103, 120));
        Button b = new Button("Open");
        b.setBackground(BLUE);
        b.setForeground(Color.WHITE);
        b.addActionListener(listener);
        card.add(t); card.add(d); card.add(b);
        return card;
    }

    private Panel buildRequestsPage() {
        Panel p = page("Help Requests", "Create and manage requests submitted by community members.");
        Panel center = new Panel(new BorderLayout(8, 8));
        center.setBackground(BACKGROUND);
        requestList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        center.add(requestList, BorderLayout.CENTER);
        Panel buttons = new Panel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(BACKGROUND);
        buttons.add(button("Create request", e -> showCreateRequest()));
        buttons.add(button("Refresh", e -> refreshRequests()));
        buttons.add(button("Find matches", e -> showMatches()));
        buttons.add(button("Claim selected", e -> claimSelected()));
        buttons.add(button("Complete selected", e -> completeSelected()));
        buttons.add(button("Cancel selected", e -> cancelSelected()));
        center.add(buttons, BorderLayout.SOUTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private Panel buildVolunteersPage() {
        Panel p = page("Volunteers", "Register volunteers and review their skills and availability.");
        Panel center = new Panel(new BorderLayout(8, 8));
        center.setBackground(BACKGROUND);
        volunteerList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        center.add(volunteerList, BorderLayout.CENTER);
        Panel buttons = new Panel(new FlowLayout(FlowLayout.LEFT));
        buttons.setBackground(BACKGROUND);
        buttons.add(button("Register volunteer", e -> showAddVolunteer()));
        buttons.add(button("Refresh", e -> refreshVolunteers()));
        center.add(buttons, BorderLayout.SOUTH);
        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private Panel buildImpactPage() {
        Panel p = page("Community Impact", "A summary generated from request records and activity history.");
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        p.add(reportArea, BorderLayout.CENTER);
        Panel bottom = new Panel(new FlowLayout(FlowLayout.LEFT));
        bottom.setBackground(BACKGROUND);
        bottom.add(button("Refresh report", e -> refreshReport()));
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private Button button(String text, ActionListener listener) {
        Button b = new Button(text);
        b.setBackground(BLUE);
        b.setForeground(Color.WHITE);
        b.addActionListener(listener);
        return b;
    }

    private void refreshAll() {
        refreshDashboard();
        refreshRequests();
        refreshVolunteers();
        refreshReport();
    }

    private void refreshDashboard() {
        List<HelpRequest> all = service.getRequests();
        totalValue.setText(String.valueOf(all.size()));
        openValue.setText(String.valueOf(countStatus(all, RequestStatus.OPEN)));
        completedValue.setText(String.valueOf(countStatus(all, RequestStatus.COMPLETED)));
        volunteerValue.setText(String.valueOf(service.getVolunteers().size()));
    }

    private long countStatus(List<HelpRequest> list, RequestStatus status) {
        return list.stream().filter(r -> r.getStatus() == status).count();
    }

    private void refreshRequests() {
        requestList.removeAll();
        for (HelpRequest r : service.getRequests()) {
            requestList.add(String.format("#%-4d %-10s %-8s %-11s %-12s qty=%-3d %s",
                    r.getId(), r.getCategory(), r.getPriority(), r.getStatus(),
                    r.getRequiredBy(), r.getQuantity(), r.getDescription()));
        }
        refreshDashboard();
    }

    private void refreshVolunteers() {
        volunteerList.removeAll();
        for (Volunteer v : service.getVolunteers()) {
            volunteerList.add(String.format("ID %-4d %-20s area=%-15s skills=%s available=%s exp=%d",
                    v.getId(), v.getName(), v.getArea(), v.getSkills(),
                    v.isAvailable(), v.getExperiencePoints()));
        }
        refreshDashboard();
    }

    private void refreshReport() {
        try { reportArea.setText(service.impactReport()); }
        catch (IOException ex) { showError("Could not load impact report: " + ex.getMessage()); }
    }

    private void showCreateRequest() {
        Dialog dialog = new Dialog(this, "Create Help Request", true);
        dialog.setLayout(new BorderLayout(8, 8));
        Panel fields = new Panel(new GridLayout(0, 2, 8, 7));
        TextField requester = new TextField();
        TextField description = new TextField();
        Choice category = new Choice();
        for (Category c : Category.values()) category.add(c.name());
        Choice priority = new Choice();
        for (Priority v : Priority.values()) priority.add(v.name());
        TextField date = new TextField(LocalDate.now().plusDays(7).toString());
        TextField area = new TextField();
        TextField quantity = new TextField("1");
        TextField details = new TextField();
        addField(fields, "Requester/member ID", requester);
        addField(fields, "Category", category);
        addField(fields, "Description", description);
        addField(fields, "Priority", priority);
        addField(fields, "Required by (YYYY-MM-DD)", date);
        addField(fields, "Area", area);
        addField(fields, "Quantity", quantity);
        addField(fields, "Category-specific details", details);
        dialog.add(fields, BorderLayout.CENTER);
        Panel controls = new Panel(new FlowLayout(FlowLayout.RIGHT));
        Button save = new Button("Create");
        Button cancel = new Button("Cancel");
        controls.add(save); controls.add(cancel);
        dialog.add(controls, BorderLayout.SOUTH);
        save.addActionListener(e -> {
            try {
                HelpRequest created = service.createRequest(
                        Integer.parseInt(requester.getText().trim()),
                        description.getText(), Category.valueOf(category.getSelectedItem()),
                        Priority.valueOf(priority.getSelectedItem()),
                        LocalDate.parse(date.getText().trim()), area.getText(),
                        Integer.parseInt(quantity.getText().trim()), details.getText());
                dialog.dispose();
                refreshAll();
                showInfo("Request #" + created.getId() + " created successfully.");
            } catch (Exception ex) { showError("Could not create request: " + ex.getMessage()); }
        });
        cancel.addActionListener(e -> dialog.dispose());
        showDialog(dialog, 560, 390);
    }

    private void showAddVolunteer() {
        Dialog dialog = new Dialog(this, "Register Volunteer", true);
        dialog.setLayout(new BorderLayout(8, 8));
        Panel fields = new Panel(new GridLayout(0, 2, 8, 7));
        TextField name = new TextField();
        TextField area = new TextField();
        TextField skills = new TextField("general");
        Checkbox available = new Checkbox("Available now", true);
        TextField experience = new TextField("0");
        addField(fields, "Volunteer name", name);
        addField(fields, "Area / neighbourhood", area);
        addField(fields, "Skills (comma-separated)", skills);
        addField(fields, "Availability", available);
        addField(fields, "Experience points (0-15)", experience);
        dialog.add(fields, BorderLayout.CENTER);
        Panel controls = new Panel(new FlowLayout(FlowLayout.RIGHT));
        Button save = new Button("Register");
        Button cancel = new Button("Cancel");
        controls.add(save); controls.add(cancel);
        dialog.add(controls, BorderLayout.SOUTH);
        save.addActionListener(e -> {
            try {
                Set<String> skillSet = new HashSet<String>();
                for (String s : skills.getText().split(",")) if (!s.trim().isEmpty()) skillSet.add(s.trim());
                int exp = Integer.parseInt(experience.getText().trim());
                if (exp < 0 || exp > 15) throw new IllegalArgumentException("Experience must be from 0 to 15.");
                Volunteer v = service.addVolunteer(name.getText(), area.getText(), skillSet, available.getState(), exp);
                dialog.dispose();
                refreshAll();
                showInfo("Volunteer registered with ID " + v.getId() + ".");
            } catch (Exception ex) { showError("Could not register volunteer: " + ex.getMessage()); }
        });
        cancel.addActionListener(e -> dialog.dispose());
        showDialog(dialog, 500, 330);
    }

    private void addField(Panel panel, String label, Component input) {
        panel.add(new Label(label));
        panel.add(input);
    }

    private void showMatches() {
        String value = ask("Enter request ID to find matching volunteers:");
        if (value == null) return;
        try {
            List<MatchingService.Match> matches = service.findMatches(Integer.parseInt(value.trim()));
            StringBuilder out = new StringBuilder("Suggested volunteers (highest score first):\n\n");
            if (matches.isEmpty()) out.append("No suitable available volunteers found.");
            for (MatchingService.Match m : matches) out.append(m).append('\n');
            showInfo(out.toString());
        } catch (Exception ex) { showError("Could not find matches: " + ex.getMessage()); }
    }

    private void claimSelected() {
        HelpRequest r = selectedRequest();
        if (r == null) return;
        List<Volunteer> volunteers = service.getVolunteers();
        if (volunteers.isEmpty()) { showError("Register a volunteer first."); return; }
        Choice choice = new Choice();
        for (Volunteer v : volunteers) choice.add(v.getId() + " - " + v.getName());
        Dialog d = new Dialog(this, "Choose Volunteer", true);
        d.setLayout(new BorderLayout(8, 8));
        d.add(new Label("Select a volunteer to claim request #" + r.getId() + ":"), BorderLayout.NORTH);
        d.add(choice, BorderLayout.CENTER);
        Panel controls = new Panel();
        Button claim = new Button("Claim");
        Button cancel = new Button("Cancel");
        controls.add(claim); controls.add(cancel);
        d.add(controls, BorderLayout.SOUTH);
        claim.addActionListener(e -> {
            try {
                int id = Integer.parseInt(choice.getSelectedItem().split(" - ")[0]);
                boolean ok = service.claimRequest(r.getId(), id);
                d.dispose();
                refreshAll();
                showInfo(ok ? "Request claimed successfully." : "This request is no longer open.");
            } catch (Exception ex) { showError("Claim failed: " + ex.getMessage()); }
        });
        cancel.addActionListener(e -> d.dispose());
        showDialog(d, 420, 160);
    }

    private void completeSelected() {
        HelpRequest r = selectedRequest();
        if (r == null) return;
        String value = ask("Volunteer hours to record (0 or more):");
        if (value == null) return;
        try {
            boolean ok = service.completeRequest(r.getId(), Integer.parseInt(value.trim()));
            refreshAll();
            showInfo(ok ? "Request marked COMPLETED." : "Only a CLAIMED request can be completed.");
        } catch (Exception ex) { showError("Could not complete request: " + ex.getMessage()); }
    }

    private void cancelSelected() {
        HelpRequest r = selectedRequest();
        if (r == null) return;
        if (!confirm("Cancel request #" + r.getId() + "?")) return;
        try {
            boolean ok = service.cancelRequest(r.getId());
            refreshAll();
            showInfo(ok ? "Request cancelled." : "This request cannot be cancelled.");
        } catch (Exception ex) { showError("Could not cancel request: " + ex.getMessage()); }
    }

    private HelpRequest selectedRequest() {
        int index = requestList.getSelectedIndex();
        if (index < 0) { showError("Select a request from the list first."); return null; }
        List<HelpRequest> all = service.getRequests();
        if (index >= all.size()) return null;
        return all.get(index);
    }

    private void runThreadDemo() {
        Thread worker = new Thread(() -> {
            try {
                ClaimRaceDemo.run();
                EventQueue.invokeLater(() -> showInfo("Thread demonstration finished.\nCheck the terminal/console for the two claim results."));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                EventQueue.invokeLater(() -> showError("Thread demonstration was interrupted."));
            }
        });
        worker.start();
    }

    private String ask(String prompt) {
        Dialog d = new Dialog(this, "HelpHub", true);
        d.setLayout(new BorderLayout(8, 8));
        TextField field = new TextField();
        d.add(new Label(prompt), BorderLayout.NORTH);
        d.add(field, BorderLayout.CENTER);
        Panel buttons = new Panel();
        Button ok = new Button("OK");
        Button cancel = new Button("Cancel");
        buttons.add(ok); buttons.add(cancel);
        d.add(buttons, BorderLayout.SOUTH);
        final String[] result = new String[1];
        ok.addActionListener(e -> { result[0] = field.getText(); d.dispose(); });
        cancel.addActionListener(e -> d.dispose());
        showDialog(d, 430, 150);
        return result[0];
    }

    private boolean confirm(String message) {
        return MessageBox.confirm(this, message);
    }

    private void showInfo(String message) {
        MessageBox.show(this, "HelpHub", message, false);
    }

    private void showError(String message) {
        MessageBox.show(this, "HelpHub - Error", message, true);
    }

    private void showDialog(Dialog dialog, int width, int height) {
        dialog.setSize(width, height);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Small AWT-only modal message helper. */
    private static class MessageBox {
        static void show(Frame owner, String title, String message, boolean error) {
            Dialog d = new Dialog(owner, title, true);
            d.setLayout(new BorderLayout(10, 10));
            TextArea text = new TextArea(message, 6, 48, TextArea.SCROLLBARS_VERTICAL_ONLY);
            text.setEditable(false);
            d.add(text, BorderLayout.CENTER);
            Button ok = new Button("OK");
            Panel p = new Panel();
            p.add(ok);
            d.add(p, BorderLayout.SOUTH);
            ok.addActionListener(e -> d.dispose());
            d.setSize(500, 220);
            d.setLocationRelativeTo(owner);
            d.setVisible(true);
        }

        static boolean confirm(Frame owner, String message) {
            Dialog d = new Dialog(owner, "Confirm", true);
            d.setLayout(new BorderLayout(8, 8));
            d.add(new Label(message, Label.CENTER), BorderLayout.CENTER);
            Panel p = new Panel();
            Button yes = new Button("Yes");
            Button no = new Button("No");
            p.add(yes); p.add(no);
            d.add(p, BorderLayout.SOUTH);
            final boolean[] answer = {false};
            yes.addActionListener(e -> { answer[0] = true; d.dispose(); });
            no.addActionListener(e -> d.dispose());
            d.setSize(350, 130);
            d.setLocationRelativeTo(owner);
            d.setVisible(true);
            return answer[0];
        }
    }
}
