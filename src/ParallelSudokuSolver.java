
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicReference;

public class ParallelSudokuSolver implements SudokuSolver {

    //  Attributes
    private final int parallelism;
    private final int threshold;

    //  Default constructor
    public ParallelSudokuSolver() {
        this(Runtime.getRuntime().availableProcessors(), 2);
    }

    //  Parameterized constructor
    public ParallelSudokuSolver(int parallelism, int threshold) {

        if (parallelism < 1 || parallelism > 32767 || threshold < 0) {
            throw new IllegalArgumentException(
                    "Invalid parallelism or threshold");
        }

        this.parallelism = parallelism;
        this.threshold = threshold;
    }

    // Solver name
    @Override
    public String getName() {
        return "Parallel";
    }

    //  Main solve method
    @Override
    public boolean solve(SudokuBoard board) {

        // Validate starting board
        if (board == null || !board.hasNoConflicts()) {
            return false;
        }

        // Shared solution
        AtomicReference<SudokuBoard> solution =
                new AtomicReference<>();

        // Create thread pool
        ForkJoinPool pool = new ForkJoinPool(parallelism);

        try {

            // Start root task
            SudokuBoard result = pool.invoke(
                    new SudokuTask(board.clone(), 0, solution)
            );

            // No solution found
            if (result == null) {
                return false;
            }

            // Copy solution into original board
            board.copyFrom(result);

            return true;

        } finally {
            pool.shutdown();
        }
    }

    //  Recursive Task
    private class SudokuTask extends RecursiveTask<SudokuBoard> {

        private final SudokuBoard board;
        private final int depth;
        private final AtomicReference<SudokuBoard> solution;

        SudokuTask(SudokuBoard board, int depth,
                   AtomicReference<SudokuBoard> solution) {

            this.board = board;
            this.depth = depth;
            this.solution = solution;
        }

        // Parallel task logic
        @Override
        protected SudokuBoard compute() {

            // Early termination
            if (solution.get() != null) {
                return solution.get();
            }

            // Threshold check
            if (depth >= threshold) {
                backtrack(board, solution);
                return solution.get();
            }

            // Find an empty cell
            int[] cell = board.findEmptyCell();

            // Base case
            if (cell == null) {
                publishSolution(board, solution);
                return solution.get();
            }

            // Store child tasks
            List<SudokuTask> tasks = new ArrayList<>();

            // Create and fork tasks
            for (SudokuBoard branch : createBranches(board, cell)) {

                if (solution.get() != null) {
                    break;
                }

                SudokuTask task = new SudokuTask(
                        branch, depth + 1, solution);

                tasks.add(task);
                task.fork();
            }

            // Join tasks
            for (SudokuTask task : tasks) {
                task.join();
            }

            return solution.get();
        }
    }

   
    /**
     * Publishes a solved board safely.
     * compareAndSet(null, ...) succeeds for ONE thread only (the first one);
     * if two tasks find a solution at the same time, the second one is ignored.
     * We store a clone so nobody can modify the published board afterwards.
     */
    private static boolean publishSolution(SudokuBoard solved,
                                           AtomicReference<SudokuBoard> solution) {
        return solution.compareAndSet(null, solved.clone());
    }

    /**
     * For the given empty cell, returns one INDEPENDENT board (a clone) per
     * valid candidate number. Each child task gets its own board, so tasks
     * never share mutable state. The parent board is never modified.
     */
    private static List<SudokuBoard> createBranches(SudokuBoard board, int[] cell) {

        List<SudokuBoard> branches = new ArrayList<>();
        int row = cell[0];
        int col = cell[1];

        for (int num = 1; num <= 9; num++) {
            if (board.isValidMove(row, col, num)) {
                SudokuBoard copy = board.clone();   // private copy for the child
                copy.set(row, col, num);
                branches.add(copy);
            }
        }

        return branches;
    }

    /**
     * Sequential recursive backtracking, used once depth >= threshold.
     * Works ONLY on the board owned by the calling task (no sharing).
     * Checks the shared "solution" reference so it stops early when
     * another task has already found an answer.
     *
     * @return true if this call found (and published) a solution
     */
    private static boolean backtrack(SudokuBoard board,
                                     AtomicReference<SudokuBoard> solution) {

        // Early termination: someone else already solved it
        if (solution.get() != null) {
            return false;
        }

        int[] cell = board.findEmptyCell();

        // No empty cells left -> check and publish
        if (cell == null) {
            if (board.isSolved()) {
                publishSolution(board, solution);
                return true;
            }
            return false;
        }

        int row = cell[0];
        int col = cell[1];

        for (int num = 1; num <= 9; num++) {

            // Stop quickly if another task finished meanwhile
            if (solution.get() != null) {
                return false;
            }

            if (board.isValidMove(row, col, num)) {
                board.set(row, col, num);

                if (backtrack(board, solution)) {
                    return true;
                }

                board.set(row, col, SudokuBoard.EMPTY);   // undo
            }
        }

        return false;
    }
}



