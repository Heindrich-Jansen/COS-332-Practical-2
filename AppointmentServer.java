import java.net.*;
import java.io.*;
import java.util.*;

public class AppointmentServer {

    private static final int PORT = 5000;
    private static final String FILE_NAME = "appointments.txt";
    private static List<String> appointments = new ArrayList<>();

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
            out.println("=== Appointment Manager ===");

            boolean running = true;

            while (running) {

                out.println("\n1. Add Appointment");
                out.println("2. View Appointments");
                out.println("3. Delete Appointment");
                out.println("4. Exit");
                out.print("Choice: ");
                out.flush();

                String choice = readAndEcho(in, out);

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

        out.print("Date (YYYY-MM-DD): ");
        out.flush();
        String date = readAndEcho(in, out);

        out.print("Time (HH:MM): ");
        out.flush();
        String time = readAndEcho(in, out);

        out.print("With whom: ");
        out.flush();
        String person = readAndEcho(in, out);

        out.print("Description: ");
        out.flush();
        String desc = readAndEcho(in, out);

        String entry = date + "|" + time + "|" + person + "|" + desc;
        appointments.add(entry);
        saveToFile();

        out.println("Appointment added.");
    }

    private static void viewAppointments(PrintWriter out) {

        out.println("\n--- Appointments ---");

        if (appointments.isEmpty()) {
            out.println("No appointments found.");
        }
        else {
            for (int i = 0; i < appointments.size(); i++) {
                out.println((i + 1) + ". " + appointments.get(i));
            }
        }
    }

    private static void deleteAppointment(BufferedReader in, PrintWriter out)
            throws IOException {

        viewAppointments(out);

        if (appointments.isEmpty())
            return;

        out.print("Enter number to delete: ");
        out.flush();
        String input = readAndEcho(in, out);

        try {
            int index = Integer.parseInt(input) - 1;

            if (index >= 0 && index < appointments.size()) {
                appointments.remove(index);
                saveToFile();
                out.println("Deleted.");
            }
            else {
                out.println("Invalid number.");
            }
        }
        catch (NumberFormatException e) {
            out.println("Invalid input.");
        }
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
        out.print("\033[2J");
        out.print("\033[H");
        out.flush();
    }
}