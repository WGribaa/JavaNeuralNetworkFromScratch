/*
 * Made by Wael Gribaa in June 2019.
 * gGitHub : https://github.com/WGribaa
 * mail : g.wael@outlook.fr
 */

package com.wgribaa;

import java.util.Random;
import java.util.function.DoubleUnaryOperator;

public class NeuralNetwork {
    private double[][][] weights;
    private double[][] biases;
    private double learningRate;
    private DoubleUnaryOperator activation, derivatedActivation;

    public NeuralNetwork(int nbInputs, int[] hiddenArchitecture, int nbOutputs) {
        this(nbInputs, hiddenArchitecture, nbOutputs, 0.1);
    }

    public NeuralNetwork(int nbInputs, int[] hiddenArchitecture, int nbOutputs, double learningRate){
        this.learningRate = learningRate;
        weights = new double[hiddenArchitecture.length+1][][];
        biases = new double[hiddenArchitecture.length+1][];
        activation = x -> 1.0/(1.0+Math.exp(-x));
        derivatedActivation = y -> y*(1.0-y);

        // initialize Layers
        weights[0] = createLayer(hiddenArchitecture[0], nbInputs);
        biases[0] = createBiases(hiddenArchitecture[0]);
        for (int l = 1; l< hiddenArchitecture.length; l++){
            weights[l] = createLayer(hiddenArchitecture[l],hiddenArchitecture[l-1]);
            biases[l] = createBiases(hiddenArchitecture[l]);
        }
        weights[weights.length-1] = createLayer(nbOutputs, hiddenArchitecture[hiddenArchitecture.length-1]);
        biases[biases.length-1] = createBiases(nbOutputs);
    }

    double[] forward(double[] inputs) throws NonMatchingSizesException {
        if(inputs.length != weights[0][0].length){
            System.out.println("FORWARD ERROR : Number of inputs incorrect. Expected : "+weights[0][0].length+", got : "+inputs.length+".");
            return null;
        }
        double[] outputs = inputs;
        for (int l = 0; l< weights.length; l++){
            outputs = Matrix.matrixProduct(weights[l],outputs);
            outputs = Matrix.add(outputs, biases[l]);
            outputs = Matrix.map(outputs, activation);
        }
        return outputs;
    }

    void back(double[] inputs, double[] targets) throws NonMatchingSizesException {
        if(inputs.length != weights[0][0].length){
            System.out.println("BACK ERROR : Number of inputs incorrect. Expected : "+weights[0][0].length+", got : "+inputs.length+".");
            return;
        }
        if (targets.length != weights[weights.length-1].length){
            System.out.println("BACK ERROR : Number of outputs incorrect. Expected : "+weights[weights.length-1].length+", got : "+targets.length+".");
            return;
        }

        int nbL = weights.length;
        double[][][] deltaWeights = new double[nbL][][];
        double[][] deltaBiases = new double[nbL][];

        // forward pass with outputs of each layer stored.
        double[][] outputs = new double[nbL][];
        for (int l = 0; l< nbL; l++){
            outputs[l] = Matrix.matrixProduct(weights[l],l>0?outputs[l-1]:inputs);
            outputs[l] = Matrix.add(outputs[l], biases[l]);
            outputs[l] = Matrix.map(outputs[l], activation);
        }

        // outputs layer backPropagation
        double[] errors = Matrix.substract(targets, outputs[nbL-1]);
        double[] gradients = Matrix.map(outputs[nbL-1],derivatedActivation);
        gradients = Matrix.multiply(gradients, errors);
        gradients = Matrix.externalProduct(gradients, learningRate);
        deltaBiases[nbL-1] = gradients;

        deltaWeights[nbL-1] = Matrix.matrixProduct(gradients,outputs[nbL-2]);

        // hidden layers backPropagation
        for (int  l = nbL-2; l>=0; l--){
            errors = Matrix.matrixProduct(Matrix.transpose(weights[l+1]),errors);
            gradients = Matrix.map(outputs[l],derivatedActivation);
            gradients = Matrix.multiply(gradients, errors);
            gradients = Matrix.externalProduct(gradients, learningRate);
            deltaBiases[l] = gradients;
            deltaWeights[l] = Matrix.matrixProduct(gradients, l>0?outputs[l-1]:inputs);
        }

        // updating the weights and biases
        for (int l = 0; l<nbL;l++){
            weights[l] = Matrix.add(weights[l],deltaWeights[l]);
            biases[l] = Matrix.add(biases[l], deltaBiases[l]);
        }

    }


    /**
     * Create a layer as a 2d matrix
     * with random double.
     * @param nbNeurones Number of neurones of the layer.
     * @param nbInputs Number of inputs coming into the layer.
     * @return A randomized double[][] of correct sizes.
     */
    private double[][] createLayer(int nbNeurones, int nbInputs){
//        return debugCreateLayer(nbNeurones, nbInputs);
        double[][] ret = new double[nbNeurones][nbInputs];
        for (int n = 0; n< nbNeurones; n++){
            for (int  w = 0; w <nbInputs; w++){
                ret[n][w] = new Random().nextDouble();
            }
        }
        return ret;
    }


    /**
     * Create a layer of biases with random doubles.
     * @param n Number of neurones in the layer.
     * @return An array of random doubles.
     */
    private double[] createBiases(int n) {
//        return debugCreateBiases(n);
        double[] ret = new double[n];
        for (int i = 0; i< n; i++)
            ret[i] = new Random().nextDouble();
        return ret;
    }


    // START NEURAL NETWORK DESCRIPTION
    public String toString(){
        StringBuilder sb = new StringBuilder("Neural Network :\n");
        StringBuilder sbNeurones = new StringBuilder("\n");
        sb.append("Dimensions = ").append(weights.length).append(" layers\n");
        // Global Architecture :
        for (int i= 0; i<weights.length; i++) {
            sb.append("Layer #").append(i)
                    .append(" has ").append(weights[i].length).append(" neurones.\n");
            sbNeurones.append(layerToString(i));
        }

        sb.append(sbNeurones);
        return sb.toString();
    }
    private String layerToString(int layerIndex){
        StringBuilder sb = new StringBuilder("Layer #").append(layerIndex).append(" : \n");
        for (int j = 0; j< weights[layerIndex].length; j++){
            sb.append("\t").append(neuroneToString(layerIndex,j)).append("\n");
        }
        return sb.toString();
    }
    private String neuroneToString(int layer, int neurone){
        String separator = " | ";
        StringBuilder sb = new StringBuilder("Neurone [").append(layer)
                .append("][").append(neurone).append("] : ")
                .append("Bias = ").append(biases[layer][neurone]).append(separator);
        for (int k = 0; k<weights[layer][neurone].length; k++){
            sb.append("Weight #").append(k).append("=").append(weights[layer][neurone][k])
                    .append(separator);
        }
        sb.delete(sb.lastIndexOf(separator), sb.length());
        return sb.toString();
    }
    // END NEURAL NETWORK DESCRIPTION

}
