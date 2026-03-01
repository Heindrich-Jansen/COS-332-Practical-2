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
    //Colors for terminal output
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_BLUE = "\u001B[34m";

    public static void main(String[] args) throws IOException {

        loadFromFile();

        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server running on port " + PORT);
        String input = "";
        while (!input.equals("exit")) {
            Socket client = server.accept();
            handleClient(client);
        }
        server.close();
    }

    private static void handleClient(Socket socket) {

        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            clearScreen(out);

            boolean running = true;

            while (running) {
                numLines += 2;
                moveCursor(out, numLines, 0);
                out.write(ANSI_BLUE + "\n1. Add Appointment");
                moveCursor(out, ++numLines, 0);
                out.write(ANSI_BLUE + "\n2. View Appointments");
                moveCursor(out, ++numLines, 0);
                out.write(ANSI_BLUE + "\n3. Delete Appointment");
                moveCursor(out, ++numLines, 0);
                out.write(ANSI_BLUE + "\n4. Search Appointments");
                moveCursor(out, ++numLines, 0);
                out.write(ANSI_BLUE + "\n5. Exit");
                moveCursor(out, ++numLines, 0);
                out.write(ANSI_RESET + "\nChoice: ");
                out.flush();

                String choice = readAndEcho(in, out);

                if (!choice.matches("[1-5]")) {
                    clearScreen(out);
                    numLines += 1;
                    moveCursor(out, numLines, 0);
                    out.write(ANSI_RED + "Invalid option. Please enter 1-5.");
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
                    out.println(ANSI_RED + "Invalid option.");
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

        boolean valid = true;
        // DATE VALIDATION
        while (true) {
            clearScreen(out);
            if (!valid) {
                numLines += 1;
                moveCursor(out, numLines, 0);
                out.write(ANSI_RED + "Please enter a valid date. (enter quit to cancel)");
            }
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write(ANSI_RESET + "Date (YYYY-MM-DD): ");
            out.flush();
            date = readAndEcho(in, out);

            if (DATE_PATTERN.matcher(date).matches() && isValidDate(date))
                break;
            if (checkToCancel(date, out)) {
                return;
            }
            valid = false;
        }
        valid = true;
        // TIME VALIDATION
        while (true) {
            clearScreen(out);
            if (!valid) {
                numLines += 1;
                moveCursor(out, numLines, 0);
                out.write(ANSI_RED + "Please enter a valid time. (enter quit to cancel)");
            }
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write(ANSI_RESET + "Time (HH:MM): ");
            out.flush();
            time = readAndEcho(in, out);

            if (TIME_PATTERN.matcher(time).matches() && isValidTime(time, date))
                break;

            if (checkToCancel(time, out)) {
                return;
            }
            valid = false;
        }
        valid = true;
        // PERSON VALIDATION
        while (true) {
            clearScreen(out);
            if (!valid) {
                numLines += 1;
                moveCursor(out, numLines, 0);
                out.write(ANSI_RED + "Name cannot be empty. (enter quit to cancel)");
            }
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write(ANSI_RESET + "With whom: ");
            out.flush();
            person = readAndEcho(in, out);

            if (!person.trim().isEmpty())
                break;
            if (checkToCancel(person, out)) {
                return;
            }
            valid = false;
        }
        valid = true;
        // DESCRIPTION VALIDATION
        while (true) {
            clearScreen(out);
            if (!valid) {
                numLines += 1;
                moveCursor(out, numLines, 0);
                out.write(ANSI_RED + "Description cannot be empty. (enter quit to cancel)");
            }
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write(ANSI_RESET + "Description: ");
            out.flush();
            desc = readAndEcho(in, out);

            if (!desc.trim().isEmpty())
                break;
            if (checkToCancel(desc, out)) {
                return;
            }
            valid = false;
        }

        String entry = date + "|" + time + "|" + person + "|" + desc;
        appointments.add(entry);
        saveToFile();

        clearScreen(out);
        numLines += 1;
        moveCursor(out, numLines, 0);
        out.write(ANSI_GREEN + "Appointment added successfully.");
    }

    private static void viewAppointments(PrintWriter out) {
        numLines += 1;
        moveCursor(out, numLines, 33);
        out.write(ANSI_BLUE + "\n--- Appointments ---");
        numLines += 2;
        if (appointments.isEmpty()) {
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write(ANSI_RED + "No appointments found.");
        }
        else {
            for (int i = 0; i < appointments.size(); i++) {
                numLines += 1;
                moveCursor(out, numLines, 20);
                out.write(ANSI_RESET + (i + 1) + ". " + appointments.get(i));
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
        out.write(ANSI_RESET + "Enter number to delete: ");
        out.flush();
        String input = readAndEcho(in, out);

        try {
            if (!input.matches("\\d+")) {
                out.write(ANSI_RED + "Please enter a valid number.");
                out.flush();
                return;
            }

            int index = Integer.parseInt(input) - 1;

            if (index >= 0 && index < appointments.size()) {
                appointments.remove(index);
                saveToFile();
                out.write(ANSI_GREEN + "Deleted.");
            }
            else {
                out.write(ANSI_RED + "Invalid number.");
            }
        }
        catch (NumberFormatException e) {
            out.write(ANSI_RED + "Invalid input.");
        }
        out.flush();
    }

    private static void searchAppointments(BufferedReader in, PrintWriter out)
            throws IOException {

        clearScreen(out);
        numLines += 1;
        moveCursor(out, numLines, 0);
        out.print(ANSI_RESET + "Enter search term: ");
        out.flush();
        String term = readAndEcho(in, out);

        numLines += 2;
        moveCursor(out, numLines, 32);
        out.write(ANSI_BLUE + "--- Search Results ---");
        numLines += 1;
        int count = 0;
        Pattern pattern = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
        for (String entry : appointments) {
            if (pattern.matcher(entry).find()) {
                numLines += 1;
                moveCursor(out, numLines, 20);
                out.write(ANSI_RESET + entry);
                count++;
            }
        }

        if (count == 0) {
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write(ANSI_RED + "No matching appointments found.");
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
        numLines += 1;
        moveCursor(out, 0, 30);
        out.write(ANSI_RESET + "=== Appointment Manager ===");
    }

    private static void moveCursor(PrintWriter out, int row, int col) {
        out.write(27);
        out.write("[" + row + ";" + col + "H");
        out.flush();
    }

    //Only schedule appointments for future dates
    private static boolean isValidDate(String date) {
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        return new Date().compareTo(new Date(year - 1900, month - 1, day)) < 0;
    }

    //Search through file to check if time is already taken for that date
    private static boolean isValidTime(String time, String date) {
        for (String entry : appointments) {
            String[] parts = entry.split("\\|");
            if (parts[0].equals(date) && parts[1].equals(time)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkToCancel(String input, PrintWriter out) {
        if (input.equals("quit"))
        {
            clearScreen(out);
            numLines += 1;
            moveCursor(out, numLines, 0);
            out.write(ANSI_RED + "Appointment cancelled.");
            return true;
        }
        return false;
    }
}