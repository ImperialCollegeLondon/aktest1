/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.dougkelly88.FLIMPlateReaderGUI.GeneralGUIComponents;

import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.sequencingThread;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.Acquisition;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.Arduino;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.DisplayImage2;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.PlateProperties;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.SeqAcqProps;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.VariableTest;
import com.github.dougkelly88.FLIMPlateReaderGUI.GeneralClasses.snapFlimImageThread;
import com.github.dougkelly88.FLIMPlateReaderGUI.InstrumentInterfaceClasses.XYZMotionInterface;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.AcqOrderTableModel;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.Comparators.FComparator;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.Comparators.SeqAcqSetupChainedComparator;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.Comparators.TComparator;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.Comparators.WellComparator;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.Comparators.XComparator;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.Comparators.YComparator;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.Comparators.ZComparator;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.FOV;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.FOVTableModel;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.FilterSetup;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.SeqAcqSetup;
import com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.Classes.TimePoint;
import com.google.common.eventbus.Subscribe;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import mmcorej.CMMCore;
import org.micromanager.MMStudio;
import org.micromanager.api.events.PropertyChangedEvent;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import mmcorej.DeviceType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


/**
 *
 * @author dk1109
 */
public class HCAFLIMPluginFrame extends javax.swing.JFrame {
    
    public CMMCore core_;
    static HCAFLIMPluginFrame frame_;
    private SeqAcqProps sap_;
    private VariableTest var_;
    public PlateProperties pp_;
    public XYZMotionInterface xyzmi_;
    private AcqOrderTableModel tableModel_;
    private JTable seqOrderTable_;
    public  FOV currentFOV_;
    private DisplayImage2 displayImage2_;
    public static HSSFWorkbook wb = new HSSFWorkbook();
    public static HSSFWorkbook wbLoad = new HSSFWorkbook();
    public Thread sequenceThread;
    public Thread snapFlimImageThread;
    public ProgressBar progressBar_;
    public Arduino arduino_;
    public boolean terminate=false;
    public int singleImage;

   
//    public static HSSFWorkbook wb = new HSSFWorkbook();

    @Subscribe
    public void onPropertyChanged(PropertyChangedEvent event) {
//        statusTextArea.setText("google eventbus triggered in device " + event.getDevice() + "\n with property " + event.getProperty() + "\n changed to value " + event.getValue());
    }

    /**
     * Creates new form HCAFLIMPluginFrame
     */
    public HCAFLIMPluginFrame(CMMCore core) {
        initComponents();
        ImageIcon icon = new ImageIcon(this.getClass().getResource("../Resources/GFPFishIcon.png"));
        this.setIconImage(icon.getImage());
        this.setTitle("OpenFLIM-HCA Plugin");
        core_ = core;
        frame_ = this;
        xYZPanel1.setParent(this);
        xYSequencing1.setParent(this);
        lightPathControls1.setParent(this);
        MMStudio gui_ = MMStudio.getInstance();
        gui_.registerForEvents(this);
        

        // Add confirm dialog when window closed using x
        frame_.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame_.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                confirmQuit();
            }
        });

        sap_ = SeqAcqProps.getInstance();
        pp_ = new PlateProperties();
        currentFOV_ = new FOV("C4", pp_, 1000);

        var_ = VariableTest.getInstance();
        currentBasePathField.setText(var_.basepath);
        

        loadDefaultPlateConfig();
        lightPathControls1.setLoadedHardwareValues();
        setupSequencingTable();
        xYZPanel1.setupAFParams(this);
        fLIMPanel1.setDelayComboBox();
        
        displayImage2_ = DisplayImage2.getInstance();
        
        try{
            String cam = core_.getCameraDevice();
            if ("HamamatsuHam_DCAM".equals(cam)){
                core_.setProperty(cam, "Binning", "2x2");
                core_.setProperty(cam, "TRIGGER SOURCE", "SOFTWARE");
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
        
        progressBar_ = new ProgressBar();
        progressBarPanel.setLayout(new BorderLayout());
        progressBarPanel.add(progressBar_, BorderLayout.SOUTH);
        
        arduino_ = Arduino.getInstance();
        arduino_.initializeArduino();
        
        core_.setAutoShutter(false);
    }

    public CMMCore getCore() {
        return core_;
    }
    
    private void setupSequencingTable(){
        
        String[] possibles = {"XYZ", "Filter change", "Time course", "Bright field"};
//        String[] possibles = {"XY", "Z", "Filter change", "Time course", "Bright field"};
        
        tableModel_ = new AcqOrderTableModel();
        tableModel_.addRow("XYZ");
        tableModel_.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {

            }
        });
        seqOrderTable_ = new JTable();
        seqOrderTable_.setModel(tableModel_);
        seqOrderTable_.setSurrendersFocusOnKeystroke(true);
        seqOrderTable_.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JScrollPane scroller = new javax.swing.JScrollPane(seqOrderTable_);
        seqOrderTable_.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 80));
        seqOrderBasePanel.setLayout(new BorderLayout());
        seqOrderBasePanel.add(scroller, BorderLayout.CENTER);
        
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Delete step");
        deleteItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int r = seqOrderTable_.getSelectedRow();
                tableModel_.removeRow(r);
            }
        });
