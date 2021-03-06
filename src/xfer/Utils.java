package xfer;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class Utils {

    public static List<String> parse(String command) {
        List<String> parsed = new ArrayList<>();

        boolean raised = false;
        char previous = ' ';
        StringBuilder sb = new StringBuilder();

        for (Character c : command.toCharArray()) {
            switch (c) {
                case '\'':
                case '\"':
                    raised = !raised;
                    break;

                case ' ':
                    // Check to see if the string is raised
                    if (raised) {
                        sb.append(c);
                        continue;
                    }

                    // Check to see if the character was escaped
                    if (previous == '\\') {
                        sb.deleteCharAt(sb.length() - 1);
                        sb.append(c);
                        continue;
                    }

                    // Must be a real split, so deal with it
                    parsed.add(sb.toString());
                    sb = new StringBuilder();
                    break;

                default:
                    sb.append(c);
                    break;
            }

            // Update things
            previous = c;
        }

        if (raised) {
            System.err.println("Unclosed string");
            System.err.flush();
            return null;
        }

        if (sb.length() != 0) {
            parsed.add(sb.toString());
        }

        return parsed;
    }

    public static String listing(File dir) {
        if (dir == null || dir.listFiles() == null) {
            return "";
        }
        // Save data in a StringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append("listing" + "\n");

        // Print the file information
        for (File f : dir.listFiles()) {
            sb.append((f.isDirectory())? 'd' : '-');
            sb.append("  ");
            sb.append(f.getName());
            sb.append("\n");
        }

        // Remove the last newline and convert
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
