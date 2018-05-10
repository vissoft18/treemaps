package UserControl.Visualiser;

import TreeMapGenerator.ApproximationTreeMap;
import TreeMapGenerator.HilbertMoore.HilbertTreeMap;
import TreeMapGenerator.HilbertMoore.MooreTreeMap;
import TreeMapGenerator.IncrementalAlgorithm.IncrementalLayout;
import TreeMapGenerator.Pivot.PivotByMiddle;
import TreeMapGenerator.Pivot.PivotBySize;
import TreeMapGenerator.Pivot.PivotBySplit;
import TreeMapGenerator.SliceAndDice;
import TreeMapGenerator.SpiralTreeMap;
import TreeMapGenerator.SpiralTreeMapLookAhead;
import TreeMapGenerator.SquarifiedTreeMap;
import TreeMapGenerator.SquarifiedTreeMapLookAhead;
import TreeMapGenerator.StripTreeMap;
import TreeMapGenerator.StripTreeMapLookAhead;
import TreeMapGenerator.TreeMapGenerator;
import java.awt.Color;
import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import statistics.Baseline.BaseLineGenerator;
import statistics.Stability.RelativeQuadrantStability;
import treemap.dataStructure.OrderEquivalentTreeMap;
import treemap.DataFaciliation.DataFacilitator;
import treemap.DataFaciliation.DataFileManager;
import treemap.DataFaciliation.DataFileManagerFast;
import treemap.DataFaciliation.Generators.RandomDataGenerator;
import treemap.DataFaciliation.Generators.RandomSequentialDataGenerator;
import treemap.DataFaciliation.Generators.RandomLogNormalDataGenerator;
import treemap.DataFaciliation.Generators.RandomLogNormalSequentialDataGenerator;
import treemap.dataStructure.DataMap;
import treemap.dataStructure.Rectangle;
import treemap.dataStructure.TreeMap;

/**
 *
 * @author Max Sondag
 */
public final class GUI extends javax.swing.JFrame {

    private TreeMapVisualisation treeMapVisualisation;
    private Visualiser visualiser;

    //Variable indicating if a change in time was due to a simulation step
    private boolean simulationStep = false;

    //Whether we are currently running a simulation
    private boolean simulationRunning = false;

    //whether we are currently running an experiment
    private boolean experimentRunning = false;

    Timer simulationTimer = new Timer();

    //The time to wait after a simulation step
    private volatile int simulationSpeed = 500;
    private boolean useStored = true;

    /**
     * Creates new form GUI
     */
    public GUI(Visualiser visualiser) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        this.visualiser = visualiser;
        initComponents();
        initTreeMapVisualistion();

        String[] facilitators = new String[]{"Coffee3Hierarchy", "Coffee2Hierarchy", "Random", "SeqRandom", "RandomLogNormal", "SeqRandomLogNormal", "Poisson", "spacemacs", "PopularNames", "PopularNames2", "KijkCijfers10"};
        dataFacilitatorSelector.setModel(new DefaultComboBoxModel(facilitators));
        dataFacilitatorSelector.setSelectedIndex(7);
        setDataFacilitator("spacemacs");

        String[] generators = new String[]{"SliceAndDice", "PivotByMiddle", "PivotBySize", "PivotBySplitSize", "Strip", "StripLookAhead", "Squarified", "SquarifiedLookAhead", "Spiral", "SpiralLookAhead", "Moore", "Hilbert", "Approximation", "Incremental", "NoMovesIncremental"};
        treeMapSelector.setModel(new DefaultComboBoxModel(generators));
        treeMapSelector.setSelectedIndex(7);
        setTreeMapGenerator("SquarifiedLookAhead");

