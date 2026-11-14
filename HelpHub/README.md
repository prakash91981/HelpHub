# HelpHub — Java AWT GUI Social Impact Project

HelpHub is a coursework prototype for community help and resource sharing. It now opens as a **desktop graphical application built with Java AWT**. The original request, volunteer matching, CSV persistence, impact-reporting, and synchronized-claim business logic are retained.

## Requirements
- JDK 17 or newer recommended.
- No external libraries.
- A desktop environment capable of displaying AWT windows is required.

## Compile

Open Terminal and move into the `HelpHub` directory:

```bash
javac -d out $(find src -name "*.java")
```

On Windows PowerShell:

```powershell
Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -d out @sources.txt
```

## Run the GUI

macOS / Linux:

```bash
java -cp out app.Main
```

Windows:

```powershell
java -cp out app.Main
```

The application opens the HelpHub desktop window. Use the left navigation to move between Dashboard, Help Requests, Volunteers, and Impact Report.

## GUI features
- Dashboard cards for total requests, open requests, registered volunteers, and completed requests.
- Create help requests using AWT text fields and dropdown choices.
- Register volunteers with skills, area, availability, and experience points.
- View requests and volunteers.
- Find suggested volunteer matches.
- Claim a selected request for a volunteer.
- Complete or cancel a selected request.
- View the community impact report.
- Run the synchronized multithreading claim demonstration.

## AWT components demonstrated
`Frame`, `Panel`, `Label`, `Button`, `TextField`, `TextArea`, `Choice`, `Checkbox`, `Dialog`, `List`, `CardLayout`, `GridLayout`, `BorderLayout`, `FlowLayout`, `ActionListener`, and `WindowAdapter`.

## Existing logic retained
- Request categories: BOOK, STATIONERY, TUTORING, VOLUNTEER, CIVIC, FOOD.
- Volunteer matching score: skill 40, availability 25, area 20, experience 15.
- Request states: OPEN, CLAIMED, COMPLETED, CANCELLED.
- CSV persistence for requests and activities.
- Java Streams-based impact summary.
- Synchronized request claiming.

## Data files
The app creates `data/requests.csv` and `data/activities.csv` in the directory from which the program is launched. Volunteer records are held in memory in this coursework prototype and must be registered again after restarting the application.

## Notes
- The thread demonstration prints its two competing claim results to the terminal from which the app was launched.
- The project is a coursework prototype, not an emergency, medical, or official civic-services platform. Do not store real sensitive personal data in the demo.
