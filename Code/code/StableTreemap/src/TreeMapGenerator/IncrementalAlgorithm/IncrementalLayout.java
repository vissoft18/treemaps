package TreeMapGenerator.IncrementalAlgorithm;

import TreeMapGenerator.ApproximationTreeMap;
import TreeMapGenerator.IncrementalChanges.OrderEquivalenceGraph;
import TreeMapGenerator.IncrementalChanges.TreeMapChangeGenerator;
import TreeMapGenerator.TreeMapGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;
import treemap.dataStructure.Tuple;
import static utility.Precision.eq;
import utility.Randomizer;

/**
 *
 * @author max
 */
public class IncrementalLayout implements TreeMapGenerator {

    private final boolean DEBUG = false;

    int maxMoveAmount = 4;//The maximal amount of moves we will use to see if it can be improved
    int repeatAmount = 1;//The maximal amount of time we will perform the best move
    DataMap currentDataMap = null;
    TreeMap currentTreeMap = null;
    /**
     * Whether edge flips are enabled
     */
    boolean noMoves = false;

    public IncrementalLayout(boolean noMoves) {
        if (noMoves) {
            maxMoveAmount = 0;
            repeatAmount = 0;
            this.noMoves = true;
        }
    }

    public IncrementalLayout() {
        this(true);
    }

    @Override
    public TreeMap generateTreeMap(DataMap newDataMap, Rectangle inputRectangle
    ) {
        System.out.println("generatedATreemap");
        if (currentDataMap == null) {
            currentTreeMap = generateNewTreeMap(newDataMap, inputRectangle);
        } else {
            checkTreemapSizes();

            //make sure the previous treemap is not changed for the animation
            currentTreeMap = currentTreeMap.deepCopy();
            if (!currentDataMap.hasEqualStructure(newDataMap)) {
                //the structure of the new treemap is different. handle deletions and addition first
                checkTreemapSizes();
                verifyStructure();
                updateDeletionTreeMap(currentDataMap, newDataMap);
                verifyStructure();
                checkTreemapSizes();
                updateAdditionTreeMap(currentDataMap, newDataMap);
                checkTreemapSizes();
                verifyStructure();

                if (!currentDataMap.hasEqualStructure(newDataMap)) {
                    System.out.println("incorrect, should have the same now");
                }
            }
            verifyStructure();
            checkTreemapSizes();
            //update the weights to get a correct initial treemap

            TreeMapChangeGenerator tmcg = new TreeMapChangeGenerator(currentTreeMap);
            currentTreeMap = tmcg.updateWeights(newDataMap);
        }

        currentDataMap = newDataMap;
        checkTreemapSizes();
        //DataMap has the same structure so we should incrementally change it.
        //Also in the case of the first generated as no moves are performed on it
        for (int i = 0; i < repeatAmount; i++) {
            currentTreeMap = updateCurrentTreeMap();
        }

        checkTreemapSizes();

        return currentTreeMap;
    }

    /**
     * Genertates the initial treemap using the approximation algorithm
     *
     * @param dataMap
     * @param treeMapRectangle
     * @return
     */
    private TreeMap generateNewTreeMap(DataMap dataMap, Rectangle treeMapRectangle) {
        ApproximationTreeMap approximationTreeMap = new ApproximationTreeMap();
        return approximationTreeMap.generateTreeMap(dataMap, treeMapRectangle);
    }

    /**
     * Updates the existing treemap
     *
     * @param dataMap
     * @return
     */
    private TreeMap updateCurrentTreeMap() {

        TreeMap updatedTreeMap = updateTreeMap(currentTreeMap, maxMoveAmount);

        return updatedTreeMap;
    }

    /**
     * Updates the treemap {@code tm}
     *
     * @param root The root of the subtreemap
     * @param moveAmount the maximal amount of moves to perform per hierarchy
     * level
     * @return
     */
    private TreeMap updateTreeMap(TreeMap root, int moveAmount) {

        if (!root.hasChildren()) {
            //root is a leaf, can't change the layout
            return root;
        }

        TreeMapChangeGenerator tmCG = new TreeMapChangeGenerator(root);
        root = tmCG.performLookAheadMoveNoTarget(moveAmount);
        currentTreeMap.replaceTreemap(root);

        //recurse in the children
        List<TreeMap> children = new ArrayList(root.getChildren());
        for (TreeMap child : children) {
            TreeMap updatedChild = updateTreeMap(child, moveAmount);
        }

        checkTreemapSizes();

        return root;
    }

