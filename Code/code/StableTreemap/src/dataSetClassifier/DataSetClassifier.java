/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataSetClassifier;

import UserControl.Simulator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import treemap.DataFaciliation.DataFacilitator;
import treemap.dataStructure.DataMap;

/**
 * This class classifies each dataset according to its attributes
 *
 * @author msondag
 */
public class DataSetClassifier {

    List<DataFacilitator> datasets;

    public static void main(String args[]) {
        List<DataFacilitator> datasets = Simulator.getDataFacilitatorFromFolder(new File("D:\\Development\\TreemapStability\\classifierSets"));
        System.out.println("got the datasets");
        DataSetClassifier dsc = new DataSetClassifier(datasets);
        System.out.println("start classifying");
        dsc.classifyDataSets();
    }

    public DataSetClassifier(List<DataFacilitator> datasets) {
        this.datasets = datasets;
    }

    public void classifyDataSets() {
        StringBuilder sb = new StringBuilder();
        sb.append("Verify that all properties are correct \r\n");
        sb.append("title,"
                + "uniqueNodes,"
                + "maxNodes,"
                + "maxHeight,"
                + "meanInbalance,"
                + "medianDegree,"
                + "medianRelativeNodeSize,"
                + "numberOfTimeSteps,"
                + "maxValueChangeRate,"
                + "medianValueChangeRate,"
                + "maxChangeRate,"
                + "medianChangeRate,"
                + "sizeVariance\r\n");

        for (DataFacilitator dataset : datasets) {
            classifyDataSet(sb, dataset);
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("D:\\Development\\TreemapStability\\datasetsReadable.csv"))) {
            //header row

            bw.append(sb);//Internally it does aSB.toString();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void classifyDataSet(StringBuilder sb, DataFacilitator dataset) {
        //stringBuilder will store the classification

        int uniqueNodes = getUniqueNodeAmount(dataset); //get the total amount of nodes in the tree
        int maxNodes = getMaxNodes(dataset); //get the maximum amount of nodes in the tree at a point in time
        int maxHeight = getHeight(dataset); //gets the maximum height of the tree        

        List<Integer> degreeOfNodes = getDegrees(dataset); //gets the distribution of degrees of nodes(aggregated over all time)
        double medianDegree = getMedian(intToDouble(degreeOfNodes));

        //TODO: node sizes
        List<Double> nodeSizes = getSizes(dataset); //get the distribution of node sizes(aggregated over all time)
        double medianRelativeNodeSize = getMedian(nodeSizes);

        List<Double> inbalances = getInbalances(dataset);
        double meanInbalance = getMean(inbalances);

        int numberOfTimeSteps = getTimeSteps(dataset);

        List<Double> dataValueRates = getValueChangeRates(dataset); //returns how much the data changed per timestep.//TODO: quantify data change
        double maxValueChangeRate = getMax(dataValueRates);
        double medianValueChangeRate = getMedian(dataValueRates);

        List<Double> deletionAmount = getDeletionAmount(dataset); //returns how many nodes where deleted per time step
        List<Double> additionAmount = getAdditionAmount(dataset); //returns how many nodes where added per time step
        List<Double> changeAmount = new ArrayList();
        changeAmount.addAll(deletionAmount);
        changeAmount.addAll(additionAmount);
        double maxChangeRate = getMax(changeAmount);
        double medianChangeRate = getMedian(changeAmount);

        double sizeVariance = getVariance(nodeSizes);
        writeClassification(sb, dataset.getDataIdentifier(), uniqueNodes, maxNodes, maxHeight, meanInbalance, medianDegree, medianRelativeNodeSize, numberOfTimeSteps, maxValueChangeRate, medianValueChangeRate, maxChangeRate, medianChangeRate,sizeVariance);
    }

