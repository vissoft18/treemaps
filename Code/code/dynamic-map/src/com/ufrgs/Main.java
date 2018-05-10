package com.ufrgs;

import com.ufrgs.model.Entity;
import com.ufrgs.model.Rectangle;
import com.ufrgs.util.DataHelper;
import com.ufrgs.util.Display;
import com.ufrgs.util.Technique;
import com.ufrgs.view.Panel;

import javax.swing.*;

public class Main {

    // Settings
    public static Display DISPLAY;

    private static Entity root;
    private static Rectangle canvas;
    public static String inputDir, outputDir;
    public static Technique technique;
    public static int width, height;

    public static void main(String[] args) {

        if (args.length == 5) {

            if (args[0].equals("-v")) {
                DISPLAY = Display.STEP;
                technique = Technique.valueOf(args[1]);
                inputDir = args[2];
                width = Integer.valueOf(args[3]);
                height = Integer.valueOf(args[4]);
            } else {
                DISPLAY = Display.ANALISYS;
                technique = Technique.valueOf(args[0]);
                inputDir = args[1];
                width = Integer.valueOf(args[2]);
                height = Integer.valueOf(args[3]);
                outputDir = args[4];
            }
                canvas = new Rectangle(width, height);
                root = DataHelper.buildHierarchy(inputDir);
                SwingUtilities.invokeLater(() -> createAndShowGUI());
        } else {
            argsError();
        }
    }

    private static void createAndShowGUI() {

        JFrame frame = new JFrame("Dynamic Treemap");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Panel panel = new Panel(root, canvas, frame, outputDir);
        frame.getContentPane().add(panel);
        frame.addKeyListener(panel);
        frame.setSize((int) canvas.width, (int) canvas.height);
        frame.setVisible(DISPLAY != Display.ANALISYS);
    }

    private static void argsError() {

        System.out.println("Usage for simple analysis (output rectangles to disk):");
        System.out.println("java -cp ./bin com.ufrgs.Main technique_code input_dir width height output_dir");
        System.out.println("If you'd like to see and interact with the generated treemap, add the frag -v:");
        System.out.println("java -cp ./bin com.ufrgs.Main -v technique_code input_dir width height");


        System.out.println("Techniques:\n" +
                "\tnmac - NMAP_ALTERNATE_CUT,\n" +
                "\tnmew - NMAP_EQUAL_WEIGHT,\n" +
                "\tsqr - SQUARIFIED_TREEMAP,\n" +
                "\totpbm - ORDERED_TREEMAP_PIVOT_BY_MIDDLE,\n" +
                "\totpbs - ORDERED_TREEMAP_PIVOT_BY_SIZE,\n" +
                "\tsnd - SLICE_AND_DICE,\n" +
                "\tstrip - STRIP,\n" +
                "\tspiral - SPIRAL.");
        System.out.println("\tWidth and Height are given in pixels (integers).");
    }
}