    /**
     * Handles the deletion of items in the treemap. Also removes the deleted
     * items from the datamap
     *
     * @param newDm
     */
    private void updateDeletionTreeMap(DataMap currentDm, DataMap newDm) {
        //We first identify the datamaps that are in one treemap but not in the other treemap on the level
        if (!currentDm.hasChildren()) {
            return;
        }

        List<DataMap> currentItems = new ArrayList(currentDm.getChildren());
        List<DataMap> newItems = new ArrayList(newDm.getChildren());

        //holds all the items that were deleted from currentItems
        List<DataMap> deletedItems = getAddedItems(newItems, currentItems);
        List<DataMap> undeletedItems = new ArrayList(currentItems);
        undeletedItems.removeAll(deletedItems);

        if (undeletedItems.isEmpty()) {
            //all children of currentDm will be deleted,
            TreeMap parent = currentTreeMap.getTreeMapWithLabel(currentDm.getLabel());
            parent.removeChildrenAndUpdateSize(newDm,parent.getTargetSize());
            currentDm.removeChildrenAndUpdateSize(newDm,parent.getTargetSize());
//            currentTreeMap.removeTreeMaps(parent.getChildren());
//            currentDataMap.removeDataMaps(currentDm.getChildren());
//            parent.updateTargetSize(newDm.getTargetSize());
//            currentDm.setTargetSize(newDm.getTargetSize());

            checkTreemapSizes();
            return;
        }
        for (DataMap deleteDm : deletedItems) {
            //delete the children of dm one by one
            checkTreemapSizes();

            //parent changes after every iteration, so we need to do this in the loop
            TreeMap parentTm = currentTreeMap.getTreeMapWithLabel(currentDm.getLabel());

            //remove it from the treemap
            TreeMapChangeGenerator tmCG = new TreeMapChangeGenerator(parentTm);
            TreeMap newParentTm = tmCG.performRemove(deleteDm);

            checkTreemapSizes();

            if (parentTm == currentTreeMap) {
                //there is no parent, replace the entire treemap
                currentTreeMap = newParentTm;
            } else {
                currentTreeMap.replaceTreemap(newParentTm);
            }
            //remove it from the datamap
            currentDataMap.removeDataMap(deleteDm);

            checkTreemapSizes();
        }
        checkTreemapSizes();
        //recurse in the undeleted items as there might be changes lower in the tree
        for (DataMap dmOld : undeletedItems) {
            checkTreemapSizes();
            DataMap dmNew = newDm.getDataMapWithLabel(dmOld.getLabel());
            updateDeletionTreeMap(dmOld, dmNew);
            checkTreemapSizes();

        }
        checkTreemapSizes();
    }

    /**
     * Gets which items were added from currentItems to newItems
     *
     * @param currentItems
     * @param newItems
     * @return
     */
    private List<DataMap> getAddedItems(List<DataMap> currentItems, List<DataMap> newItems) {
        List<DataMap> itemsToBeAdded = new ArrayList(); //fill added and unaddedItems
        for (DataMap newItem : newItems) {
            boolean found = false;
            for (DataMap currentItem : currentItems) {
                if (newItem.getLabel().equals(currentItem.getLabel())) {
                    found = true;
                    break;
                }
            }
            if (found == false) {
                itemsToBeAdded.add(newItem);
            }
        }
        return itemsToBeAdded;
    }

