/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.GUIComponents;

import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.PlateProperties;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.SeqAcqProps;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralGUIComponents.HCAFLIMPluginFrame;
import com.github.dougkelly88.FLIMPlateReaderGUI.InstrumentInterfaceClasses.XYZMotionInterface;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.FOV;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.FOVTableModel;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.TableRenderer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author dk1109
 */
public class XYSequencing extends javax.swing.JPanel {

    PlateProperties pp_;
    PlateMapDrawPanel pmdp_;
    public FOVTableModel tableModel_;
    JTable fovTable_;
    SeqAcqProps sap_;
    HCAFLIMPluginFrame parent_;
    final static String um = "(" + "\u00B5" + "m)";
    boolean zAsOffset_ = true;
    double[] zStackParams = {0.0, 0.0, 1.0};
    XYZMotionInterface xyzmi_;

    /**
     * Creates new form XYSequencing
     */
    public XYSequencing() {
        initComponents();
        setControlDefaults();
    }

    private void setControlDefaults() {

        pmdp_ = new PlateMapDrawPanel(this);
        sap_ = SeqAcqProps.getInstance();
        plateMapBasePanel.setLayout(new BorderLayout());
        plateMapBasePanel.add(pmdp_, BorderLayout.CENTER);

        tableModel_ = new FOVTableModel(pp_);
        tableModel_.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {

            }
        });

        fovTable_ = new JTable();
        fovTable_.setModel(tableModel_);
        fovTable_.setSurrendersFocusOnKeystroke(true);
        fovTable_.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JScrollPane scroller = new javax.swing.JScrollPane(fovTable_);
        fovTable_.setPreferredScrollableViewportSize(new java.awt.Dimension(190, 130));
        fovTablePanel.setLayout(new BorderLayout());
        fovTablePanel.add(scroller, BorderLayout.CENTER);

        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete FOV");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int r = fovTable_.getSelectedRow();
                tableModel_.removeRow(r);
            }
        });
        JMenuItem addItem = new JMenuItem("Add FOV");
        addItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int r = fovTable_.getSelectedRow();
                tableModel_.insertRow(r, new FOV("A1", pp_, 6000));
            }
        });
        JMenuItem goToFOVItem = new JMenuItem("Go to FOV");
        goToFOVItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int r = fovTable_.getSelectedRow();
//                FOV fov = tableModel_.getData().get(r);
                xyzmi_.gotoFOV(tableModel_.getData().get(r));
                if (!zAsOffset_){
                    double zval = tableModel_.getData().get(r).getZ();
                    xyzmi_.moveZAbsolute(zval);
                }
                else {
                    // obviously, this isn't quite right - we want to get
                    // the offset of the CURRENT FOV (perhaps from parent in 
                    // later implementations?) and subtract from that of the 
                    // NEWLY SELECTED FOV. 
                    // TODO: fix for proper zAsOffset behaviour. 
                    xyzmi_.moveZRelative(tableModel_.getData().get(r).getZ());
                }
            }
        });
        
        popupMenu.add(addItem);
        popupMenu.add(deleteItem);
        popupMenu.add(goToFOVItem);

        fovTable_.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
