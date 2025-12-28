package org.aharon.minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class MinesweeperFrame extends JFrame {
    private Minesweeper game;
    private ColoredButton[][] buttons;
    private JLabel statusLabel;
    private JLabel flagLabel;
    private JLabel timerLabel;
    private Timer timer;
    private int elapsedTime;

    public MinesweeperFrame() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        game = new Minesweeper();
        buttons = new ColoredButton[game.getBoardSize()][game.getBoardSize()];

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(game.getBoardSize(), game.getBoardSize()));

        for (int i = 0; i < game.getBoardSize(); i++) {
            for (int j = 0; j < game.getBoardSize(); j++) {
                buttons[i][j] = new ColoredButton();
                buttons[i][j].setPreferredSize(new Dimension(50, 50));
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 16));
                buttons[i][j].setFocusPainted(false);
                final int row = i;
                final int col = j;

                buttons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            handleRightClick(row, col);
                        } else if (SwingUtilities.isLeftMouseButton(e)) {
                            handleLeftClick(row, col);
                        }
                    }
                });

                boardPanel.add(buttons[i][j]);
            }
        }

        statusLabel = new JLabel("Minesweeper - Click to reveal, Right-click to flag");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        flagLabel = new JLabel("Flags: 0");
        flagLabel.setFont(new Font("Arial", Font.BOLD, 18));

        timerLabel = new JLabel("Time: 000");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        elapsedTime = 0;
        timer = new Timer(1000, e -> {
            elapsedTime++;
            updateTimer();
        });
        timer.start();

        JButton resetButton = new JButton("New Game");
        resetButton.addActionListener(e -> resetGame());

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel infoPanel = new JPanel(new FlowLayout());
        infoPanel.add(flagLabel);
        infoPanel.add(resetButton);
        infoPanel.add(timerLabel);
        topPanel.add(infoPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleLeftClick(int row, int col) {
        game.reveal(row, col);
        updateBoard();
        updateStatus();
    }

    private void handleRightClick(int row, int col) {
        game.toggleFlag(row, col);
        updateBoard();
        updateFlagCounter();
    }

    private void updateBoard() {
        for (int i = 0; i < game.getBoardSize(); i++) {
            for (int j = 0; j < game.getBoardSize(); j++) {
                Cell cell = game.getCell(i, j);
                ColoredButton btn = buttons[i][j];

                if (cell.isRevealed()) {
                    btn.setEnabled(false);
                    if (cell.isMine()) {
                        btn.setText("💣");
                        btn.setBackground(Color.RED);
                    } else {
                        int adjacent = cell.getAdjacentMines();
                        btn.setText(adjacent > 0 ? String.valueOf(adjacent) : "");
                        btn.setBackground(new Color(192, 192, 192));
                        btn.setForeground(getNumberColor(adjacent));
                    }
                } else if (cell.isFlagged()) {
                    btn.setText("🚩");
                    btn.setEnabled(true);
                } else {
                    btn.setText("");
                    btn.setEnabled(true);
                }
            }
        }
    }

    private Color getNumberColor(int num) {
        switch (num) {
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            case 3: return Color.RED;
            case 4: return new Color(0, 0, 128);
            case 5: return new Color(128, 0, 0);
            case 6: return Color.CYAN;
            case 7: return Color.BLACK;
            case 8: return Color.GRAY;
            default: return Color.BLACK;
        }
    }

    private void updateStatus() {
        if (game.isGameOver()) {
            statusLabel.setText("Game Over! You hit a mine!");
            statusLabel.setForeground(Color.RED);
            timer.stop();
        } else if (game.isGameWon()) {
            statusLabel.setText("Congratulations! You won!");
            statusLabel.setForeground(Color.GREEN);
            timer.stop();
        }
    }

    private void updateFlagCounter() {
        flagLabel.setText("Flags: " + game.getFlagCount());
    }

    private void updateTimer() {
        timerLabel.setText(String.format("Time: %03d", elapsedTime));
    }

    private void resetGame() {
        game = new Minesweeper();
        statusLabel.setText("Minesweeper - Click to reveal, Right-click to flag");
        statusLabel.setForeground(Color.BLACK);

        elapsedTime = 0;
        updateTimer();
        updateFlagCounter();
        timer.restart();

        for (int i = 0; i < game.getBoardSize(); i++) {
            for (int j = 0; j < game.getBoardSize(); j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
                buttons[i][j].setBackground(null);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MinesweeperFrame());
    }
}