import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class MinesweeperGame extends JFrame {
    private int rows, cols, mines;
    private Cell[][] cells;
    private boolean gameOver = false;
    private int score;
    private JLabel scoreLabel;
    private JFrame gameFrame;
    private static List<Player> leaderboard = new ArrayList<>(
        Arrays.asList(
            new Player("Saquib", 75),
            new Player("Maaz", 50),
            new Player("Arpit", 85),
            new Player("Bhaumik", 100),
            new Player("Eve", 60),
            new Player("Frank", 45),
            new Player("Grace", 120)
        )
    );
    private static final String playerName = "Player";

    enum Difficulty {
        EASY(8, 8, 10),
        MEDIUM(16, 16, 40),
        HARD(24, 24, 99);

        int rows, cols, mines;
        Difficulty(int rows, int cols, int mines) {
            this.rows = rows;
            this.cols = cols;
            this.mines = mines;
        }
    }

    public MinesweeperGame() {
        showMenu();
    }

    private void showMenu() {
        setTitle("Minesweeper");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title Panel
        JLabel titleLabel = new JLabel("Minesweeper", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Main Panel for Difficulty, Guide, and Leaderboard
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(5, 1, 10, 10));
        mainPanel.setBackground(new Color(60, 63, 65));

        // Difficulty Label and Dropdown
        JLabel difficultyLabel = new JLabel("Select Difficulty:", SwingConstants.CENTER);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        difficultyLabel.setForeground(Color.WHITE);

        JComboBox<Difficulty> difficultyComboBox = new JComboBox<>(Difficulty.values());
        difficultyComboBox.setFont(new Font("Arial", Font.BOLD, 16));
        difficultyComboBox.setBackground(new Color(100, 149, 237));
        difficultyComboBox.setForeground(Color.WHITE);

        // Start Game Button
        JButton startButton = createMenuButton("Start Game");
        startButton.addActionListener(e -> {
            Difficulty selectedDifficulty = (Difficulty) difficultyComboBox.getSelectedItem();
            startGame(selectedDifficulty);
            dispose();
        });

        // Guide Button
        JButton guideButton = createMenuButton("How to Play");
        guideButton.addActionListener(e -> showGameGuide());

        // Leaderboard Button
        JButton leaderboardButton = createMenuButton("Leaderboard");
        leaderboardButton.addActionListener(e -> showLeaderboard());

        // Add components to main panel
        mainPanel.add(difficultyLabel);
        mainPanel.add(difficultyComboBox);
        mainPanel.add(startButton);
        mainPanel.add(guideButton);
        mainPanel.add(leaderboardButton);

        add(mainPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(new Color(100, 149, 237));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private void showGameGuide() {
        JOptionPane.showMessageDialog(this,
                "Minesweeper Game Guide:\n\n" +
                "1. Select a difficulty and start the game.\n" +
                "2. Click on cells to reveal them.\n" +
                "3. Avoid clicking on mines!\n" +
                "4. Numbers indicate how many mines are adjacent.\n" +
                "5. Use the 'burst' feature by clicking empty cells to reveal safe spaces.",
                "How to Play Minesweeper",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showLeaderboard() {
        leaderboard.sort((p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
        StringBuilder leaderboardText = new StringBuilder("Leaderboard:\n");
        int rank = 1;
        for (Player p : leaderboard) {
            leaderboardText.append(rank++).append(". ").append(p.getName()).append(": ").append(p.getScore()).append("\n");
        }

        JOptionPane.showMessageDialog(this,
                leaderboardText.toString(),
                "Leaderboard",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void startGame(Difficulty difficulty) {
        this.rows = difficulty.rows;
        this.cols = difficulty.cols;
        this.mines = difficulty.mines;
        this.score = 0;

        gameFrame = new JFrame("Minesweeper");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(cols * 50, rows * 50 + 80);
        gameFrame.setLayout(new BorderLayout());

        JPanel scorePanel = new JPanel();
        scorePanel.setBackground(new Color(60, 63, 65));
        scoreLabel = new JLabel("Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 18));
        scoreLabel.setForeground(Color.WHITE);
        scorePanel.add(scoreLabel);
        gameFrame.add(scorePanel, BorderLayout.NORTH);

        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(rows, cols));
        gamePanel.setBackground(Color.LIGHT_GRAY);
        cells = new Cell[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell(r, c);
                cells[r][c].setFont(new Font("Arial", Font.BOLD, 16));
                cells[r][c].setBackground(new Color(211, 211, 211));
                cells[r][c].addActionListener(this::handleClick);
                gamePanel.add(cells[r][c]);
            }
        }

        gameFrame.add(gamePanel, BorderLayout.CENTER);
        placeMines();
        gameFrame.setVisible(true);
    }

    private void handleClick(ActionEvent e) {
        if (gameOver) return;

        Cell cell = (Cell) e.getSource();
        if (cell.isRevealed()) return;

        cell.reveal();
        if (cell.hasMine()) {
            gameOver = true;
            revealAllMines();
            updateLeaderboard();
            askForRestart("You hit a mine! Restart?");
        } else {
            updateScore();
            if (cell.getAdjacentMines() == 0) {
                burst(cell.getRow(), cell.getCol());
            }
            if (score == (rows * cols - mines)) {
                updateLeaderboard();
                askForRestart("Congratulations! You won! Play again?");
            }
        }
    }

    private void updateLeaderboard() {
        boolean playerExists = false;
        for (Player player : leaderboard) {
            if (player.getName().equals(playerName)) {
                playerExists = true;
                if (score > player.getScore()) {
                    player.setScore(score);
                }
                break;
            }
        }

        if (!playerExists) {
            leaderboard.add(new Player(playerName, score));
        }
    }

    private void askForRestart(String message) {
        int option = JOptionPane.showConfirmDialog(gameFrame, message, "Game Over", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            gameFrame.dispose();
            new MinesweeperGame();
        } else {
            System.exit(0);
        }
    }

    private void burst(int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int newRow = row + dr;
                int newCol = col + dc;
                if (isInBounds(newRow, newCol) && !cells[newRow][newCol].isRevealed()) {
                    cells[newRow][newCol].reveal();
                    updateScore();
                    if (cells[newRow][newCol].getAdjacentMines() == 0) {
                        burst(newRow, newCol);
                    }
                }
            }
        }
    }

    private void placeMines() {
        Random random = new Random();
        int placedMines = 0;
        while (placedMines < mines) {
            int r = random.nextInt(rows);
            int c = random.nextInt(cols);
            if (!cells[r][c].hasMine()) {
                cells[r][c].setMine(true);
                placedMines++;
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (!cells[r][c].hasMine()) {
                    int adjacentMines = countAdjacentMines(r, c);
                    cells[r][c].setAdjacentMines(adjacentMines);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int newRow = row + dr;
                int newCol = col + dc;
                if (isInBounds(newRow, newCol) && cells[newRow][newCol].hasMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    private void revealAllMines() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].hasMine()) {
                    cells[r][c].reveal();
                }
            }
        }
    }

    private void updateScore() {
        score++;
        scoreLabel.setText("Score: " + score);
    }

    private class Cell extends JButton {
        private final int row, col;
        private boolean mine, revealed;
        private int adjacentMines;

        public Cell(int row, int col) {
            this.row = row;
            this.col = col;
            this.revealed = false;
        }

        public boolean hasMine() { return mine; }
        public void setMine(boolean mine) { this.mine = mine; }
        public boolean isRevealed() { return revealed; }

        public void reveal() {
            revealed = true;
            setEnabled(false);
            setBackground(hasMine() ? Color.RED : Color.LIGHT_GRAY);
            if (hasMine()) {
                setText("💣");
                setForeground(Color.BLACK);
            } else if (adjacentMines > 0) {
                setText(Integer.toString(adjacentMines));
                setForeground(getColorForNumber(adjacentMines));
            }
        }

        public int getAdjacentMines() { return adjacentMines; }
        public void setAdjacentMines(int count) { adjacentMines = count; }
        public int getRow() { return row; }
        public int getCol() { return col; }

        private Color getColorForNumber(int number) {
            if (number == 1) {
                return Color.BLUE;
            } else if (number == 2) {
                return Color.GREEN;
            } else if (number == 3) {
                return Color.RED;
            } else if (number == 4) {
                return new Color(0, 0, 128); // Dark Blue
            } else if (number == 5) {
                return new Color(139, 69, 19); // Brown
            } else if (number == 6) {
                return Color.CYAN;
            } else if (number == 7) {
                return Color.BLACK;
            } else if (number == 8) {
                return Color.GRAY;
            } else {
                return Color.BLACK; // Default color
            }
        }

    }

    private static class Player {
        private String name;
        private int score;

        public Player(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() { return name; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MinesweeperGame::new);
    }
}