//        JMenuItem addItem = new JMenuItem("Add step");
//        addItem.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                int r = seqOrderTable_.getSelectedRow();
//                tableModel_.insertRow(r+1, "XY");
//            }
//        });    
        
        setupAddStepMenu(popupMenu, possibles);
        
//        popupMenu.add(addItem);
        popupMenu.add(deleteItem);
        seqOrderTable_.addMouseListener(new MouseAdapter() {
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
        
        JComboBox stepCombo = new JComboBox();
        populateCombo(stepCombo, possibles);
        
    
    }
    
    private void setupAddStepMenu(JPopupMenu menu, String[] possibles){
    
        for (final String str : possibles){
            JMenuItem addItem = new JMenuItem("Add step: " + str);
            addItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int r = seqOrderTable_.getSelectedRow();
                    tableModel_.insertRow(r+1, str);
                }
            }); 
        
        menu.add(addItem);
        }
        
    }
    
    private void populateCombo(JComboBox stepCombo, String[] possibles){
        for (String str : possibles){
            stepCombo.addItem(str);
        }
        
        seqOrderTable_.getColumnModel().getColumn(tableModel_.DESC_INDEX).setCellEditor(new DefaultCellEditor(stepCombo));
            stepCombo.addItemListener(new ItemListener(){

                @Override
                public void itemStateChanged(ItemEvent event) {
                    
                    if (event.getStateChange() == ItemEvent.SELECTED){
                        Object item = event.getItem();
                        int r = seqOrderTable_.getSelectedRow();
                        tableModel_.setValueAt(item, r, tableModel_.DESC_INDEX);
                    }
                }
            });
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameScrollPane = new javax.swing.JScrollPane();
        basePanel = new javax.swing.JPanel();
        FLIMPanel = new javax.swing.JTabbedPane();
        lightPathControls1 = new com.github.dougkelly88.FLIMPlateReaderGUI.LightPathClasses.GUIComponents.LightPathPanel();
        xYZPanel1 = new com.github.dougkelly88.FLIMPlateReaderGUI.XYZClasses.GUIComponents.XYZPanel();
        fLIMPanel1 = new com.github.dougkelly88.FLIMPlateReaderGUI.FLIMClasses.GUIComponents.FLIMPanel();
        proSettingsGUI1 = new ProSettingsGUI.ProSettingsPanel();
        statusLabel = new javax.swing.JLabel();
        HCAsequenceProgressBar = new javax.swing.JPanel();
        snapFLIMButton = new javax.swing.JButton();
        startSequenceButton = new javax.swing.JButton();
        snapBFButton = new javax.swing.JButton();
        currentBasePathField = new javax.swing.JFormattedTextField();
        jLabel1 = new javax.swing.JLabel();
        seqOrderBasePanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        accFramesField = new javax.swing.JFormattedTextField();
        progressBarPanel = new javax.swing.JPanel();
        stopSequenceButton = new javax.swing.JToggleButton();
        experimentNameField = new javax.swing.JTextField();
        experimentNameText = new javax.swing.JLabel();
        sequenceSetupBasePanel = new javax.swing.JPanel();
        sequenceSetupTabbedPane = new javax.swing.JTabbedPane();
        xYSequencing1 = new com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.GUIComponents.XYSequencing();
        spectralSequencing1 = new com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.GUIComponents.SpectralSequencing();
        timeCourseSequencing1 = new com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.GUIComponents.TimeCourseSequencing();
        jMenuBar2 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        loadPlateConfigMenu = new javax.swing.JMenuItem();
        loadPlateMetadataMenu = new javax.swing.JMenuItem();
        setBaseFolderMenu = new javax.swing.JMenuItem();
        saveMetadataMenu = new javax.swing.JMenuItem();
        loadSoftwareConfig = new javax.swing.JMenuItem();
        saveSequencingTablesMenu = new javax.swing.JMenuItem();
        quitMenu = new javax.swing.JMenuItem();
        loadSequencingTablesMenu = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        advancedMenu = new javax.swing.JMenuItem();
        calibrationMenu = new javax.swing.JMenuItem();
        wizardMenu = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        FLIMHCAHelpMenu = new javax.swing.JMenuItem();
        aboutMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        FLIMPanel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                FLIMPanelStateChanged(evt);
            }
        });
        FLIMPanel.addTab("Light path control", lightPathControls1);

        xYZPanel1.setEnabled(false);
        FLIMPanel.addTab("XYZ control", xYZPanel1);
        FLIMPanel.addTab("FLIM control", fLIMPanel1);
        FLIMPanel.addTab("ProSettings", proSettingsGUI1);

        HCAsequenceProgressBar.setBorder(javax.swing.BorderFactory.createTitledBorder("FLIM acquisition"));

        snapFLIMButton.setText("Snap FLIM image");
        snapFLIMButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapFLIMButtonActionPerformed(evt);
            }
        });

        startSequenceButton.setText("Start HCA sequence");
        startSequenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startSequenceButtonActionPerformed(evt);
            }
        });

        snapBFButton.setText("Snap brightfield image");
        snapBFButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapBFButtonActionPerformed(evt);
            }
        });

        currentBasePathField.setEditable(false);
        currentBasePathField.setText("C:/Users/dk1109/FLIMFromJava.ome.tiff");
        currentBasePathField.setToolTipText("Change this value using *** in File menu");
        currentBasePathField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currentBasePathFieldActionPerformed(evt);
            }
        });

        jLabel1.setText("Base folder: ");

        javax.swing.GroupLayout seqOrderBasePanelLayout = new javax.swing.GroupLayout(seqOrderBasePanel);
        seqOrderBasePanel.setLayout(seqOrderBasePanelLayout);
        seqOrderBasePanelLayout.setHorizontalGroup(
            seqOrderBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 192, Short.MAX_VALUE)
        );
        seqOrderBasePanelLayout.setVerticalGroup(
            seqOrderBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 83, Short.MAX_VALUE)
        );

        jLabel2.setText("Accumulate frames: ");

        accFramesField.setText("1");
        accFramesField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accFramesFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout progressBarPanelLayout = new javax.swing.GroupLayout(progressBarPanel);
        progressBarPanel.setLayout(progressBarPanelLayout);
        progressBarPanelLayout.setHorizontalGroup(
            progressBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        progressBarPanelLayout.setVerticalGroup(
            progressBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 64, Short.MAX_VALUE)
        );

        stopSequenceButton.setText("Stop HCA sequence");
        stopSequenceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopSequenceButtonActionPerformed(evt);
            }
        });

        experimentNameField.setText("TestExperiment");
        experimentNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                experimentNameFieldActionPerformed(evt);
            }
        });

        experimentNameText.setText("Name experiment");

        javax.swing.GroupLayout HCAsequenceProgressBarLayout = new javax.swing.GroupLayout(HCAsequenceProgressBar);
        HCAsequenceProgressBar.setLayout(HCAsequenceProgressBarLayout);
        HCAsequenceProgressBarLayout.setHorizontalGroup(
            HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HCAsequenceProgressBarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressBarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, HCAsequenceProgressBarLayout.createSequentialGroup()
                        .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, HCAsequenceProgressBarLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(currentBasePathField, javax.swing.GroupLayout.PREFERRED_SIZE, 542, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, HCAsequenceProgressBarLayout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(seqOrderBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(15, 15, 15)
                                .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(snapFLIMButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(snapBFButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(startSequenceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(HCAsequenceProgressBarLayout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(stopSequenceButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(HCAsequenceProgressBarLayout.createSequentialGroup()
                                        .addGap(7, 7, 7)
                                        .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(experimentNameText)
                                            .addComponent(experimentNameField)
                                            .addGroup(HCAsequenceProgressBarLayout.createSequentialGroup()
                                                .addComponent(jLabel2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(accFramesField, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                        .addGap(0, 27, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        HCAsequenceProgressBarLayout.setVerticalGroup(
            HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, HCAsequenceProgressBarLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(experimentNameText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(HCAsequenceProgressBarLayout.createSequentialGroup()
                        .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(snapFLIMButton)
                            .addComponent(experimentNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(snapBFButton)
                            .addComponent(jLabel2)
                            .addComponent(accFramesField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(startSequenceButton)
                            .addComponent(stopSequenceButton)))
                    .addComponent(seqOrderBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(HCAsequenceProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(currentBasePathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(111, 111, 111))
        );

        sequenceSetupBasePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Setup HCA sequenced acquisition"));

        sequenceSetupTabbedPane.addTab("XYZ positions", xYSequencing1);
        sequenceSetupTabbedPane.addTab("Filter sets", spectralSequencing1);
        sequenceSetupTabbedPane.addTab("Time course", timeCourseSequencing1);

        javax.swing.GroupLayout sequenceSetupBasePanelLayout = new javax.swing.GroupLayout(sequenceSetupBasePanel);
        sequenceSetupBasePanel.setLayout(sequenceSetupBasePanelLayout);
        sequenceSetupBasePanelLayout.setHorizontalGroup(
            sequenceSetupBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequenceSetupBasePanelLayout.createSequentialGroup()
                .addComponent(sequenceSetupTabbedPane)
                .addContainerGap())
        );
        sequenceSetupBasePanelLayout.setVerticalGroup(
            sequenceSetupBasePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sequenceSetupBasePanelLayout.createSequentialGroup()
                .addComponent(sequenceSetupTabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout basePanelLayout = new javax.swing.GroupLayout(basePanel);
        basePanel.setLayout(basePanelLayout);
        basePanelLayout.setHorizontalGroup(
            basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelLayout.createSequentialGroup()
                .addComponent(FLIMPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 622, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(statusLabel))
                    .addComponent(HCAsequenceProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sequenceSetupBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(611, Short.MAX_VALUE))
        );
        basePanelLayout.setVerticalGroup(
            basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(basePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addComponent(FLIMPanel)
                        .addContainerGap())
                    .addGroup(basePanelLayout.createSequentialGroup()
                        .addComponent(sequenceSetupBasePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(HCAsequenceProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusLabel)
                        .addGap(74, 74, 74))))
        );

        frameScrollPane.setViewportView(basePanel);

        fileMenu.setText("File");

        loadPlateConfigMenu.setText("Load plate properties...");
        loadPlateConfigMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadPlateConfigMenuActionPerformed(evt);
            }
        });
        fileMenu.add(loadPlateConfigMenu);

        loadPlateMetadataMenu.setText("Load plate metadata...");
        loadPlateMetadataMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadPlateMetadataMenuActionPerformed(evt);
            }
        });
        fileMenu.add(loadPlateMetadataMenu);

        setBaseFolderMenu.setText("Set Base Folder");
        setBaseFolderMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setBaseFolderMenuActionPerformed(evt);
            }
        });
        fileMenu.add(setBaseFolderMenu);

        saveMetadataMenu.setText("Save Metadata");
        saveMetadataMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMetadataMenuActionPerformed(evt);
            }
        });
        fileMenu.add(saveMetadataMenu);

        loadSoftwareConfig.setText("Load Software Config...");
        loadSoftwareConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSoftwareConfigActionPerformed(evt);
            }
        });
        fileMenu.add(loadSoftwareConfig);

        saveSequencingTablesMenu.setText("Save sequencing tables");
        saveSequencingTablesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSequencingTablesMenuActionPerformed(evt);
            }
        });
        fileMenu.add(saveSequencingTablesMenu);

        quitMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        quitMenu.setText("Quit");
        quitMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuActionPerformed(evt);
            }
        });
        fileMenu.add(quitMenu);

        loadSequencingTablesMenu.setText("Load sequencing tables");
        loadSequencingTablesMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSequencingTablesMenuActionPerformed(evt);
            }
        });
        fileMenu.add(loadSequencingTablesMenu);

        jMenuBar2.add(fileMenu);

        toolsMenu.setText("Tools");

        advancedMenu.setText("Advanced options...");
        toolsMenu.add(advancedMenu);

        calibrationMenu.setText("Calibration...");
        toolsMenu.add(calibrationMenu);

        wizardMenu.setText("FLIMWizard...");
        toolsMenu.add(wizardMenu);

        jMenuBar2.add(toolsMenu);

        helpMenu.setText("Help");

        FLIMHCAHelpMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        FLIMHCAHelpMenu.setText("Help");
        FLIMHCAHelpMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FLIMHCAHelpMenuActionPerformed(evt);
            }
        });
        helpMenu.add(FLIMHCAHelpMenu);

        aboutMenu.setText("About");
        aboutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenu);

        jMenuBar2.add(helpMenu);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addComponent(frameScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1306, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(frameScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1017, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    public static HCAFLIMPluginFrame getInstance() {
      return frame_;
  }
  
    private void testSorting(List<SeqAcqSetup> sass){
        System.out.println("Before sorting: ");
            for (SeqAcqSetup sas : sass){
                System.out.println(sas.toString());
            }
            
            Collections.sort(sass, new SeqAcqSetupChainedComparator(
                    new XComparator(), 
                    new YComparator(), 
                    new ZComparator(), 
                    new FComparator(), 
                    new TComparator()));
            
            System.out.println("After sorting XYZFT: ");
            for (SeqAcqSetup sas : sass){
                System.out.println(sas.toString());
            }
            
            Collections.sort(sass, new SeqAcqSetupChainedComparator(
                    new FComparator(), 
                    new TComparator(),
                    new XComparator(), 
                    new YComparator(), 
                    new ZComparator()));
            System.out.println("After sorting FTXYZ: ");
            for (SeqAcqSetup sas : sass){
                System.out.println(sas.toString());
            }
            
            Collections.sort(sass, new SeqAcqSetupChainedComparator(
                    new TComparator(),
                    new XComparator(), 
                    new YComparator(), 
                    new ZComparator(), 
                    new FComparator()));
            System.out.println("After sorting TXYZF: ");
            for (SeqAcqSetup sas : sass){
                System.out.println(sas.toString());
            }
    }
    
    private void aboutMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuActionPerformed
        Splash s = new Splash();
        s.setVisible(true);
        s.setAlwaysOnTop(rootPaneCheckingEnabled);
    }//GEN-LAST:event_aboutMenuActionPerformed

    private void quitMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuActionPerformed
        confirmQuit();
    }//GEN-LAST:event_quitMenuActionPerformed

    private void FLIMHCAHelpMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FLIMHCAHelpMenuActionPerformed
        try {
            Desktop.getDesktop().browse(new URL("https://github.com/dougkelly88/uManagerPlugins/wiki/0-Home").toURI());
        } catch (Exception e) {
            System.out.println("Problem displaying the help wiki: " + e.getMessage());
//            statusTextArea.setText("Problem displaying the help wiki: " + e.getMessage());
        }
    }//GEN-LAST:event_FLIMHCAHelpMenuActionPerformed

    private void setBaseFolderMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setBaseFolderMenuActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select target directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        Component parentFrame = null;
        int returnVal = chooser.showOpenDialog(parentFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            var_.basepath = chooser.getSelectedFile().getPath();
        }

