package treemap;

import TreeMapGenerator.IncrementalAlgorithm.IncrementalLayout;
import TreeMapGenerator.IncrementalChanges.OrderEquivalenceGraph;
import TreeMapGenerator.IncrementalChanges.TreeMapChangeGenerator;
import statistics.StatisticalTracker;
import TreeMapGenerator.TreeMapGenerator;
import UserControl.CommandController;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.DataFaciliation.DataFacilitator;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public class ModelController {

    /**
     * Holds from which treemap seed we started performing incremental moves
     */
    private int initialMoveStart = -1;

    private final CommandController commandController;
    private TreeMapGenerator treeMapGenerator;
    private DataFacilitator dataFacilitator;
    private StatisticalTracker statisticalTracker;
    private TreeMap currentTreeMap;

    /**
     * Holds the current treemap on which moves are performed. Might not be the
     * same treemap as currentTreeMap. Used for performance issues
     */
    private TreeMap currentMoveTreeMap;

    public ModelController(CommandController commandController) {
        this.commandController = commandController;
        statisticalTracker = new StatisticalTracker(this);
    }

    /**
     * Key is a string of the format "Time;dataMap;Algorithm" which returns the
     * treemap at the given time for the given algorithm
     */
    HashMap<String, TreeMap> treeMapStorage = new HashMap();

    public void setTimeoutTreeMap(int time, Rectangle treeMapRectangle, String commandIdentifier) {
        if (dataFacilitator == null || treeMapGenerator == null) {
            return;
        }
        //Store the initial seed
        initialMoveStart = time;

        DataMap dataMap = dataFacilitator.getData(time);
        if (dataMap == null) {
            return;
        }
        //get the last
        String key = time - 1 + ";" + dataMap + ";" + treeMapGenerator.getClass().getName();
        currentTreeMap = treeMapStorage.get(key);
        //update to next
        key = time + ";" + dataMap + ";" + treeMapGenerator.getClass().getName();
        treeMapStorage.put(key, currentTreeMap);
    }

    /**
     *
     * @param time The time seed to generate the treemap
     * @param treeMapRectangle
     * @param commandIdentifier
     * @return
     */
    public TreeMap getTreeMap(int time, boolean useStored, Rectangle treeMapRectangle, String commandIdentifier) {
        if (dataFacilitator == null || treeMapGenerator == null) {
            return null;
        }
        //Store the initial seed
        initialMoveStart = time;

        DataMap dataMap = dataFacilitator.getData(time);
        if (dataMap == null) {
            return null;
        }
        String key = time + ";" + dataMap + ";" + treeMapGenerator.getClass().getName();
        if (useStored) {

            if (treeMapStorage.containsKey(key)) {
                System.out.println("stores");
                currentTreeMap = treeMapStorage.get(key);
            } else {
                //get the new data for the treemap
                //generate the treemap according to the selected algorithm
                currentTreeMap = treeMapGenerator.generateTreeMap(dataMap, treeMapRectangle);
                treeMapStorage.put(key, currentTreeMap);
            }
        } else { //don't use stored. Used for debugging purposes.

            //get the new data for the treemap
            //generate the treemap according to the selected algorithm
            currentTreeMap = treeMapGenerator.generateTreeMap(dataMap, treeMapRectangle);
            treeMapStorage.put(key, currentTreeMap);
        }
        //get statistical properties of the new treeMap
        statisticalTracker.treeMapUpdated(currentTreeMap, dataFacilitator, treeMapGenerator, commandIdentifier);

        return currentTreeMap;
    }

    /**
     * Changes the weights of the treemap while maintaining the same order
     * equivalence graph
     *
     * @param time
     * @return
     */
    public TreeMap updateCurrentTreeMap(int time) {
        if (dataFacilitator == null || currentTreeMap == null) {
            return null;
        }
        //get the new data for the treemap
        DataMap dataMap = dataFacilitator.getData(time);
        //TODO assert that dataMap has the same structure as the treemap
        TreeMapChangeGenerator tmChangeGenerator = new TreeMapChangeGenerator(currentTreeMap);
        currentTreeMap = tmChangeGenerator.updateWeights(dataMap);
        //get statistical properties of the new treeMap
        statisticalTracker.treeMapUpdated(currentTreeMap, dataFacilitator, treeMapGenerator, "simulation");
        return currentTreeMap;
    }

    public void setOutputFile(File outputFile) {
        statisticalTracker.setOutputFile(outputFile,false);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters and setters">
    public void setTreeMapGenerator(TreeMapGenerator treeMapGenerator) {
        this.treeMapGenerator = treeMapGenerator;
    }

    public TreeMapGenerator getTreeMapGenerator(TreeMapGenerator treeMapGenerator) {
        return treeMapGenerator;
    }

    public void setDataFacilitator(DataFacilitator dataFacilitator) {
        this.dataFacilitator = dataFacilitator;
    }

    public DataFacilitator getDataFacilitator() {
        return dataFacilitator;
    }
    //</editor-fold>


    public void setOrderStability(Map<String, Double> stabilities) {
        commandController.setStability(stabilities);
    }

    public void takeSnapShot(String identifier) {
        statisticalTracker.takeSnapShot(identifier);
    }

    public TreeMap getCurrentTreeMap() {
        return currentTreeMap;
    }

    /**
     * Gets a newly generated treemap but does not change the initial seed
     *
     * @param time
     * @param treeMapRectangle
     * @param commandIdentifier
     * @return
     */
    public TreeMap getNewGeneratedTreeMap(int time, boolean useStored, Rectangle treeMapRectangle, String commandIdentifier) {
        int initialSeed = this.initialMoveStart;
        getTreeMap(time, useStored, treeMapRectangle, commandIdentifier);
        this.initialMoveStart = initialSeed;
        return currentTreeMap;
    }

    public TreeMap getMovesTreeMap(int currentTime, boolean useStored, int moveAmount, Rectangle treeMapRectangle, String commandIdentifier) {

        if (initialMoveStart > currentTime) {
            return null;
        }
        if (moveAmount == 0) {
            //take the layout from the initial move
            getNewGeneratedTreeMap(initialMoveStart, useStored, treeMapRectangle, commandIdentifier);
            //update it with the weight of the current treeMap
            updateCurrentTreeMap(currentTime);
            return currentTreeMap;
        }

        //at least one move performed. We only allow for treemaps on which moves
        //have already been performed for performance issues. We thus
        //show the treemap that was generated using the simulation
        return currentMoveTreeMap;

    }

    public void newStatisticsFile(File outputFile,boolean directory) {
        statisticalTracker.setOutputFile(outputFile,directory);
    }

    public void closeStatisticsOutput() {
        statisticalTracker.closeOutput();
    }

    public TreeMap performMove() {
        if (treeMapGenerator.getClass().equals(IncrementalLayout.class)) {
            IncrementalLayout generator = (IncrementalLayout) treeMapGenerator;
            return generator.performMove();
        }
        return currentTreeMap;
    }

}
