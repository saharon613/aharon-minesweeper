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
            cell.setFlagged(false);
            flagCount--;
        } else if (flagCount < MAX_FLAGS) {
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

    public void autoFlag() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell cell = board[i][j];
                if (!cell.isRevealed() || cell.getAdjacentMines() == 0) {
                    continue;
                }
                int[] counts = countNeighbors(i, j);
                if (counts[0] + counts[1] == cell.getAdjacentMines()) {
                    flagHiddenNeighbors(i, j);
                }
            }
        }
    }

    public void autoReveal() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell cell = board[i][j];
                if (!cell.isRevealed() || cell.getAdjacentMines() == 0) {
                    continue;
                }
                int[] counts = countNeighbors(i, j);
                if (counts[1] == cell.getAdjacentMines()) {
                    revealHiddenNeighbors(i, j);
                }
            }
        }
    }

    private int[] countNeighbors(int row, int col) {
        int hidden = 0;
        int flagged = 0;
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                if (di == 0 && dj == 0) {
                    continue;
                }
                int ni = row + di;
                int nj = col + dj;
                if (isValidCell(ni, nj)) {
                    Cell n = board[ni][nj];
                    if (n.isFlagged()) {
                        flagged++;
                    } else if (!n.isRevealed()) {
                        hidden++;
                    }
                }
            }
        }
        return new int[]{hidden, flagged};
    }

    private void flagHiddenNeighbors(int row, int col) {
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                if (di == 0 && dj == 0) {
                    continue;
                }
                int ni = row + di;
                int nj = col + dj;
                if (isValidCell(ni, nj)) {
                    Cell n = board[ni][nj];
                    if (!n.isRevealed() && !n.isFlagged() && flagCount < MAX_FLAGS) {
                        n.setFlagged(true);
                        flagCount++;
                    }
                }
            }
        }
    }

    private void revealHiddenNeighbors(int row, int col) {
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                if (di == 0 && dj == 0) {
                    continue;
                }
                int ni = row + di;
                int nj = col + dj;
                if (isValidCell(ni, nj) && !board[ni][nj].isRevealed() && !board[ni][nj].isFlagged()) {
                    reveal(ni, nj);
                }
            }
        }
    }

    public Minesweeper deepCopy() {
        Minesweeper copy = new Minesweeper();

        copy.gameOver = this.gameOver;
        copy.gameWon = this.gameWon;
        copy.cellsRevealed = this.cellsRevealed;
        copy.flagCount = this.flagCount;
        copy.firstClick = this.firstClick;

        copy.board = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                copy.board[i][j] = this.board[i][j].deepCopy();
            }
        }

        return copy;
    }

    public double[] toInput() {
        double[] input = new double[BOARD_SIZE * BOARD_SIZE];
        int index = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell cell = board[i][j];

                if (cell.isFlagged()) {
                    input[index] = 1.0;
                } else if (cell.isRevealed()) {
                    int adjacentMines = cell.getAdjacentMines();
                    input[index] = (adjacentMines + 1) * 0.1;
                } else {
                    input[index] = 0.0;
                }

                index++;
            }
        }

        return input;
    }

    public double[] toOutput() {
        double[] output = new double[BOARD_SIZE * BOARD_SIZE];
        int index = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell cell = board[i][j];

                if (cell.isMine()) {
                    output[index] = 1.0;
                } else {
                    output[index] = 0.0;
                }

                index++;
            }
        }

        return output;
    }
}