    private int getUniqueNodeAmount(DataFacilitator dataSet) {
        Set<String> nodeNames = new HashSet();
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            List<DataMap> allNodes = data.getAllChildren();
            allNodes.add(data);
            for (DataMap dm : allNodes) {
                nodeNames.add(dm.getLabel());
            }
        }
        return nodeNames.size();
    }

    private int getMaxNodes(DataFacilitator dataSet) {
        int maxNodes = 0;
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            List<DataMap> allNodes = data.getAllChildren();
            allNodes.add(data);
            maxNodes = Math.max(allNodes.size(), maxNodes);
        }
        return maxNodes;
    }

    private int getHeight(DataFacilitator dataSet) {
        int maxHeight = 0;
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            int height = getHeight(data);

            maxHeight = Math.max(height, maxHeight);
        }
        return maxHeight;
    }

    private int getHeight(DataMap data) {
        int maxHeight = 0;
        for (DataMap dm : data.getChildren()) {
            maxHeight = Math.max(getHeight(dm) + 1, maxHeight);
        }
        return maxHeight;
    }

    private List<Integer> getDegrees(DataFacilitator dataSet) {
        List<Integer> degrees = new ArrayList();
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            List<DataMap> allNodes = data.getAllChildren();
            allNodes.add(data);
            for (DataMap dm : allNodes) {
                //degree 1 and 0 are not interesting
                if (dm.hasChildren() && dm.getChildren().size() > 1) {
                    degrees.add(dm.getChildren().size());
                }
            }

        }
        return degrees;
    }

    /**
     * Returns a list of relative sizes. For each leaf node we store the
     * relative size compared to its parents
     *
     * @param dataSet
     * @return
     */
    private List<Double> getSizes(DataFacilitator dataSet) {
        List<Double> sizes = new ArrayList();
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            List<DataMap> allNodes = data.getAllChildren();
            for (DataMap dm : allNodes) {
                sizes.add(dm.getTargetSize() / data.getTargetSize());
                if (!dm.hasChildren()) {
//                    List<Double> childSizes = new ArrayList();
//                    double minValue = Double.MAX_VALUE;
//                    for (DataMap child : allNodes) {
//                        childSizes.add(child.getTargetSize());
//                        minValue = Math.min(minValue, child.getTargetSize());
//                    }
//                    //store the normalized list.
//                    for (DataMap child : allNodes) {
//                        sizes.add(child.getTargetSize() / minValue);
////                        sizes.add(child.getTargetSize());
//                    }
                }
            }
        }
        return sizes;
    }

    private List<Double> getInbalances(DataFacilitator dataSet) {
        List<Double> inbalances = new ArrayList();
        for (int time = 0; time < dataSet.getMaxTime(); time++) {
            DataMap data = dataSet.getData(time);
            List<DataMap> allNodes = data.getAllChildren();
            allNodes.add(data);
            for (DataMap dm : allNodes) {

                if (dm.hasChildren() && dm.getChildren().size() > 1) {
                    double minValue = Double.MAX_VALUE;
                    double maxValue = Double.MIN_VALUE;
                    for (DataMap child : dm.getChildren()) {
                        minValue = Math.min(minValue, child.getHeight());
                        maxValue = Math.max(maxValue, child.getHeight());
                    }
                    //store the inbalance
                    inbalances.add(maxValue / minValue);
                }
            }
        }
        return inbalances;
    }

    private int getTimeSteps(DataFacilitator dataset) {
        return dataset.getMaxTime();
    }

    private List<Double> getValueChangeRates(DataFacilitator dataset) {
        List<Double> medianDataChanges = new ArrayList();
        for (int t1 = 0; t1 < (dataset.getMaxTime() - 1); t1++) {
            //get the data change for a single timestep
            List<Double> dataChanges = new ArrayList();
//            double sumOfChanges = 0;
            double amountPresent = 0;

            DataMap dataT1 = dataset.getData(t1);
            List<DataMap> allNodesT1 = dataT1.getAllChildren();
            allNodesT1.add(dataT1);

            DataMap dataT2 = dataset.getData(t1 + 1);
            List<DataMap> allNodesT2 = dataT2.getAllChildren();
            allNodesT1.add(dataT2);

            double tw1 = dataT1.getTargetSize();
            double tw2 = dataT2.getTargetSize();

            for (DataMap dm1 : allNodesT1) {
                for (DataMap dm2 : allNodesT2) {
                    if (dm1.getLabel().equals(dm2.getLabel())) {
                        //dm1 is present in dm2
//                        double change = Math.abs(dm2.getTargetSize() - dm1.getTargetSize());
//                        double change = dm2.getTargetSize()/dm1.getTargetSize();

//                        double change = Math.max(dm2.getTargetSize()/dm1.getTargetSize(),dm1.getTargetSize()/dm2.getTargetSize());
//                        dataChange.add(change);
//                        amountPresent++;
//                        double change = Math.max(dm2.getTargetSize() / dm1.getTargetSize(), dm1.getTargetSize() / dm2.getTargetSize());
//                        totalChangeRatio += change;
                        amountPresent++;
                        double w1 = dm1.getTargetSize() / tw1;
                        double w2 = dm2.getTargetSize() / tw2;
                        dataChanges.add(Math.abs(w1 - w2));

                        break;//no need to look further for dm2
                    }
                }
            }
            if (amountPresent == 0) {//used if the entire dataset changed
                continue;
            }
            medianDataChanges.add(getMedian(dataChanges));
        }
        return medianDataChanges;
    }

    private List<Double> getDeletionAmount(DataFacilitator dataset) {
        List<Double> deletionAmounts = new ArrayList();
        for (int t1 = 0; t1 < (dataset.getMaxTime() - 1); t1++) {
            int amountDeleted = 0;

            DataMap dataT1 = dataset.getData(t1);
            List<DataMap> allNodesT1 = dataT1.getAllChildren();
            allNodesT1.add(dataT1);

            DataMap dataT2 = dataset.getData(t1 + 1);
            List<DataMap> allNodesT2 = dataT2.getAllChildren();
            allNodesT2.add(dataT2);

            for (DataMap dm1 : allNodesT1) {
                boolean found = false;
                for (DataMap dm2 : allNodesT2) {
                    if (dm1.getLabel().equals(dm2.getLabel())) {
                        found = true;
                        break;//no need to look further for dm2
                    }
                }
                if (!found) {
                    amountDeleted++;
                }
            }

            deletionAmounts.add((double) amountDeleted / (double) allNodesT1.size());
        }
        return deletionAmounts;
    }

    private List<Double> getAdditionAmount(DataFacilitator dataset) {
        List<Double> addedAmounts = new ArrayList();
        for (int t1 = 0; t1 < (dataset.getMaxTime() - 1); t1++) {
            int amountAdded = 0;

            DataMap dataT1 = dataset.getData(t1);
            List<DataMap> allNodesT1 = dataT1.getAllChildren();
            allNodesT1.add(dataT1);

            DataMap dataT2 = dataset.getData(t1 + 1);
            List<DataMap> allNodesT2 = dataT2.getAllChildren();
            allNodesT1.add(dataT2);

            for (DataMap dm2 : allNodesT2) {
                boolean found = false;
                for (DataMap dm1 : allNodesT1) {
                    if (dm1.getLabel().equals(dm2.getLabel())) {
                        found = true;
                        break;//no need to look further for dm2
                    }
                }
                if (!found) {
                    amountAdded++;
                }
            }
            addedAmounts.add((double) amountAdded / (double) allNodesT1.size());
        }
        return addedAmounts;
    }

    private double getMean(List<Double> doubleList) {

        double mean = 0;
        for (double d : doubleList) {
            mean += d;
        }
        return mean / (double) doubleList.size();
    }

    private double getMedian(List<Double> doubleList) {

        Collections.sort(doubleList);
        return doubleList.get(doubleList.size() / 2);
    }

    private double getMax(List<Double> doubleList) {
        double max = 0;
        for (double d : doubleList) {
            max = Math.max(max, d);
        }
        return max;
    }

    private double getVariance(List<Double> doubleList) {
        double mean = getMean(doubleList);
        double variance = 0;
        for (Double d : doubleList) {
            variance += Math.pow((d - mean), 2);
        }
        return variance/doubleList.size();
    }

    private List<Double> intToDouble(List<Integer> degreeOfNodes) {
        List<Double> doubles = new ArrayList();
        for (Integer i : degreeOfNodes) {
            doubles.add((double) i);
        }
        return doubles;
    }

    private void writeClassification(StringBuilder sb, String dataIdentifier, int uniqueNodes, int maxNodes, int maxHeight, double meanInbalance,
            double medianDegree, double medianRelativeNodeSize, int numberOfTimeSteps,
            double maxDataChangeRate, double medianDataChangeRate, double maxChangeRate, double medianChangeRate,double sizeVariance) {

        String dataSetName = dataIdentifier.substring(dataIdentifier.lastIndexOf("\\") + 1, dataIdentifier.lastIndexOf("."));
        sb.append(dataSetName);
        sb.append("," + uniqueNodes);
        sb.append("," + maxNodes);
        sb.append("," + maxHeight);
        sb.append("," + meanInbalance);
        sb.append("," + medianDegree);
        sb.append("," + medianRelativeNodeSize);
        sb.append("," + numberOfTimeSteps);
        sb.append("," + maxDataChangeRate);
        sb.append("," + medianDataChangeRate);
        sb.append("," + maxChangeRate);
        sb.append("," + medianChangeRate);
        sb.append("," + sizeVariance + "\r\n");

    }

}
