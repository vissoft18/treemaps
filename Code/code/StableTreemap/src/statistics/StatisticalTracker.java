package statistics;

import TreeMapGenerator.TreeMapGenerator;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import statistics.Stability.RelativePinPointPositionStability;
import statistics.Stability.RelativeQuadrantStability;
import statistics.Stability.StabilityLayoutDistance;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.DataFaciliation.DataFacilitator;
import treemap.ModelController;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public class StatisticalTracker {
    //OrderEquivalentTreeMap has a problem with rounding errors that might
    //affect the stability numbers for very small rectangles.

    private File outputFile;
    private FileWriter fw;

    TreeMap oldTreeMap;
    TreeMap newTreeMap;

    OrderEquivalentTreeMap oldOrderTreeMap;
    OrderEquivalentTreeMap newOrderTreeMap;

    ModelController modelController;

    public StatisticalTracker(ModelController modelController) {
        this.modelController = modelController;
        oldTreeMap = null;
    }

    public void setOutputFile(File outputFile, boolean directory) {
//        System.out.println("outputFile = " + outputFile);
        this.outputFile = outputFile;
        if (directory) {
            outputFile.mkdir();
        } else {
            try {
                fw = new FileWriter(outputFile, true);
            } catch (IOException ex) {
                Logger.getLogger(StatisticalTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void closeOutput() {
        try {
            if (fw != null) {
                fw.close();
            }
            fw = null;
        } catch (IOException ex) {
            Logger.getLogger(StatisticalTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
        //reset
        oldTreeMap = null;
        newTreeMap = null;
        oldOrderTreeMap = null;
        newOrderTreeMap = null;
    }

    private void writeToOutput(String outputData) {
        try {
            if (fw == null) {
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                //get current date time with Date()
                Date date = new Date();
                setOutputFile(new File("experiment/experimentOutput" + dateFormat.format(date) + ".csv"), false);
            }
            fw.write(outputData + "\r\n");
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(StatisticalTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void calculateStabilities(String commandIdentifier) {
        Map<String, Double> stabilities = new HashMap<>();

        if (oldTreeMap != null) {

            double stabilityQuadrant = new RelativeQuadrantStability().getStability(oldTreeMap, newTreeMap);
            stabilities.put("stabilityRelative", stabilityQuadrant);
            double StabilitylayoutDistance = new StabilityLayoutDistance().getStability(oldTreeMap, newTreeMap);
            stabilities.put("StabilitylayoutDistance", StabilitylayoutDistance);
            modelController.setOrderStability(stabilities);
        } else {
            //negative values encode an undefined stability
            modelController.setOrderStability(null);
        }

        //need to store the data
        if (commandIdentifier.contains("experiment")) {
            String stabilityMeasures = "stabilityRelative=" + stabilities.get("stabilityRelative")
                    + ";stabilityQuadrant=" + stabilities.get("stabilityQuadrant")
                    + ";StabilitylayoutDistance=" + stabilities.get("StabilitylayoutDistance");
            String aspectRatio = "maxAspectRatio=" + newTreeMap.getMaxAspectRatio()
                    + ";averageAspectRatio=" + newTreeMap.getAverageAspectRatio()
                    + ";medianAspectRatio=" + newTreeMap.getMedianAspectRatio();
            String timeStamp = "timeStamp=" + System.currentTimeMillis();

            writeToOutput(commandIdentifier + ";" + stabilityMeasures + ";" + aspectRatio + ";" + timeStamp);

        }

    }

    public void treeMapUpdated(TreeMap tm, DataFacilitator dataFacilitator, TreeMapGenerator treeMapGenerator, String commandIdentifier) {
        oldTreeMap = newTreeMap;
        newTreeMap = tm;

        //  oldOrderTreeMap = newOrderTreeMap;
        //  newOrderTreeMap = new OrderEquivalentTreeMap(tm);
        //  oldWrongOrderTreeMap = newWrongOrderTreeMap;
        //  newWrongOrderTreeMap = new WrongOrderEquivalentTreeMap(tm);
        if (!commandIdentifier.contains("noStability")) {
            calculateStabilities(commandIdentifier);
        }

        if (commandIdentifier.contains("experiment")) {
            outputRectangles(newTreeMap, treeMapGenerator, dataFacilitator);
        }

    }

    public double getMeanAspectRatio(TreeMap treeMap) {
        List<TreeMap> allTreeMaps = new LinkedList();
        allTreeMaps.add(treeMap);
        List<TreeMap> allChildren = treeMap.getAllChildren();
        allTreeMaps.addAll(allChildren);

        double aspectRatio = 0;
        for (TreeMap tm : allTreeMaps) {
            aspectRatio += tm.getRectangle().getAspectRatio();
        }
        return aspectRatio / allChildren.size();
    }

    public double getMeanAspectRatioLeafs(TreeMap treeMap) {
        List<TreeMap> allChildren = treeMap.getAllChildren();

        int leafs = 0;
        double aspectRatio = 0;
        for (TreeMap tm : allChildren) {
            if (!tm.hasChildren()) {
                aspectRatio += tm.getRectangle().getAspectRatio();
                leafs++;
            }
        }
        return aspectRatio / leafs;
    }

    public void takeSnapShot(String identifier) {
        try {
            Dimension screenRect = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage capture = new Robot().createScreenCapture(new java.awt.Rectangle(screenRect));
            ImageIO.write(capture, "jpg", new File(identifier + ".jpg"));

        } catch (IOException ex) {
            Logger.getLogger(StatisticalTracker.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (AWTException ex) {
            Logger.getLogger(StatisticalTracker.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void outputRectangles(TreeMap newTreeMap, TreeMapGenerator treeMapGenerator, DataFacilitator dataFacilitator) {
        try {
            //use Algoname for folder
            String algoName = treeMapGenerator.getClass().getSimpleName();
            //use time for file
            String time = "" + dataFacilitator.getLastTime();

            File f = new File(outputFile + "/");
            f.mkdirs();
            f = new File(outputFile + "/t" + time + ".rect");
            f.createNewFile();

            FileWriter fw = new FileWriter(f, false);

            for (TreeMap tm : newTreeMap.getAllLeafs()) {
                fw.append(tm.getLabel()).append(",");
                fw.append("" + tm.getRectangle().getX()).append(",");
                fw.append("" + tm.getRectangle().getY()).append(",");
                fw.append("" + tm.getRectangle().getWidth()).append(",");
                fw.append("" + tm.getRectangle().getHeight()).append("\n");
            }
            fw.close();

        } catch (IOException ex) {
            Logger.getLogger(StatisticalTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