    /**
     * Handles the addition of items in the treemap. Deletion of items should be
     * done already. CurrentDm and newDm are both present in the tree, but their
     * children might not be
     *
     * @param currentDm The current datamap
     * @param currentDm The new datamap
     * @param newDm
     */
    private void updateAdditionTreeMap(DataMap currentDm, DataMap newDm) {
        checkTreemapSizes();
        //get the treemap corresponding to oldDataMap
        TreeMap parentTm = currentTreeMap.getTreeMapWithLabel(currentDm.getLabel());

        if (!currentDm.hasChildren()) {
            //handle newDm completely by generating a new treemap
            checkTreemapSizes();
            TreeMap newParentTm = generateNewTreeMap(newDm, parentTm.getRectangle());

            if (parentTm == currentTreeMap) {
                currentDataMap = newDm;//it does not have a parent in this case
                currentTreeMap = newParentTm;
            } else {
                currentDataMap.replaceDataMap(newDm);
                currentTreeMap.replaceTreemap(newParentTm);
            }

            checkTreemapSizes();
            return;
        }

        //We identify the datamaps that are in one treemap but not in the other treemap. We do this level by level
        List<DataMap> currentItems = new ArrayList(currentDm.getChildren());
        List<DataMap> newItems = new ArrayList(newDm.getChildren());

        //contains the nodes that are only present in the old dataset
        List<DataMap> itemsToBeAdded = getAddedItems(currentItems, newItems);
        checkTreemapSizes();

        //Add each item that should be added
        for (DataMap addDm : itemsToBeAdded) {
            //get the best addition
            checkTreemapSizes();

            Tuple<DataMap, Boolean> bestAddition = getBestForAddition(parentTm, currentDm, addDm);
            DataMap bestDataMap = bestAddition.x;
            boolean horizontal = bestAddition.y;

            //perform the best addition
            TreeMapChangeGenerator tmCG = new TreeMapChangeGenerator(parentTm);
            TreeMap newParentTm = tmCG.performAdd(bestDataMap, addDm, horizontal);
            //newParentTm contains addDm
            if (addDm.hasChildren()) {
                //if the added treemap has children, we generate a new treemap for this part
                TreeMap addTm = generateNewTreeMap(addDm, newParentTm.getChildWithLabel(addDm.getLabel()).getRectangle());
                //replace addTm
                newParentTm.replaceTreemap(addTm);
            }
            //update the treemap
            if (parentTm == currentTreeMap) {
                currentTreeMap = newParentTm;
            } else {
                currentTreeMap.replaceTreemap(newParentTm);
            }
            parentTm = newParentTm;
            //Add the dataMap
            currentDataMap.addDatamap(addDm, currentDm);
            checkTreemapSizes();
        }

        checkTreemapSizes();

        for (DataMap oldDm : currentItems) {
            //Find the datamap in the new dm with the same label. must exist as deletion already occured
            DataMap newChildDm = newDm.getDataMapWithLabel(oldDm.getLabel());
            if (newChildDm.hasChildren()) {
                //if it does not have children, we do not need to recurse
                updateAdditionTreeMap(oldDm, newChildDm);
            }

        }

    }

    /**
     * Returns the best place to insert dataMap {@code addDM} and whether it
     * should inserted horizontal
     *
     * @paremt parentTm the treemap where we are going to insert addDm
     * @param parentDm The parent of the possible datamaps where we can insert
     * addDm
     * @return (Best dataMap to insert,horizontal insertion)
     */
    private Tuple<DataMap, Boolean> getBestForAddition(TreeMap parentTm, DataMap parentDm, DataMap addDm) {
        if (parentTm.getChildren().size() != parentDm.getChildren().size()) {
            System.err.println("Inconsistent sizes");
        }

        //determine what the best option is by trying them all out. Candidates are all the children of the parent
        List<DataMap> additionCandidates = getAdditionCandidates(parentDm, parentTm);

        OrderEquivalenceGraph oeg = new OrderEquivalenceGraph(parentTm);

        double bestRatio = Double.MAX_VALUE;
        DataMap bestDataMap = null;
        boolean bestHor = false;
        for (int hor = 0; hor <= 1; hor++) {
            //test both horizontal and vertical
            boolean horizontal;
            if (hor == 0) {
                horizontal = false;
            } else {
                horizontal = true;
            }
            for (DataMap dm : additionCandidates) {
                //Check if this is the best option for the addition by adding it
                TreeMap parentTmCopy = parentTm.deepCopy();

                //first update the size such that the totals add up again
                parentTmCopy.updateTargetSize(parentTmCopy.getTargetSize() + addDm.getTargetSize());

                OrderEquivalenceGraph oegCopy = oeg.deepCopy();
                oegCopy.originalTreeMap = parentTmCopy;
                //add the dataMap
                TreeMapChangeGenerator tmCG = new TreeMapChangeGenerator(parentTmCopy, oegCopy);
                parentTmCopy = tmCG.performAdd(dm, addDm, horizontal);
                //check if it is better
                if (parentTmCopy.getMaxAspectRatio() < bestRatio) {
                    bestDataMap = dm;
                    bestRatio = parentTmCopy.getMaxAspectRatio();
                    bestHor = horizontal;
                }
            }
        }
        return new Tuple(bestDataMap, bestHor);
    }

    @Override
    public String getParamaterDescription() {
        return "";
    }

    @Override
    public TreeMapGenerator reinitialize() {
        return new IncrementalLayout(noMoves);
    }

    /**
     * Performs a single optimal move on the current treemap
     */
    public TreeMap performMove() {
        currentTreeMap = updateTreeMap(currentTreeMap, 1);
        return currentTreeMap;
    }

