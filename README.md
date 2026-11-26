# Parallel Sudoku Solver

A Java-based Sudoku solver that uses recursive backtracking to solve standard 9×9 Sudoku puzzles. The project aims to compare sequential execution with parallel execution using Java's Fork/Join framework.

## Project Overview

The application solves Sudoku puzzles using two approaches:

- **Sequential Solver:** Uses recursive backtracking to find a valid solution.
- **Parallel Solver:** Uses task splitting with Java Fork/Join to explore possible solutions concurrently.

The main goal is to study how parallel processing can improve solving performance compared with a traditional sequential algorithm.

## Sudoku Rules

- The board contains 9 rows and 9 columns.
- Empty cells are represented by 0.
- Each row must contain the numbers 1–9 without repetition.
- Each column must contain the numbers 1–9 without repetition.
- Each 3×3 subgrid must contain the numbers 1–9 without repetition.

## Project Structure

```text
src/
├── SudokuBoard.java
├── SudokuSolver.java
├── SequentialSudokuSolver.java
├── ParallelSudokuSolver.java
├── SudokuIO.java
├── SudokuExperiment.java
└── SudokuGUI.java
```

### Main Components

| Class / Interface | Responsibility |
|---|---|
| SudokuBoard | Represents the 9×9 board and provides validation, cloning, and board operations. |
| SudokuSolver | Defines the common solver interface. |
| SequentialSudokuSolver | Solves puzzles using recursive backtracking. |
| ParallelSudokuSolver | Uses Fork/Join task splitting to explore solutions in parallel. |
| SudokuIO | Handles puzzle input and output. |
| SudokuExperiment | Runs experiments and compares execution times. |


## Sequential Algorithm

The sequential solver uses the backtracking algorithm:

1. Validate the initial board.
2. Find an empty cell.
3. Try numbers from 1 to 9.
4. Check whether each number is a valid move.
5. Place a valid number and recursively solve the remaining board.
6. If the choice leads to a dead end, undo the move and try another number.
7. Continue until the board is solved or all possibilities fail.

The solver works on a cloned board to ensure that the original board remains unchanged if no solution is found.

## Parallel Algorithm

The parallel solver is designed using Java's Fork/Join framework.

Its intended approach includes:

- Splitting the search into independent tasks.
- Exploring different candidate values concurrently.
- Using cloned board states to avoid shared-state conflicts.
- Applying a branching threshold to control task creation.
- Stopping unnecessary tasks when a valid solution is found.

## Solver Contract

Both solver implementations follow the same interface:

```java
public interface SudokuSolver {
    boolean solve(SudokuBoard board);
    String getName();
}
```

- Returns `true` when a valid solution is found.
- Returns `false` when the puzzle is invalid or has no solution.
- Writes the solution into the original board on success.
- Leaves the original board unchanged on failure.

## Testing



## Performance Evaluation

The project aims to compare sequential and parallel execution using 3–5 Sudoku puzzles with different difficulty levels:

- Easy
- Medium
- Hard

Execution time will be measured for both approaches.

| Puzzle | Difficulty | Sequential Time | Parallel Time |
|---|---|---|---|
| Puzzle 1 | Easy | To be measured | To be measured |
| Puzzle 2 | Medium | To be measured | To be measured |
| Puzzle 3 | Hard | To be measured | To be measured |

The results will be used to analyze the performance benefits and overhead of parallel execution.

## Technologies Used

- Java
- Object-Oriented Programming (OOP)
- Recursive Backtracking
- Java Concurrency
- Fork/Join Framework

## How to Run

1. Clone or download the project.
2. Open it in a Java IDE such as IntelliJ IDEA or Eclipse.
3. Make sure all source files are in the correct package.
4. Run `Main.java`.
5. Provide a Sudoku puzzle and execute the selected solver.

## Project Objectives

- Implement a correct sequential Sudoku solver.
- Develop a parallel Sudoku solver using task splitting.
- Apply object-oriented design principles.
- Compare sequential and parallel performance.
- Understand the impact of concurrency and task management on execution time.

## Team Project

Developed as a university project to study parallel processing, recursive algorithms, and performance comparison in Java.
