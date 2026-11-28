
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

public class SudokuExperiment {

    // Number of measured runs for each solver
    private static final int RUNS = 10;

    // Warm-up runs to reduce JVM initialization effects
    private static final int WARMUP_RUNS = 3;

    // Directory containing the Sudoku puzzle files
    private static final String PUZZLES_DIR = "puzzles";

    // Store the performance and correctness results of each puzzle
    private static class ExperimentResult {

        String puzzleName;
        double seqTime;
        double parTime;
        double speedup;
        boolean seqValid;
        boolean parValid;
        boolean sameSolution;

        // Initialize the result object with the experiment data
        ExperimentResult(String puzzleName,
                         double seqTime,
                         double parTime,
                         double speedup,
                         boolean seqValid,
                         boolean parValid,
                         boolean sameSolution) {

            this.puzzleName = puzzleName;
            this.seqTime = seqTime;
            this.parTime = parTime;
            this.speedup = speedup;
            this.seqValid = seqValid;
            this.parValid = parValid;
            this.sameSolution = sameSolution;
        }
    }

    // 1. Main method: manages the experiments for all puzzles
    public static void main(String[] args) throws IOException {

        // Use the common SudokuSolver interface for both implementations
        SudokuSolver sequential = new SequentialSudokuSolver();
        SudokuSolver parallel = new ParallelSudokuSolver();

        // Retrieve all puzzle file paths from the puzzles directory
        List<Path> puzzleFiles =
                SudokuIO.listPuzzleFiles(PUZZLES_DIR);

        // Store the results of all experiments
        List<ExperimentResult> results = new ArrayList<>();

        // Stop the experiment if no puzzle files are available
        if (puzzleFiles.isEmpty()) {
            System.out.println("No puzzle files found.");
            return;
        }

        // Display the experiment configuration
        System.out.println("Sudoku Performance Experiment");

        System.out.println("Available processors: "
                + Runtime.getRuntime().availableProcessors());

        System.out.println("Parallel threshold: 2");
        System.out.println("Runs per solver: " + RUNS);
        System.out.println("Warm-up runs: " + WARMUP_RUNS);

        // Run both solvers on every puzzle and collect the results
        for (Path file : puzzleFiles) {

            ExperimentResult result =
                    runExperiment(file, sequential, parallel);

            results.add(result);
        }

        // Print the final comparison table for all puzzles
        printSummary(results);
    }

    // 2. Run a complete performance experiment on one puzzle
    private static ExperimentResult runExperiment(
            Path file,
            SudokuSolver sequential,
            SudokuSolver parallel) throws IOException {

        // Load the original puzzle and keep it unchanged
        SudokuBoard originalBoard = SudokuIO.loadFromFile(file);

        System.out.println("\n========================================");
        System.out.println("Puzzle: " + file.getFileName());
        System.out.println("========================================");

        SudokuIO.print("Original Puzzle:", originalBoard);

        // Warm up both solvers before measuring execution time.
        // These runs are excluded from the performance calculations.
        for (int i = 0; i < WARMUP_RUNS; i++) {
            sequential.solve(originalBoard.clone());
            parallel.solve(originalBoard.clone());
        }

        // Accumulate execution times across all measured runs
        double totalSeqTime = 0;
        double totalParTime = 0;

        // Keep the boards produced by the measured runs
        SudokuBoard seqBoard = null;
        SudokuBoard parBoard = null;

        // Perform the measured runs for both solvers
        for (int i = 0; i < RUNS; i++) {

            // Create independent copies before every run.
            // Both solvers must start with the same unsolved puzzle.
            seqBoard = originalBoard.clone();
            parBoard = originalBoard.clone();

            // Alternate execution order to reduce measurement bias.
            // Even iterations: Sequential first.
            // Odd iterations: Parallel first.
            if (i % 2 == 0) {

                totalSeqTime += measureTime(
                        sequential, seqBoard, originalBoard);

                totalParTime += measureTime(
                        parallel, parBoard, originalBoard);

            } else {

                totalParTime += measureTime(
                        parallel, parBoard, originalBoard);

                totalSeqTime += measureTime(
                        sequential, seqBoard, originalBoard);
            }
        }

        // Calculate the average execution time of each solver
        double avgSeqTime = totalSeqTime / RUNS;
        double avgParTime = totalParTime / RUNS;

        // Calculate the performance ratio between both solvers
        double speedup = calculateSpeedup(
                avgSeqTime, avgParTime);

        // Validate the solutions produced by the final measured run
        boolean seqValid = validateSolution(
                originalBoard, seqBoard);

        boolean parValid = validateSolution(
                originalBoard, parBoard);

        // Compare the contents of both solved boards
        boolean sameSolution = seqBoard.equals(parBoard);

        // Display the final solution produced by each solver
        SudokuIO.print("\nSequential Solution:", seqBoard);
        SudokuIO.print("\nParallel Solution:", parBoard);

        // Store all results of the current puzzle in one object
        ExperimentResult result = new ExperimentResult(
                file.getFileName().toString(),
                avgSeqTime,
                avgParTime,
                speedup,
                seqValid,
                parValid,
                sameSolution
        );

        // Display the individual puzzle results
        printResults(sequential, parallel, result);

        // Return the result to be included in the final comparison
        return result;
    }