    private List<DataMap> getAdditionCandidates(DataMap parentDm, TreeMap parentTm) {
        //Check promising candiates. I.e., datamaps with the highest aspect ratio
        //If there are less than maxCandidates, just try all of them.
        int maxCandidates = 6;

        ArrayList<DataMap> additionCandidates = new ArrayList();

        List<TreeMap> children = new ArrayList(parentTm.getChildren());

//        //option 1: Sort in descending order of aspect ratio
//        children.sort((TreeMap o1, TreeMap o2) -> Double.compare(o2.getRectangle().getAspectRatio(), o1.getRectangle().getAspectRatio()));
//        for (int i = 0; (i < maxCandidates) && (i < children.size()); i++) {
//            TreeMap tm = children.get(i);
//            DataMap dm = parentDm.getDataMapWithLabel(tm.getLabel());
//            additionCandidates.add(dm);
//        }
//      //option 2:random
//        Collections.shuffle(children);
//        for (int i = 0; (i < maxCandidates) && (i < children.size()); i++) {
//            TreeMap tm = children.get(i);
//            DataMap dm = parentDm.getDataMapWithLabel(tm.getLabel());
//            additionCandidates.add(dm);
//        }
        //option 3: 50-50 of both
        children.sort((TreeMap o1, TreeMap o2) -> Double.compare(o2.getRectangle().getAspectRatio(), o1.getRectangle().getAspectRatio()));
        for (int i = 0; (i < maxCandidates / 2) && (i < children.size()); i++) {
            TreeMap tm = children.get(i);
            DataMap dm = parentDm.getDataMapWithLabel(tm.getLabel());
            additionCandidates.add(dm);
        }

        for (int i = 0; (i < maxCandidates / 2) && (i < children.size()); i++) {
            int index = (int) Math.floor(Randomizer.getRandomDouble() * children.size());
            TreeMap tm = children.get(index);
            DataMap dm = parentDm.getDataMapWithLabel(tm.getLabel());
            if (!additionCandidates.contains(dm)) {
                additionCandidates.add(dm);
            }
        }

        return additionCandidates;
    }

    private void checkTreemapSizes(DataMap newDm, TreeMap child) {
        if (!DEBUG) {
            return;
        }
        double sumTm = 0;
        double sumDm = 0;

        for (TreeMap tm : child.getAllChildren()) {
            sumTm += tm.getTargetSize();
        }
        for (DataMap dm : newDm.getAllChildren()) {
            sumDm += dm.getTargetSize();
            if (dm.getTargetSize() == 0) {
                System.err.println("Targetsize is 0, should not happen");
            }
        }
        if (Math.abs((sumDm - sumTm)) > 1) {
            System.out.println("sumDm-sumTm = " + (sumDm - sumTm));
        }

    }

    private void verifyStructure() {
        if (!DEBUG) {
            return;
        }
        if (currentDataMap.getChildren().size() != currentTreeMap.getChildren().size()) {
            System.err.println("Inconsistent sizes");
        }
        for (DataMap dm : currentDataMap.getAllChildren()) {
            TreeMap childWithLabel = currentTreeMap.getTreeMapWithLabel(dm.getLabel());
            if (childWithLabel == null) {
                System.err.println("no treemap for this child");
            }
            if (dm.getChildren().size() != childWithLabel.getChildren().size()) {
                System.err.println("Children size does not match up");
            }
        }

        for (TreeMap tm : currentTreeMap.getAllChildren()) {
            DataMap childWithLabel = currentDataMap.getDataMapWithLabel(tm.getLabel());
            if (childWithLabel == null) {
                System.err.println("no dataMap for this child");
            }
            if (childWithLabel.getChildren().size() != tm.getChildren().size()) {
                System.err.println("Children size does not match up");
            }
        }
    }

    /**
     * Check if the treemap sizes are the same as the datamap sizes
     */
    private void checkTreemapSizes() {
        if (!DEBUG) {
            return;
        }
        double sumTm = 0;
        double sumDm = 0;

        for (TreeMap tm : currentTreeMap.getAllChildren()) {
            sumTm += tm.getTargetSize();
        }
        for (DataMap dm : currentDataMap.getAllChildren()) {
            sumDm += dm.getTargetSize();
            if (dm.getTargetSize() == 0) {
                System.err.println("Targetsize is 0, should not happen");
            }
        }
        if (Math.abs((sumDm - sumTm)) > 1) {
            System.out.println("sumDm-sumTm = " + (sumDm - sumTm));
        }
    }

}
