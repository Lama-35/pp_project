public class SequentialSudokuSolver implements SudokuSolver {

    @Override
    public boolean solve(SudokuBoard board) {

        // Handle null or invalid starting board
        if (board == null || !board.hasNoConflicts()) {
            return false;
        }

        // Work on a copy of the board to preserve the original board
        SudokuBoard workingBoard = board.clone();

        // Try to solve the copied board
        //return true if found sol. and copy it to original board
        if (backtrack(workingBoard)) {

            // Copy the solution into the original board
            board.copyFrom(workingBoard);

            return true;
        }

        // No solution found; original board remains unchanged
        return false;
    }

    private boolean backtrack(SudokuBoard board) {

        // Step 1: Find an empty cell
        // write row & col of empty cell in array
        int[] emptyCell = board.findEmptyCell();

        // Step 2: Base case - no empty cells remain
        if (emptyCell == null) {
            return board.isSolved();
        }

        int row = emptyCell[0];
        int col = emptyCell[1];

        // Step 3: Try numbers from 1 to 9
        for (int num = 1; num <= 9; num++) {

            // Step 4: Check if the number is valid
            if (board.isValidMove(row, col, num)) {

                // Step 5: Place the number
                board.set(row, col, num);

                // Step 6: Recursively solve the remaining board
                if (backtrack(board)) {
                    return true;
                }

                // Step 7: Backtrack - undo the previous move
                board.set(row, col, SudokuBoard.EMPTY);
            }
        }

        // No valid number worked for this cell
        return false;
    }

    @Override
    public String getName() {
        return "Sequential";
    }
}