        setVisible(true);
        treeMapVisualisation.updateTreeMapRectangle();
        Rectangle treeMapRectangle = treeMapVisualisation.getTreemapRectangle();
        visualiser.setTreeMapRectangle(treeMapRectangle);

    }

    public void setDataFacilitator(String identifier) {
        int minItems = (int) minItemSpinner.getValue();
        int maxItems = (int) maxItemSpinner.getValue();
        int minDepth = (int) minDepthSpinner.getValue();
        int maxDepth = (int) maxDepthSpinner.getValue();
        int minSize = (int) minSizeSpinner.getValue();
        int maxSize = (int) maxSizeSpinner.getValue();
        int changeValue = (int) changeValueSpinner.getValue();
        int changeChance = (int) chanceChanceSpinner.getValue();
        int time = (int) timeSpinner.getValue();

        DataFacilitator df = null;
        switch (identifier) {
            case "Random":
                df = new RandomDataGenerator(minItems, maxItems, minDepth, maxDepth, minSize, maxSize);
                break;
            case "SeqRandom":
                df = new RandomSequentialDataGenerator(minItems, maxItems, minDepth, maxDepth, minSize, maxSize, changeValue, changeChance, time);
                break;
            case "RandomLogNormal":
                df = new RandomLogNormalDataGenerator(minItems, maxItems);
                break;
            case "SeqRandomLogNormal":
                df = new RandomLogNormalSequentialDataGenerator(minItems, maxItems, time);
                break;
            case "spacemacs":
                df = new DataFileManagerFast("D:\\Development\\TreemapStability\\dataset-temp\\spacemacs.data");
                break;
            case "PopularNames":
                df = new DataFileManager(new File("").getAbsolutePath() + "\\datasets\\PopularNamesSince1993.csv");
                break;
            case "PopularNames2":
                df = new DataFileManager("D:\\Development\\FinishedProjects\\StableTreemap\\datasets\\namen.csv");
                break;
            case "KijkCijfers10":
                df = new DataFileManager(new File("").getAbsolutePath() + "\\datasets\\zendersPercentages.csv");
                break;
            case "Coffee3Hierarchy":
                df = new DataFileManager(new File("").getAbsolutePath() + "\\datasets\\coffee20YearConsequitiveV2.csv");
                break;
            case "Coffee2Hierarchy":
                df = new DataFileManager(new File("").getAbsolutePath() + "\\datasets\\coffee20YearConsequitiveV4.csv");
                break;
        }
        visualiser.setDataFacilitator(df);
    }

    public void setTreeMapGenerator(String identifier) {
        TreeMapGenerator tmg = null;
        switch (identifier) {
            case "SliceAndDice":
                tmg = new SliceAndDice();
                break;
            case "PivotByMiddle":
                tmg = new PivotByMiddle();
                break;
            case "PivotBySize":
                tmg = new PivotBySize();
                break;
            case "PivotBySplitSize":
                tmg = new PivotBySplit();
                break;
            case "Strip":
                tmg = new StripTreeMap();
                break;
            case "StripLookAhead":
                tmg = new StripTreeMapLookAhead();
                break;
            case "Squarified":
                tmg = new SquarifiedTreeMap();
                break;
            case "SquarifiedLookAhead":
                tmg = new SquarifiedTreeMapLookAhead();
                break;
            case "Spiral":
                tmg = new SpiralTreeMap();
                break;
            case "SpiralLookAhead":
                tmg = new SpiralTreeMapLookAhead();
                break;
            case "Moore":
                tmg = new MooreTreeMap();
                break;
            case "Hilbert":
                tmg = new HilbertTreeMap();
                break;
            case "Approximation":
                tmg = new ApproximationTreeMap();
                break;
            case "Incremental":
                tmg = new IncrementalLayout(false);
                break;
            case "NoMovesIncremental":
                tmg = new IncrementalLayout(true);
                break;
        }
        visualiser.setTreeMapGenerator(tmg);
        getNewTreeMap();
    }

    public void updateTreeMap(TreeMap treeMap) {
        treeMapVisualisation.updateTreeMap(treeMap);
        setAspectRatioBeforeMoves(treeMap.getMaxAspectRatio());

        int time = (int) timeSpinner.getValue();
        if ("F:\\Development\\Treemap\\datasets\\PopularNamesSinceBirth.csv".equals(visualiser.getDataFacilitator().getDataIdentifier()));
        {
            int year = 2014 - time;
            Title.setText("" + year);
        }
    }

    private void initTreeMapVisualistion() {
        treeMapVisualisation = new TreeMapVisualisation();

        visualisationPanel.add(treeMapVisualisation);

        treeMapVisualisation.setBackground(Color.WHITE);
        treeMapVisualisation.setSize(visualisationPanel.getSize());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        Title = new javax.swing.JLabel();
        visualisationPanel = new javax.swing.JPanel();
        dataFacilitatorSelector = new javax.swing.JComboBox();
        treeMapSelector = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        timeSpinner = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        animationSpeedSlider = new javax.swing.JSlider();
        drawWeightCheckBox = new javax.swing.JCheckBox();
        minItemSpinner = new javax.swing.JSpinner();
        maxItemSpinner = new javax.swing.JSpinner();
        minItemsLabel = new javax.swing.JLabel();
        maxItemsLabel = new javax.swing.JLabel();
        minDepthLabel = new javax.swing.JLabel();
        minDepthSpinner = new javax.swing.JSpinner();
        maxDepthLabel = new javax.swing.JLabel();
        maxDepthSpinner = new javax.swing.JSpinner();
        minSizeLabel = new javax.swing.JLabel();
        maxSizeLabel = new javax.swing.JLabel();
        maxSizeSpinner = new javax.swing.JSpinner();
        minSizeSpinner = new javax.swing.JSpinner();
        changeValueLabel = new javax.swing.JLabel();
        changeValueSpinner = new javax.swing.JSpinner();
        chanceChanceLabel = new javax.swing.JLabel();
        chanceChanceSpinner = new javax.swing.JSpinner();
        orderEnabledCheckBox = new javax.swing.JCheckBox();
        orderLabelsEnabledCheckBox = new javax.swing.JCheckBox();
        simulationButton = new javax.swing.JButton();
        animationEnabledBox1 = new javax.swing.JCheckBox();
        jLabel16 = new javax.swing.JLabel();
        maxAspectRatioBeforeMove = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        simulationSpeedSlider = new javax.swing.JSlider();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        buttonShowNewGenerated = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0));
        jLabel19 = new javax.swing.JLabel();
        stabilityRelativeValue = new javax.swing.JLabel();
        useStoredButton = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        performMoveButton = new javax.swing.JButton();
        baseLineButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        Title.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        Title.setText("TreeMap visualiser");

        visualisationPanel.setBackground(new java.awt.Color(255, 0, 0));
        visualisationPanel.setPreferredSize(new java.awt.Dimension(500, 400));
        visualisationPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                visualisationPanelComponentResized(evt);
            }
        });

        javax.swing.GroupLayout visualisationPanelLayout = new javax.swing.GroupLayout(visualisationPanel);
        visualisationPanel.setLayout(visualisationPanelLayout);
        visualisationPanelLayout.setHorizontalGroup(
            visualisationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 933, Short.MAX_VALUE)
        );
        visualisationPanelLayout.setVerticalGroup(
            visualisationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        dataFacilitatorSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        dataFacilitatorSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                dataFacilitatorSelectorItemStateChanged(evt);
            }
        });

        treeMapSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        treeMapSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                treeMapSelectorItemStateChanged(evt);
            }
        });

        jLabel2.setText("Time");

        jLabel3.setText("Generation Method");

        jLabel4.setText("Facilitator");

        timeSpinner.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        timeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                timeSpinnerStateChanged(evt);
            }
        });

        jLabel5.setText("Animation speed");

        animationSpeedSlider.setPaintTicks(true);
        animationSpeedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                animationSpeedSliderStateChanged(evt);
            }
        });

        drawWeightCheckBox.setText("Draw weights");
        drawWeightCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                drawWeightCheckBoxStateChanged(evt);
            }
        });
        drawWeightCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawWeightCheckBoxActionPerformed(evt);
            }
        });

        minItemSpinner.setModel(new javax.swing.SpinnerNumberModel(3, 1, null, 1));
        minItemSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minItemSpinnerStateChanged(evt);
            }
        });

        maxItemSpinner.setModel(new javax.swing.SpinnerNumberModel(4, 1, null, 1));
        maxItemSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxItemSpinnerStateChanged(evt);
            }
        });

        minItemsLabel.setText("minItems");

        maxItemsLabel.setText("maxItems");

        minDepthLabel.setText("minDepth");

        minDepthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        minDepthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minDepthSpinnerStateChanged(evt);
            }
        });

        maxDepthLabel.setText("maxDepth");

        maxDepthSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        maxDepthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxDepthSpinnerStateChanged(evt);
            }
        });

        minSizeLabel.setText("minSize");

        maxSizeLabel.setText("maxSize");

        maxSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(40, 0, null, 1));
        maxSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxSizeSpinnerStateChanged(evt);
            }
        });

        minSizeSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        minSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minSizeSpinnerStateChanged(evt);
            }
        });

        changeValueLabel.setText("changeValue");

        changeValueSpinner.setModel(new javax.swing.SpinnerNumberModel(5, 1, null, 1));
        changeValueSpinner.setAutoscrolls(true);
        changeValueSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                changeValueSpinnerStateChanged(evt);
            }
        });

        chanceChanceLabel.setText("chanceChance");

        chanceChanceSpinner.setModel(new javax.swing.SpinnerNumberModel(50, 0, null, 1));
        chanceChanceSpinner.setMinimumSize(new java.awt.Dimension(40, 20));
        chanceChanceSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                chanceChanceSpinnerStateChanged(evt);
            }
        });

        orderEnabledCheckBox.setText("Show order-equivalance relations");
        orderEnabledCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                orderEnabledCheckBoxStateChanged(evt);
            }
        });

        orderLabelsEnabledCheckBox.setText("Show order-equivalance labels");
        orderLabelsEnabledCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                orderLabelsEnabledCheckBoxStateChanged(evt);
            }
        });
        orderLabelsEnabledCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderLabelsEnabledCheckBoxActionPerformed(evt);
            }
        });

        simulationButton.setText("Start simulation");
        simulationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simulationButtonActionPerformed(evt);
            }
        });

        animationEnabledBox1.setSelected(true);
        animationEnabledBox1.setText("Animation enabled");
        animationEnabledBox1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                animationEnabledBox1StateChanged(evt);
            }
        });
        animationEnabledBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                animationEnabledBox1ActionPerformed(evt);
            }
        });

        jLabel16.setText("Max aspect ratio");

        maxAspectRatioBeforeMove.setText("Undefined");

        jLabel18.setText("Simulation speed");

        simulationSpeedSlider.setPaintTicks(true);
        simulationSpeedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                simulationSpeedSliderStateChanged(evt);
            }
        });

        buttonShowNewGenerated.setText("Show new generation");
        buttonShowNewGenerated.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonShowNewGeneratedActionPerformed(evt);
            }
        });

        jLabel19.setText("StabilityRelative");

        stabilityRelativeValue.setText("Undefined");

        useStoredButton.setSelected(true);
        useStoredButton.setText("Use stored");
        useStoredButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                useStoredButtonStateChanged(evt);
            }
        });
        useStoredButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useStoredButtonActionPerformed(evt);
            }
        });

        jButton1.setText("Export");
        jButton1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jButton1StateChanged(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        performMoveButton.setText("Perform Move");
        performMoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performMoveButtonActionPerformed(evt);
            }
        });

        baseLineButton.setText("baseLine");
        baseLineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseLineButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addComponent(visualisationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 933, Short.MAX_VALUE)
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(Title)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel18)
                            .addComponent(jLabel5)
                            .addComponent(dataFacilitatorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(animationSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(simulationSpeedSlider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                    .addComponent(simulationButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                                    .addComponent(performMoveButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE))))
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(minDepthLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minDepthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(maxDepthLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxDepthSpinner))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(minSizeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minSizeSpinner))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(maxSizeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxSizeSpinner))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(changeValueLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(changeValueSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(chanceChanceLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chanceChanceSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(minItemsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(minItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(maxItemsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(buttonShowNewGenerated)
                            .addComponent(baseLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(timeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)
                            .addComponent(treeMapSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(orderEnabledCheckBox)
                            .addComponent(orderLabelsEnabledCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(143, 143, 143)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(drawWeightCheckBox)
                                    .addComponent(animationEnabledBox1)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(filler2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, 0)
                                        .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(useStoredButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jButton1))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel19)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(stabilityRelativeValue))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addComponent(jLabel16)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                                    .addComponent(maxAspectRatioBeforeMove))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(filler3, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {chanceChanceLabel, changeValueLabel, maxDepthLabel, maxItemsLabel, maxSizeLabel, minDepthLabel, minItemsLabel, minSizeLabel});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {chanceChanceSpinner, changeValueSpinner, maxDepthSpinner, maxSizeSpinner, minSizeSpinner});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(Title)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(visualisationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE)
                .addGap(30, 30, 30))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(filler3, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(16, 16, 16)
                                        .addComponent(treeMapSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jLabel4))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(76, 76, 76)
                                        .addComponent(dataFacilitatorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(minItemsLabel)
                                            .addComponent(minItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(maxItemsLabel)
                                            .addComponent(maxItemSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(4, 4, 4)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(minDepthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(minDepthLabel))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(timeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(simulationButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(performMoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(91, 91, 91)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(animationSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(1, 1, 1)
                                        .addComponent(jLabel18)
                                        .addGap(18, 18, 18)
                                        .addComponent(simulationSpeedSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(maxDepthLabel)
                                            .addComponent(maxDepthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(minSizeLabel)
                                            .addComponent(minSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(maxSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(maxSizeLabel))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(changeValueLabel)
                                            .addComponent(changeValueSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(chanceChanceSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(chanceChanceLabel))
                                        .addGap(3, 3, 3)
                                        .addComponent(animationEnabledBox1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(drawWeightCheckBox)
                                        .addGap(30, 30, 30)
                                        .addComponent(baseLineButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(buttonShowNewGenerated)))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orderEnabledCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orderLabelsEnabledCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel16)
                            .addComponent(maxAspectRatioBeforeMove))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(stabilityRelativeValue)
                            .addComponent(jLabel19))))
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useStoredButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addGap(15, 15, 15)
                .addComponent(filler1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 60, Short.MAX_VALUE)
                .addComponent(filler2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {chanceChanceSpinner, changeValueSpinner, maxDepthSpinner, maxItemSpinner, maxSizeSpinner, minDepthSpinner, minItemSpinner, minSizeSpinner});

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {chanceChanceLabel, changeValueLabel, maxDepthLabel, maxItemsLabel, maxSizeLabel, minDepthLabel, minItemsLabel, minSizeLabel});

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void timeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_timeSpinnerStateChanged
        if (!simulationStep) {
            getNewTreeMap();
        }
        simulationStep = false;
    }//GEN-LAST:event_timeSpinnerStateChanged

    private void visualisationPanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_visualisationPanelComponentResized
        if (treeMapVisualisation != null) {
            treeMapVisualisation.setSize(visualisationPanel.getSize());
            treeMapVisualisation.updateTreeMapRectangle();
            visualiser.setTreeMapRectangle(treeMapVisualisation.getTreemapRectangle());
            if (treeMapVisualisation.isShowingTreeMap()) {
                getNewTreeMap();
            }
        }
    }//GEN-LAST:event_visualisationPanelComponentResized

    private void treeMapSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_treeMapSelectorItemStateChanged
        setTreeMapGenerator((String) treeMapSelector.getSelectedItem());
    }//GEN-LAST:event_treeMapSelectorItemStateChanged

    private void dataFacilitatorSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_dataFacilitatorSelectorItemStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());        // TODO add your handling code here:
    }//GEN-LAST:event_dataFacilitatorSelectorItemStateChanged

    private void animationSpeedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_animationSpeedSliderStateChanged
        int sliderValue = animationSpeedSlider.getValue();
        treeMapVisualisation.setAnimationSpeed(sliderValue);
    }//GEN-LAST:event_animationSpeedSliderStateChanged

    private void drawWeightCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_drawWeightCheckBoxStateChanged

    }//GEN-LAST:event_drawWeightCheckBoxStateChanged


    private void minItemSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minItemSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_minItemSpinnerStateChanged

    private void maxItemSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxItemSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_maxItemSpinnerStateChanged

    private void minSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minSizeSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_minSizeSpinnerStateChanged

    private void maxDepthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxDepthSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_maxDepthSpinnerStateChanged

    private void minDepthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minDepthSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_minDepthSpinnerStateChanged

    private void maxSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxSizeSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_maxSizeSpinnerStateChanged

    private void changeValueSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeValueSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_changeValueSpinnerStateChanged

    private void chanceChanceSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_chanceChanceSpinnerStateChanged
        setDataFacilitator((String) dataFacilitatorSelector.getSelectedItem());
    }//GEN-LAST:event_chanceChanceSpinnerStateChanged

    private void orderEnabledCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_orderEnabledCheckBoxStateChanged
    }//GEN-LAST:event_orderEnabledCheckBoxStateChanged

    private void orderLabelsEnabledCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_orderLabelsEnabledCheckBoxStateChanged
    }//GEN-LAST:event_orderLabelsEnabledCheckBoxStateChanged

    private void simulationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simulationButtonActionPerformed
        if (visualiser == null || !visualiser.isInitialized() && treeMapVisualisation != null && !treeMapVisualisation.isShowingTreeMap()) {
            return;
        }
        if (simulationRunning) {
            stopSimulation();
            simulationButton.setText("Start simulation");
        } else {
            startSimulation();
            simulationButton.setText("Stop simulation");
        }
        simulationRunning = !simulationRunning;
    }//GEN-LAST:event_simulationButtonActionPerformed

    private Thread simulationThread;
    private volatile boolean simulationThreadStop;

    private void startSimulation() {
        //disallow changes to the timespinner while the simulation is running
        timeSpinner.setEnabled(false);
        int currentTime = (int) timeSpinner.getValue();
        //get a new initial treeMap
        //if (!treeMapVisualisation.isShowingTreeMap()) {
        visualiser.getTreeMap(currentTime, useStored, "");
//        }

        simulationThreadStop = false;
        //TODO start both of these after the animation has finished.
        simulationThread = new Thread(new Runnable() {

            @Override
            public void run() {
                runSimulation();
            }

        });

        simulationThread.start();
    }

    private void runSimulation() {
//        try {
        while (!simulationThreadStop) {
            //letting the spinner know that the change was coming from the simulation button
            simulationStep = true;
            int currentTime = (int) timeSpinner.getValue();

            int newTime = currentTime + 1;
            timeSpinner.setValue(newTime);

            System.out.print("\tnewTime = " + newTime + "\t");
            getNewTreeMap();
            treeMapVisualisation.toIpe("ipe//ipeFromTime_" + newTime + ".ipe");

//                while (treeMapVisualisation.treeMapRepaint == false) {
//                    Thread.sleep(50);
//                }
//                Thread.sleep(simulationSpeed);
//                while (treeMapVisualisation.treeMapRepaint == false) {
//                    Thread.sleep(50);
//                }
//                Thread.sleep(simulationSpeed);
        }
//        } 
//        catch (InterruptedException ex) {
//            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void stopSimulation() {
        simulationThreadStop = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    simulationThread.join();
                    timeSpinner.setEnabled(true);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }

    private void animationEnabledBox1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_animationEnabledBox1StateChanged
    }//GEN-LAST:event_animationEnabledBox1StateChanged

    private void simulationSpeedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_simulationSpeedSliderStateChanged
        int sliderValue = simulationSpeedSlider.getValue();
        //value is between 0 and 100
        simulationSpeed = (100 - sliderValue) * 50;


    }//GEN-LAST:event_simulationSpeedSliderStateChanged

    private void orderLabelsEnabledCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderLabelsEnabledCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_orderLabelsEnabledCheckBoxActionPerformed

    private void drawWeightCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawWeightCheckBoxActionPerformed
        treeMapVisualisation.setDrawWeight(drawWeightCheckBox.isSelected());
    }//GEN-LAST:event_drawWeightCheckBoxActionPerformed

    private void animationEnabledBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_animationEnabledBox1ActionPerformed
        treeMapVisualisation.setAnimationEnabled(animationEnabledBox1.isSelected());
    }//GEN-LAST:event_animationEnabledBox1ActionPerformed

    private void buttonShowNewGeneratedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonShowNewGeneratedActionPerformed
        int currentTime = (int) timeSpinner.getValue();
        TreeMap tm = visualiser.getNewGeneratedTreeMap(currentTime);
        treeMapVisualisation.updateTreeMap(tm);
    }//GEN-LAST:event_buttonShowNewGeneratedActionPerformed

    private void useStoredButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_useStoredButtonStateChanged
        this.useStored = useStoredButton.isSelected();
        // TODO add your handling code here:
    }//GEN-LAST:event_useStoredButtonStateChanged

    private void useStoredButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useStoredButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_useStoredButtonActionPerformed

    private void jButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jButton1StateChanged
        // TODO add your handling code here:


    }//GEN-LAST:event_jButton1StateChanged

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        treeMapVisualisation.toIpe();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void performMoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performMoveButtonActionPerformed
        if (visualiser != null && ((String) treeMapSelector.getSelectedItem()).equals("NoMovesIncremental")) {
            visualiser.performMove();
        }
    }//GEN-LAST:event_performMoveButtonActionPerformed

    private void baseLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseLineButtonActionPerformed
        if (visualiser.getCurrentTreeMap() != null) {
            BaseLineGenerator blg = new BaseLineGenerator();
            TreeMap currentTreemap = visualiser.getCurrentTreeMap();

            int currentTime = (int) timeSpinner.getValue();
            DataMap currentDm = visualiser.getDataFacilitator().getData(currentTime);
            DataMap newDm = visualiser.getDataFacilitator().getData(currentTime + 1);

            TreeMap baseLine = blg.generateBaseLine(currentTreemap, currentDm, newDm);
            visualiser.setTreeMap(baseLine);

            RelativeQuadrantStability rStab = new RelativeQuadrantStability();
            rStab.getStability(currentTreemap, baseLine);

            TreeMap newTm = visualiser.getNewGeneratedTreeMap(currentTime + 1);
            double score = rStab.getStability(currentTreemap, newTm);
            double baseScore = rStab.getStability(currentTreemap, baseLine);

            System.out.println("baseScore = " + baseScore + ";" + "score = " + score);

        }
    }//GEN-LAST:event_baseLineButtonActionPerformed

    private void getNewTreeMap() {
        if (visualiser == null || !visualiser.isInitialized()) {
            return;
        }
        int time = (int) timeSpinner.getValue();
        visualiser.getTreeMap(time, useStored, "Test");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Title;
    private javax.swing.JCheckBox animationEnabledBox1;
    private javax.swing.JSlider animationSpeedSlider;
    private javax.swing.JButton baseLineButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton buttonShowNewGenerated;
    private javax.swing.JLabel chanceChanceLabel;
    private javax.swing.JSpinner chanceChanceSpinner;
    private javax.swing.JLabel changeValueLabel;
    private javax.swing.JSpinner changeValueSpinner;
    private javax.swing.JComboBox dataFacilitatorSelector;
    private javax.swing.JCheckBox drawWeightCheckBox;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel maxAspectRatioBeforeMove;
    private javax.swing.JLabel maxDepthLabel;
    private javax.swing.JSpinner maxDepthSpinner;
    private javax.swing.JSpinner maxItemSpinner;
    private javax.swing.JLabel maxItemsLabel;
    private javax.swing.JLabel maxSizeLabel;
    private javax.swing.JSpinner maxSizeSpinner;
    private javax.swing.JLabel minDepthLabel;
    private javax.swing.JSpinner minDepthSpinner;
    private javax.swing.JSpinner minItemSpinner;
    private javax.swing.JLabel minItemsLabel;
    private javax.swing.JLabel minSizeLabel;
    private javax.swing.JSpinner minSizeSpinner;
    private javax.swing.JCheckBox orderEnabledCheckBox;
    private javax.swing.JCheckBox orderLabelsEnabledCheckBox;
    private javax.swing.JButton performMoveButton;
    private javax.swing.JButton simulationButton;
    private javax.swing.JSlider simulationSpeedSlider;
    private javax.swing.JLabel stabilityRelativeValue;
    private javax.swing.JSpinner timeSpinner;
    private javax.swing.JComboBox treeMapSelector;
    private javax.swing.JCheckBox useStoredButton;
    private javax.swing.JPanel visualisationPanel;
    // End of variables declaration//GEN-END:variables

    public void setStability(Map<String, Double> stabilities) {
        if (stabilities == null) {
//            stabilityNoModifierValue.setText("Undefined");
//            stabilitySizeValue.setText("Undefined");
//            stabilityDistanceValue.setText("Undefined");
//            stabilityMaxDistanceValue.setText("Undefined");
            stabilityRelativeValue.setText("Undefined");
            return;
        }

        if (stabilities.containsKey("stabilityRelative")) {
            double modifierValue = stabilities.get("stabilityRelative");
            String value = "" + (Math.floor(modifierValue * 100) / 100);
            //make sure it has 2 decimals
            int decimals = value.substring(value.indexOf(".") + 1).length();

            if (decimals == 1) {
                value += "0";
            }
            if (decimals == 0) {
                value += "00";
            }
            stabilityRelativeValue.setText("" + value);
        } else {
            stabilityRelativeValue.setText("Undefined");
        }

//        if (stabilities.containsKey("NoModifier")) {
//            double modifierValue = stabilities.get("NoModifier");
//            stabilityNoModifierValue.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilityNoModifierValue.setText("Undefined");
//        }
//        if (stabilities.containsKey("Size")) {
//            double modifierValue = stabilities.get("Size");
//            stabilitySizeValue.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilitySizeValue.setText("Undefined");
//        }
//        if (stabilities.containsKey("Distance")) {
//            double modifierValue = stabilities.get("Distance");
//            stabilityDistanceValue.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilityDistanceValue.setText("MaxDistance");
//        }
//        if (stabilities.containsKey("MaxDistance")) {
//            double modifierValue = stabilities.get("MaxDistance");
//            stabilityMaxDistanceValue.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilityMaxDistanceValue.setText("Undefined");
//        }
//        if (stabilities.containsKey("EdgeModifications")) {
//            double modifierValue = stabilities.get("EdgeModifications");
//            stabilityEdgeModifications.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilityEdgeModifications.setText("Undefined");
//        }
//
//        if (stabilities.containsKey("WeightedEdgeModifications")) {
//            double modifierValue = stabilities.get("WeightedEdgeModifications");
//            stabilityWeightedEdgeModifications.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilityWeightedEdgeModifications.setText("Undefined");
//        }
//        if (stabilities.containsKey("String")) {
//            double modifierValue = stabilities.get("String");
//            stabilityString.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilityString.setText("Undefined");
//        }
//        if (stabilities.containsKey("StringPositive")) {
//            double modifierValue = stabilities.get("StringPositive");
//            stabilityStringPositive.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilityStringPositive.setText("Undefined");
//        }
//        if (stabilities.containsKey("StringUnique")) {
//            double modifierValue = stabilities.get("StringUnique");
//            stabilityStringUnique.setText("" + (Math.floor(modifierValue * 100) / 100));
//        } else {
//            stabilityStringUnique.setText("Undefined");
//        }
    }

    public void setAspectRatioBeforeMoves(double maxAspectRatio) {
        String value = "" + (Math.floor(maxAspectRatio * 100) / 100);
        //make sure it has 2 decimals
        int decimals = value.substring(value.indexOf(".") + 1).length();
        if (decimals == 1) {
            value += "0";
        }
        if (decimals == 0) {
            value += "00";
        }
        maxAspectRatioBeforeMove.setText("" + value);
    }

    public void setAspectRatioAfterMoves(double maxAspectRatio) {
        String value = "" + (Math.floor(maxAspectRatio * 100) / 100);
        //make sure it has 2 decimals
        int decimals = value.substring(value.indexOf(".") + 1).length();

        if (decimals == 1) {
            value += "0";
        }
        if (decimals == 0) {
            value += "00";
        }
        maxAspectRatioBeforeMove.setText("" + value);
    }

}