//                System.out.println("pressed");
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    JTable source = (JTable) e.getSource();
                    int row = source.rowAtPoint(e.getPoint());
                    int column = source.columnAtPoint(e.getPoint());

                    if (!source.isRowSelected(row)) {
                        source.changeSelection(row, column, false, false);
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

//        fovTable_.setDefaultRenderer(FOV.class, new TableRenderer());
    }

    public void onPlateConfigLoaded(boolean enable, PlateProperties pp) {
        pmdp_.setEnabled(enable, pp);
        pp_ = pp;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        storedXYZPanel = new javax.swing.JPanel();
        clearXYZButton = new javax.swing.JButton();
        storeXYZButton = new javax.swing.JButton();
        fovTablePanel = new javax.swing.JPanel();
        genZStackButton = new javax.swing.JButton();
        zModeCombo = new javax.swing.JComboBox();
        clearZButton = new javax.swing.JButton();
        prefindPanel = new javax.swing.JPanel();
        quickPFButton = new javax.swing.JButton();
        advancedPFButton = new javax.swing.JToggleButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        FOVToFindField = new javax.swing.JFormattedTextField();
        attemptsField = new javax.swing.JFormattedTextField();
        intensityThresoldField = new javax.swing.JFormattedTextField();
        plateMapBasePanel = new javax.swing.JPanel();
        autoFOVPanel = new javax.swing.JPanel();
        autoGenerateFOVsCheck = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        noFOVsField = new javax.swing.JFormattedTextField();
        FOVPatternCombo = new javax.swing.JComboBox();
        ringRadiusField = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        groupDescField = new javax.swing.JTextField();

        storedXYZPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Stored XYZ positions"));

        clearXYZButton.setText("Clear stored XYZ");
        clearXYZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearXYZButtonActionPerformed(evt);
            }
        });

        storeXYZButton.setText("Store current XYZ");
        storeXYZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeXYZButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout fovTablePanelLayout = new javax.swing.GroupLayout(fovTablePanel);
        fovTablePanel.setLayout(fovTablePanelLayout);
        fovTablePanelLayout.setHorizontalGroup(
            fovTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        fovTablePanelLayout.setVerticalGroup(
            fovTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        genZStackButton.setText("Generate Z stack...");
        genZStackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genZStackButtonActionPerformed(evt);
            }
        });

        zModeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Z as offset", "Absolute Z" }));
        zModeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zModeComboActionPerformed(evt);
            }
        });

        clearZButton.setText("Clear Z stack");
        clearZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearZButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout storedXYZPanelLayout = new javax.swing.GroupLayout(storedXYZPanel);
        storedXYZPanel.setLayout(storedXYZPanelLayout);
        storedXYZPanelLayout.setHorizontalGroup(
            storedXYZPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, storedXYZPanelLayout.createSequentialGroup()
                .addComponent(fovTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(storedXYZPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(storeXYZButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(clearXYZButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(genZStackButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(zModeCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(clearZButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(34, 34, 34))
        );
        storedXYZPanelLayout.setVerticalGroup(
            storedXYZPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, storedXYZPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(storedXYZPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fovTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(storedXYZPanelLayout.createSequentialGroup()
                        .addGap(0, 3, Short.MAX_VALUE)
                        .addComponent(zModeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(genZStackButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearZButton)
                        .addGap(5, 5, 5)
                        .addComponent(storeXYZButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearXYZButton)))
                .addGap(37, 37, 37))
        );

        prefindPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Prefind"));

        quickPFButton.setText("Quick prefind");
        quickPFButton.setMargin(new java.awt.Insets(2, 8, 2, 8));
        quickPFButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quickPFButtonActionPerformed(evt);
            }
        });

        advancedPFButton.setText("Setup advanced prefind...");
        advancedPFButton.setMargin(new java.awt.Insets(2, 4, 2, 4));
        advancedPFButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedPFButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Intensity threshold value (DN)");

        jLabel4.setText("Desired number of FOV/well");

        jLabel5.setText("Attempts before failing");

        FOVToFindField.setText("4");
        FOVToFindField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FOVToFindFieldActionPerformed(evt);
            }
        });

        attemptsField.setText("4");
        attemptsField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attemptsFieldActionPerformed(evt);
            }
        });

        intensityThresoldField.setText("1000");
        intensityThresoldField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                intensityThresoldFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout prefindPanelLayout = new javax.swing.GroupLayout(prefindPanel);
        prefindPanel.setLayout(prefindPanelLayout);
        prefindPanelLayout.setHorizontalGroup(
            prefindPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(prefindPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(prefindPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(prefindPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(intensityThresoldField, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(prefindPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(FOVToFindField, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(prefindPanelLayout.createSequentialGroup()
                        .addComponent(quickPFButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(advancedPFButton))
                    .addGroup(prefindPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(attemptsField, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        prefindPanelLayout.setVerticalGroup(
            prefindPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, prefindPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(prefindPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(intensityThresoldField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(prefindPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(FOVToFindField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(prefindPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(attemptsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(prefindPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(quickPFButton)
                    .addComponent(advancedPFButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout plateMapBasePanelLayout = new javax.swing.GroupLayout(plateMapBasePanel);
        plateMapBasePanel.setLayout(plateMapBasePanelLayout);
        plateMapBasePanelLayout.setHorizontalGroup(
            plateMapBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 470, Short.MAX_VALUE)
        );
        plateMapBasePanelLayout.setVerticalGroup(
            plateMapBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 320, Short.MAX_VALUE)
        );

        autoGenerateFOVsCheck.setText("Auto-generate FOVs?");
        autoGenerateFOVsCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoGenerateFOVsCheckActionPerformed(evt);
            }
        });

        jLabel1.setText("# FOVs ");

        noFOVsField.setText("4");
        noFOVsField.setEnabled(false);
        noFOVsField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noFOVsFieldActionPerformed(evt);
            }
        });

        FOVPatternCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Spiral", "Ring", "ZFish" }));
        FOVPatternCombo.setEnabled(false);
        FOVPatternCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FOVPatternComboActionPerformed(evt);
            }
        });

        ringRadiusField.setText("3000");
        ringRadiusField.setEnabled(false);
        ringRadiusField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ringRadiusFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Ring radius um:");

        jLabel6.setText("Group description:");

        groupDescField.setText("Experiment");
        groupDescField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupDescFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout autoFOVPanelLayout = new javax.swing.GroupLayout(autoFOVPanel);
        autoFOVPanel.setLayout(autoFOVPanelLayout);
        autoFOVPanelLayout.setHorizontalGroup(
            autoFOVPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autoFOVPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(autoFOVPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(autoFOVPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ringRadiusField, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(autoGenerateFOVsCheck, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, autoFOVPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(noFOVsField, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(FOVPatternCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6)
                    .addComponent(groupDescField))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        autoFOVPanelLayout.setVerticalGroup(
            autoFOVPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(autoFOVPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(autoGenerateFOVsCheck)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(autoFOVPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(noFOVsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(FOVPatternCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(autoFOVPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ringRadiusField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(52, 52, 52)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(groupDescField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(prefindPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(storedXYZPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(plateMapBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(autoFOVPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(storedXYZPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(prefindPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(plateMapBasePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(autoFOVPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void autoGenerateFOVsCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoGenerateFOVsCheckActionPerformed
        boolean autoFOV = autoGenerateFOVsCheck.isSelected();
        noFOVsField.setEnabled(autoFOV);
        FOVPatternCombo.setEnabled(autoFOV);
        ringRadiusField.setEnabled(autoFOV & (FOVPatternCombo.getSelectedIndex() == 1));
        if (autoFOV) {
            generateFOVs();
        }
    }//GEN-LAST:event_autoGenerateFOVsCheckActionPerformed

    public void generateFOVs() {
        if (autoGenerateFOVsCheck.isSelected()) {

            ArrayList<FOV> fovs = new ArrayList<FOV>();
            ArrayList<FOV> preexisting = new ArrayList<FOV>(tableModel_.getData());
            tableModel_.clearAllData();

            for (int cols = 0; cols < pp_.getPlateColumns(); cols++) {
                ArrayList<Boolean> temp = pmdp_.wellsSelected_.get(cols);
                for (int rows = 0; rows < pp_.getPlateRows(); rows++) {
                    if (temp.get(rows)) {

                        String wellString = Character.toString((char) (65 + rows)) + Integer.toString(cols + 1);
                        if (FOVPatternCombo.getSelectedIndex() == 0) { // spiral pattern
                            fovs = generateSpiral(Integer.parseInt(noFOVsField.getText()),
                                    wellString);
                        } else {
                            fovs = generateRing(Integer.parseInt(noFOVsField.getText()),
                                    wellString);
                        }

                        for (FOV fov : fovs) {
                            if (preexisting.contains(fov)) {
//                                int ind = preexisting.indexOf(fov);
                                fov.setGroup(preexisting.get(preexisting.indexOf(fov)).getGroup());
                            } else {
                                fov.setGroup(groupDescField.getText());
                            }
                            
                            tableModel_.addRow(fov);
//                            xyzmi_.fovXYtoStageXY(fov);
                        }
                        
                        doZStackGeneration(getZStackParams());
//                        tableModel_.addRow(new FOV(wellString, pp_, 0));
                    }
                }
            }
        }
    }

    private ArrayList<FOV> generateSpiral(int noFOV, String wellString) {

        // cover whole well in a rectangle; remove those outwith well bounds;
        // finally trim to #fov. Deals with asymmetric FOV
        ArrayList<FOV> spiralFOVs = new ArrayList<FOV>();
        FOV fov = new FOV(wellString, pp_, 0);
        double[] centrexy = {fov.getX(), fov.getY()};
//        double[] DXY = {sap_.getFLIMFOVSize()[0], sap_.getFLIMFOVSize()[1]};
        double[] DXY = {parent_.currentFOV_.getWidth_(), parent_.currentFOV_.getHeight_()};
        
        int[][] dir = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        double[] dxy = new double[2];
        int stepsInCurrentDir;

        spiralFOVs.add(fov);
        int fovind = 1;
        int dirind = 0;
        while (fovind < noFOV & dirind < 100) {   // just in case we have a runaway case...

            stepsInCurrentDir = (int) Math.ceil((double) (dirind) / 2);

            dxy[0] = dir[dirind % 4][0] * DXY[0];
            dxy[1] = dir[dirind % 4][1] * DXY[1];
            for (int j = 0; j < stepsInCurrentDir; j++) {
                centrexy[0] += dxy[0];
                centrexy[1] += dxy[1];
                fov = new FOV(centrexy[0], centrexy[1], 0,
                        wellString, pp_);
                if (fov.isValid()) {
                    spiralFOVs.add(fov);
                    fovind++;
                }
            }
            dirind++;
            System.out.print("Dirind = " + dirind + "\n");
        }
        // trim, a bit hacky but works
        int currsize = spiralFOVs.size();
        for (int j = currsize - 1; j > noFOV - 1; j--) {
            spiralFOVs.remove(j);
        }
        return spiralFOVs;
    }

    private ArrayList<FOV> generateRing(int noFOV, String wellString) {
        return new ArrayList<FOV>();
    }

    private void noFOVsFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noFOVsFieldActionPerformed
        generateFOVs();
    }//GEN-LAST:event_noFOVsFieldActionPerformed

    private void FOVPatternComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FOVPatternComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FOVPatternComboActionPerformed

    private void ringRadiusFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ringRadiusFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ringRadiusFieldActionPerformed

    private void quickPFButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quickPFButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quickPFButtonActionPerformed

    private void advancedPFButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedPFButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_advancedPFButtonActionPerformed

    private void FOVToFindFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FOVToFindFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FOVToFindFieldActionPerformed

    private void attemptsFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attemptsFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_attemptsFieldActionPerformed

    private void intensityThresoldFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_intensityThresoldFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_intensityThresoldFieldActionPerformed

    private void clearXYZButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearXYZButtonActionPerformed
        tableModel_.clearAllData();
        pmdp_.clearAllWells();
    }//GEN-LAST:event_clearXYZButtonActionPerformed

    private void genZStackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genZStackButtonActionPerformed
        //TODO: generate new FOV in current position if FOV table is empty. 
        //TODO: 

        double startUm = -1.0;
        double endUm = -1.0;
        double stepUm = 1.0;

        String um = "(" + "\u00B5" + "m)";
        JLabel messageLabel = new JLabel("<html>Please enter start, end and delta values for Z stack: </html>");
        JLabel startLabel = new JLabel("Start Z position " + um + ":");
        JLabel endLabel = new JLabel("End Z position " + um + ":");
        JLabel stepLabel = new JLabel("Step size " + um + ":");

        // Uses a custom NumberFormat.
        NumberFormat customFormat = NumberFormat.getInstance(new Locale("en_US"));
        JFormattedTextField customFormatField
                = new JFormattedTextField(new NumberFormatter(customFormat));

        JFormattedTextField startField = new JFormattedTextField(customFormat);
        startField.setValue(-3.0);
        JFormattedTextField endField = new JFormattedTextField(customFormat);
        endField.setValue(3.0);
        JFormattedTextField stepField = new JFormattedTextField(customFormat);
        stepField.setValue(1.0);

        JPanel zStackDialog = new JPanel();
        zStackDialog.setLayout(new BorderLayout(50, 100));

        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new GridLayout(3, 2, 50, 10));

        controlsPanel.add(startLabel);
        controlsPanel.add(startField);
        controlsPanel.add(endLabel);
        controlsPanel.add(endField);
        controlsPanel.add(stepLabel);
        controlsPanel.add(stepField);

        zStackDialog.add(messageLabel, BorderLayout.PAGE_START);
        zStackDialog.add(controlsPanel, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(this, zStackDialog,
                "Z stack setup", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {

            // must be a better way to achieve this...?
            if (startField.getValue().getClass() == Double.class) {
                startUm = (Double) (startField.getValue());
            } else {
                startUm = ((Long) startField.getValue()).doubleValue();
            }

            if (endField.getValue().getClass() == Double.class) {
                endUm = (Double) (endField.getValue());
            } else {
                endUm = ((Long) endField.getValue()).doubleValue();
            }

            if (stepField.getValue().getClass() == Double.class) {
                stepUm = (Double) (stepField.getValue());
            } else {
                stepUm = ((Long) stepField.getValue()).doubleValue();
            }

            setZStackParams(startUm, endUm, stepUm);
            
            doZStackGeneration(getZStackParams());
        }

    }//GEN-LAST:event_genZStackButtonActionPerformed

    private void doZStackGeneration(double[] z) {
        double startUm = z[0];
        double endUm = z[1];
        double stepUm = z[2];
        
        ArrayList<FOV> temp = tableModel_.getData();
        ArrayList<FOV> newtemp = new ArrayList<FOV>();
        ArrayList<FOV> unique = new ArrayList<FOV>();

        for (FOV fov : temp) {
            if (!unique.contains(fov)) {
                fov.setZ(0);
                unique.add(fov);
            }
        }

        int Nz = (int) ((endUm - startUm) / stepUm + 1);

        for (FOV fov : unique) {

            for (int zpos = 0; zpos < Nz; zpos++) {

                double zed = startUm + zpos * stepUm;
                newtemp.add(new FOV(fov.getX(), fov.getY(), fov.getZ() + zed,
                        fov.getWell(), fov.getPlateProps()));
            }
        }

        tableModel_.addWholeData(newtemp);
    }

    private void zModeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zModeComboActionPerformed
        zAsOffset_ = (String) zModeCombo.getSelectedItem() == "Z as offset";
    }//GEN-LAST:event_zModeComboActionPerformed

    private void storeXYZButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeXYZButtonActionPerformed
        
        FOV newfov = xyzmi_.getCurrentFOV();
        tableModel_.addRow(newfov);
//        doZStackGeneration(getZStackParams());
        // deal with ZFish case (tile FOVS)
        if (( (String) FOVPatternCombo.getSelectedItem()).equals("ZFish") & autoGenerateFOVsCheck.isSelected()){
            // currently hardcode fov size for 10 x objective...
            // boilerplate - tidy up!
            double fovx = 1256;
            double fovy = 920;
            FOV q1 = new FOV(newfov.getX() - fovx/2, newfov.getY() - fovy/2, newfov.getZ(), pp_);
            FOV q2 = new FOV(newfov.getX() + fovx/2, newfov.getY() - fovy/2, newfov.getZ(), pp_);
            FOV q3 = new FOV(newfov.getX() + fovx/2, newfov.getY() + fovy/2, newfov.getZ(), pp_);
            FOV q4 = new FOV(newfov.getX() - fovx/2, newfov.getY() + fovy/2, newfov.getZ(), pp_);
            tableModel_.addRow(q1);
            tableModel_.addRow(q2);
            tableModel_.addRow(q3);
            tableModel_.addRow(q4);
            
        }
        pmdp_.addSelectedWell(newfov.getWell());
    }//GEN-LAST:event_storeXYZButtonActionPerformed

    private void groupDescFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupDescFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_groupDescFieldActionPerformed

    private void clearZButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearZButtonActionPerformed
        setZStackParams(0.0,0.0,1);
        doZStackGeneration(getZStackParams());
    }//GEN-LAST:event_clearZButtonActionPerformed

    public void setPlateProperties(PlateProperties pp) {
        pp_ = pp;
    }

    public void setParent(Object o) {
        parent_ = (HCAFLIMPluginFrame) o;
    }
    
    public double[] getZStackParams() {
        if (zAsOffset_)
            return zStackParams;
        else {
            try{
                double z1 = xyzmi_.getZAbsolute();
                double[] zs = {z1, z1, z1};
                for (int ind = 1; ind < 3; ind++){
                    zs[ind] = zs[ind] + zStackParams[ind];
                }
                return zs;
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        return zStackParams;
    }
    
    public ArrayList<FOV> getFOVTable(){
        return tableModel_.getData();
    }

    public void setZStackParams(double start, double end, double step) {
        this.zStackParams[0] = start;
        this.zStackParams[1] = end;
        this.zStackParams[2] = step;
    }
    
    public XYZMotionInterface getXYZMotionInterface() {
        return xyzmi_;
    }

    public void setXYZMotionInterface(XYZMotionInterface xyzmi_) {
        this.xyzmi_ = xyzmi_;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox FOVPatternCombo;
    private javax.swing.JFormattedTextField FOVToFindField;
    private javax.swing.JToggleButton advancedPFButton;
    private javax.swing.JFormattedTextField attemptsField;
    private javax.swing.JPanel autoFOVPanel;
    private javax.swing.JCheckBox autoGenerateFOVsCheck;
    private javax.swing.JButton clearXYZButton;
    private javax.swing.JButton clearZButton;
    private javax.swing.JPanel fovTablePanel;
    private javax.swing.JButton genZStackButton;
    private javax.swing.JTextField groupDescField;
    private javax.swing.JFormattedTextField intensityThresoldField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JFormattedTextField noFOVsField;
    private javax.swing.JPanel plateMapBasePanel;
    private javax.swing.JPanel prefindPanel;
    private javax.swing.JButton quickPFButton;
    private javax.swing.JFormattedTextField ringRadiusField;
    private javax.swing.JButton storeXYZButton;
    private javax.swing.JPanel storedXYZPanel;
    private javax.swing.JComboBox zModeCombo;
    // End of variables declaration//GEN-END:variables
}
