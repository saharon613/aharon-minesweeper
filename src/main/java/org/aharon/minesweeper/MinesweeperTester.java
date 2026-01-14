package org.aharon.minesweeper;

import basicneuralnetwork.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MinesweeperTester {
    private static final int BOARD_SIZE = 5;
    private static final int NUM_GAMES = 1000;
    private static final String MODEL_FILE = "minesweeper_model.nn";
    private static final double FLAG_THRESHOLD = 0.9;

    private NeuralNetwork network;
    private Random random;

    public MinesweeperTester() {
        random = new Random();

        // Step 1: Load the trained neural network
        try {
            network = NeuralNetwork.readFromFile(MODEL_FILE);
            System.out.println("Loaded trained model from " + MODEL_FILE);
        } catch (Exception e) {
            System.err.println("Error loading model from " + MODEL_FILE);
            e.printStackTrace();  // prints the stack trace
            System.err.println("Make sure you've trained the model first by running MinesweeperTrainer");
            System.exit(1);
        }
    }

    public void test() {
        int gamesWon = 0;
        int gamesLost = 0;

        for (int gameNum = 1; gameNum <= NUM_GAMES; gameNum++) {
            boolean won = playGameWithNn();

            if (won) {
                gamesWon++;
            } else {
                gamesLost++;
            }

            // Print progress every 100 games
            if (gameNum % 100 == 0) {
                double winRate = (double) gamesWon / gameNum * 100;
                System.out.printf("Game %d/%d - Win rate: %.2f%% (%d won, %d lost)%n",
                        gameNum, NUM_GAMES, winRate, gamesWon, gamesLost);
            }
        }

        double finalWinRate = (double) gamesWon / NUM_GAMES * 100;
        System.out.println("\n=== TEST RESULTS ===");
        System.out.printf("Games won: %d%n", gamesWon);
        System.out.printf("Games lost: %d%n", gamesLost);
        System.out.printf("Win rate: %.2f%%%n", finalWinRate);

        if (finalWinRate > 50) {
            System.out.println("✓ Success! Win rate is above 50%");
        } else {
            System.out.println("✗ Win rate is below 50% - may need more training");
        }
    }

    private boolean playGameWithNn() {
        // Step 2: Create new game
        Minesweeper game = new Minesweeper();

        // Step 3: Make initial random move
        int[] firstMove = getRandomUnrevealedCell(game);
        game.reveal(firstMove[0], firstMove[1]);

        // Check if first move hit a mine (shouldn't happen)
        if (game.isGameOver()) {
            return false;
        }

        // Steps 4-9: Play the game using neural network
        while (!game.isGameOver() && !game.isGameWon()) {
            // Step 4: Get input from current game state
            double[] input = game.toInput();

            // Step 5: Get neural network prediction
            double[] output = network.guess(input);

            // Step 6: Flag cells where probability >= 0.9
            int flagsBefore = game.getFlagCount();
            flagHighProbabilityCells(game, output);
            int flagsAfter = game.getFlagCount();
            boolean flagsAdded = flagsAfter > flagsBefore;

            // Step 7: If flags were added, try AutoReveal
            if (flagsAdded) {
                game.autoReveal();
            }

            // Step 8: If no flags added, reveal random cell
            if (!flagsAdded && !game.isGameOver() && !game.isGameWon()) {
                int[] nextMove = getRandomUnrevealedCell(game);
                if (nextMove != null) {
                    game.reveal(nextMove[0], nextMove[1]);
                } else {
                    break; // No more moves available
                }
            }
        }

        // Step 9: Return result
        return game.isGameWon();
    }

    private void flagHighProbabilityCells(Minesweeper game, double[] predictions) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int index = i * BOARD_SIZE + j;
                Cell cell = game.getCell(i, j);

                // If prediction is high and cell is not revealed or flagged
                if (predictions[index] >= FLAG_THRESHOLD
                        && !cell.isRevealed()
                        && !cell.isFlagged()) {
                    game.toggleFlag(i, j);
                }
            }
        }
    }

    private int[] getRandomUnrevealedCell(Minesweeper game) {
        List<int[]> available = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell cell = game.getCell(i, j);
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    available.add(new int[]{i, j});
                }
            }
        }

        if (available.isEmpty()) {
            return null;
        }

        return available.get(random.nextInt(available.size()));
    }

    public static void main(String[] args) {
        System.out.println("Testing Minesweeper Neural Network");
        System.out.println("Board size: " + BOARD_SIZE + "x" + BOARD_SIZE);
        System.out.println("Test games: " + NUM_GAMES);
        System.out.println("Flag threshold: " + FLAG_THRESHOLD);
        System.out.println();

        MinesweeperTester tester = new MinesweeperTester();
        tester.test();
    }
}