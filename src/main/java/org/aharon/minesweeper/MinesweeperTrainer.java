package org.aharon.minesweeper;

import java.io.File;
import java.util.Random;

public class MinesweeperTrainer {
    private static final int BOARD_SIZE = 5;
    private static final int NUM_MINES = 3;
    private static final int INPUT_SIZE = BOARD_SIZE * BOARD_SIZE;
    private static final int HIDDEN_SIZE = 128;
    private static final int OUTPUT_SIZE = BOARD_SIZE * BOARD_SIZE;
    private static final int NUM_GAMES = 1_000_000;
    private static final String MODEL_FILE = "minesweeper_model.nn";

    private NeuralNetwork network;
    private Random random;

    public MinesweeperTrainer() {
        random = new Random();

        // Step 1: Load or create neural network
        File modelFile = new File(MODEL_FILE);
        if (modelFile.exists()) {
            try {
                network = NeuralNetwork.readFromFile(MODEL_FILE);
                System.out.println("Loaded existing model from " + MODEL_FILE);
            } catch (Exception e) {
                System.out.println("Error loading model, creating new one: " + e.getMessage());
                network = new NeuralNetwork(INPUT_SIZE, HIDDEN_SIZE, OUTPUT_SIZE);
            }
        } else {
            network = new NeuralNetwork(INPUT_SIZE, HIDDEN_SIZE, OUTPUT_SIZE);
            System.out.println("Created new neural network");
        }
    }

    public void train() {
        int gamesWon = 0;
        int gamesLost = 0;

        for (int gameNum = 1; gameNum <= NUM_GAMES; gameNum++) {
            boolean won = playAndTrainGame();

            if (won) {
                gamesWon++;
            } else {
                gamesLost++;
            }

            // Print progress every 10,000 games
            if (gameNum % 10000 == 0) {
                double winRate = (double) gamesWon / gameNum * 100;
                System.out.printf("Game %d/%d - Win rate: %.2f%% (%d won, %d lost)%n",
                        gameNum, NUM_GAMES, winRate, gamesWon, gamesLost);
            }
        }

        // Step 12: Save the trained network
        try {
            network.writeToFile(MODEL_FILE);
            System.out.println("\nTraining complete! Model saved to " + MODEL_FILE);
            System.out.printf("Final stats - Games won: %d, Games lost: %d, Win rate: %.2f%%%n",
                    gamesWon, gamesLost, (double) gamesWon / NUM_GAMES * 100);
        } catch (Exception e) {
            System.err.println("Error saving model: " + e.getMessage());
        }
    }

    private boolean playAndTrainGame() {
        // Step 2: Create new game
        Minesweeper original = new Minesweeper();

        // Step 3: Make initial random move
        int[] firstMove = getRandomUnrevealedCell(original);
        original.reveal(firstMove[0], firstMove[1]);

        // Check if first move hit a mine (shouldn't happen with first-click safety)
        if (original.isGameOver()) {
            return false;
        }

        // Step 4-10: Training loop
        while (!original.isGameOver() && !original.isGameWon()) {
            // Step 4: Create deep copy
            Minesweeper copy = original.deepCopy();

            // Step 5: AutoFlag on the copy
            int flagsBefore = copy.getFlagCount();
            copy.autoFlag();
            int flagsAfter = copy.getFlagCount();
            boolean flagsAdded = flagsAfter > flagsBefore;

            // Step 6: Get input and output arrays
            double[] input = original.toInput();
            double[] output = copy.toOutput();

            // Step 7: Train the network
            network.train(input, output);

            // Step 8: Update original to match copy
            original = copy;

            // Step 9: Make next move
            if (flagsAdded) {
                // If flags were added, try AutoReveal
                original.autoReveal();
            }

            // If game still going, reveal a random cell
            if (!original.isGameOver() && !original.isGameWon()) {
                int[] nextMove = getRandomUnrevealedCell(original);
                if (nextMove != null) {
                    original.reveal(nextMove[0], nextMove[1]);
                }
            }
        }

        // Step 10: Return whether game was won
        return original.isGameWon();
    }

    private int[] getRandomUnrevealedCell(Minesweeper game) {
        // Find all unrevealed, unflagged cells
        int[][] available = new int[BOARD_SIZE * BOARD_SIZE][2];
        int count = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell cell = game.getCell(i, j);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    available[count][0] = i;
                    available[count][1] = j;
                    count++;
                }
            }
        }

        if (count == 0) {
            return null;
        }

        // Pick random cell from available
        int index = random.nextInt(count);
        return available[index];
    }

    public static void main(String[] args) {
        System.out.println("Starting Minesweeper Neural Network Training");
        System.out.println("Board size: " + BOARD_SIZE + "x" + BOARD_SIZE);
        System.out.println("Number of mines: " + NUM_MINES);
        System.out.println("Training games: " + NUM_GAMES);
        System.out.println();

        MinesweeperTrainer trainer = new MinesweeperTrainer();
        trainer.train();
    }
}