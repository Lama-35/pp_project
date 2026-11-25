
public interface SudokuSolver {

    /**
     * Tries to solve the given puzzle.
     *
     * Contract (every implementation must follow it):
     *  - The solution is written INTO the board that is passed in.
     *  - Returns true if a valid solution was found; the board is then full.
     *  - Returns false if the puzzle has no solution (or the starting board
     *    already breaks the Sudoku rules); the board is left unchanged.
     *
     * @param board the puzzle to solve (0 = blank cell)
     * @return true if solved, false otherwise
     */
    boolean solve(SudokuBoard board);

    /** Short name used when printing results, e.g. "Sequential" or "Parallel". */
    String getName();
}
