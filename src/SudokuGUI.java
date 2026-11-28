
import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

public class SudokuGUI extends JFrame {

    // Colors
    private final Color cream = new Color(245, 238, 233);
    private final Color burgundy = new Color(121, 62, 80);
    private final Color pink = new Color(229, 200, 206);
    private final Color beige = new Color(234, 219, 214);
    private final Color green = new Color(56, 115, 72);

    // GUI components
    private final JTextField[][] cells = new JTextField[9][9];

    private final JComboBox<String> puzzleBox = new JComboBox<>();

    private final JRadioButton seqRadio =
            new JRadioButton("Sequential", true);

    private final JRadioButton parRadio =
            new JRadioButton("Parallel");

    private final JLabel seqTimeLabel = new JLabel("-- ms");
    private final JLabel parTimeLabel = new JLabel("-- ms");
    private final JLabel speedupLabel = new JLabel("-- x");

    private final JLabel seqValidLabel = new JLabel("Valid: --");
    private final JLabel parValidLabel = new JLabel("Valid: --");

    private final JLabel sameSolutionLabel =
            new JLabel("Same Solution: --");

    private final JLabel fasterSolverLabel =
            new JLabel("Faster Solver: --");

    private final JLabel statusLabel =
            new JLabel("Select a puzzle.");

    private final JLabel puzzleTitle =
            new JLabel("Puzzle Board");

    private List<Path> puzzleFiles;

    private SudokuBoard originalBoard;
    private SudokuBoard seqSolution;
    private SudokuBoard parSolution;

    private double seqTime = -1;
    private double parTime = -1;

    private final SudokuSolver sequential =
            new SequentialSudokuSolver();

    private final SudokuSolver parallel =
            new ParallelSudokuSolver();

