
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

    // ==========================================
    // YASSMINE PART 
    // ==========================================
}

