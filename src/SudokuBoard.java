import java.util.Arrays;

public class SudokuBoard implements Cloneable {

    public static final int SIZE = 9;      // 9x9 grid
    public static final int BOX_SIZE = 3;  // 3x3 subgrids
    public static final int EMPTY = 0;     // value that marks an empty cell

    private final int[][] grid = new int[SIZE][SIZE];

    /** Creates an empty board (all zeros). */
    public SudokuBoard() {
    }

    public SudokuBoard(int[][] values) {
        if (values == null || values.length != SIZE) {
            throw new IllegalArgumentException("Board must have exactly 9 rows.");
        }
        for (int r = 0; r < SIZE; r++) {
            if (values[r] == null || values[r].length != SIZE) {
                throw new IllegalArgumentException("Row " + r + " must have exactly 9 columns.");
            }
            for (int c = 0; c < SIZE; c++) {
                checkValue(values[r][c], true);
                grid[r][c] = values[r][c];
            }
        }
    }


    /** Returns the value at (row, col); 0 means empty. */
    public int get(int row, int col) {
        checkIndex(row, col);
        return grid[row][col];
    }

    /**
     * Writes a value (0..9) at (row, col). Passing 0 clears the cell.
     * this does NOT check Sudoku rules; call isValidMove() first
     * when you want to respect them (the solver does).
     */
    public void set(int row, int col, int value) {
        checkIndex(row, col);
        checkValue(value, true);
        grid[row][col] = value;
    }

    /** True if the cell at (row, col) is empty. */
    public boolean isEmpty(int row, int col) {
        return get(row, col) == EMPTY;
    }


    /**
     * Can 'value' (1..9) be placed at (row, col) without breaking a rule?
     * Checks the row, the column and the 3x3 subgrid. The cell itself is
     * ignored, so it also works on a cell that already holds that value.
     */
    public boolean isValidMove(int row, int col, int value) {
        checkIndex(row, col);
        if (value < 1 || value > 9) {
            return false;
        }
        // row and column
        for (int i = 0; i < SIZE; i++) {
            if (i != col && grid[row][i] == value) return false;
            if (i != row && grid[i][col] == value) return false;
        }
        // 3x3 subgrid
        int boxRow = (row / BOX_SIZE) * BOX_SIZE;
        int boxCol = (col / BOX_SIZE) * BOX_SIZE;
        for (int r = boxRow; r < boxRow + BOX_SIZE; r++) {
            for (int c = boxCol; c < boxCol + BOX_SIZE; c++) {
                if ((r != row || c != col) && grid[r][c] == value) return false;
            }
        }
        return true;
    }

    /** True if there are no empty cells left (says nothing about correctness). */
    public boolean isComplete() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == EMPTY) return false;
            }
        }
        return true;
    }

    /**
     * VALIDATOR for a partially filled board: true if no row, column or
     * subgrid contains the same digit twice. Blanks are ignored.
     * Use it to reject an invalid starting puzzle before solving.
     */
    public boolean hasNoConflicts() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                int v = grid[r][c];
                if (v != EMPTY && !isValidMove(r, c, v)) return false;
            }
        }
        return true;
    }

    /**
     * VALIDATOR for a finished board: true if the board is full AND every
     * row, column and 3x3 subgrid contains each digit 1..9 exactly once.
     * Use it in tests/experiments to prove that a solver's output is valid.
     */
    public boolean isSolved() {
        return isComplete() && hasNoConflicts();
    }

    /**
     * Returns {row, col} of the first empty cell (scanning row by row),
     * or null if the board has no empty cells.
     */
    public int[] findEmptyCell() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == EMPTY) return new int[]{r, c};
            }
        }
        return null;
    }
    /**
     * DEEP copy of the board. Each row array is copied too, so changing the
     * clone never changes the original (and vice versa), The parallel solver
     * gives each Fork/Join task its own clone to avoid shared-state races
     */
    @Override
    public SudokuBoard clone() {
        SudokuBoard copy = new SudokuBoard();
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(grid[r], 0, copy.grid[r], 0, SIZE);
        }
        return copy;
    }

    /** Copies all cell values of another board into this one (used to hand back a solution). */
    public void copyFrom(SudokuBoard other) {
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(other.grid[r], 0, grid[r], 0, SIZE);
        }
    }

    /** Returns the grid as a new 9x9 array (هااام يجدعان دي مجرد نسخه )*/
    public int[][] toArray() {
        int[][] out = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(grid[r], 0, out[r], 0, SIZE);
        }
        return out;
    }

    // Object methods
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < SIZE; r++) {
            if (r > 0 && r % BOX_SIZE == 0) {
                sb.append("------+-------+------\n");
            }
            for (int c = 0; c < SIZE; c++) {
                if (c > 0 && c % BOX_SIZE == 0) sb.append("| ");
                sb.append(grid[r][c] == EMPTY ? "." : String.valueOf(grid[r][c]));
                if (c < SIZE - 1) sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SudokuBoard)) return false;
        return Arrays.deepEquals(grid, ((SudokuBoard) o).grid);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }

    // Input checking
    private static void checkIndex(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            throw new IndexOutOfBoundsException(
                    "Cell (" + row + ", " + col + ") is outside the 9x9 board.");
        }
    }

    private static void checkValue(int value, boolean allowZero) {
        int min = allowZero ? 0 : 1;
        if (value < min || value > 9) {
            throw new IllegalArgumentException(
                    "Invalid value " + value + " (allowed: " + min + " to 9).");
        }
    }
}

