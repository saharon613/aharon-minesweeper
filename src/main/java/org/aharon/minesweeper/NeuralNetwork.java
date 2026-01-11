package org.aharon.minesweeper;

import java.io.*;
import java.util.Random;

public class NeuralNetwork {
    private int inputSize;
    private int hiddenSize;
    private int outputSize;

    private double[][] weightsInputHidden;
    private double[] biasHidden;
    private double[][] weightsHiddenOutput;
    private double[] biasOutput;

    private double learningRate = 0.01;
    private Random random;

    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;
        this.random = new Random();

        // Initialize weights and biases with random values
        weightsInputHidden = new double[inputSize][hiddenSize];
        biasHidden = new double[hiddenSize];
        weightsHiddenOutput = new double[hiddenSize][outputSize];
        biasOutput = new double[outputSize];

        initializeWeights();
    }

    private void initializeWeights() {
        // Xavier initialization
        double limitInputHidden = Math.sqrt(6.0 / (inputSize + hiddenSize));
        double limitHiddenOutput = Math.sqrt(6.0 / (hiddenSize + outputSize));

        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] = random.nextDouble() * 2 * limitInputHidden - limitInputHidden;
            }
        }

        for (int i = 0; i < hiddenSize; i++) {
            biasHidden[i] = 0;
            for (int j = 0; j < outputSize; j++) {
                weightsHiddenOutput[i][j] = random.nextDouble() * 2 * limitHiddenOutput - limitHiddenOutput;
            }
        }

        for (int i = 0; i < outputSize; i++) {
            biasOutput[i] = 0;
        }
    }

    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private double sigmoidDerivative(double x) {
        return x * (1.0 - x);
    }

    public double[] predict(double[] input) {
        // Forward pass
        double[] hidden = new double[hiddenSize];
        double[] output = new double[outputSize];

        // Input to hidden
        for (int i = 0; i < hiddenSize; i++) {
            hidden[i] = biasHidden[i];
            for (int j = 0; j < inputSize; j++) {
                hidden[i] += input[j] * weightsInputHidden[j][i];
            }
            hidden[i] = sigmoid(hidden[i]);
        }

        // Hidden to output
        for (int i = 0; i < outputSize; i++) {
            output[i] = biasOutput[i];
            for (int j = 0; j < hiddenSize; j++) {
                output[i] += hidden[j] * weightsHiddenOutput[j][i];
            }
            output[i] = sigmoid(output[i]);
        }

        return output;
    }

    public void train(double[] input, double[] targetOutput) {
        // Forward pass
        double[] hidden = new double[hiddenSize];
        double[] output = new double[outputSize];

        // Input to hidden
        for (int i = 0; i < hiddenSize; i++) {
            hidden[i] = biasHidden[i];
            for (int j = 0; j < inputSize; j++) {
                hidden[i] += input[j] * weightsInputHidden[j][i];
            }
            hidden[i] = sigmoid(hidden[i]);
        }

        // Hidden to output
        for (int i = 0; i < outputSize; i++) {
            output[i] = biasOutput[i];
            for (int j = 0; j < hiddenSize; j++) {
                output[i] += hidden[j] * weightsHiddenOutput[j][i];
            }
            output[i] = sigmoid(output[i]);
        }

        // Backward pass (backpropagation)
        double[] outputError = new double[outputSize];
        double[] outputDelta = new double[outputSize];

        // Calculate output error
        for (int i = 0; i < outputSize; i++) {
            outputError[i] = targetOutput[i] - output[i];
            outputDelta[i] = outputError[i] * sigmoidDerivative(output[i]);
        }

        // Calculate hidden error
        double[] hiddenError = new double[hiddenSize];
        double[] hiddenDelta = new double[hiddenSize];

        for (int i = 0; i < hiddenSize; i++) {
            hiddenError[i] = 0;
            for (int j = 0; j < outputSize; j++) {
                hiddenError[i] += outputDelta[j] * weightsHiddenOutput[i][j];
            }
            hiddenDelta[i] = hiddenError[i] * sigmoidDerivative(hidden[i]);
        }

        // Update weights and biases (hidden to output)
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weightsHiddenOutput[i][j] += learningRate * outputDelta[j] * hidden[i];
            }
        }

        for (int i = 0; i < outputSize; i++) {
            biasOutput[i] += learningRate * outputDelta[i];
        }

        // Update weights and biases (input to hidden)
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weightsInputHidden[i][j] += learningRate * hiddenDelta[j] * input[i];
            }
        }

        for (int i = 0; i < hiddenSize; i++) {
            biasHidden[i] += learningRate * hiddenDelta[i];
        }
    }

    public void writeToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeInt(inputSize);
            oos.writeInt(hiddenSize);
            oos.writeInt(outputSize);
            oos.writeDouble(learningRate);

            oos.writeObject(weightsInputHidden);
            oos.writeObject(biasHidden);
            oos.writeObject(weightsHiddenOutput);
            oos.writeObject(biasOutput);
        }
    }

    public static NeuralNetwork readFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            int inputSize = ois.readInt();
            int hiddenSize = ois.readInt();
            int outputSize = ois.readInt();
            double learningRate = ois.readDouble();

            NeuralNetwork nn = new NeuralNetwork(inputSize, hiddenSize, outputSize);
            nn.learningRate = learningRate;

            nn.weightsInputHidden = (double[][]) ois.readObject();
            nn.biasHidden = (double[]) ois.readObject();
            nn.weightsHiddenOutput = (double[][]) ois.readObject();
            nn.biasOutput = (double[]) ois.readObject();

            return nn;
        }
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }
}