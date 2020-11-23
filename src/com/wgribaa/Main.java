/*
 * Made by Wael Gribaa in June 2019.
 * gGitHub : https://github.com/WGribaa
 * mail : g.wael@outlook.fr
 */

package com.wgribaa;

import java.io.File;


public class Main {

    public static void main(String[] args) {
//        colorBrain();
//        hexaBrain();
//        invertedHexaBrain();
        // Our NN architecture and parameters.
        int EPOCHS = 50;
        double learningRate = 0.01;
        int[] hiddenArchitecture = {50,50};
        NeuralNetwork mnistBrain = new NeuralNetwork(784, hiddenArchitecture, 10,
                learningRate);

        // Reading the Data with a convenient class, made for another project of mine!
        String datasetFolder ="mnist_dataset";
        MnistReader trainSet = new MnistReader(new File(datasetFolder+"/train-images.idx3-ubyte"),
                new File(datasetFolder+"/train-labels.idx1-ubyte"));
        MnistReader testSet = new MnistReader(new File(datasetFolder+"/t10k-images.idx3-ubyte"),
                new File(datasetFolder+"/t10k-labels.idx1-ubyte"));

        // Normalizing the input data
        // We get the mean and standard deviation of the train set.
        double[] mean_std = trainSet.calcNormalizer();
        double mean = mean_std[0], std = mean_std[1];
        // We normalize the trainset and the testset with the mean and std of the trainset !
        double[][] trainImages = trainSet.applyNormalizer(mean, std),
            testImages = testSet.applyNormalizer(mean, std);

        // Getting labels : "oneHot" for train, value for test.
        double[][] trainLabels = trainSet.formatLabels();
        int[] testAnswers = testSet.getLabelsInt();

        // Training !
        for (int epoch = 0; epoch<EPOCHS; epoch++) {
            for (int i = 0; i < trainImages.length; i++) {
                try {
                    mnistBrain.back(trainImages[i],
                            trainLabels[i]);
                } catch (NonMatchingSizesException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Epoch "+(epoch+1)+" / "+EPOCHS);
            // Predicting !
            int correct = 0;
            for (int i=0; i<testImages.length; i++){
                try {
                    int predicted = argMax(mnistBrain.forward(testImages[i]));
                    if (predicted ==testAnswers[i]) correct++;
                } catch (NonMatchingSizesException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Accuracy = "+correct+" / "+testImages.length);
        }
    }

    private static int argMax(double[] outputs){
        double maxi = 0;
        int answer = -1;
        for (int i =0; i< outputs.length; i++){
            if (outputs[i]>maxi){
                maxi=outputs[i];
                answer = i;
            }
        }
        return answer;
    }

}
