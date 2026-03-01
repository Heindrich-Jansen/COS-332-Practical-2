import java.net.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class AppointmentServer {

    private static final int PORT = 5000;
    private static int numLines = 0;
    private static final String FILE_NAME = "appointments.txt";
    private static List<String> appointments = new ArrayList<>();
    private static final Pattern DATE_PATTERN =
        Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private static final Pattern TIME_PATTERN =
        Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    public static void main(String[] args) throws IOException {

        loadFromFile();

        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server running on port " + PORT);

        while (true) {
            Socket client = server.accept();
            handleClient(client);
        }
    }

    private static void handleClient(Socket socket) {

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            clearScreen(out);
            numLines += 1;
            out.println("=== Appointment Manager ===");

            boolean running = true;

            while (running) {
                numLines += 2;
                moveCursor(out, numLines, 0);
                out.write("\n1. Add Appointment");
                moveCursor(out, ++numLines, 0);
                out.write("\n2. View Appointments");
                moveCursor(out, ++numLines, 0);
                out.write("\n3. Delete Appointment");
                moveCursor(out, ++numLines, 0);
                out.write("\n4. Search Appointments");
                moveCursor(out, ++numLines, 0);
                out.write("\n5. Exit");
                moveCursor(out, ++numLines, 0);
                out.write("\nChoice: ");
                out.flush();

                String choice = readAndEcho(in, out);

                if (!choice.matches("[1-5]")) {
                    clearScreen(out);
                    out.println("Invalid option. Please enter 1-5.");
                    continue;
                }

                clearScreen(out);
                if (choice.equals("1")) {
                    addAppointment(in, out);
                }
                else if (choice.equals("2")) {
                    viewAppointments(out);
                }
                else if (choice.equals("3")) {
                    deleteAppointment(in, out);
                }
                else if (choice.equals("4")) {
                    searchAppointments(in, out);
                }
                else if (choice.equals("5")) {
                    running = false;
                }
                else {
                    out.println("Invalid option.");
                }
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readAndEcho(BufferedReader in, PrintWriter out)
            throws IOException {

        String line = in.readLine();

        if (line == null) {
            return "";
        }

        out.println(line);
        return line;
    }

    private static void addAppointment(BufferedReader in, PrintWriter out)
            throws IOException {

        String date;
        String time;
        String person;
        String desc;

        // DATE VALIDATION
        while (true) {
            clearScreen(out);
            out.print("Date (YYYY-MM-DD): ");
            out.flush();
            date = readAndEcho(in, out);

            if (DATE_PATTERN.matcher(date).matches())
                break;

            out.println("Invalid date format. Try again.");
        }

        // TIME VALIDATION
        while (true) {
            clearScreen(out);
            out.print("Time (HH:MM): ");
            out.flush();
            time = readAndEcho(in, out);

            if (TIME_PATTERN.matcher(time).matches())
                break;

            out.println("Invalid time format. Use 24-hour format.");
        }

        // PERSON VALIDATION
        while (true) {
            clearScreen(out);
            out.print("With whom: ");
            out.flush();
            person = readAndEcho(in, out);

            if (!person.trim().isEmpty())
                break;

            out.println("Name cannot be empty.");
        }

        // DESCRIPTION VALIDATION
        while (true) {
            clearScreen(out);
            out.print("Description: ");
            out.flush();
            desc = readAndEcho(in, out);

            if (!desc.trim().isEmpty())
                break;

            out.println("Description cannot be empty.");
        }

        String entry = date + "|" + time + "|" + person + "|" + desc;
        appointments.add(entry);
        saveToFile();

        clearScreen(out);
        out.println("Appointment added successfully.");
    }

    private static void viewAppointments(PrintWriter out) {
        numLines += 1;
        out.write("\n--- Appointments ---");

        if (appointments.isEmpty()) {
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write("No appointments found.");
        }
        else {
            for (int i = 0; i < appointments.size(); i++) {
                numLines += 1;
                moveCursor(out, numLines, 0);
                out.write((i + 1) + ". " + appointments.get(i));
            }
        }
        out.flush();
    }

    private static void deleteAppointment(BufferedReader in, PrintWriter out)
            throws IOException {

        viewAppointments(out);

        if (appointments.isEmpty())
            return;
        numLines += 2;
        moveCursor(out, numLines, 0);
        out.write("Enter number to delete: ");
        out.flush();
        String input = readAndEcho(in, out);

        try {
            if (!input.matches("\\d+")) {
                out.write("Please enter a valid number.");
                out.flush();
                return;
            }

            int index = Integer.parseInt(input) - 1;

            if (index >= 0 && index < appointments.size()) {
                appointments.remove(index);
                saveToFile();
                out.write("Deleted.");
            }
            else {
                out.write("Invalid number.");
            }
        }
        catch (NumberFormatException e) {
            out.write("Invalid input.");
        }
        out.flush();
    }

    private static void searchAppointments(BufferedReader in, PrintWriter out)
            throws IOException {

        clearScreen(out);
        out.print("Enter search term: ");
        out.flush();
        String term = readAndEcho(in, out);

        numLines += 1;
        moveCursor(out, numLines, 0);
        out.write("\n--- Search Results ---");

        int count = 0;
        Pattern pattern = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
        for (String entry : appointments) {
            if (pattern.matcher(entry).find()) {
                numLines += 1;
                moveCursor(out, numLines, 0);
                out.write(entry);
                count++;
            }
        }

        if (count == 0) {
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write("No matching appointments found.");
        }
        out.flush();
    }

    private static void loadFromFile() {

        try {
            File file = new File(FILE_NAME);
            if (!file.exists())
                return;

            BufferedReader reader = new BufferedReader(
                    new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                appointments.add(line);
            }

            reader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveToFile() {

        try {
            PrintWriter writer = new PrintWriter(
                    new FileWriter(FILE_NAME));

            for (String entry : appointments) {
                writer.println(entry);
            }

            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void clearScreen(PrintWriter out) {
        numLines = 0;
        out.print("\033[2J");
        out.print("\033[H");
        out.flush();
    }

    private static void moveCursor(PrintWriter out, int row, int col) {
        out.write(27);
        out.write("[" + row + ";" + col + "H");
        out.flush();
    }
}