/** test **/
public class Main {
    static int passed = 0, failed = 0;

    static void check(String name, boolean condition) {
        if (condition) { passed++; System.out.println("PASS  " + name); }
        else           { failed++; System.out.println("FAIL  " + name); }
    }

    public static void main(String[] args) {
        int[][] puzzle = {
            {5,3,0, 0,7,0, 0,0,0},
            {6,0,0, 1,9,5, 0,0,0},
            {0,9,8, 0,0,0, 0,6,0},
            {8,0,0, 0,6,0, 0,0,3},
            {4,0,0, 8,0,3, 0,0,1},
            {7,0,0, 0,2,0, 0,0,6},
            {0,6,0, 0,0,0, 2,8,0},
            {0,0,0, 4,1,9, 0,0,5},
            {0,0,0, 0,8,0, 0,7,9}
        };
        int[][] solution = {
            {5,3,4, 6,7,8, 9,1,2},
            {6,7,2, 1,9,5, 3,4,8},
            {1,9,8, 3,4,2, 5,6,7},
            {8,5,9, 7,6,1, 4,2,3},
            {4,2,6, 8,5,3, 7,9,1},
            {7,1,3, 9,2,4, 8,5,6},
            {9,6,1, 5,3,7, 2,8,4},
            {2,8,7, 4,1,9, 6,3,5},
            {3,4,5, 2,8,6, 1,7,9}
        };

        SudokuBoard b = new SudokuBoard(puzzle);
        System.out.println(b);

        check("get returns given value", b.get(0, 0) == 5);
        check("blank cell is empty", b.isEmpty(0, 2));
        check("puzzle is not complete", !b.isComplete());
        check("puzzle has no conflicts", b.hasNoConflicts());
        check("puzzle is not 'solved'", !b.isSolved());

        check("row conflict detected (5 in row 0)", !b.isValidMove(0, 2, 5));
        check("column conflict detected (6 in col 0)", !b.isValidMove(2, 0, 6));
        check("subgrid conflict detected (9 in top-left box)", !b.isValidMove(0, 2, 9));
        check("legal move accepted (4 at 0,2)", b.isValidMove(0, 2, 4));
        check("value 0 / 10 rejected as move", !b.isValidMove(0, 2, 0) && !b.isValidMove(0, 2, 10));

        int[] first = b.findEmptyCell();
        check("findEmptyCell -> (0,2)", first != null && first[0] == 0 && first[1] == 2);

        // clone independence
        SudokuBoard copy = b.clone();
        copy.set(0, 2, 4);
        check("clone is equal before change", b.clone().equals(b));
        check("changing clone leaves original untouched", b.get(0, 2) == 0);
        check("clone received the change", copy.get(0, 2) == 4);

        // constructor copies input array
        int[][] raw = {{1,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},
                       {0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},
                       {0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0}};
        SudokuBoard fromRaw = new SudokuBoard(raw);
        raw[0][0] = 9;
        check("constructor copies the array", fromRaw.get(0, 0) == 1);

        // full valid solution
        SudokuBoard solved = new SudokuBoard(solution);
        check("solution is complete", solved.isComplete());
        check("solution isSolved()", solved.isSolved());
        check("findEmptyCell null on full board", solved.findEmptyCell() == null);

        // full but invalid board
        SudokuBoard bad = solved.clone();
        bad.set(0, 0, 3); // duplicates the 3 in row 0
        check("full but wrong board is NOT solved", bad.isComplete() && !bad.isSolved());

        // invalid starting puzzle
        SudokuBoard dup = new SudokuBoard();
        dup.set(0, 0, 7); dup.set(0, 8, 7);
        check("duplicate in a row -> hasNoConflicts false", !dup.hasNoConflicts());

        // invalid input handling
        boolean ex1 = false, ex2 = false, ex3 = false;
        try { b.set(9, 0, 1); } catch (IndexOutOfBoundsException e) { ex1 = true; }
        try { b.set(0, 0, 10); } catch (IllegalArgumentException e) { ex2 = true; }
        try { new SudokuBoard(new int[8][9]); } catch (IllegalArgumentException e) { ex3 = true; }
        check("set with bad index throws", ex1);
        check("set with bad value throws", ex2);
        check("constructor with wrong size throws", ex3);

        System.out.println("\n" + passed + " passed, " + failed + " failed");
    }
}
