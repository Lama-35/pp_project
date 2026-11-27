import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All input/output for the Sudoku project: reading puzzles, validating the
 * file format, printing and saving solutions, and listing puzzle files.
 *
 * It only talks to SudokuBoard (get / set / isValidMove / toString) and never
 * to a solver, so IO stays separate from the algorithms.
 *
 * FILE FORMAT: 9 lines, 9 characters each. '0' or '.' = empty cell, '1'-'9' = given.
 * Spaces and blank lines are ignored.
 */
public class SudokuIO {

    private SudokuIO() { } // static utility class, no objects

    // ---------- Reading ----------

    /** Loads a puzzle from a text file. */
    public static SudokuBoard loadFromFile(String path) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            throw new IOException("Puzzle file not found: " + path);
        }
        return parse(Files.readAllLines(p, StandardCharsets.UTF_8));
    }

    public static SudokuBoard loadFromFile(Path path) throws IOException {
        return loadFromFile(path.toString());
    }

    /** Loads every .txt puzzle in a folder (sorted by file name). */
    public static List<SudokuBoard> loadAll(String dir) throws IOException {
        List<SudokuBoard> boards = new ArrayList<>();
        for (Path p : listPuzzleFiles(dir)) {
            boards.add(loadFromFile(p));
        }
        return boards;
    }

    /** Parses text lines into a board; throws IllegalArgumentException with a clear message if invalid. */
    public static SudokuBoard parse(List<String> rawLines) {
        if (rawLines == null) {
            throw new IllegalArgumentException("Puzzle text is null.");
        }
        List<String> lines = new ArrayList<>();
        for (String l : rawLines) {
            String t = l.replace("\uFEFF", "").replaceAll("\\s+", ""); // BOM + spaces
            if (!t.isEmpty()) lines.add(t);
        }
        if (lines.size() != SudokuBoard.SIZE) {
            throw new IllegalArgumentException(
                    "Puzzle must have 9 rows, found " + lines.size());
        }

        SudokuBoard board = new SudokuBoard();
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            String row = lines.get(r);
            if (row.length() != SudokuBoard.SIZE) {
                throw new IllegalArgumentException(
                        "Row " + (r + 1) + " must have 9 cells, found " + row.length());
            }
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                char ch = row.charAt(c);
                int value;
                if (ch == '.' || ch == '0') value = SudokuBoard.EMPTY;
                else if (ch >= '1' && ch <= '9') value = ch - '0';
                else throw new IllegalArgumentException(
                        "Invalid character '" + ch + "' at row " + (r + 1) + ", col " + (c + 1));

                if (value != SudokuBoard.EMPTY && !board.isValidMove(r, c, value)) {
                    throw new IllegalArgumentException(
                            "Puzzle breaks Sudoku rules at row " + (r + 1) + ", col " + (c + 1)
                                    + " (digit " + value + " repeated)");
                }
                board.set(r, c, value);
            }
        }
        return board;
    }

    // ---------- Writing ----------

    /** Nicely formatted text (with 3x3 box separators). */
    public static String format(SudokuBoard board) {
        if (board == null) throw new IllegalArgumentException("Board is null.");
        return board.toString();
    }

    public static void print(SudokuBoard board) {
        System.out.print(format(board));
    }

    public static void print(String title, SudokuBoard board) {
        System.out.println(title);
        print(board);
    }

    /** Saves a board to a file (creates missing folders, overwrites the file). */
    public static void saveToFile(SudokuBoard board, String path) throws IOException {
        Path p = Paths.get(path);
        if (p.getParent() != null) Files.createDirectories(p.getParent());
        Files.write(p, format(board).getBytes(StandardCharsets.UTF_8));
    }

    /** Saves in the SAME input format (9 lines of digits), so the file can be loaded again. */
    public static void saveAsPuzzleFile(SudokuBoard board, String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) sb.append(board.get(r, c));
            sb.append(System.lineSeparator());
        }
        Path p = Paths.get(path);
        if (p.getParent() != null) Files.createDirectories(p.getParent());
        Files.write(p, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    // ---------- Files ----------

    /** All .txt files in a folder, sorted by name. */
    public static List<Path> listPuzzleFiles(String dir) throws IOException {
        Path d = Paths.get(dir);
        if (!Files.isDirectory(d)) {
            throw new IOException("Puzzles folder not found: " + dir);
        }
        try (Stream<Path> s = Files.list(d)) {
            return s.filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                    .sorted()
                    .collect(Collectors.toList());
        }
    }
}