//        statusTextArea.setText("Selected base path: " + var_.basepath);
        currentBasePathField.setText(var_.basepath);
    }//GEN-LAST:event_setBaseFolderMenuActionPerformed

    private void saveMetadataMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMetadataMenuActionPerformed
        var_.saveMetadata();
    }//GEN-LAST:event_saveMetadataMenuActionPerformed

    private void loadPlateConfigMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadPlateConfigMenuActionPerformed
        final JFileChooser fc = new JFileChooser("mmplugins/OpenHCAFLIM/XPLT");   // for debug, make more general
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            try {
                pp_ = pp_.loadProperties(file);
                xYZPanel1.onPlateConfigLoaded(true, pp_);
//                xYSequencing1.onPlateConfigLoaded(true, pp_);
                xYSequencing1.setPlateProperties(pp_);

            } catch (Exception e) {
                System.out.println("problem accessing file" + file.getAbsolutePath());
                System.out.println("Problem accessing plate config at " + file.getAbsolutePath()
                        + " resulting in error: " + e.getMessage());
            }
        } else {
            System.out.println("File access cancelled by user.");
        }

    }//GEN-LAST:event_loadPlateConfigMenuActionPerformed

    private void loadDefaultPlateConfig() {

//        String fp = new File("").getAbsolutePath();
        File file = new File("mmplugins/OpenHCAFLIM/XPLT/Greiner uClear.xplt"); // relative path now
        try {
            pp_ = pp_.loadProperties(file);
            xYZPanel1.onPlateConfigLoaded(true, pp_);
            xYSequencing1.onPlateConfigLoaded(true, pp_);
            xyzmi_ = new XYZMotionInterface(this);
            xYSequencing1.setXYZMotionInterface(xyzmi_);
            xYZPanel1.setXYZMotionInterface(xyzmi_);
            
        } catch (Exception e) {
            System.out.println("problem accessing file" + file.getAbsolutePath());
            System.out.println("Problem accessing plate config at " + file.getAbsolutePath()
                    + " resulting in error: " + e.getMessage());
        }
    }

    private void loadSoftwareConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSoftwareConfigActionPerformed
        // Load ConfigSoftware in testText and set loaded values in all panels
        // load ConfigSoftware in testText
        FileReader allConfig = null;
        try {
            allConfig = new FileReader(var_.basepath + "\\ConfigSoftware.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SaveData.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
//            System.out.println.read(allConfig, null);
            // TODO: HAS REMOVING STATUS TEXT AREA BROKEN THIS?!
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // set loaded values in all panels
        lightPathControls1.setLoadedSoftwareValues();
        fLIMPanel1.setLoadedSoftwareValues();
    }//GEN-LAST:event_loadSoftwareConfigActionPerformed

    private void loadPlateMetadataMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadPlateMetadataMenuActionPerformed
       // http://howtodoinjava.com/2013/05/27/parse-csv-files-in-java/
    }//GEN-LAST:event_loadPlateMetadataMenuActionPerformed

    private void currentBasePathFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currentBasePathFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_currentBasePathFieldActionPerformed

   
    private void startSequenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startSequenceButtonActionPerformed
        // starts sequence in new thread
            sequenceThread =new Thread(new sequencingThread(this));
            sequenceThread.start();
        // set progress bar to 0
            progressBar_.setStart("FLIM sequence");
       
        
    }//GEN-LAST:event_startSequenceButtonActionPerformed

    public void doSequenceAcquisition() throws InterruptedException{
        Acquisition acq = new Acquisition();
        ArrayList<FOV> fovs = new ArrayList<FOV>();
        ArrayList<TimePoint> tps = new ArrayList<TimePoint>();
        ArrayList<FilterSetup> fss = new ArrayList<FilterSetup>();
        int endOk=0;
        int ind=0;
        singleImage=0;
        
            // get all sequence parameters and put them together into an 
            // array list of objects containing all acquisition points...
            // Note that if a term is absent from the sequence setup, current
            // values should be used instead...
            
            List<SeqAcqSetup> sass = new ArrayList<SeqAcqSetup>();
            ArrayList<String> order = tableModel_.getData();
            if (!order.contains("XYZ")){
                fovs.add(xyzmi_.getCurrentFOV());
            } else {
                fovs = xYSequencing1.getFOVTable();
            }
            
            if (!order.contains("Time course")){
                tps.add(new TimePoint(0.0));
            } else {
                tps = timeCourseSequencing1.getTimeTable();
            }
            
            if (!order.contains("Filter change")){
                int intTime = 100;
                try {
                    intTime = (int) core_.getExposure();
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
               fss.add(new FilterSetup(lightPathControls1, intTime, fLIMPanel1));
            } else {
                fss = spectralSequencing1.getFilterTable();
            } 
            
            List<Comparator<SeqAcqSetup>> comparators = new ArrayList<Comparator<SeqAcqSetup>>();
            
            for (FOV fov : fovs){
                for (TimePoint tp : tps){
                    for (FilterSetup fs : fss){
                        sass.add(new SeqAcqSetup(fov, tp, fs));
                    }
                }
            }
            
            // use chained comparators to sort by multiple fields SIMULTANEOUSLY,
            // based on order determined in UI table.
            for (String str : order){
                if (str.equals("XYZ")){
                        comparators.add(new WellComparator());
                        comparators.add(new ZComparator());
                }
                else if (str.equals("Filter change"))
                    comparators.add(new FComparator());
                else if (str.equals("Time course"))
                    comparators.add(new TComparator());
            }
            Collections.sort(sass, new SeqAcqSetupChainedComparator(comparators));
            int sassSize=sass.size();
            
            
            long start_time = System.currentTimeMillis();
            // TODO: modify data saving such that time courses, z can be put in a 
            // single OME.TIFF. DISCUSS WITH IAN!
            // N.B. z should be relatively easy...
            // for now, just make a base folder and name files based on 
            // filterlabel, time point.  
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String baseLevelPath = currentBasePathField.getText() + "/Sequenced FLIM acquisition " +
                    timeStamp;
            for (FilterSetup fs : fss){
                String flabel = fs.getLabel();

                File f = new File(baseLevelPath + "/" + flabel);
                try {
                    boolean check1 = f.mkdirs();
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
            
//            for (SeqAcqSetup sas : sass){
            Double lastTime = 0.0;
            String lastFiltLabel = "";
            FOV lastFOV = new FOV(0, 0, 0, pp_);
            Double lastZ = 0.0;
//            int fovSinceLastAF = 0;
            for ( ind = 0; ind < sass.size(); ind++){
            
                //check for flag (stop button) and abort sequence
                if(terminate){
                    endOk=1;
                break;
                }

                
                // TODO: how much can these steps be parallelised?
                // set FOV params
                SeqAcqSetup sas = sass.get(ind);;
                // if time point changed different from last time, wait until 
                // next time point reached...
                if ((!sas.getTimePoint().getTimeCell().equals(lastTime)) & (order.contains("Time course"))){
                    Double next_time = sas.getTimePoint().getTimeCell() * 1000;
                    while ((System.currentTimeMillis() - start_time) < next_time){
                        Double timeLeft = next_time - (System.currentTimeMillis() - start_time);
                        System.out.println("Waiting for " + timeLeft + " until next time point...");
                        //check for flag (stop button) and abort time course wait
                        if(terminate){
                            endOk=1;
                            break;    
                        }
                    }
                }
                // if FOV different, deal with it here...
                if ( ( (!sas.getFOV().equals(lastFOV)) | (sas.getFOV().getZ() != lastZ) ) & (order.contains("XYZ")) ){
                    // TODO: this needs tweaking in order that autofocus works properly with Z stacks...
                    // Perhaps only do when XY change, and not Z?
                    xyzmi_.gotoFOV(sas.getFOV());
                    if (xYZPanel1.getAFInSequence())
                        xyzmi_.customAutofocus(xYZPanel1.getSampleAFOffset());
                    if(terminate){
                        endOk=1;
                        break;
                    }
                }
                
                // set filter params - can these be handled by a single class?
                if ( (!sas.getFilters().getLabel().equals(lastFiltLabel)) & order.contains("Filter change") ){
                    try {
                        String s = core_.getShutterDevice();
                        if (!"".equals(s))
                            core_.setShutterOpen(false);
                        s = sas.getFilters().getExFilt();
                        if (!"".equals(s))
                            core_.setProperty("SpectralFW", "Label", s);
                        s = sas.getFilters().getCube();
                        if (!"".equals(s))
                            core_.setProperty("FilterCube", "Label", s);
                        s = sas.getFilters().getEmFilt();
                        if (!"".equals(s))
                            core_.setProperty("CSUX-Filter Wheel", "Label", s);
                        s = sas.getFilters().getNDFilt();
                        if (!"".equals(s))
                            core_.setProperty("NDFW", "Label", s);
                        core_.setExposure(sas.getFilters().getIntTime());
                        s = sas.getFilters().getDiFilt();
                        if (!"".equals(s))
                            core_.setProperty("CSUX-Dichroic Mirror", "Label", s);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                //Get laser intensity
                String intensity=Double.toString(arduino_.getLaserIntensity());
                // do acquisition
                String fovLabel = String.format("%05d", ind);
                String path;
                if(sas.getFilters().getLabel().equals("Unknown")){
                    path=baseLevelPath + "/"+ 
                        " Well=" + sas.getFOV().getWell() +                        
                        " X=" + sas.getFOV().getX() +
                        " Y=" + sas.getFOV().getY() +
                        "T=" + sas.getTimePoint().getTimeCell() + 
                        " Filterset=" + sas.getFilters().getLabel() + 
                        " Z=" + sas.getFOV().getZ() +
                        " ID=" + fovLabel+
                        " Laser intensity=" + intensity;
                } else{
                    path=baseLevelPath + "/" + sas.getFilters().getLabel() + "/"+ 
                        " Well=" + sas.getFOV().getWell() +                        
                        " X=" + sas.getFOV().getX() +
                        " Y=" + sas.getFOV().getY() +
                        "T=" + sas.getTimePoint().getTimeCell() + 
                        " Filterset=" + sas.getFilters().getLabel() + 
                        " Z=" + sas.getFOV().getZ() +
                        " ID=" + fovLabel+
                        " Laser intensity=" + intensity;
                }
                try{
                    boolean abort=arduino_.checkSafety();
                    if(abort==true){
                        break;
                    }
                    core_.waitForDeviceType(DeviceType.XYStageDevice);
                    core_.waitForDeviceType(DeviceType.AutoFocusDevice);
                    arduino_.setDigitalOutHigh();
                    wait(var_.shutterResponse);
                    
                }
                catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                
                acq.snapFLIMImage(path, sas.getFilters().getDelays(), sas);
            //    saveSequencingTablesForDebugging(path);
                
                // shutter laser
                // TODO: have this work properly in line with auto-shutter?
                try {
                    arduino_.setDigitalOutLow();
                    lightPathControls1.setLaserToggleFalse();
                    lightPathControls1.setLaserToggleText("Turn laser ON");
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                
                lastTime = sas.getTimePoint().getTimeCell();
                lastFOV = sas.getFOV();
                lastZ = sas.getFOV().getZ();
                lastFiltLabel = sas.getFilters().getLabel();
                
                
            
            // RESET DELAY TO BE CONSISTENT WITH UI
            try{
                core_.setProperty("Delay box", "Delay (ps)", fLIMPanel1.getCurrentDelay());
            } catch (Exception  e){
                System.out.println(e.getMessage());
            }
            //set progress bar one increment further
    //99        displayImage2_.showImageInIJ();
            progressBar_.stepIncrement(ind, sass.size());
            endOk=0;
            
        }
            
        // Set progressbar to 100% if not aborted before
        if(endOk==1){
                setStopButtonFalse(ind, sass.size(), "FLIM sequence");
            } else {
            progressBar_.setEnd("FLIM sequence");
        }
        
        // Send Email after finishing acquisition!
     /*   if(xYSequencing1.sendEmailBoolean){
            xYSequencing1.sendEmail();
        }*/
            
    }

    public void setStopButtonFalse(int step, int end, String name) throws InterruptedException{
        progressBar_.stepIncrement(step, end);
        progressBar_.setTitel(name+ " stoped at...");
        progressBar_.setColor(2);
        terminate=false;
        stopSequenceButton.setSelected(false);
    }
    
    private void snapFLIMButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapFLIMButtonActionPerformed
        // is button pressed?
        singleImage=1;
        // open new Thread for snap Image
        progressBar_.setStart("Snap FLIM image");
        snapFlimImageThread =new Thread(new snapFlimImageThread(this));
        snapFlimImageThread.start();
    }//GEN-LAST:event_snapFLIMButtonActionPerformed

    public void snapFLIMImageButton(){
        boolean jump=false;
        try{
            boolean abort=arduino_.checkSafety();;
            if(abort==true){
                jump=true;
            } else{
                arduino_.setDigitalOutHigh();
                wait(var_.shutterResponse);
            }
            core_.waitForDeviceType(DeviceType.XYStageDevice);
            core_.waitForDeviceType(DeviceType.AutoFocusDevice);
        } catch (Exception ex) {
            Logger.getLogger(HCAFLIMPluginFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jump==false){
            Acquisition acq = new Acquisition();
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            String intensity=Double.toString(arduino_.getLaserIntensity());
            String fullname = (currentBasePathField.getText()+ "/" + timeStamp + " Laser intensity=" + intensity + "_FLIMSnap.ome.tiff");
            //        acq.dummyTest();
            //        acq.doacqModulo();
            int exp = 100;
            try {
                exp = (int) core_.getExposure();
            } catch (Exception e){
                System.out.println(e.getMessage());
            }
            // so that same functions can be used, generate dummy SequencedAcquisitionSetup
            acq.snapFLIMImage(fullname, fLIMPanel1.getDelays(), 
                    new SeqAcqSetup(currentFOV_, new TimePoint(0.0,0.0,false), new FilterSetup(lightPathControls1, exp, fLIMPanel1)));
            progressBar_.setEnd("Snap FLIM image");
        }
        arduino_.setDigitalOutLow();
        lightPathControls1.setLaserToggleFalse();
        lightPathControls1.setLaserToggleText("Turn laser ON");
    }
    
    private void snapBFButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapBFButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_snapBFButtonActionPerformed

    private void saveSequencingTablesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSequencingTablesMenuActionPerformed
        saveSequencingTablesFunction();

    }//GEN-LAST:event_saveSequencingTablesMenuActionPerformed

    private void loadSequencingTablesMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSequencingTablesMenuActionPerformed
        try {
            loadSequencingTablesFunction();// TODO add your handling code here:
        } catch (IOException ex) {
            Logger.getLogger(HCAFLIMPluginFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_loadSequencingTablesMenuActionPerformed

    private void accFramesFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accFramesFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_accFramesFieldActionPerformed

    private void stopSequenceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopSequenceButtonActionPerformed
        terminate=stopSequenceButton.isSelected();
    }//GEN-LAST:event_stopSequenceButtonActionPerformed

    private void experimentNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_experimentNameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_experimentNameFieldActionPerformed

    private void FLIMPanelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_FLIMPanelStateChanged
        int tab=FLIMPanel.getSelectedIndex();
        if (tab==0){
            lightPathControls1.updatePanel();
        }else if (tab==1){
            xYZPanel1.updatePanel();
        }else if (tab==2){
            fLIMPanel1.updatePanel();
        }else if (tab==3){
            proSettingsGUI1.updatePanel();
        }
    }//GEN-LAST:event_FLIMPanelStateChanged
   
    public void changeAbortHCAsequencBoolean(){
    //abortHCAsequencBoolean=abortHCAsequencesButton.isSelected();
    }
    
    public void loadSequencingTablesFunction() throws IOException{
        
            FileInputStream fileInputStream1 = new FileInputStream(var_.basepath + "\\OpenHCAFLIM_Sequenzing.xls");
            wbLoad = new HSSFWorkbook(fileInputStream1);
            xYSequencing1.tableModel_.loadFOVTableModelfromSpreadsheet();
            spectralSequencing1.tableModel_.loadFilterTableModelfromSpreadsheet();
            timeCourseSequencing1.tableModel_.loadTimeCourseTableModelfromSpreadsheet();
            fileInputStream1.close();
       
    }
    
    public void saveSequencingTablesFunction(){
    // write sheets to .xls
        wb = new HSSFWorkbook();
        xYSequencing1.tableModel_.saveFOVTableModelAsSpreadsheet();
        spectralSequencing1.tableModel_.saveFilterTableModelAsSpreadsheet();
        timeCourseSequencing1.tableModel_.saveTimeCourseTableModelAsSpreadsheet();
    
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(var_.basepath + "\\OpenHCAFLIM_Sequenzing.xls");
            wb.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FOVTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FOVTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }}
    
    public void saveSequencingTablesForDebugging(String path){
            // write sheets to .xls
        wb = new HSSFWorkbook();
        xYSequencing1.tableModel_.saveFOVTableModelAsSpreadsheet();
        spectralSequencing1.tableModel_.saveFilterTableModelAsSpreadsheet();
        timeCourseSequencing1.tableModel_.saveTimeCourseTableModelAsSpreadsheet();
    
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(path+".xls");
            wb.write(fileOut);
            fileOut.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FOVTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FOVTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void calibrationMenuActionPerformed(java.awt.event.ActionEvent evt) {                                                
        final JFileChooser fc = new JFileChooser("mmplugins/OpenHCAFLIM/KentechCalibration/CalibrationWithoutBias.csv");   // for debug, make more general
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String file = fc.getSelectedFile().getAbsolutePath();
            try {
                core_.setProperty("Delay box", "CalibrationPath", file);
            } catch (Exception e) {
                System.out.println("problem accessing file" + file);
                System.out.println("Problem accessing plate config at " + file
                        + " resulting in error: " + e.getMessage());
            }
        } else {
            System.out.println("File access cancelled by user.");
        }
    }                                               
     
    public int getAccFrames(){
        return Integer.parseInt(accFramesField.getText());
    }
    
    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) 
//    {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(HCAFLIMPluginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(HCAFLIMPluginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(HCAFLIMPluginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(HCAFLIMPluginFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem FLIMHCAHelpMenu;
    private javax.swing.JTabbedPane FLIMPanel;
    private javax.swing.JPanel HCAsequenceProgressBar;
    private javax.swing.JMenuItem aboutMenu;
    private javax.swing.JFormattedTextField accFramesField;
    private javax.swing.JMenuItem advancedMenu;
    private javax.swing.JPanel basePanel;
    private javax.swing.JMenuItem calibrationMenu;
    private javax.swing.JFormattedTextField currentBasePathField;
    private javax.swing.JTextField experimentNameField;
    private javax.swing.JLabel experimentNameText;
    private com.github.dougkelly88.FLIMPlateReaderGUI.FLIMClasses.GUIComponents.FLIMPanel fLIMPanel1;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JScrollPane frameScrollPane;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenuBar jMenuBar2;
    private com.github.dougkelly88.FLIMPlateReaderGUI.LightPathClasses.GUIComponents.LightPathPanel lightPathControls1;
    private javax.swing.JMenuItem loadPlateConfigMenu;
    private javax.swing.JMenuItem loadPlateMetadataMenu;
    private javax.swing.JMenuItem loadSequencingTablesMenu;
    private javax.swing.JMenuItem loadSoftwareConfig;
    private ProSettingsGUI.ProSettingsPanel proSettingsGUI1;
    private javax.swing.JPanel progressBarPanel;
    private javax.swing.JMenuItem quitMenu;
    private javax.swing.JMenuItem saveMetadataMenu;
    private javax.swing.JMenuItem saveSequencingTablesMenu;
    private javax.swing.JPanel seqOrderBasePanel;
    private javax.swing.JPanel sequenceSetupBasePanel;
    private javax.swing.JTabbedPane sequenceSetupTabbedPane;
    private javax.swing.JMenuItem setBaseFolderMenu;
    private javax.swing.JButton snapBFButton;
    private javax.swing.JButton snapFLIMButton;
    private com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.GUIComponents.SpectralSequencing spectralSequencing1;
    private javax.swing.JButton startSequenceButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JToggleButton stopSequenceButton;
    private com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.GUIComponents.TimeCourseSequencing timeCourseSequencing1;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JMenuItem wizardMenu;
    private com.github.dougkelly88.FLIMPlateReaderGUI.SequencingClasses.GUIComponents.XYSequencing xYSequencing1;
    private com.github.dougkelly88.FLIMPlateReaderGUI.XYZClasses.GUIComponents.XYZPanel xYZPanel1;
    // End of variables declaration//GEN-END:variables

    private void confirmQuit() {
        int n = JOptionPane.showConfirmDialog(frame_,
                "Quit: are you sure?", "Quit", JOptionPane.YES_NO_OPTION);
        if (n == JOptionPane.YES_OPTION) {
            arduino_.setDigitalOutLow();
            dispose();
        }
    }

    private String test(String dev, String prop) {
        String out;
        try {
            out = core_.getProperty(dev, prop);
        } catch (Exception e) {
            out = "Error:" + e.getMessage();
        }
        return out;
    }
    

}
