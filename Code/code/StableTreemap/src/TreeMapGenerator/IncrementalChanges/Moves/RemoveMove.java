/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TreeMapGenerator.IncrementalChanges.Moves;

import TreeMapGenerator.IncrementalChanges.Block;
import TreeMapGenerator.IncrementalChanges.OrderEquivalenceGraph;
import TreeMapGenerator.IncrementalChanges.OrderEquivalentMaximalSegment;
import treemap.dataStructure.Rectangle;

/**
 *
 * @author msondag
 */
public class RemoveMove extends Move {

    public RemoveMove(OrderEquivalenceGraph graph) {
        super(graph);
    }

    /**
     * Removes the block {@code b} from the graph
     *
     * @param b
     */
    public void removeBlock(Block b) {
        //first check if it is one-sided
        OrderEquivalentMaximalSegment oneSided = b.getOneSided();

        if (oneSided != null) {
            removeBlockOneSided(b, oneSided);
        } else {
            //block is the center of a windmill
            System.out.println("Removing a windmill");
            removeBlockMultiSided(b);
        }

        graph.removeBlock(b);
        graph.updateEndpointsRelations();

        graph.updateAdjecencyLength();
    }

    /**
     * Removes the block {@code b} given that {@code b} is adjacent to {@code s}
     * and is the only block adjacent to one side of {@code s}
     *
     * @param b
     * @param s
     */
    private void removeBlockOneSided(Block b, OrderEquivalentMaximalSegment s) {
        if (s.horizontal) {
            if (s.adjacentBlockList1.contains(b)) {
                //block is to the bottom of s

                //add blocks above s to the maximalsegment below b
                OrderEquivalentMaximalSegment msBottom = b.getMsBottom();
                msBottom.addToAdjacencyList(false, s.adjacentBlockList2);
                for (Block b2 : s.adjacentBlockList2) {
                    b2.rectangle = new Rectangle(b2.rectangle.getX(), b2.rectangle.getY(), b2.rectangle.getWidth(),
                            b2.rectangle.getHeight() + b.rectangle.getHeight());

                }
            } else {
                //block is to the top of s

                //add blocks below s to the maximalsegment above b
                OrderEquivalentMaximalSegment msTop = b.getMsTop();
                graph.removeSegment(s);
                msTop.addToAdjacencyList(true, s.adjacentBlockList1);
                for (Block b2 : s.adjacentBlockList1) {
                    b2.rectangle = new Rectangle(b2.rectangle.getX(), b.rectangle.getY(), b2.rectangle.getWidth(),
                            b2.rectangle.getHeight() + b.rectangle.getHeight());
                }
            }
        } else //vertical
        {
            if (s.adjacentBlockList1.contains(b)) {
                //block is to the left of s

                //add blocks right s to the maximalsegment left of b
                OrderEquivalentMaximalSegment msLeft = b.getMsLeft();
                msLeft.addToAdjacencyList(false, s.adjacentBlockList2);
                for (Block b2 : s.adjacentBlockList2) {
                    b2.rectangle = new Rectangle(b.rectangle.getX(), b2.rectangle.getY(),
                            b2.rectangle.getWidth() + b.rectangle.getWidth(), b2.rectangle.getHeight());
                }
            } else {
                //block is to the right of s

                //add blocks left s to the maximalsegment right of b
                OrderEquivalentMaximalSegment msRight = b.getMsRight();
                msRight.addToAdjacencyList(true, s.adjacentBlockList1);
                for (Block b2 : s.adjacentBlockList1) {
                    b2.rectangle = new Rectangle(b2.rectangle.getX(), b2.rectangle.getY(),
                            b2.rectangle.getWidth() + b.rectangle.getWidth(), b2.rectangle.getHeight());
                }
            }
        }
        //remove the segment and the block
        graph.removeAdjacencies(b);
        graph.removeSegment(s);
        graph.removeBlock(b);

        graph.updateAdjecencyLength();
    }

    /**
     * Removes the block {@code b} given that {@code b} does not have a
     * one-sided side
     *
     * @param b
     */
    private void removeBlockMultiSided(Block b) {
        //Find a maximal segment adjacent to b that is not a 
        OrderEquivalentMaximalSegment ms = null;
        OrderEquivalentMaximalSegment bottomSegment = b.getMsBottom();
        OrderEquivalentMaximalSegment topSegment = b.getMsTop();
        OrderEquivalentMaximalSegment leftSegment = b.getMsLeft();
        OrderEquivalentMaximalSegment rightSegment = b.getMsRight();
        if (!bottomSegment.adjacentBlockList1.isEmpty() && !bottomSegment.adjacentBlockList2.isEmpty()) {
            ms = bottomSegment;
        } else if (!topSegment.adjacentBlockList1.isEmpty() && !topSegment.adjacentBlockList2.isEmpty()) {
            ms = topSegment;
        } else if (!leftSegment.adjacentBlockList1.isEmpty() && !leftSegment.adjacentBlockList2.isEmpty()) {
            ms = leftSegment;
        } else if (!rightSegment.adjacentBlockList1.isEmpty() && !rightSegment.adjacentBlockList2.isEmpty()) {
            ms = rightSegment;
        }

        if (ms == null) {
            System.err.println("Can't remove the only block left in the treemap");
            return;
        }

        //keep stretching the block to the right untill it becomes oneSided or hits the boundary
        while (b.getOneSided() == null) {

            StretchMove move = new StretchMove(graph);
            ms = b.getMsRight();

            if (ms.adjacentBlockList2.isEmpty()) {
                //it is a segment on the boundary, so it must be one-sided
                return;
            }

            if (b == ms.getLeftBottomBlock()) {
                move.performMove(ms, true);
            } else {//leftTopmostblock, otherwise it would be one-sided
                move.performMove(ms, false);
            }

        }
        ms = b.getOneSided();
        //ms now only contains b on one side

        removeBlockOneSided(b, ms);
    }

}
