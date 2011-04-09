/*
 * MainFrame.java
 *
 * Main GUI for MV-Plan.
 *
 * @author Guy Wittig
 * @version 13-May-2007
 *
 *   This program is part of MV-Plan
 *   Copyright 2005-2007 Guy Wittig
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   The GNU General Public License can be read at http://www.gnu.org/licenses/licenses.html
 */

package mvplan.gui;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.math.BigDecimal;
import java.util.Locale;
import mvplan.gui.components.ModelDisplayComponent;
import mvplan.gui.components.ProgressIndicator;
import mvplan.main.Mvplan;
import mvplan.datamodel.GasModel;
import mvplan.datamodel.DiveSegmentModel;
import mvplan.dive.*;
import mvplan.gas.Gas;
import mvplan.model.AbstractModel;
import mvplan.model.ModelDAO;
import mvplan.prefs.PrefsDAO;
import mvplan.segments.SegmentDive;
import mvplan.segments.SegmentAbstract;
import mvplan.updater.*;
import mvplan.util.*;
import mvplan.gui.text.*;
import mvplan.gui.components.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.print.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.util.MissingResourceException;

public class MainFrame extends JFrame
{       
        // Change this to enable text labels in Dive and Gas panel buttons
        private final static boolean BUTTON_ICONS_ONLY = true;
        
        /** These are the data models for the main GUI
         *  currentProfile and currentTable cannot exist together.
         */        
        Profile currentProfile=null;    // Current Profile (Single Dive) model
        AbstractModel currentModel=null;        // Current tissue model
        TableGeneratorModel currentTable=null;  // Current multi-profile table model.         
        ArrayList knownGases;           // Maintains known gases
        GasModel knownGasModel;         // For Gas table
        ArrayList knownSegments;        // Maintains known Dive Segments
        DiveSegmentModel knownSegmentsModel;    // For Dive Profile Table

        // State fields
        File lastModelFile;             // Saves last file name used in Save command        
        boolean repetitiveMode=false;   // Controls repetitive mode
        boolean updateAvailable=false;  // Saves update available state
        
                
        // Components        
        JPanel mainContentPane;
        JFrame mainFrame;
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JPanel topPanel = new JPanel();
        JPanel topProgressPanel = new JPanel();
        JPanel leftPanel = new JPanel();
        JPanel gasPanel = new JPanel();
        JPanel gasToolPanel = new JPanel();
        JPanel divePanel = new JPanel();
        JPanel diveToolPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        JPanel repetitiveDivePanel = new JPanel();

        GridBagLayout mainContentLayout = new GridBagLayout();
        GridBagLayout rightPanelLayout = new GridBagLayout();
        GridBagLayout gasPanelLayout = new GridBagLayout();
        GridBagLayout divePanelLayout = new GridBagLayout();
        GridBagLayout leftPanelLayout = new GridBagLayout();

        JToolBar toolBar = new JToolBar();
        JButton calcButton = new JButton();
        JButton tableButton = new JButton();
        JButton clearButton = new JButton();
        JButton prefsButton = new JButton();
        JButton loadButton = new JButton();
        JButton saveButton = new JButton();
        JButton exitButton = new JButton();
        JButton printButton = new JButton();
        JButton aboutButton = new JButton();
        JButton gasAddButton = new JButton();
        JButton gasEditButton = new JButton();
        JButton gasDeleteButton = new JButton();
        JButton diveAddButton = new JButton();
        JButton diveDeleteButton = new JButton();
        JButton diveUpButton = new JButton();
        JButton diveDownButton = new JButton();
        
        JCheckBox gasOCDecoCB = new JCheckBox();
        JCheckBox repetitiveModeCB = new JCheckBox();       

        JPopupMenu gasPopupMenu = new JPopupMenu();
        JMenuItem gasAddMenuItem;
        JMenuItem gasEditMenuItem;
        JMenuItem gasDeleteMenuItem;
        JPopupMenu divePopupMenu = new JPopupMenu();
        JMenuItem diveAddMenuItem;
        JMenuItem diveDeleteMenuItem;
        JPopupMenu textPopupMenu = new JPopupMenu();
        JMenuItem textCopyMenuItem;
        
        JTable gasTable;
        JTable diveTable;

        JTextArea textArea;     // Main text output area
        
        JTextField surfaceIntervalField = new JTextField();
        JLabel tissueStatusLabel = new JLabel();
        JLabel repetitiveLabel = new JLabel();
        JLabel updateLabel = new JLabel();
        
        JScrollPane rightScrollPane ;
        JScrollPane leftScrollPane;

        JComboBox gasComboBox;        
        
        ProfilePopup profilePopup; 
        ModelDisplayComponent tissueComponent;
        
        // Test are used for the dive table 
        DoubleCellEditor depthEditor;
        DoubleCellEditor timeEditor;
        DoubleCellEditor spEditor;
        
        //private ImageIcon tissueClearIcon, tissueLoadedIcon;
        private Image okImage, updateImage;     // For update progress indicator        
        private ProgressIndicator progress = new ProgressIndicator(12,15, null); // Was 12,15
        
        private PageFormat pageFormat;        // Remember page setup
        

    public MainFrame()
    {
        mainFrame=(this);
        // Resize to last saved size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();        
        this.setSize(Mvplan.prefs.getFrameSizeX(),Mvplan.prefs.getFrameSizeY());
        this.setLocation((screenSize.width-Mvplan.prefs.getFrameSizeX())/2,(screenSize.height-Mvplan.prefs.getFrameSizeY())/2);
        
        // Create and initialise persistant data structures
        knownGases=new ArrayList();
        knownSegments = new ArrayList();
        knownSegmentsModel = new DiveSegmentModel(knownSegments);
        knownGasModel = new GasModel(knownGases);
        // Copy known gases from Preferences object
        Iterator i=Mvplan.prefs.getPrefGases().iterator();
        while(i.hasNext()) {
            knownGases.add((Gas)i.next());
        }
        // Copy known segments from Preferences object
        i=Mvplan.prefs.getPrefSegments().iterator();
        while(i.hasNext()) {
            knownSegments.add((mvplan.segments.SegmentAbstract)i.next());
        }
        // Create GUI
        guiInit();
     
        // Display DEBUG mode warning
        if(Mvplan.DEBUG>0) {
            if ( Mvplan.preferredLocale != null) 
                textArea.append("Preferred locale: "+ Mvplan.preferredLocale.getDisplayName()+"\n");
            textArea.append("System locale: "+  Locale.getDefault().getDisplayName()+ "\n\n");
            
            textArea.append("*** DEBUG MODE - ");
            
            if(Mvplan.prefs.getUnits()==Mvplan.prefs.METRIC)
                textArea.append("METRIC ***\n\n");
            else
                textArea.append("IMPERIAL ***\n\n");
            
        }
               
        // Verify T&Cs agreed to        
        if( !Mvplan.prefs.getAgreedToTerms()) {
            new ConditionsAcceptance(mainFrame, true);
            // If still not agreed then exit
            if (!Mvplan.prefs.getAgreedToTerms()) {
                dispose();
                System.exit(0);
            }                                                                
        }
        
        // Show main window
        setVisible(true);        
        // Display terms and conditions
        ConditionsDisplay td = new ConditionsDisplay(textArea);   
        
        // Check current version
        doCheckVersion(Mvplan.DEBUG>0);
    }