    // Constructor
    public SudokuGUI() {

        setTitle("Sudoku | Performance Lab");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 15));

        mainPanel.setBackground(cream);

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(createControlPanel(), BorderLayout.WEST);
        mainPanel.add(createBoardPanel(), BorderLayout.CENTER);
        mainPanel.add(createResultsPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        loadPuzzleList();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Left control panel
    private JPanel createControlPanel() {

        JPanel panel = new JPanel();

        panel.setBackground(cream);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(230, 440));

        JLabel title = new JLabel("Sudoku Solver");

        // Main title font
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(burgundy);

        JLabel puzzleLabel = new JLabel("Choose your puzzle");

        JButton loadButton = createButton("Open puzzle", pink);
        JButton runButton = createButton("Run solver", burgundy);
        JButton resetButton = createButton("Reset board", beige);

        puzzleBox.setMaximumSize(new Dimension(230, 32));

        // Select only one solving method
        ButtonGroup group = new ButtonGroup();

        group.add(seqRadio);
        group.add(parRadio);

        seqRadio.setBackground(cream);
        parRadio.setBackground(cream);

        seqRadio.setForeground(burgundy);
        parRadio.setForeground(burgundy);

        panel.add(title);
        panel.add(Box.createVerticalStrut(30));

        panel.add(puzzleLabel);
        panel.add(Box.createVerticalStrut(8));

        panel.add(puzzleBox);
        panel.add(Box.createVerticalStrut(10));

        panel.add(loadButton);
        panel.add(Box.createVerticalStrut(25));

        JLabel methodLabel = new JLabel("Solving method");

        methodLabel.setFont(new Font("Arial", Font.BOLD, 14));
        methodLabel.setForeground(burgundy);

        panel.add(methodLabel);
        panel.add(Box.createVerticalStrut(10));

        panel.add(seqRadio);
        panel.add(parRadio);

        panel.add(Box.createVerticalStrut(20));
        panel.add(runButton);

        panel.add(Box.createVerticalStrut(10));
        panel.add(resetButton);

        // Button actions
        loadButton.addActionListener(e -> loadPuzzle());

        runButton.addActionListener(e -> {

            if (seqRadio.isSelected()) {
                solvePuzzle(sequential);
            } else {
                solvePuzzle(parallel);
            }
        });

        resetButton.addActionListener(e -> resetPuzzle());

        // Align all components to the left
        for (Component component : panel.getComponents()) {

            if (component instanceof JComponent) {

                ((JComponent) component).setAlignmentX(
                        Component.LEFT_ALIGNMENT);
            }
        }

        return panel;
    }

    // Create a colored button
    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setBackground(color);

        button.setForeground(
                color.equals(burgundy) ? Color.WHITE : burgundy);

        button.setFont(new Font("Arial", Font.BOLD, 13));

        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);

        button.setMaximumSize(new Dimension(230, 38));
        button.setPreferredSize(new Dimension(230, 38));

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // Right board panel
    private JPanel createBoardPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));

        panel.setBackground(cream);

        puzzleTitle.setFont(new Font("Arial", Font.BOLD, 16));
        puzzleTitle.setForeground(burgundy);

        panel.add(puzzleTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 3));

        grid.setPreferredSize(new Dimension(405, 405));

        for (int boxRow = 0; boxRow < 3; boxRow++) {

            for (int boxCol = 0; boxCol < 3; boxCol++) {

                JPanel box = new JPanel(new GridLayout(3, 3));

                box.setBorder(
                        BorderFactory.createLineBorder(burgundy, 1));

                for (int r = 0; r < 3; r++) {

                    for (int c = 0; c < 3; c++) {

                        int row = boxRow * 3 + r;
                        int col = boxCol * 3 + c;

                        JTextField cell = new JTextField();

                        cell.setHorizontalAlignment(JTextField.CENTER);

                        cell.setFont(
                                new Font("Arial", Font.PLAIN, 22));

                        cell.setEditable(false);
                        cell.setFocusable(false);
                        cell.setBackground(Color.WHITE);

                        cell.setBorder(
                                BorderFactory.createLineBorder(beige));

                        cells[row][col] = cell;

                        box.add(cell);
                    }
                }

                grid.add(box);
            }
        }

        panel.add(grid, BorderLayout.CENTER);

        JLabel legend = new JLabel(
                "Pink: Given numbers     Cream: Other cells");

        legend.setFont(new Font("Arial", Font.PLAIN, 12));
        legend.setForeground(burgundy);

        panel.add(legend, BorderLayout.SOUTH);

        return panel;
    }

    // Bottom results panel
    private JPanel createResultsPanel() {

        JPanel panel = new JPanel(new BorderLayout(0, 10));

        panel.setBackground(cream);

        JPanel results = new JPanel(new GridLayout(1, 3, 10, 0));

        results.setBackground(cream);

        results.add(createResultBox(
                "Sequential", seqTimeLabel, seqValidLabel));

        results.add(createResultBox(
                "Parallel", parTimeLabel, parValidLabel));

        results.add(createResultBox(
                "Speedup", speedupLabel,
                new JLabel("Performance ratio")));

        // Same solution and faster solver
        JPanel comparisonPanel =
                new JPanel(new GridLayout(2, 1, 0, 8));

        comparisonPanel.setBackground(beige);

        comparisonPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 15, 10, 15));

        sameSolutionLabel.setForeground(burgundy);
        fasterSolverLabel.setForeground(burgundy);

        sameSolutionLabel.setFont(
                new Font("Arial", Font.BOLD, 14));

        fasterSolverLabel.setFont(
                new Font("Arial", Font.BOLD, 14));

        comparisonPanel.add(sameSolutionLabel);
        comparisonPanel.add(fasterSolverLabel);

        statusLabel.setForeground(burgundy);

        statusLabel.setFont(
                new Font("Arial", Font.PLAIN, 13));

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));

        bottomPanel.setBackground(cream);

        bottomPanel.add(comparisonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        panel.add(results, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);

        return panel;
    }

    // Create one result box
    private JPanel createResultBox(
            String title, JLabel value, JLabel extraInfo) {

        JPanel panel = new JPanel(new GridLayout(3, 1, 0, 5));

        panel.setBackground(beige);

        panel.setBorder(
                BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JLabel name = new JLabel(title);

        name.setFont(new Font("Arial", Font.PLAIN, 13));
        name.setForeground(burgundy);

        value.setFont(new Font("Arial", Font.BOLD, 20));
        value.setForeground(burgundy);

        extraInfo.setFont(new Font("Arial", Font.PLAIN, 13));
        extraInfo.setForeground(burgundy);

        panel.add(name);
        panel.add(value);
        panel.add(extraInfo);

        return panel;
    }

    // Read puzzle file names
    private void loadPuzzleList() {

        try {

            puzzleFiles = SudokuIO.listPuzzleFiles("puzzles");

            for (Path file : puzzleFiles) {

                puzzleBox.addItem(file.getFileName().toString());
            }

            if (!puzzleFiles.isEmpty()) {

                loadPuzzle();

            } else {

                statusLabel.setText("No puzzles found.");
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Load selected puzzle
    private void loadPuzzle() {

        int index = puzzleBox.getSelectedIndex();

        if (puzzleFiles == null || index < 0) {
            return;
        }

        try {

            Path file = puzzleFiles.get(index);

            originalBoard = SudokuIO.loadFromFile(file);

            puzzleTitle.setText(file.getFileName().toString());

            displayBoard(originalBoard);

            clearResults();

            statusLabel.setText("Puzzle loaded. Ready to solve.");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Display puzzle or solution
    private void displayBoard(SudokuBoard board) {

        for (int r = 0; r < 9; r++) {

            for (int c = 0; c < 9; c++) {

                int value = board.get(r, c);

                JTextField cell = cells[r][c];

                if (value == SudokuBoard.EMPTY) {

                    cell.setText("");

                } else {

                    cell.setText(String.valueOf(value));
                }

                // Original numbers
                if (originalBoard.get(r, c) != SudokuBoard.EMPTY) {

                    cell.setBackground(pink);
                    cell.setForeground(burgundy);

                    cell.setFont(
                            new Font("Arial", Font.BOLD, 22));

                } else {

                    // Empty cells and solved numbers
                    if ((r / 3 + c / 3) % 2 == 0) {

                        cell.setBackground(
                                new Color(255, 252, 247));

                    } else {

                        cell.setBackground(cream);
                    }

                    cell.setForeground(new Color(65, 65, 65));

                    cell.setFont(
                            new Font("Arial", Font.PLAIN, 22));
                }
            }
        }
    }

    // Validate solution and preserve original clues
    private boolean validateSolution(
            SudokuBoard original, SudokuBoard solved) {

        if (solved == null || !solved.isSolved()) {
            return false;
        }

        for (int r = 0; r < 9; r++) {

            for (int c = 0; c < 9; c++) {

                int value = original.get(r, c);

                if (value != SudokuBoard.EMPTY
                        && value != solved.get(r, c)) {

                    return false;
                }
            }
        }

        return true;
    }

    // Solve and measure execution time
    private void solvePuzzle(SudokuSolver solver) {

        if (originalBoard == null) {

            statusLabel.setText("Please load a puzzle first.");
            return;
        }

        SudokuBoard board = originalBoard.clone();

        long start = System.nanoTime();

        boolean solved = solver.solve(board);

        long end = System.nanoTime();

        double time = (end - start) / 1_000_000.0;

        boolean valid = solved
                && validateSolution(originalBoard, board);

        // Store sequential results
        if (solver == sequential) {

            seqTime = valid ? time : -1;
            seqSolution = valid ? board.clone() : null;

            seqTimeLabel.setText(
                    String.format("%.4f ms", time));

            seqValidLabel.setText("Valid: " + valid);

            seqValidLabel.setForeground(
                    valid ? green : Color.RED);

        } else {

            // Store parallel results
            parTime = valid ? time : -1;
            parSolution = valid ? board.clone() : null;

            parTimeLabel.setText(
                    String.format("%.4f ms", time));

            parValidLabel.setText("Valid: " + valid);

            parValidLabel.setForeground(
                    valid ? green : Color.RED);
        }

        if (valid) {

            displayBoard(board);

            statusLabel.setText(
                    "Solved successfully using " + solver.getName());

        } else {

            displayBoard(originalBoard);

            statusLabel.setText(
                    solver.getName()
                    + " failed to produce a valid solution.");
        }

        updateComparison();
    }

    // Compare both solvers
    private void updateComparison() {

        // Wait until both solvers produce valid solutions
        if (seqSolution == null || parSolution == null) {

            sameSolutionLabel.setText("Same Solution: --");
            fasterSolverLabel.setText("Faster Solver: --");
            speedupLabel.setText("-- x");

            return;
        }

        // Check if both solutions are identical
        boolean sameSolution = seqSolution.equals(parSolution);

        sameSolutionLabel.setText(
                "Same Solution: " + sameSolution);

        // Calculate speedup
        if (parTime > 0) {

            double speedup = seqTime / parTime;

            speedupLabel.setText(
                    String.format("%.4fx", speedup));

        } else {

            speedupLabel.setText("-- x");
        }

        // Determine the faster solver
        if (seqTime < parTime) {

            fasterSolverLabel.setText(
                    "Faster Solver: Sequential");

        } else if (parTime < seqTime) {

            fasterSolverLabel.setText(
                    "Faster Solver: Parallel");

        } else {

            fasterSolverLabel.setText(
                    "Faster Solver: Equal");
        }

        if (sameSolution) {

            statusLabel.setText(
                    "Both solvers completed successfully.");

        } else {

            statusLabel.setText(
                    "Both solvers found different valid solutions.");
        }
    }

    // Reset all results
    private void clearResults() {

        seqTime = -1;
        parTime = -1;

        seqSolution = null;
        parSolution = null;

        seqTimeLabel.setText("-- ms");
        parTimeLabel.setText("-- ms");
        speedupLabel.setText("-- x");

        seqValidLabel.setText("Valid: --");
        parValidLabel.setText("Valid: --");

        seqValidLabel.setForeground(burgundy);
        parValidLabel.setForeground(burgundy);

        sameSolutionLabel.setText("Same Solution: --");
        fasterSolverLabel.setText("Faster Solver: --");
    }

    // Restore the original puzzle
    private void resetPuzzle() {

        if (originalBoard != null) {

            displayBoard(originalBoard);

            clearResults();

            statusLabel.setText("Board reset.");
        }
    }

    // Start the GUI
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new SudokuGUI());
    }
}
