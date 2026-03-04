package utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A static utility class for parsing delimited text files (CSV, Pipe).
 * <p>
 * This class reads text files and converts each line into a Key-Value Map,
 * where the Keys are the column headers and the Values are the row data.
 * This generic approach allows the Repository to handle any file structure dynamically.
 * </p>
 */
public class TextDataReader {

    /**
     * Reads a standard comma-separated (CSV) file.
     * @param filePath The relative or absolute path to the file.
     * @return A list of Maps, where each Map represents one row of data.
     * @throws IOException If the file cannot be found or read.
     */
    public static List<Map<String, String>> readData(String filePath) throws IOException {
        return readData(filePath, ",");
    }

    /**
     * Reads a text file using a custom delimiter (e.g., Pipe "|").
     * <p>
     * Useful for files that might contain commas within the data fields (like Task Descriptions).
     * Automatically handles UTF-8 BOM characters to prevent parsing errors.
     * </p>
     * @param filePath The path to the file.
     * @param delimiter The character used to separate columns (regex escaped).
     * @return A list of Maps containing the parsed data.
     * @throws IOException If file access fails.
     */
    public static List<Map<String, String>> readData(String filePath, String delimiter) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        
        // Escape the delimiter to treat it as a literal string in Regex (e.g., "|" becomes "\\|")
        String splitRegex = "\\Q" + delimiter + "\\E"; 
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) return rows;
            // Remove Byte Order Mark (BOM) if present at start of UTF-8 files
            headerLine = headerLine.replace("\uFEFF", "");

            String[] headers = headerLine.split(splitRegex, -1);
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] cols = line.split(splitRegex, -1);
                Map<String, String> map = new HashMap<>();
                
                for (int i = 0; i < headers.length; i++) {
                    if (i < cols.length) {
                        map.put(headers[i], cols[i].trim());
                    } else {
                        map.put(headers[i], "");
                    }
                }
                rows.add(map);
            }
        }
        return rows;
    }
}