    /******************* GUI INITIALISATION ********************/
    
    private void guiInit()
    {
        mainContentPane = (JPanel)this.getContentPane();

        try {
            this.setTitle(Mvplan.appName);
            layoutToolbar();
            layoutLeftPanel();
            layoutDivePanel();
            layoutGasPanel();
            layoutRightPanel();
            layoutRepetitivePanel(); 
        } catch (MissingResourceException e) {
            System.err.println("mvplan.gui.mainframe: missing resource, "+e);
            dispose();
            System.exit(0);            
        }
                  
        // Layout main pane
        mainContentPane.setLayout(mainContentLayout);
        mainContentPane.add(topPanel, 
            new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0));
        mainContentPane.add(mainSplitPane, 
            new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.SOUTH,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0));
        mainSplitPane.setRightComponent(rightPanel);
        mainSplitPane.setLeftComponent(leftPanel);
        if (Mvplan.prefs.getFrameSplit()>0)
            mainSplitPane.setDividerLocation(Mvplan.prefs.getFrameSplit());
  
        // Set initial state
        if(Mvplan.prefs.getLastModelFile()!= null)
            lastModelFile=new File(Mvplan.prefs.getLastModelFile());        
        printButton.setEnabled(false);
        clearButton.setEnabled(false);
        loadButton.setEnabled(false);
        saveButton.setEnabled(false);
        repetitiveDivePanelRefresh();
        
        // Control Window close event
        this.addWindowListener( new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                saveAndClose();
            }
        });
         
    }

    /** Layout right panel */
    private void layoutRightPanel() throws MissingResourceException {
        Border rightPanelBorder = BorderFactory.createEtchedBorder();
        rightPanel.setBorder(BorderFactory.createTitledBorder(rightPanelBorder,Mvplan.getResource("mvplan.gui.MainFrame.rightPanelBorder.text")));
        rightPanel.setPreferredSize( new Dimension(300,300));
        rightPanel.setMinimumSize( new Dimension(200,300));
        rightPanel.setLayout(rightPanelLayout);
        textArea = new JTextArea("",25,80);
        textArea.setMargin(new Insets(10,10,10,10));
        textArea.setEditable(false);
        textArea.setFont(new Font("MONOSPACED",Font.PLAIN,12));
        rightScrollPane = new JScrollPane();
        rightPanel.add(rightScrollPane,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(10,10,10,10),0,0));
        rightScrollPane.getViewport().add(textArea);   
        textCopyMenuItem = new JMenuItem( Mvplan.getResource("mvplan.gui.MainFrame.textCopyMenuItem.text") );
        textPopupMenu.add(textCopyMenuItem);
        textArea.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if((e.getButton()== MouseEvent.BUTTON3) || ( (e.getModifiers() & InputEvent.CTRL_MASK)!=0 && e.getButton()== MouseEvent.BUTTON1 )) {                                
                    textPopupMenu.show( e.getComponent(),e.getX(), e.getY());    
                } 
            }                      
        });
        textCopyMenuItem.addActionListener ( new ActionListener  ()  {
            public void actionPerformed(ActionEvent actionEvent) {
                StringSelection stringSelection = new StringSelection( textArea.getText() );
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents( stringSelection, null );
            }
        });
    }
    
    /** Layout gas Panel */
    private void layoutGasPanel() throws MissingResourceException {
        gasPanel.setLayout(gasPanelLayout);
        gasTable = new JTable(knownGasModel);
        gasTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);         
        gasTable.setRowHeight(gasTable.getRowHeight()+2);
        gasTable.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.gasTable.tip" ));
        JScrollPane gasScrollPane = new JScrollPane();
        gasPanel.add(gasScrollPane,
            new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, new Insets(10,10,5,10),0,0));
        gasScrollPane.getViewport().add(gasTable);
        try {
            gasDeleteButton.setIcon( createImageIcon("/mvplan/resources/rowdelete24.gif"));
            gasAddButton.setIcon( createImageIcon("/mvplan/resources/insert24.gif"));
            gasEditButton.setIcon( createImageIcon("/mvplan/resources/edit24.gif"));
        } catch ( Exception e ) {
            System.err.println("mvplan.gui.MainFrame: Error loading icons: "+ e);
        }
        // Optionally add text labels
        if( !BUTTON_ICONS_ONLY ) {
            gasAddButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.gasAddButton.text"));
            gasEditButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.gasEditButton.text"));
            gasDeleteButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.gasDeleteButton.text"));
        }
        gasAddButton.setPreferredSize(new Dimension(35,30));
        gasEditButton.setPreferredSize(new Dimension(35,30));
        gasDeleteButton.setPreferredSize(new Dimension(35,30));       
        gasAddButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.gasAddButton.tip"));       
        gasEditButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.gasEditButton.tip"));
        gasEditButton.setEnabled(false);        
        gasDeleteButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.gasDeleteButton.tip"));
        gasDeleteButton.setEnabled(false);
        gasOCDecoCB.setText(Mvplan.getResource("mvplan.gui.MainFrame.gasOCDecoCB.text"));
        gasOCDecoCB.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.gasOCDecoCB.tip"));
        gasOCDecoCB.setSelected(Mvplan.prefs.getOcDeco());
        gasToolPanel.add(gasAddButton);
        gasToolPanel.add(gasEditButton);
        gasToolPanel.add(gasDeleteButton);
        gasToolPanel.add(gasOCDecoCB);
        gasPanel.add(gasToolPanel,
            new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.SOUTHWEST,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0));
        
        // Gas profilePopup menu
        gasAddMenuItem = new JMenuItem(Mvplan.getResource("mvplan.gui.MainFrame.gasAddButton.text"));
        gasEditMenuItem = new JMenuItem(Mvplan.getResource("mvplan.gui.MainFrame.gasEditButton.text"));
        gasEditMenuItem.setEnabled(false);
        gasDeleteMenuItem = new JMenuItem(Mvplan.getResource("mvplan.gui.MainFrame.gasDeleteButton.text"));
        gasDeleteMenuItem.setEnabled(false);
        gasPopupMenu.add(gasAddMenuItem);
        gasPopupMenu.add(gasEditMenuItem);
        gasPopupMenu.add(gasDeleteMenuItem);
        MouseListener gasListener = new GasPopupListener();        
        gasTable.addMouseListener(gasListener);
        gasPanel.addMouseListener(gasListener);
        gasScrollPane.addMouseListener(gasListener);        
        gasAddMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                addGas();
            }
        });
        gasEditMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                editGas();
            }
        });
        gasDeleteMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deleteGas();
            }
        });
        
        // Add selection listener to control button enabling/disabling
        gasTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (gasTable.getSelectedRow()>=0) {
                    gasEditButton.setEnabled(true);
                    gasEditMenuItem.setEnabled(true);
                    if(gasTable.getRowCount()>1) {
                        gasDeleteButton.setEnabled(true);                    
                        gasDeleteMenuItem.setEnabled(true);
                    }
                } else {
                    gasEditButton.setEnabled(false);
                    gasDeleteButton.setEnabled(false); 
                    gasEditMenuItem.setEnabled(false);
                    gasDeleteMenuItem.setEnabled(false);
                }
            }            
        });   
        // Table events
        gasPanel.addMouseListener(new MouseAdapter () {
            public void mouseEntered(MouseEvent e) {
                // Close combo box if opened to avoid contention with gas table
                if(gasComboBox.isFocusOwner())          
                    gasComboBox.setSelectedIndex(gasComboBox.getSelectedIndex());
            }
        }) ;
        // Gas panel buttons
        gasDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteGas();
            }
        });
        gasAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addGas();
            }
        });
        gasEditButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editGas();
            }
        });
        gasOCDecoCB.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.DESELECTED) Mvplan.prefs.setOcDeco(false);
                    else Mvplan.prefs.setOcDeco(true);
            }
        });
        repetitiveModeCB.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                repetitiveDivePanelRefresh();
            }
        });
    }
    
    /** Layout Dive Panel */
    private void layoutDivePanel() throws MissingResourceException {
        divePanel.setLayout(divePanelLayout);
        diveTable = new JTable(knownSegmentsModel);
        diveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        diveTable.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.diveTable.tip" ));
        JScrollPane diveScrollPane = new JScrollPane();
        divePanel.add(diveScrollPane,
            new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, new Insets(10,10,5,10),0,0));
        diveScrollPane.getViewport().add(diveTable);
        
        JPanel diveUpDownPanel = new JPanel();
        diveUpDownPanel.setLayout(new GridLayout(1,2));
        try {
            diveUpButton.setIcon( createImageIcon("/mvplan/resources/up16.gif")); 
            diveDownButton.setIcon( createImageIcon("/mvplan/resources/down16.gif")); 
            diveDeleteButton.setIcon( createImageIcon("/mvplan/resources/rowdelete24.gif"));
            diveAddButton.setIcon( createImageIcon("/mvplan/resources/insert24.gif"));
        } catch ( Exception e ) {
            System.err.println("mvplan.gui.MainFrame: Error loading icons: "+ e);
        }
        
        // Optionally add text labels
        if( !BUTTON_ICONS_ONLY ) {
            diveAddButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.diveAddButton.text"));
            diveDeleteButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.diveDeleteButton.text"));            
        }        
        diveAddButton.setPreferredSize(new Dimension(35,30));
        diveDeleteButton.setPreferredSize(new Dimension(35,30));
        diveUpButton.setPreferredSize(new Dimension(18,18));
        diveDownButton.setPreferredSize(new Dimension(18,18));
        diveUpDownPanel.add(diveUpButton);
        diveUpDownPanel.add(diveDownButton);
        diveUpButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.diveUpButton.tip"));
        diveDownButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.diveDownButton.tip"));        
        diveUpButton.setEnabled(false);
        diveDownButton.setEnabled(false);                
        diveAddButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.diveAddButton.tip"));
        diveDeleteButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.diveDeleteButton.tip"));
        diveDeleteButton.setEnabled(false);
        diveToolPanel.add(diveUpDownPanel);
        diveToolPanel.add(diveAddButton);
        diveToolPanel.add(diveDeleteButton);        
        divePanel.add(diveToolPanel,
            new GridBagConstraints(0,1,1,1,0.0,0.0,GridBagConstraints.SOUTH,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0));

        diveAddMenuItem = new JMenuItem( Mvplan.getResource("mvplan.gui.MainFrame.diveAddButton.text") );        
        diveDeleteMenuItem = new JMenuItem (Mvplan.getResource("mvplan.gui.MainFrame.diveDeleteButton.text"));
        diveDeleteMenuItem.setEnabled(false);
        divePopupMenu.add(diveAddMenuItem);
        divePopupMenu.add(diveDeleteMenuItem);
        MouseListener diveListener = new DivePopupListener();        
        diveTable.addMouseListener(diveListener);
        divePanel.addMouseListener(diveListener);
        diveScrollPane.addMouseListener(diveListener);
        diveAddMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                addSegment();
            }
        });
        diveDeleteMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                deleteSegment();
            }
        });                
        
        // Configure Dive Table
        // Set column preferred widths explcitly
        TableColumn column = null;   
        diveTable.setRowHeight(diveTable.getRowHeight()+2);
        for (int i = 0; i < 5; i++) {
             column = diveTable.getColumnModel().getColumn(i);
            if (i == 2) {
                column.setPreferredWidth(80); //gas column is bigger
            } else {
                 column.setPreferredWidth(40);
            }
        }
        // Set up combo box for diveTabel column index 2
        column = diveTable.getColumnModel().getColumn(2);
        gasComboBox = new JComboBox();
        gasComboBox.setBackground(Color.WHITE);
        updateGasCombo();
        column.setCellEditor(new DefaultCellEditor(gasComboBox));

        // In order to get better behavior for the JTable, configure custom cell editors
        // Set custom editor for depth column
        column = diveTable.getColumnModel().getColumn(0);
        depthEditor = new DoubleCellEditor( new JTextField(),0.0,Mvplan.prefs.getMaxDepth());
        column.setCellEditor(depthEditor);
        // Set custom editor for Segment Time column
        column = diveTable.getColumnModel().getColumn(1);
        timeEditor = new DoubleCellEditor( new JTextField(),0.0,Mvplan.prefs.getMaxSegmentTime());
        column.setCellEditor(timeEditor);
        // Set custom editor for Set Point column
        column = diveTable.getColumnModel().getColumn(3);
        spEditor =  new DoubleCellEditor( new JTextField(),0.0,Mvplan.prefs.getMaxSetpoint());
        column.setCellEditor(spEditor);
        // Add focus listeners to pre-select the cell text for easier editing
        depthEditor.getComponent().addFocusListener(new CellEditorFocusListener ());
        timeEditor.getComponent().addFocusListener(new CellEditorFocusListener ());
        spEditor.getComponent().addFocusListener(new CellEditorFocusListener ());                         
        
        // Add selection listener to control button enabling/disabling
        diveTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (diveTable.getSelectedRow()>=0) {
                    if(diveTable.getRowCount()>1) {
                        diveDeleteButton.setEnabled(true);  
                        diveDeleteMenuItem.setEnabled(true);
                        diveUpButton.setEnabled(true);
                        diveDownButton.setEnabled(true);
                    }
                } else {
                    diveDeleteButton.setEnabled(false); 
                    diveDeleteMenuItem.setEnabled(false);
                    diveUpButton.setEnabled(false);
                    diveDownButton.setEnabled(false);                    
                }
            }            
        });  
        // Dive Panel Toolbar button events
        diveUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Find row selected
                int row = diveTable.getSelectedRow();
                if (row >=0) {
                    knownSegmentsModel.moveRowUp(diveTable.getSelectedRow());
                    row = (row>0) ? row-1 : 0;
                    diveTable.setRowSelectionInterval(row,row);
                }
            }
        });
        diveDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Find row selected
                int row = diveTable.getSelectedRow();
                if (row>=0) {
                    knownSegmentsModel.moveRowDown(diveTable.getSelectedRow());
                    row = (row<diveTable.getRowCount()-1) ? row+1 : diveTable.getRowCount()-1;
                    diveTable.setRowSelectionInterval(row,row);
                }
            }
        });

        diveDeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteSegment();
            }
        });
        diveAddButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Find row selected
                addSegment();
            }
        });
    }
    
    /** Layout repetitve panel */
    private void layoutRepetitivePanel() throws MissingResourceException {
        repetitiveDivePanel.setLayout( new GridBagLayout());  
        repetitiveLabel.setText(Mvplan.getResource("mvplan.gui.MainFrame.repetitiveLabel.text"));
        surfaceIntervalField.setColumns(4);
        surfaceIntervalField.setMinimumSize(new Dimension(40,20));
        surfaceIntervalField.setText("0");  
        surfaceIntervalField.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.repetitiveLabel.tip"));
        repetitiveModeCB.setSelected(repetitiveMode);
        repetitiveModeCB.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.repetitiveModeCB.tip"));        
        tissueComponent = new ModelDisplayComponent(currentModel);
        tissueComponent.setPreferredSize(new Dimension(31,31));
        tissueComponent.setMinimumSize(new Dimension(31,31));        
        tissueComponent.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.tissueComponent.tip"));                
        tissueComponent.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e){
                if(e.getButton()==1) {
                    if(profilePopup==null)
                        profilePopup = new ProfilePopup(mainFrame,Mvplan.getResource("mvplan.gui.MainFrame.profilePopup.title.text"), currentModel);
                    else 
                        profilePopup.setVisible(true);
                }
            }            
        });
                
        repetitiveDivePanel.add(repetitiveModeCB,
            new GridBagConstraints(0,0,1,1,0.1,1.0,GridBagConstraints.WEST,GridBagConstraints.NONE, new Insets(5,0,5,5),0,0));       
        repetitiveDivePanel.add(repetitiveLabel,
            new GridBagConstraints(1,0,1,1,0.3,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE, new Insets(5,0,5,5),0,0));       
        repetitiveDivePanel.add(surfaceIntervalField,
            new GridBagConstraints(2,0,1,1,0.3,1.0,GridBagConstraints.WEST,GridBagConstraints.NONE, new Insets(5,5,5,5),0,0)); 
         repetitiveDivePanel.add(tissueComponent,
            new GridBagConstraints(3,0,1,1,0.3,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));    
    }
     
    /** Layout left panel */
    private void layoutLeftPanel() throws MissingResourceException {
        // Lay out left panel
        leftPanel.setLayout(leftPanelLayout);
        Border repetitivePanelBorder = BorderFactory.createEtchedBorder();             
        Border divePanelBorder = BorderFactory.createEtchedBorder();
        Border gasPanelBorder = BorderFactory.createEtchedBorder();
        repetitiveDivePanel.setBorder(BorderFactory.createTitledBorder(repetitivePanelBorder));        
        divePanel.setBorder(BorderFactory.createTitledBorder(divePanelBorder,Mvplan.getResource("mvplan.gui.MainFrame.diveTableBorder.text")));
        gasPanel.setBorder(BorderFactory.createTitledBorder(gasPanelBorder,Mvplan.getResource("mvplan.gui.MainFrame.gasTableBorder.text"))); 
        leftPanel.add(repetitiveDivePanel,
                new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, new Insets(5,5,5,5),0,0));
        leftPanel.add(divePanel,
                new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.CENTER,GridBagConstraints.BOTH, new Insets(5,5,5,5),0,0));
        leftPanel.add(gasPanel,
                new GridBagConstraints(0,2,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, new Insets(5,5,5,5),0,0));
    }
    
    /** // Layout the toolbar and top panel */
    private void layoutToolbar() throws MissingResourceException {   
        Toolkit toolkit = Toolkit.getDefaultToolkit();        
        //URL icon;
        try {
            calcButton.setIcon( createImageIcon("/mvplan/resources/new16.gif"));
            tableButton.setIcon( createImageIcon("/mvplan/resources/table16.gif"));
            clearButton.setIcon(createImageIcon("/mvplan/resources/cross16.gif"));
            prefsButton.setIcon( createImageIcon("/mvplan/resources/preferences16.gif"));
            loadButton.setIcon( createImageIcon("/mvplan/resources/open16.gif"));
            saveButton.setIcon( createImageIcon("/mvplan/resources/save16.gif"));
            exitButton.setIcon( createImageIcon("/mvplan/resources/stop16.gif"));
            printButton.setIcon( createImageIcon("/mvplan/resources/print16.gif"));             
            aboutButton.setIcon( createImageIcon("/mvplan/resources/about16.gif") );               
        } catch ( Exception e ) {
            System.err.println("mvplan.gui.MainFrame: Error loading icons: "+ e);
        }
        // Load images for progress indicator
        try {                                                
            okImage  = toolkit.createImage( getClass().getResource("/mvplan/resources/ok-12.gif")  );
            updateImage  = toolkit.createImage( getClass().getResource("/mvplan/resources/new-12.gif")  );
        } catch (Exception e) {
            System.err.println("mvplan.gui.MainFrame: Error loading images: "+ e);    
        }

        calcButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.calcButton.text"));
        calcButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.calcButton.tip"));
        tableButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.tableButton.text"));
        tableButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.tableButton.tip"));
        clearButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.clearButton.text"));
        clearButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.clearButton.tip"));
        prefsButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.prefsButton.text"));
        prefsButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.prefsButton.tip"));
        loadButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.loadButton.text"));
        loadButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.loadButton.tip"));
        saveButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.saveButton.text"));
        saveButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.saveButton.tip"));
        exitButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.exitButton.text"));
        exitButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.exitButton.tip"));
        printButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.printButton.text"));
        printButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.printButton.tip"));
        aboutButton.setText(Mvplan.getResource("mvplan.gui.MainFrame.aboutButton.text"));
        aboutButton.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.aboutButton.tip")+Mvplan.NAME);            
        progress.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.progress.check.tip"));
        progress.setFocusable(false);
        
        toolBar.add(calcButton,null);
        toolBar.add(tableButton, null);
        toolBar.add(clearButton,null);
        toolBar.add(printButton,null);
        toolBar.add(prefsButton,null);
        toolBar.add(loadButton,null);
        toolBar.add(saveButton,null);
        toolBar.add(aboutButton,null);
        toolBar.add(exitButton,null);               
        toolBar.setFloatable(false);
        exitButton.setSize(new Dimension(50,50));
                                
        // Place components in top Panel

        progress.setPreferredSize(new Dimension(22,22));
        topPanel.setLayout(new GridBagLayout());
        topPanel.add(toolBar,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.WEST,GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
        topProgressPanel.setMinimumSize(new Dimension(30,30));
        topProgressPanel.setLayout(new GridBagLayout());
        topProgressPanel.add(progress,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE, new Insets(0,0,0,0),0,0));
        topPanel.add(topProgressPanel,new GridBagConstraints(1,0,1,1,1.0,1.0,GridBagConstraints.EAST,GridBagConstraints.NONE, new Insets(0,5,0,5),0,0));
        
        // ***** Toolbar buttons ******
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveAndClose();
            }
        });
        
        prefsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int oldUnits=Mvplan.prefs.getUnits();                
                // Prefs frame
                new PrefsDialog(mainFrame);
                // If extendedLimits or Units changed need to update table renderer limits
                // So do it anyway
                depthEditor.setLimits(0.0,Mvplan.prefs.getMaxDepth());
                timeEditor.setLimits(0.0, Mvplan.prefs.getMaxSegmentTime());
                spEditor.setLimits(0.0, Mvplan.prefs.getMaxSetpoint()); 
                // If units changed, clear model
                if (oldUnits != Mvplan.prefs.getUnits()) {
                    if( !Mvplan.prefs.isDisableModUpdate()) doConvertDepths(oldUnits);
                    clearDive();
                }                               
            }
        });
        printButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Print
                //printPlan();
                printProfileDisplayComponent();
            }
        });
        loadButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadModel();
            }                  
        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(currentModel!=null)
                    saveModel();
            }
        });

        calcButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doDive();
            }
        });
        tableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doTableGenerator(textArea);
            }
        });        
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearDive();
            }
        });

        aboutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doAbout();
            }
        }); 
        progress.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if( !updateAvailable)                    
                    doCheckVersion(true);                    
                if (updateAvailable) {
                    try  {                       
                        new InfoURLDialog(mainFrame, Mvplan.getResource("mvplan.gui.MainFrame.updateAvailableDialog.title.text"), true ,
                                Mvplan.getResource("mvplan.gui.MainFrame.updateAvailableDialog.info1.text"),
                                Mvplan.getResource("mvplan.gui.MainFrame.updateAvailableDialog.info2.text")+ " ", 
                                Mvplan.getResource("mvplan.gui.MainFrame.updateAvailableDialog.info3.text"),
                                "http://wittig.net.au");    //TODO - put this in Prefs
                    } catch (MissingResourceException e) {
                        System.err.append("mvplan.gui.mainFrame: missing resource -"+e);
                    }
                 }                
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
            }
        });               
                
    }
    
    /*************** UTILITY METHODS ********************/
    
    /** Save Prefs, Close and exit */
    private void saveAndClose() {        
        savePrefs();
        setVisible(false);
        dispose();
        System.exit(0); 
    }
    
    /** Save model to XML file using object serialisation */
    private void saveModel() {
        final JFileChooser fc = new JFileChooser();
        MyFileFilter ff = new MyFileFilter("XML","MV-Plan XML files");
        fc.setFileFilter(ff);
        ModelSaveAccessory fa=new ModelSaveAccessory();
        if (lastModelFile != null)
            fc.setCurrentDirectory(lastModelFile);        
        // FileChooser is normally 500x326
        fc.setPreferredSize(new Dimension(600,326));
        fc.setAccessory(fa);
        fa.setMetaData(currentModel.getMetaData());
        try {
            fa.setMetaDataLabel(Mvplan.getResource("mvplan.gui.MainFrame.saveModelAccessory.text"));
        } catch (MissingResourceException e) {}
        
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            lastModelFile = fc.getSelectedFile();
            
            // Verify and fix extension
            if(ff.getExtension(lastModelFile)==null)
                // need to rename or add extension
                lastModelFile=new File(ff.getName(lastModelFile)+".xml");
            // Check for overwrite
            if(lastModelFile.exists() &&
                (JOptionPane.showConfirmDialog(this,
                    Mvplan.getResource("mvplan.gui.MainFrame.saveModelOverwriteDialog.text"),
                    Mvplan.getResource("mvplan.gui.MainFrame.saveModelOverwriteDialog.title"),
                    JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION)) {
                // Do nothing   
                    
            } else {
                // Save model
                currentModel.setMetaData(fa.getMetaData());
                ModelDAO dao=new ModelDAO();
                if(dao.saveModel(currentModel,lastModelFile.toString()) != ModelDAO.SUCCESS)
                    JOptionPane.showMessageDialog(this,
                            Mvplan.getResource("mvplan.gui.MainFrame.saveModelErrorDialog.text"),
                            Mvplan.getResource("mvplan.gui.MainFrame.saveModelErrorDialog.title"),
                            JOptionPane.ERROR_MESSAGE);                             
            }    
        }       
    }
    
    /** Load tissue model from XML file */
    private void loadModel() {
        final JFileChooser fs = new JFileChooser();
        MyFileFilter ff = new MyFileFilter("XML","MV-Plan XML files");
        fs.setFileFilter(ff);        
        if (lastModelFile != null)
            fs.setCurrentDirectory(lastModelFile);
        int returnVal = fs.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            lastModelFile = fs.getSelectedFile();
            ModelDAO dao=new ModelDAO();
            currentModel=dao.loadModel(lastModelFile.toString());                                    
            currentProfile=null;
            if(currentModel!=null) {
                // Check units
                if(currentModel.getUnits()!=Mvplan.prefs.getUnits()) {
                        JOptionPane.showMessageDialog(this,
                            Mvplan.getResource("mvplan.gui.MainFrame.loadModelUnitErrorDialog.text"),
                            Mvplan.getResource("mvplan.gui.MainFrame.loadModelUnitErrorDialog.title"),
                            JOptionPane.ERROR_MESSAGE);   
                        clearDive();                        
                }  else {                              
                    textArea.setText(Mvplan.getResource("mvplan.gui.MainFrame.loadModel.modelLoaded.text")+" "+currentModel.getMetaData()+'\n');               
                    setTissueIcon();
                    printButton.setEnabled(false);  // No profile to print
                    clearButton.setEnabled(true);  
                    saveButton.setEnabled(true);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        Mvplan.getResource("mvplan.gui.MainFrame.loadModelErrorDialog.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.loadModelErrorDialog.title"),
                        JOptionPane.ERROR_MESSAGE);
                clearDive();
            }               
        } 
    }
    
    /** Clear dive model and display */
    private void clearDive() {
        textArea.setLineWrap(false);
        textArea.setText("");
        currentProfile=null;
        currentModel=null;
        currentTable=null;
        printButton.setEnabled(false);
        clearButton.setEnabled(false);
        saveButton.setEnabled(false);
        setTissueIcon();
    }
    
    /** Refresh repetitive dive panel and set buttons */
    private void repetitiveDivePanelRefresh() {
        repetitiveMode=repetitiveModeCB.isSelected();
        repetitiveLabel.setEnabled(repetitiveMode);
        tissueStatusLabel.setEnabled(repetitiveMode);
        surfaceIntervalField.setEnabled(repetitiveMode);
        loadButton.setEnabled(repetitiveMode);
        if (!repetitiveMode)    clearButton.doClick();
    }
    
    /** Returns an ImageIcon, or null if the path was invalid.  */
    private static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = MainFrame.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /** Creates (or updates) the Gas Combo Box from the list of known gases  */
    private void updateGasCombo()
    {
        gasComboBox.removeAllItems();
        Iterator it=knownGases.iterator();
        while(it.hasNext()) {
            gasComboBox.addItem((Gas)it.next());
        }
    }

    /* Delete dive segment from knownSegments ArrayList() */
    private void deleteSegment() {
        // Find row selected
        if (diveTable.getSelectedRow()>=0)
            knownSegmentsModel.removeRow(diveTable.getSelectedRow());
    }
    
    /* Add dive segment to knownSegments ArrayList()  */
    private void addSegment()
    {
        int row;
        Gas g = (Gas)knownGases.get(0);
        SegmentDive s=new SegmentDive(0.0,0.0,g,0.0);
        row = diveTable.getSelectedRow();
        if (row>=0) {
            knownSegments.add(diveTable.getSelectedRow(),s);
        } else {
            knownSegments.add(s);
            row=diveTable.getRowCount()-1;
        }
        knownSegmentsModel.fireTableDataChanged();
        diveTable.setRowSelectionInterval(row,row);
    }

    /* Add new gas to knownGases ArrayList()  */
    private void addGas()
    {
        // Initialise gas
        Gas g=new Gas(0.0,0.0,0.0);
        GasDialog dialog=new GasDialog((this), g);
        if(g.getFO2()>0.0) {    // Is the gas valid ? Was it added ok ?
            // Add to ArrayList()
            knownGases.add(g);
            updateGasCombo();
            knownGasModel.fireTableDataChanged();
        }
    }
    
    /** Deletes gas from the knownGases ArrayList() */
   private void deleteGas() {
       // If a row is selected, and this is no the only row, then remove it.
        if (gasTable.getSelectedRow()>=0 && gasTable.getRowCount()>1) {
            knownGasModel.removeRow(gasTable.getSelectedRow());
            updateGasCombo();
        }
    }
    
    /** Edits a gas using the GasDialog */
    private void editGas() {
       if (gasTable.getSelectedRow()>=0) {         
            GasDialog dialog=new GasDialog((this), knownGasModel.getGas(gasTable.getSelectedRow()) );   
            updateGasCombo();
            knownGasModel.fireTableDataChanged();   
       }
    }

    /* Prints the graphical profile display component  */
    private void printProfileDisplayComponent() {
       int h,l;
       TablePreviewDialog tpd;
       // Make heading like this:
       // "MV-Plan GF:20/90 ZHL16B" or dislay altitude instead of model
       h=(int)Math.round(Mvplan.prefs.getGfHigh()*100);
       l=(int)Math.round(Mvplan.prefs.getGfLow()*100);
       String heading=String.format("%1$s  GF:%2$02d/%3$02d",Mvplan.NAME,l,h);
       // Display single dive table, or ...
       if(currentModel!=null) {
            if(Mvplan.prefs.getAltitude()>0.0)
                heading=heading+String.format("  %1$4.0f%2$s",
                   Mvplan.prefs.getAltitude(),Mvplan.prefs.getDepthShortString());
            else
                heading=heading+String.format("  %1$s", currentProfile.getModel().getModelName());
           if(currentProfile.getIsRepetitiveDive())
                heading=heading+"   SI:"+currentProfile.getSurfaceInterval();
           tpd = new TablePreviewDialog(this,currentProfile.getProfile(),heading);
       } else if (currentTable!=null) {
           // Display multiple dive table
           if(Mvplan.prefs.getAltitude()>0.0)
                heading=heading+String.format("  %1$4.0f%2$s",
                   Mvplan.prefs.getAltitude(),Mvplan.prefs.getDepthShortString());
           heading=heading+String.format("  %1$s", currentTable.getModelName());

           tpd = new TablePreviewDialog(this,currentTable,heading);
       }
       
    }
        
    /* Write preferences to a file via serialisation  */
    private void savePrefs()
    {
        // Update prefs with known Gases
        Mvplan.prefs.setPrefGases(new ArrayList());  // Zap old object
        Iterator i = knownGases.iterator();
        while(i.hasNext()) {
            Mvplan.prefs.getPrefGases().add((Gas)i.next());
        }
        // Update prefs with known Segments
        Mvplan.prefs.setPrefSegments(new ArrayList());
        i = knownSegments.iterator();
        while(i.hasNext()) {
            Mvplan.prefs.getPrefSegments().add((SegmentAbstract)i.next());
        }       

        // Update screensize
        Mvplan.prefs.setFrameSizeX(this.getSize().width);
        Mvplan.prefs.setFrameSizeY(this.getSize().height);
        Mvplan.prefs.setFrameSplit(mainSplitPane.getDividerLocation());
        
        if(lastModelFile!=null)
            Mvplan.prefs.setLastModelFile(lastModelFile.toString());
        
        PrefsDAO dao=new PrefsDAO();
        try {
            if(Mvplan.DEBUG>0) System.out.println("Writing prefs to: "+Mvplan.prefFile);
            dao.setPrefs(Mvplan.prefs,Mvplan.prefFile);
        } catch (Exception ex) {
            // TODO no try block here.
            JOptionPane.showMessageDialog((this),
                Mvplan.getResource("mvplan.gui.MainFrame.prefsSaveErrorDialog.info.text")+'\n'+Mvplan.prefFile,
                Mvplan.getResource("mvplan.gui.MainFrame.prefsSaveErrorDialog.title.text"),
                JOptionPane.ERROR_MESSAGE);
        }
    }    
    
    /**
     * Do TableGeneratorModel dive 
     */
    private void doTableGenerator(JTextArea text) {
        int[] modifiers = Mvplan.prefs.getModifiers();    // holds array of time modifiers
        //boolean nothingToProcess=false;
        int returnCode;
        TableGeneratorDialog mdd=null;
        
        clearDive();    // Start with clear tissue model
        // Create a multiDive model with known segments, gases and modifiers
        TableGeneratorModel mp = new TableGeneratorModel(knownSegments, knownGases, modifiers);
        // Create new multiDiveDialog and initialise it with the multiProfile model
        mdd = new TableGeneratorDialog(this, mp);
        // Show dialog. Returns with boolean true to continue. Modifiers may have been altered.
        if(mdd.showDialog()) {        
            // Execute the profiles with new modifers
            returnCode = mp.doMultiDive();
            // Check the returnCode to see if there were problems
            switch(returnCode) {
                case Profile.SUCCESS:
                    TablePrinter tp = new TablePrinter(mp, text);
                    tp.doPrintTable();                      
                    currentTable=mp;
                    printButton.setEnabled(true);
                    clearButton.setEnabled(true);    
                    break;

            case Profile.CEILING_VIOLATION:
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.MainFrame.ceilingViolation.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.ceilingViolation.title"),
                        JOptionPane.ERROR_MESSAGE);
                    break;

            case Profile.NOTHING_TO_PROCESS:
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.MainFrame.noSegments.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.noSegments.title"),
                        JOptionPane.INFORMATION_MESSAGE );
                    break;
            case Profile.PROCESSING_ERROR:
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.MainFrame.processingError.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.processingError.title"),
                        JOptionPane.ERROR_MESSAGE);
            case Profile.INFINITE_DECO:
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.MainFrame.decoNotPossible.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.decoNotPossible.title"),
                        JOptionPane.ERROR_MESSAGE);                            
            default:        break;                    
            }
        }
    }       
    
    /** doDive(): This method conducts the dive */
    private void doDive()
    {
        int returnCode;
        Profile p;
        
        // Create model for dive
        if( !repetitiveMode || currentModel==null) {
            clearDive();
            p=new Profile(knownSegments,knownGases,null);
        } else {
            p=new Profile(knownSegments,knownGases,currentModel);        
            if( p.isDiveSegments() || JOptionPane.showConfirmDialog(this,
                                    Mvplan.getResource("mvplan.gui.MainFrame.noSegmentsDoSurfaceInterval.text"),
                                       Mvplan.getResource("mvplan.gui.MainFrame.noSegmentsDoSurfaceInterval.title"),
                                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)  {    // Is there anything to process
            p.doSurfaceInterval(Integer.parseInt(surfaceIntervalField.getText()));
            textArea.append(Mvplan.getResource("mvplan.gui.MainFrame.modelUpdated.text")
                            +" "+p.getSurfaceInterval()+
                            Mvplan.getResource("mvplan.minutes.shortText")+".\n");
            }
        }
        
        returnCode=p.doDive();
        if((Mvplan.DEBUG > 0)) System.out.println("doDive: return code="+returnCode);
        switch(returnCode) {
            case Profile.SUCCESS:
                            currentProfile=p;   // Save as current profile
                            p.doGasCalcs();     // Calculate gases
                            // Save ZHL16BModel
                            currentModel=p.getModel();
                            //System.out.println("Dive Metadata:"+currentModel.getMetaData());
                            printButton.setEnabled(true);
                            clearButton.setEnabled(true);
                            saveButton.setEnabled(true);
                            setTissueIcon();
                            new ProfilePrinter(currentProfile,textArea, knownGases).doPrintTable();
                            break;

            case Profile.CEILING_VIOLATION:
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.MainFrame.ceilingViolation.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.ceilingViolation.title"),
                        JOptionPane.ERROR_MESSAGE);
                    break;

            case Profile.NOTHING_TO_PROCESS:
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.MainFrame.noSegments.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.noSegments.title"),
                        JOptionPane.INFORMATION_MESSAGE );
                    break;
            case Profile.PROCESSING_ERROR:
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.MainFrame.processingError.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.processingError.title"),
                        JOptionPane.ERROR_MESSAGE);
            case Profile.INFINITE_DECO:
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.MainFrame.decoNotPossible.text"),
                        Mvplan.getResource("mvplan.gui.MainFrame.decoNotPossible.title"),
                        JOptionPane.ERROR_MESSAGE);                            
            default:        break;
        }           
    }
    
    /** setTissueIcon(): Sets tissue status icon and tooltip depending on the state of the model */
    private void setTissueIcon(){
        if(profilePopup!=null){
            profilePopup.setModel(currentModel);            
        }
        tissueComponent.setModel(currentModel);                
    }
    
    /** doCheckVersion(): Checks if there is a new version available 
     *  @param required - true if the check is not-optional, false if optional depending on the date last checked
     */
    private void doCheckVersion(boolean required) {
        // Check current version
        final VersionManager vm=new VersionManager();
        //System.out.println("Mainframe: doCheckVersion()");
        if ( required || vm.updateRequired() ) {
            // Display progress indicator
            progress.start();
            progress.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.progress.nowChecking.tip"));
            // Create SwingWorker object to do version check
            final mvplan.util.SwingWorker worker = new mvplan.util.SwingWorker() {
                public Object construct() {
                    // Tasks to perform in thread
                    vm.updateCheck();
                    return null;
                }                
                public void finished() {
                    // Tasks to complete
                    progress.stop();                   
                    if (vm.getResultCode() == VersionManager.ERROR) {
                        //updateLabel.setIcon(updateDiscIcon);
                        // Error response
                        progress.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.progress.noContact.tip"));
                        updateAvailable=false;                        
                    } else if( vm.getResultCode() == VersionManager.UPDATE ) {
                        progress.setImage(updateImage);
                        progress.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.progress.update.tip"));
                        if( mainFrame.isActive() ) {

                                new InfoURLDialog(mainFrame, Mvplan.getResource("mvplan.gui.MainFrame.updateAvailableDialog.title.text"), true ,
                                        Mvplan.getResource("mvplan.gui.MainFrame.updateAvailableDialog.info1.text"),
                                        Mvplan.getResource("mvplan.gui.MainFrame.updateAvailableDialog.info2.text")+ " ", 
                                        Mvplan.getResource("mvplan.gui.MainFrame.updateAvailableDialog.info3.text"),
                                        "http://wittig.net.au");    //TODO - put this in Prefs

                        }                                                           
                        updateAvailable=true;
                    } else if (vm.getResultCode() == VersionManager.CURRENT) {
                        progress.setImage(okImage);
                        progress.setToolTipText(Mvplan.getResource("mvplan.gui.MainFrame.progress.current.tip"));
                        updateAvailable=false;
                    }                                                       
                }
            };
            // Start thread
            worker.start();  
        }
    }
    
    /* Converts gas MODS after units change */
    private void doConvertDepths(int oldUnits) {
        Iterator i1 = knownGases.iterator();
        Iterator i2 = knownSegments.iterator();
        // Set conversion factor depending on direction of conversion
        double conversion = (oldUnits==Mvplan.prefs.METRIC ? Mvplan.prefs.METERS_TO_FEET : 1/Mvplan.prefs.METERS_TO_FEET);
        // Go through gases        
        while( i1.hasNext()){
            Gas g = (Gas)i1.next();
            g.setMod( roundDouble(0, g.getMod()*conversion) ) ;
        }             
        knownGasModel.fireTableDataChanged();
        // Go through segments
        while (i2.hasNext()) {
            SegmentAbstract s = (SegmentAbstract)i2.next();
            s.setDepth( roundDouble(0, s.getDepth()*conversion));
        }
        knownSegmentsModel.fireTableDataChanged();
    }  
    
    /* 
     * Rounds double values
     */
    private double roundDouble(int precision, double d){        
        int decimalPlace = precision;
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(decimalPlace,BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();                
    }        
    
    private void doAbout() {
       // Use HTML message for better font control and word wrapping
        String s,s2;
        s="<html><p>"+Mvplan.appName+"  ("+Mvplan.BUILD_DATE+")</p>";
        s=s+"<p>\u00A9 2005-2010 Guy Wittig&nbsp</p>";
        // Add localisation credit string
        try{
            s2 = Mvplan.getResource("mvplan.gui.Mainframe.AboutDialog.localisedBy.text");
            s=s+s2;
        } catch (Exception e) { }
                    
        s=s+"<p>&nbsp</p>"; // Add space
 
        s=s+Mvplan.getResource("mvplan.gui.MainFrame.AboutDialog.about.text");

        
        AboutDialog about = new AboutDialog(this, s);
    }
    /****************** GENERAL ACCESSORS AND MUTATORS ****************/
    
    /** Accessor for persistant PageFormat object */    
    public PageFormat getPageFormat() {
        return pageFormat;
    }
    /** Mutator for persistant PageFormat object */
    public void setPageFormat(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
    }
    
    
    /***************** HELPER CLASSES *******************/
    private class GasPopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            doPopup(e);
        }                
        private void doPopup(MouseEvent e) {
            if((e.getButton()== MouseEvent.BUTTON3) || ( (e.getModifiers() & InputEvent.CTRL_MASK)!=0 && e.getButton()== MouseEvent.BUTTON1 )) {                                
                gasPopupMenu.show( e.getComponent(),e.getX(), e.getY());    
            } else if(e.getButton()== MouseEvent.BUTTON1 && e.getClickCount()==2) {
                editGas();
            }
        }        
    } 
    private class DivePopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            doPopup(e);
        }                
        private void doPopup(MouseEvent e) {
            // This caters for the PC (right button) and Mac (CTRL + left button)
            if( (e.getButton()== MouseEvent.BUTTON3) || ( (e.getModifiers() & InputEvent.CTRL_MASK)!=0 && e.getButton()== MouseEvent.BUTTON1 )) {                                
                divePopupMenu.show( e.getComponent(),e.getX(), e.getY());    
            } 
        }        
    } 
    
    /**
     * Used to listen to focus events on table cells and pre-select the whole field
     */
    private class CellEditorFocusListener implements FocusListener {
            public void focusGained(FocusEvent e) {
                JTextField tf = (JTextField)(e.getComponent());
                tf.selectAll();                
            }
            public void focusLost(FocusEvent e) {
                JTextField tf = (JTextField)(e.getComponent());
                tf.postActionEvent(); 
            }     
    }    
}