    // 3. Measure the execution time of a single solver run
    private static double measureTime(
            SudokuSolver solver,
            SudokuBoard board,
            SudokuBoard originalBoard) {

        // Record the start time in nanoseconds
        long start = System.nanoTime();

        // Execute the selected solving algorithm
        boolean solved = solver.solve(board);

        // Record the end time immediately after solving
        long end = System.nanoTime();

        // Validate every measured run.
        // Validation is performed outside the timed section.
        // Stop the experiment if the solver produces an invalid solution.
        if (!solved || !validateSolution(originalBoard, board)) {
            throw new IllegalStateException(
                    solver.getName()
                    + " failed to produce a valid solution.");
        }

        // Convert elapsed time from nanoseconds to milliseconds
        return (end - start) / 1_000_000.0;
    }

    // 4. Check solution correctness and preserve the original clues
    private static boolean validateSolution(
            SudokuBoard originalBoard,
            SudokuBoard solvedBoard) {

        // Ensure the board exists, is complete,
        // and satisfies all Sudoku constraints
        if (solvedBoard == null || !solvedBoard.isSolved()) {
            return false;
        }

        // Check every cell to ensure the original clues are preserved
        for (int row = 0; row < SudokuBoard.SIZE; row++) {

            for (int col = 0; col < SudokuBoard.SIZE; col++) {

                int originalValue = originalBoard.get(row, col);

                // Original non-empty cells must not be modified
                if (originalValue != SudokuBoard.EMPTY
                        && originalValue != solvedBoard.get(row, col)) {
                    return false;
                }
            }
        }

        // The solution satisfies all validation requirements
        return true;
    }

    // 5. Calculate speedup using the average execution times
    private static double calculateSpeedup(
            double seqTime, double parTime) {

        // Speedup = Sequential Time / Parallel Time
        return seqTime / parTime;
    }

    // 6. Display the performance results of an individual puzzle
    private static void printResults(
            SudokuSolver sequential,
            SudokuSolver parallel,
            ExperimentResult result) {

        System.out.println("\nPerformance Results:");

        // Print the table headers
        System.out.printf("%-15s %-20s %-10s%n",
                "Solver", "Average Time (ms)", "Valid");

        System.out.println("---------------------------------------------");

        // Display sequential execution time and validation result
        System.out.printf("%-15s %-20.4f %-10b%n",
                sequential.getName(),
                result.seqTime,
                result.seqValid);

        // Display parallel execution time and validation result
        System.out.printf("%-15s %-20.4f %-10b%n",
                parallel.getName(),
                result.parTime,
                result.parValid);

        // Display speedup and solution equality
        System.out.printf("%nSpeedup: %.4fx%n",
                result.speedup);

        System.out.println("Same solution: "
                + result.sameSolution);

        // Different solutions are acceptable if both are valid,
        // since some Sudoku puzzles may have multiple solutions.
        if (!result.sameSolution
                && result.seqValid && result.parValid) {

            System.out.println(
                    "Both solvers found different valid solutions.");
        }

        // Display the solver with the lower average execution time
        System.out.println("Faster solver: "
                + getFasterSolver(result.speedup));
    }

    // 7. Determine the faster solver based on the speedup value
    private static String getFasterSolver(double speedup) {

        // Speedup > 1: Parallel is faster
        // Speedup < 1: Sequential is faster
        // Speedup = 1: Both have equal execution times
        if (speedup > 1) {
            return "Parallel";
        } else if (speedup < 1) {
            return "Sequential";
        } else {
            return "Equal";
        }
    }

    // 8. Display the final comparison table for all tested puzzles
    private static void printSummary(
            List<ExperimentResult> results) {

        System.out.println("\nFINAL PERFORMANCE COMPARISON");

        System.out.println(
                "============================================================"
                + "==================================================");

        // Print the comparison table headers
        System.out.printf(
                "%-22s %12s %12s %10s %10s %10s %10s %12s%n",
                "Puzzle",
                "Seq (ms)",
                "Par (ms)",
                "Speedup",
                "Seq Valid",
                "Par Valid",
                "Same Sol.",
                "Faster");

        System.out.println(
                "------------------------------------------------------------"
                + "--------------------------------------------------");

        // Print one row for each puzzle using its stored results
        for (ExperimentResult result : results) {

            System.out.printf(
                    "%-22s %12.4f %12.4f %9.4fx %10b %10b %10b %12s%n",
                    result.puzzleName,
                    result.seqTime,
                    result.parTime,
                    result.speedup,
                    result.seqValid,
                    result.parValid,
                    result.sameSolution,
                    getFasterSolver(result.speedup));
        }

        System.out.println(
                "============================================================"
                + "==================================================");

        // Display the total number of tested puzzles
        System.out.println("Total puzzles tested: " + results.size());
    }
}

