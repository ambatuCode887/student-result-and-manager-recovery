package utility;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A low-level file I/O helper class.
 * <p>
 * Handles the raw bytes of reading and writing files. It ensures consistent
 * UTF-8 encoding and standardizes how headers are skipped or written across the application.
 * Declared 'final' with a private constructor to prevent instantiation.
 * </p>
 */
public final class FileStorage {
    private FileStorage() {}

    /**
     * Reads all lines from a file, strictly ignoring the first line (header).
     * @param path The file path to read.
     * @return A list of strings (lines), excluding the header.
     */
    public static List<String> readLinesSkipHeader(String path) {
        List<String> out = new ArrayList<>();
        Path p = Paths.get(path);
        if (!Files.exists(p))
            return out;
        try (BufferedReader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String header = r.readLine();
            String line;
            while ((line = r.readLine()) != null)
                out.add(line);
        } catch (IOException e) {
            System.err.println("readLinesSkipHeader failed: " + e.getMessage());
        }
        return out;
    }

    /**
     * Overwrites a file completely with new data.
     * <p>
     * Automatically creates parent directories if they don't exist.
     * Writes the provided header array first, followed by the data lines.
     * </p>
     * @param path The target file path.
     * @param lines The list of strings to write.
     * @param header The column headers to write at the top (can be null).
     * @return true if the write operation succeeded.
     */
    public static boolean writeLinesWithHeader(String path, List<String> lines, String[] header) {
        Path p = Paths.get(path);
        try {
            Files.createDirectories(p.getParent());
        } catch (Exception ignored) {
        }
        try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            if (header != null && header.length > 0) {
                w.write(String.join(",", header));
                w.newLine();
            }
            for (int i = 0; i < lines.size(); i++) {
                w.write(lines.get(i));
                if (i < lines.size() - 1)
                    w.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("writeLinesWithHeader failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Appends a single line to an existing file.
     * <p>
     * If the file does not exist, it creates it and writes the header first.
     * This ensures the file structure is always valid, even on the first write.
     * </p>
     * @param path The target file path.
     * @param line The data string to append.
     * @param headerIfMissing The header to write if creating a new file.
     * @return true if the append operation succeeded.
     */
    public static boolean appendLine(String path, String line, String headerIfMissing) {
        Path p = Paths.get(path);
        try {
            Files.createDirectories(p.getParent());
            if (!Files.exists(p)) {
                try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW)) {
                    if (headerIfMissing != null && !headerIfMissing.isBlank()) {
                        w.write(headerIfMissing);
                        w.newLine();
                    }
                    w.write(line);
                    w.newLine();
                    return true;
                }
            }
            try (BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                w.write(line);
                w.newLine();
                return true;
            }
        } catch (IOException e) {
            System.err.println("appendLine failed: " + e.getMessage());
            return false;
        }
    }
}