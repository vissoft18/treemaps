package treemap.dataStructure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Max Sondag
 */
public class DataMap {

    private String label;
    private double targetSize;
    private List<DataMap> children;
    private Color color;
    private float startHue;

    public DataMap(String label, double size, List<DataMap> children, Color color) {
        this.label = label;
        this.targetSize = size;
        this.children = children;
        if (children == null) {
            this.children = new LinkedList();
        }
        this.color = color;
    }

    public DataMap(String label, double size, List<DataMap> children, Color color, float startHue) {
        this.label = label;
        this.targetSize = size;
        this.children = children;
        if (children == null) {
            this.children = new LinkedList();
        }
        this.color = color;
    }

    public static double getTotalSize(Collection<DataMap> list) {
        double size = 0;
        for (DataMap dm : list) {
            size += dm.getTargetSize();
        }
        return size;
    }

    public static double getTotalSizeList(Collection<List<DataMap>> list) {
        double size = 0;
        for (List<DataMap> subList : list) {
            for (DataMap dm : subList) {
                size += dm.getTargetSize();
            }
        }
        return size;
    }

    public boolean hasChildren() {
        if (children.isEmpty()) {
            return false;
        }
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="getters and setters">
    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    public float getStartHue() {
        return startHue;
    }

    /**
     * @return the targetSize
     */
    public double getTargetSize() {
        return targetSize;
    }

    /**
     * @return the children
     */
    public List<DataMap> getChildren() {
        return children;
    }

    /**
     * Recursively finds all the child nodes
     *
     * @return all (grand)children of this dataMap
     */
    public List<DataMap> getAllChildren() {
        List<DataMap> allChildren = new LinkedList();
        allChildren.addAll(children);
        for (DataMap child : children) {
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;
    }

    /**
     * Recursively finds all the child nodes
     *
     * @return all (grand)children of this dataMap
     */
    public List<DataMap> getAllLeafs() {
        List<DataMap> leafs = new LinkedList();
        if (!hasChildren()) {
            leafs.add(this);
        }

        for (DataMap child : children) {
            leafs.addAll(child.getAllLeafs());
        }
        return leafs;
    }

    public Color getColor() {
        return color;
    }
    //</editor-fold>

    public boolean hasEqualStructure(DataMap dataMap) {
        if (!dataMap.getLabel().equals(label)) {
            return false;
        }

        if (children.size() != dataMap.children.size()) {
            return false;
        }

        for (DataMap d1 : children) {
            DataMap d2 = d1.getDataMapSameLabel(dataMap.getChildren());
            if (d2 == null) {
                return false;
            }
            if (!d1.hasEqualStructure(d2)) {
                return false;
            }
        }

        return true;
    }

    private DataMap getDataMapSameLabel(List<DataMap> dataMaps) {
        for (DataMap d : dataMaps) {
            if (d.getLabel().equals(label)) {
                return d;
            }
        }
        //no equal datamap
        return null;
    }

    public DataMap getDataMapWithLabel(String label) {
        List<DataMap> dataMaps = getAllChildren();
        for (DataMap dm : dataMaps) {
            if (dm.label.equals(label)) {
                return dm;
            }
        }
        return null;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!DataMap.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        DataMap other = (DataMap) obj;

        if (!label.equals(other.label)) {
            return false;
        }

        for (DataMap child1 : children) {
            boolean found = false;
            for (DataMap child2 : other.children) {
                if (child1.equals(child2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        StringBuilder builder = new StringBuilder();
        builder.append(label);
        for (DataMap dm : children) {
            builder.append(dm.hashCode());
        }
        return builder.toString().hashCode();

    }

    public void setTargetSize(double size) {
        this.targetSize = size;
    }

    public void addDatamap(DataMap addDm, DataMap parent) {
        if (getAllChildren().contains(parent)) {
            targetSize += addDm.targetSize;
            if (this == parent) {
                System.out.println("prob");
            }
        } else {
            if (this == parent) {
                targetSize += addDm.targetSize;
                children.add(addDm);
            }
            //there is no child which contains the parent
            return;
        }

        for (DataMap dm : children) {
            dm.addDatamap(addDm, parent);
        }
    }

    public void addDataMaps(List<DataMap> toAdd, DataMap parent) {
        for (DataMap dm : toAdd) {
            addDatamap(dm, parent);
        }
    }

    public void removeDataMap(DataMap removeDm) {
        if (getAllChildren().contains(removeDm)) {
            targetSize -= removeDm.targetSize;
        } else {
            //no children which contain removeDm
            return;
        }

        if (getChildren().contains(removeDm)) {
            children.remove(removeDm);
            return;
        }

        for (DataMap dm : children) {
            dm.removeDataMap(removeDm);
        }
    }

    public void removeDataMaps(List<DataMap> toDelete) {
        while (!toDelete.isEmpty()) {
            DataMap dm = toDelete.get(0);
            removeDataMap(dm);
            toDelete.remove(dm);
        }
    }

    /**
     * Replace currentDm with newDm
     *
     * @param currentDm
     * @param replaceDm
     */
    public void replaceDataMap(DataMap replaceDm) {

        DataMap currentDm = getDataMapWithLabel(replaceDm.getLabel());
        DataMap parent = getParent(currentDm);
        if (parent == null) {
            System.err.println("This datamap does not have a parent");
        }
        removeDataMap(currentDm);
        addDatamap(replaceDm, parent);
    }

    public DataMap getParent(DataMap child) {
        if (children.contains(child)) {
            return this;
        }
        for (DataMap c : children) {
            DataMap parent = c.getParent(child);
            if (parent != null) {
                return parent;
            }
        }

        return null;

    }

    /**
     * Removes datamaps with size 0
     */
    public void removeEmptyDataMaps() {
        Set<DataMap> toDelete = new HashSet();
        for (DataMap dm : children) {
            if (dm.targetSize == 0) {
                toDelete.add(dm);
            }
            if (dm.hasChildren()) {
                dm.removeEmptyDataMaps();
            }
        }
        children.removeAll(toDelete);
    }

    public void removeDataMapsKeepSize(List<DataMap> children) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void removeChildren(double targetSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean removeChildrenAndUpdateSize(DataMap newDm, double oldSize) {
        if (label.equals(newDm.getLabel())) {
            children = new ArrayList();
            targetSize = newDm.getTargetSize();
            return true;
        } else {
            for (DataMap child : children) {
                boolean contained = child.removeChildrenAndUpdateSize(newDm, oldSize);
                if (contained) {
                    this.targetSize += (newDm.getTargetSize() - oldSize);
                    return true;
                }
            }
        }
        return false;

    }

    public int getHeight() {
        int height = 1;
        if (!hasChildren()) {
            return 1;
        }
        for (DataMap dm : children) {
            height = Math.max(height, dm.getHeight() + 1);
        }
        return height;
    }

}
