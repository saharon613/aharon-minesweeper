package org.aharon.minesweeper;

import java.util.Random;

class Minesweeper {
    private static final int BOARD_SIZE = 9;
    private static final int NUM_MINES = 10;
    private static final int MAX_FLAGS = NUM_MINES;

    private Cell[][] board;
    private boolean gameOver;
    private boolean gameWon;
    private int cellsRevealed;
    private int flagCount;
    private boolean firstClick;

    public Minesweeper() {
        board = new Cell[BOARD_SIZE][BOARD_SIZE];
        initializeBoard();
        gameOver = false;
        gameWon = false;
        cellsRevealed = 0;
        flagCount = 0;
        firstClick = true;
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = new Cell();
            }
        }
    }

    private void placeMines(int excludeRow, int excludeCol) {
        Random rand = new Random();
        int minesPlaced = 0;

        while (minesPlaced < NUM_MINES) {
            int row = rand.nextInt(BOARD_SIZE);
            int col = rand.nextInt(BOARD_SIZE);
            if (!board[row][col].isMine() && !(row == excludeRow && col == excludeCol)) {
                board[row][col].setMine(true);
                minesPlaced++;
            }
        }
    }

    private void calculateAdjacentMines() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (!board[i][j].isMine()) {
                    int count = countAdjacentMines(i, j);
                    board[i][j].setAdjacentMines(count);
                }
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (isValidCell(newRow, newCol) && board[newRow][newCol].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isValidCell(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    public boolean reveal(int row, int col) {
        if (gameOver || gameWon || !isValidCell(row, col)) {
            return false;
        }

        if (firstClick) {
            placeMines(row, col);
            calculateAdjacentMines();
            firstClick = false;
        }

        Cell cell = board[row][col];

        if (cell.isRevealed() || cell.isFlagged()) {
            return false;
        }

        cell.setRevealed(true);
        cellsRevealed++;

        if (cell.isMine()) {
            gameOver = true;
            revealAllMines();
            return false;
        }

        if (cell.getAdjacentMines() == 0) {
            revealAdjacentCells(row, col);
        }

        checkWin();
        return true;
    }

    private void revealAdjacentCells(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = row + i;
                int newCol = col + j;
                if (isValidCell(newRow, newCol) && !board[newRow][newCol].isRevealed()) {
                    reveal(newRow, newCol);
                }
            }
        }
    }

    private void revealAllMines() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j].isMine()) {
                    board[i][j].setRevealed(true);
                }
            }
        }
    }

    public void toggleFlag(int row, int col) {
        if (gameOver || gameWon || !isValidCell(row, col)) {
            return;
        }

        Cell cell = board[row][col];
        if (cell.isRevealed()) {
            return;
        }

        if (cell.isFlagged()) {
            // always allow removing a flag
            cell.setFlagged(false);
            flagCount--;
        } else if (flagCount < MAX_FLAGS) {
            // only allow placing if under limit
            cell.setFlagged(true);
            flagCount++;
        }
    }

    private void checkWin() {
        int totalCells = BOARD_SIZE * BOARD_SIZE;
        int nonMineCells = totalCells - NUM_MINES;

        if (cellsRevealed == nonMineCells) {
            gameWon = true;
        }
    }

    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    public int getBoardSize() {
        return BOARD_SIZE;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public int getFlagCount() {
        return flagCount;
    }

    public int getNumMines() {
        return NUM_MINES;
    }
}