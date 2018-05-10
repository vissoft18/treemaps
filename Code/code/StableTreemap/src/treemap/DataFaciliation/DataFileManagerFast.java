package treemap.DataFaciliation;

import com.opencsv.CSVReader;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import treemap.dataStructure.DataMap;

/**
 *
 * @author Max Sondag
 */
public class DataFileManagerFast implements DataFacilitator {

    File inputFile;
    Map<Integer, DataMap> timeMap;
    private int time = 0;
    
    public DataFileManagerFast(String inputFileLocation) {
        this.inputFile = new File(inputFileLocation);
        timeMap = new HashMap();
        if (inputFile.toString().endsWith(".csv"));
        {
            readCSVFile();
        }
    }

    public DataFileManagerFast() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void readCSVFile() {
        /**
         * Format of csv files should be as follows: id, parentId(root if not
         * present), size at time 0, size at time 1 ..... First line can not contain headers
         */
        try {
            CSVReader reader = new CSVReader(new FileReader(inputFile), ',');
            String[] nextLine;

            List<StoredData> dataList = new LinkedList();
//            //skip the first line
//            nextLine = reader.readNext();
//
//            //whether color values are included or note
            int hasColor = 0;//There are no colors
//            if (nextLine[2].equals("Color")) {
//                hasColor = 1;
//            }

            while ((nextLine = reader.readNext()) != null) {
                String id = nextLine[0];
                String parentId = nextLine[1];

                Color color = null;

                List sizes = new LinkedList();
                for (int i = (2 + hasColor); i < nextLine.length; i++) {
                    double size = Double.parseDouble(nextLine[i]);
                    sizes.add(size);
                }

                StoredData data = new StoredData(id, parentId, sizes, color);
                dataList.add(data);
            }

            //Go through all the times, convert them to datamaps and add them to the mapping
            float startHue = 226f / 360f;
            int maxTime = dataList.get(0).getDataAmount();
            for (int time = 0; time < maxTime; time++) {
                DataMap convertToDataMap = convertToDataMap(dataList, time);
                timeMap.put(time, convertToDataMap);
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DataFileManagerFast.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DataFileManagerFast.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int count = 0;

    /**
     *
     * @param id The id of the datamap we are
     * @param dataList the complete list of data
     * @param time the time identifier
     * @return
     */
    private DataMap convertToDataMap(List<StoredData> dataList, int time) {
        HashMap<String, DataMap> dataMapMapping = new HashMap();
        HashMap<String, List<DataMap>> childMapping = new HashMap();
        //create the datamaps
        for (StoredData sd : dataList) {
            if (sd.getSizes().get(time) == 0) {
                continue;
            }
            DataMap dm = new DataMap(sd.getId(), sd.getSizes().get(time), null, Color.red);
            dataMapMapping.put(sd.getId(), dm);
            if (!childMapping.containsKey(sd.getParentId())) {
                childMapping.put(sd.getParentId(), new ArrayList<>());
            }
            childMapping.get(sd.getParentId()).add(dm);
        }

        DataMap root = new DataMap("root", 0, null, Color.red);
        dataMapMapping.put("root", root);

        //create the parent child relations
        for (DataMap parent : dataMapMapping.values()) {
            if (childMapping.containsKey(parent.getLabel())) {
                parent.addDataMaps(childMapping.get(parent.getLabel()),parent);
            }
        }

        //all the weights will be wrong, so we have to fix that
        
        //recursively update the weights
        initializeWeights(root);
        return root;
    }

    /**
     * Returns the StoredData element with the given id or null if it does not
     * exist
     *
     * @param dataList
     * @param id
     * @return
     */
    private Color getColor(float startHue, int childNumber, int childCount) {
        float number = childNumber;
        float count = childCount - 1;
        float saturationValue = number / count * 0.6f + 0.2f;
        return Color.getHSBColor(startHue, saturationValue, 1f);
    }

    public StoredData getStoredDataElement(List<StoredData> dataList, String id) {
        for (StoredData sd : dataList) {
            if (sd.getId().equals(id)) {
                return sd;
            }
        }
        return null;
    }

    @Override
    public DataMap getData(int time) {
        this.time = time;
        return timeMap.get(time);
    }

    @Override
    public String getDataIdentifier() {
        return inputFile.getAbsolutePath();
    }

    @Override
    public String getParamaterDescription() {
        return "FileName=" + inputFile.getName();
    }

    @Override
    public String getExperimentName() {
        return inputFile.getName();
    }

    @Override
    public DataFacilitator reinitializeWithSeed(int seed) {
        //not possible for this class
        return null;
    }

    @Override
    public boolean hasMaxTime() {
        return true;
    }

    @Override
    public int getMaxTime() {
        int maxTime = 0;
        for (int t : timeMap.keySet()) {
            maxTime = Math.max(t, maxTime);
        }
        return maxTime;
    }

    private void initializeWeights(DataMap root) {
        double size = 0;
        for (DataMap dm : root.getChildren()) {
            initializeWeights(dm);
            size += dm.getTargetSize();
        }
        if (root.hasChildren()) {
            root.setTargetSize(size);
        }
    }

    @Override
    public int getLastTime() {
        return time;
    }

}
