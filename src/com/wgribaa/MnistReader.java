/*
  Made by Wael Gribaa in June 2019.
  gGitHub : https://github.com/WGribaa
  mail : g.wael@outlook.fr
 */

package com.wgribaa;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MnistReader {
    private int labelsMagicNumber = 2049,
            imagesMagicNumber = 2051;
    private File imagesFile;
    private File labelsFile;
    private byte[][] images;
    private byte[] labels;
    private double[][] normalizedImages;
    private int rowCount = -1;

    /**
     * Main constructor.
     * The Mnist Reader needs two files : the images file and the corresponding labels file.
     * This constructor allows to enter each file as a File object or as a String of the pathFile.
     * @param images File or String of the images file.
     * @param labels File or String of the labels file.
     */
    MnistReader(Object images, Object labels){
        if (images.getClass()== File.class)
            imagesFile = (File)images;
        else if (images.getClass() == String.class)
            imagesFile = new File((String)images);
        if (labels.getClass()== File.class)
            labelsFile = (File)labels;
        else if (labels.getClass() == String.class)
            labelsFile = new File((String)labels);

        loadFile(imagesFile,imagesMagicNumber, true);
        loadFile(labelsFile, labelsMagicNumber,false);
    }

    byte[][] getImages() {
        return images;
    }
    double[][] getImagesDouble() {
        System.out.println(images.length + " images of length "+images[0].length);
        double[][] ret = new double[images.length][images[0].length];
        for (int i=0; i < ret.length; i++)
            ret[i] = toDoubleArray(images[i]);
        System.out.println(ret.length +" images returned of length "+ ret[0].length);
        return ret;
    }
    byte[] getLabels() {
        return labels;
    }
    int[] getLabelsInt(){
        int[] ret = new int[labels.length];
        for (int i=0; i<labels.length; i++)
            ret[i] = labels[i];
        return ret;
    }
    int getRowCount(){
        return rowCount;
    }
    int getColumnCount(){
        return images[0].length/rowCount;
    }

    private void loadFile(File file, int expectedMagicNumber, boolean isImage){
        FileInputStream fis = null;
        BufferedInputStream bis = null;
//                , bisLabels;
        byte[] buffer = new byte[4];

        // First : imagesFiles
        try {
            // loading the file as a BufferedInputStream
            fis= new FileInputStream(file);
            bis = new BufferedInputStream(fis);

            // checking if the magic number is correct.
            bis.read(buffer);
            int actualMagicNumber= bytesToInt(buffer);
            if (actualMagicNumber != expectedMagicNumber) {
                System.out.println(String.format("Expected magic number = %d but found %d",expectedMagicNumber, actualMagicNumber));
                return;
            }
            System.out.println("The magic number is correct for file "+file.getCanonicalPath());

            // reading the number of items in the file.
            buffer = new byte[4];
            bis.read(buffer);
            int numberOfItems = bytesToInt(buffer);
            System.out.println(String.format("There are %d items in the file",numberOfItems));

            // reading the number of rows and columns in the images
            if(isImage) {
                buffer = new byte[4];
                bis.read(buffer);
                rowCount = bytesToInt(buffer);
                buffer = new byte[4];
                bis.read(buffer);
                int numberOfColumns = bytesToInt(buffer);
                System.out.println(String.format("Images have %d rows and %d columns", rowCount, numberOfColumns));

                // loading all images as bytes[]
                images = new byte[numberOfItems][numberOfColumns * rowCount];
                byte[] pixels;
                for (int i = 0; i < numberOfItems; i++) {
                    pixels = new byte[numberOfColumns * rowCount];
                    bis.read(pixels);
                    images[i] = pixels;
                }
            } else{
                // loading all labels as bytes
                labels = new byte[numberOfItems];
                bis.read(labels);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (bis != null) bis.close();
                if(fis != null) fis.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static int bytesToInt(byte[] bytes){
        return bytes[0]<<24 | (bytes[1]&0xFF)<<16 | (bytes[2]&0xFF) <<8 | (bytes[3]&0xFF);

    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(images == null)
            sb.append("Images are not loaded");
        else if(labels == null)
            sb.append("Labels are not loaded");
        else{
            for (int i = 0; i<images.length; i++) {
                sb.append("\nImage ").append(i+1).append(" is a \"").append(labels[i]).append("\" ::\n");
                for (int p = 0; p<images[i].length; p++){
                    sb.append("\t").append(images[i][p]);
                    if((p+1)%rowCount==0)
                        sb.append("\n");
                }
            }
        }
        return sb.toString();


    }

    void describeFiles(){
        System.out.println("Images file = "+imagesFile.getAbsolutePath());
        System.out.println("File name = "+imagesFile.getName());
        System.out.println("Existing ? "+imagesFile.exists());
        System.out.println("True file ? "+imagesFile.isFile());

        System.out.println("Label Files = "+labelsFile.getAbsolutePath());
        System.out.println("File name = "+labelsFile.getName());
        System.out.println("Existing ? "+labelsFile.exists());
        System.out.println("True file ? "+labelsFile.isFile());
    }

    private static double[] toDoubleArray(byte[] byteArray){
        double[] doubles = new double[byteArray.length];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = byteArray[i]&0xFF;
        }
        return doubles;
    }

    double[][] formatLabels(){
        double[][]ret = new double[labels.length][10];
        for (int i =0; i<labels.length; i++)
            ret[i][labels[i]] = 1;
        return ret;
    }

    double[] calcNormalizer(){
        double mean = 0;
        for (byte[] image : images) {
            for (byte b : image)
                mean += b & 0xFF;
        }
        mean /= (images.length*images[0].length);
        double std = 0;
        for (byte[] image : images) {
            for (byte b : image)
                std += Math.pow((b & 0xFF)-mean, 2);
        }
        std = Math.pow( std / (images.length*images[0].length),0.5);
        return new double[]{mean, std};
    }

    double[][] applyNormalizer(double mean, double std){
        normalizedImages = new double[images.length][images[0].length];
        for (int i=0; i< images.length; i++){
            for (int j=0; j<images[i].length; j++)
                normalizedImages[i][j] = (((double)(images[i][j]&0xFF)) - mean) / std;
        }
        return normalizedImages;
    }


}
