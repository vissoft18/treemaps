package UserControl;

import TreeMapGenerator.*;
import TreeMapGenerator.HilbertMoore.HilbertTreeMap;
import TreeMapGenerator.HilbertMoore.MooreTreeMap;
import TreeMapGenerator.IncrementalAlgorithm.IncrementalLayout;
import TreeMapGenerator.Pivot.*;
import TreeMapGenerator.TreeMapGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import statistics.Baseline.BaseLineGenerator;
import treemap.DataFaciliation.DataFacilitator;
import treemap.DataFaciliation.DataFileManager;
import treemap.DataFaciliation.DataFileManagerFast;
import treemap.ModelController;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public class Simulator extends SimulatorMaster {

    /**
     * where the input data is stored. In the case of baseline this should be a
     * single file
     */
    private File inputFolder;
    /**
     * where the output rectangles will be sent to
     */
    private File outputFolder;
    /**
     * the width of the input rectangle
     */
    private int width;
    /**
     * the height of the input rectangle
     */
    private int height;
    /**
     * the technique used
     */
    private String technique;
    /**
     * whether we are going to generate baselines
     */
    private boolean generateBaseLines = false;

    public static void main(String args[]) {
        new Simulator(args);
    }

    public Simulator(String args[]) {
        super();
        parseArguments(args);
        treeMapRectangle = new Rectangle(0, 0, width, height);
        runExperiments();
    }

    private void parseArguments(String args[]) {
        List<String> argumentList = Arrays.asList(args);
        ListIterator<String> it = argumentList.listIterator();
        while (it.hasNext()) {
            String arg = it.next();
            System.out.println("arg = " + arg);
            switch (arg) {
                case "-technique":
                    technique = it.next();
                    System.out.println("technique = " + technique);
                    break;
                case "-baseline":
                    generateBaseLines = Boolean.parseBoolean(it.next());
                    System.out.println("generateBaseLines = " + generateBaseLines);
                    break;
                case "-inputfolder":
                    inputFolder = new File(it.next());
                    System.out.println("inputFolder = " + inputFolder);
                    break;
                case "-outputfolder":
                    outputFolder = new File(it.next());
                    System.out.println("outputFolder = " + outputFolder);
                    break;
                case "-width":
                    width = Integer.parseInt(it.next());
                    System.out.println("width = " + width);
                    break;
                case "-height":
                    height = Integer.parseInt(it.next());
                    System.out.println("height = " + height);
                    break;
            }
        }
    }

    public Simulator() {
        super();
        treeMapRectangle = new Rectangle(0, 0, 1920, 1080);
        runExperiments();
    }

    @Override
    public void setStability(Map<String, Double> stabilities) {
        //Do nothing, not needed
    }

    @Override
    public void setAspectRatioBeforeMoves(double maxAspectRatio) {
        //Do nothing, not needed
    }

    @Override
    public void setAspectRatioAfterMoves(double maxAspectRatio) {
        //Do nothing, not needed
    }

    @Override
    protected TreeMap updateCurrentTreeMap(int time) {
        return modelController.updateCurrentTreeMap(time);
    }

    @Override
    protected boolean getTreeMap(int time, boolean useStored, String commandIdentifier) {
//        TreeMap nextTreeMap = modelController.getTreeMap(time, false, treeMapRectangle, "noStability");
        TreeMap nextTreeMap = modelController.getTreeMap(time, false, treeMapRectangle, commandIdentifier);
        if (nextTreeMap == null) {
            return false;
        } else {
//            updateTreeMap(nextTreeMap);
            return true;
        }
    }

    public void setTimeOutTreeMap(int time, String commandIdentifier) {
        modelController.setTimeoutTreeMap(time, treeMapRectangle, commandIdentifier);
    }

    public void closeStatisticsOutput() {
        modelController.closeStatisticsOutput();
    }

    public void newStatisticsOutput(File outputFile, boolean directory) {
        modelController.newStatisticsFile(outputFile, directory);
    }

    private void runExperiments() {
        BaseLineGenerator blg = new BaseLineGenerator();

        int timeSteps = 100;
        List<TreeMapGenerator> generators = getTreeMapGenerators();
        System.out.println("generators.size() = " + generators.size());
        List<DataFacilitator> facilitators = getDataFacilitators();
        //TODO REVERIFY THAT THE ORDER DOES NOT MATTER

        for (TreeMapGenerator generator : generators) {
            System.out.println("starting with  with generator: " + generator.getClass().getName());
            long startTime = System.currentTimeMillis();
            //get the facilitators later to make sure they are refreshed for every data set

            for (DataFacilitator facilitator : facilitators) {
                System.out.println("facilitator = " + facilitator.getExperimentName());
//                modelController.newStatisticsFile(new File("experiment/" + generator.getClass().getName() + ";" + generator.getParamaterDescription()));
                String name = facilitator.getDataIdentifier();
                name = name.substring(name.lastIndexOf("\\") + 1);
                String faceOutputString = "" + outputFolder.getAbsoluteFile() + "\\" + name;
                System.out.println("faceOutputString = " + faceOutputString);
                File facOutput = new File(faceOutputString);

                modelController.newStatisticsFile(facOutput, true);

                //need to reinitialize the generator after every run to make sure it is not persistent
                //for the incremental algorithms
                generator = generator.reinitialize();
                //in case the facilitator is random, we put a seed into it
                facilitator.reinitializeWithSeed(0);

                Experiment e = new Experiment(facilitator, generator, timeSteps, this);
                e.runExperiment();

                if (generateBaseLines) {
                    File baseLineOutputFolder = new File("" + outputFolder.getAbsoluteFile() + "\\baseLine" + name);
                    baseLineOutputFolder.mkdir();
                    blg.generateBaseLines(facilitator, facOutput, baseLineOutputFolder);
                }

                modelController.closeStatisticsOutput();
                modelController = new ModelController(this);

            }

            System.out.println("Done with generator: " + generator.getClass().getName());
            long endTime = System.currentTimeMillis();
            System.out.println("Time in total is: " + (endTime - startTime) + " milliseconds");
        }

        System.out.println("Done!");
        System.out.println("It is now aafe to exit the program");
    }

    private List<TreeMapGenerator> getTreeMapGenerators() {
        List<TreeMapGenerator> generatorList = new ArrayList();
        TreeMapGenerator tmg = null;

//
        switch (this.technique) {
            case "moore":
                tmg = new MooreTreeMap();
                generatorList.add(tmg);
                break;
            case "snd":
                tmg = new SliceAndDice();
                generatorList.add(tmg);
                break;
            case "sqr":
                tmg = new SquarifiedTreeMap();
                generatorList.add(tmg);
                break;
            case "otpbm":
                tmg = new PivotByMiddle();
                generatorList.add(tmg);
                break;
            case "otbpsize":
                tmg = new PivotBySize();
                generatorList.add(tmg);
                break;
            case "otbpsplit":
                tmg = new PivotBySplit();
                generatorList.add(tmg);
                break;
             case "spiral":
                tmg = new SpiralTreeMap();
                generatorList.add(tmg);
                break;
             case "strip":
                tmg = new StripTreeMap();
                generatorList.add(tmg);
                break;
            case "hilb":
                tmg = new HilbertTreeMap();
                generatorList.add(tmg);
                break;
            case "appr":
                tmg = new ApproximationTreeMap();
                generatorList.add(tmg);
                break;
            case "otpbss":
                tmg = new PivotBySplit();
                generatorList.add(tmg);
                break;
            case "ssv":
                tmg = new IncrementalLayout(false);
                generatorList.add(tmg);
                break;
            default:
                System.out.println("Invalid technique.");
        }
        // tmg = new SliceAndDice();
        // generatorList.add(tmg);
        //
        // tmg = new PivotByMiddle();
        // generatorList.add(tmg);
        //
        // tmg = new PivotBySize();
        // generatorList.add(tmg);
        //
        // tmg = new StripTreeMapLookAhead();
        // generatorList.add(tmg);
        //
        // tmg = new SquarifiedTreeMapLookAhead();
        // generatorList.add(tmg);
        //
        // tmg = new SpiralTreeMapLookAhead();
        // generatorList.add(tmg);
//        tmg = new IncrementalLayout(false);
//        generatorList.add(tmg);
        return generatorList;
    }

    public static List<DataFacilitator> getDataFacilitatorFromFolder(File inputFolder) {
        ArrayList<DataFacilitator> facilitators = new ArrayList();

        for (File f : inputFolder.listFiles()) {
            DataFacilitator df = new DataFileManagerFast(f.getAbsolutePath());
            facilitators.add(df);
        }

        //TODO: read the files in the folder and parse them to get the correct files out of it
        return facilitators;
    }

    private List<DataFacilitator> getDataFacilitators() {
        if (inputFolder != null) {
            return getDataFacilitatorFromFolder(inputFolder);
        }

        List<DataFacilitator> facilitators = new ArrayList();
//
//        double changeChance = 100;
//        int minItemsPerLevel = 5;
//        int minDepth = 1;
//        int maxDepth = 1;
//        int minSize = 1;
//        int time = 0;
//
//        int maxItemsPerLevel = 25;
//        int maxSize = 100;
//        double changeValue = 5;
//        int addRemoveChange = 0;
//        List<Integer> minMaxItemList = new ArrayList();
////        minMaxItemList.add(5);
////        minMaxItemList.add(10);
////        minMaxItemList.add(25);
//        minMaxItemList.add(50);
//
//        List<Integer> maxSizes = new ArrayList();
////        maxSizes.add(100);
//        maxSizes.add(1000);
////        maxSizes.add(10000);
//
//        List<Double> changeVals = new ArrayList();
//        //in percentage of maxsize
////        changeVals.add(5.0);
//        changeVals.add(25.0);
//
//        List<Integer> addRemoveChances = new ArrayList();
////        addRemoveChances.add(0);
////        addRemoveChances.add(5);
//        addRemoveChances.add(10);
//////
//        for (int newMaxItemsPerLevel : minMaxItemList) {
//            RandomSequentialDataGenerator faciliator = new RandomSequentialDataGenerator(minItemsPerLevel, newMaxItemsPerLevel, minDepth, maxDepth, minSize, maxSize, changeValue, changeChance, time, addRemoveChange, false, "maxItems");
//            facilitators.add(faciliator);
//        }
//
//        for (double newChangeValue : changeVals) {
//            RandomSequentialDataGenerator faciliator = new RandomSequentialDataGenerator(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize, newChangeValue, changeChance, time, addRemoveChange, false, "changeValue");
//            facilitators.add(faciliator);
//        }//        for (int newAddRemoveChange : addRemoveChances) {
//            RandomSequentialDataGenerator faciliator = new RandomSequentialDataGenerator(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize, changeValue, changeChance, time, newAddRemoveChange, false, "AddRemove");
//            facilitators.add(faciliator);
//        }
//        for (int newMaxSize : maxSizes) {
//            RandomSequentialDataGenerator faciliator = new RandomSequentialDataGenerator(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, newMaxSize, changeValue, changeChance, time, addRemoveChange, false, "maxSize");
//            facilitators.add(faciliator);
//        }
//
//        //logNormal true
//        DataFacilitator faciliator = new RandomLogNormalSequentialDataGenerator(minItemsPerLevel, maxItemsPerLevel, time, addRemoveChange, "LogNormal");
//        facilitators.add(faciliator);
//        //exponential false
//        faciliator = new RandomSequentialDataGenerator(minItemsPerLevel, maxItemsPerLevel, minDepth, maxDepth, minSize, maxSize, changeValue, changeChance, time, addRemoveChange, false, "baseLine");
//        facilitators.add(faciliator);

//        DataFacilitator faciliator = new DataFileManager("D:\\Development\\StableTreemap\\datasets\\coffee20YearConsequitiveV3.csv");
//        facilitators.add(faciliator);
        DataFacilitator faciliator = new DataFileManager("datasets\\popularNamesAll.csv");
        facilitators.add(faciliator);

//         faciliator = new DataFileManager("D:\\Development\\StableTreemap\\datasets\\kijkCijfers.csv");
//        facilitators.add(faciliator);
//
//        faciliator = new DataFileManager("D:\\Development\\StableTreemap\\datasets\\PopularNamesSince1993.csv");
//        facilitators.add(faciliator);
        return facilitators;
    }

}
