/*
 * TablePreviewDialog.java
 *
 * Displays a print preview of the dive table. Allows selection of print
 * options.
 *
 * @author Guy Wittig
 * @version 04-Mar-2005
 *
 *   This program is part of MV-Plan
 *   Copywrite 2006 Guy Wittig
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.ArrayList;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;
import java.lang.reflect.*;

import mvplan.main.Mvplan;
import mvplan.dive.TableGeneratorModel;


public class TablePreviewDialog  extends JDialog implements  ActionListener {

    JButton printButton=new JButton();
    JButton setupButton = new JButton();
    JButton cancelButton = new JButton();
    ColourButton colourButton = new ColourButton(); 
    JPanel profilePanel = new JPanel();
    JPanel pagePanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JPanel backPanel = new JPanel();   
    JPanel colourOptionsPanel = new JPanel();
    JPanel profileOptionsPanel = new JPanel();
    JPanel tableOptionsPanel = new JPanel();
    JPanel optionsPanel = new JPanel();  
    JCheckBox stopTimeCheckBox = new JCheckBox();
    JCheckBox gasFirstCheckBox = new JCheckBox();
    JCheckBox showColourCheckBox = new JCheckBox();
    JCheckBox showSecondsCheckBox = new JCheckBox();   
    // These are the font sizes for the dive tables. 
    JComboBox printFontSizeCombo = new JComboBox(new Object [] {new Integer(9),new Integer(10), new Integer(12),new Integer(14)});   
    Insets displayComponentInsets=new Insets(0,0,0,0);      
    TableGeneratorModel model;
    ArrayList segments;
    String heading;
    Frame parent;   
    AbstractDisplayComponent displayComponent;
    JScrollPane scrollPane;    
    PrinterJob job;
    PageFormat pageFormat;
   
    /**
     * Constructor for TableGeneratorModels == multipe dive plans
     */
    public TablePreviewDialog(Frame frame, TableGeneratorModel model, String heading) {
        super(frame,false);
        this.setTitle(Mvplan.getResource("mvplan.gui.TablePreviewDialog.title.text"));
        this.heading=heading;        
        this.model=model;   
        this.parent=frame;             
        displayComponent=new TableDisplayComponent(model,heading);
        init();       
        showSecondsCheckBox.setEnabled(false);
    }
    
    /**
     * Constructor for simple ArrayLists of Segments == singe dive plans
     */
    public TablePreviewDialog(Frame frame, ArrayList segments, String heading) {
        super(frame,false);
        this.setTitle(Mvplan.getResource("mvplan.gui.TablePreviewDialog.title.text"));
        this.heading=heading;        
        this.segments=segments;   
        this.parent=frame;      
        displayComponent=new ProfileDisplayComponent(segments,heading);
        init();    
        gasFirstCheckBox.setEnabled(false);
        stopTimeCheckBox.setEnabled(false);      
    }
    
    /**
     * Init GUI
     */
    public void init()
    {
        job = PrinterJob.getPrinterJob();
        pageFormat=((MainFrame)parent).getPageFormat();   // Get persistant pageFormat from parent  
        if(pageFormat==null) 
            pageFormat=job.defaultPage();        
        
        Container contentPane=getContentPane(); 
        
        // Set up labels
        printButton.setText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.print.text"));
        setupButton.setText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.pageSetup.text"));
        cancelButton.setText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.cancel.text"));         
        stopTimeCheckBox.setText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.stopTime.text"));        
        gasFirstCheckBox.setText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.gasFirst.text"));
        showColourCheckBox.setText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.showColour.text"));
        showSecondsCheckBox.setText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.showSeconds.text"));
        colourButton.setToolTipText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.colour.tip"));
        
        // Lay out Colour Options
        printFontSizeCombo.setSelectedItem(new Integer(Mvplan.prefs.getPrintFontBodySize())); 
        colourButton.setSize( new Dimension(15,15));
        colourButton.setPreferredSize( new Dimension(15,15));
        // This does not work on Mac OSX. Could create an imageIcon I guess ?
        colourButton.setBackground(Mvplan.prefs.getBackgroundColour());
        colourButton.setActionCommand("colour");
        colourButton.addActionListener(this);                
        colourOptionsPanel.setLayout(new GridBagLayout());       
        colourOptionsPanel.add(new JLabel(Mvplan.getResource("mvplan.gui.TablePreviewDialog.fontSize.text")), 
                new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.EAST,GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2),0,0));
        colourOptionsPanel.add(printFontSizeCombo, 
                new GridBagConstraints(1,0,1,1,1.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(0,2,2,2),0,0));
        
        colourOptionsPanel.add(new JLabel( Mvplan.getResource("mvplan.gui.TablePreviewDialog.colour.text")), 
                new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.EAST,GridBagConstraints.HORIZONTAL, new Insets(2,2,2,2),0,0));
        colourOptionsPanel.add(colourButton, 
                new GridBagConstraints(1,1,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE, new Insets(0,2,2,2),0,0));

        printFontSizeCombo.addActionListener(this);
        printFontSizeCombo.setActionCommand("fontSize");
        
        // Profile options
        showSecondsCheckBox.setSelected(Mvplan.prefs.isShowSeconds());
        showSecondsCheckBox.setToolTipText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.showSeconds.tip"));    
        showColourCheckBox.setSelected(Mvplan.prefs.isPrintColour());
        showColourCheckBox.setToolTipText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.useColour.tip"));        
        profileOptionsPanel.setLayout(new GridBagLayout());
        profileOptionsPanel.add(showSecondsCheckBox, 
                new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0)); 
        profileOptionsPanel.add(showColourCheckBox, 
                new GridBagConstraints(0,1,2,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0)); 
        showSecondsCheckBox.addActionListener(this);
        showSecondsCheckBox.setActionCommand("showSeconds");
        showColourCheckBox.addActionListener(this);
        showColourCheckBox.setActionCommand("showColour");
        
        // Table Options
        stopTimeCheckBox.setSelected(Mvplan.prefs.isShowStopTime());        
        gasFirstCheckBox.setSelected(Mvplan.prefs.isShowGasFirst());  
        stopTimeCheckBox.setToolTipText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.stopTime.tip"));
        gasFirstCheckBox.setToolTipText(Mvplan.getResource("mvplan.gui.TablePreviewDialog.gasFirst.tip"));
        stopTimeCheckBox.addActionListener(this);
        stopTimeCheckBox.setActionCommand("stopTime");
        gasFirstCheckBox.addActionListener(this);
        gasFirstCheckBox.setActionCommand("gasFirst");
        tableOptionsPanel.setLayout(new GridBagLayout());
        tableOptionsPanel.add(gasFirstCheckBox, 
                new GridBagConstraints(0,0,2,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0)); 
        tableOptionsPanel.add(stopTimeCheckBox, 
                new GridBagConstraints(0,1,2,1,1.0,0.0,GridBagConstraints.NORTH,GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0)); 

        optionsPanel.setBorder(BorderFactory.createEtchedBorder());        
        
        // Lay out the preview 
        optionsPanel.add(colourOptionsPanel);
        optionsPanel.add(profileOptionsPanel);
        optionsPanel.add(tableOptionsPanel);
        buttonPanel.add(optionsPanel);
        buttonPanel.add(printButton);
        buttonPanel.add(setupButton);
        buttonPanel.add(cancelButton);
        printButton.addActionListener(this);
        setupButton.addActionListener(this);
        cancelButton.addActionListener(this);
        printButton.setActionCommand("print");
        setupButton.setActionCommand("setup");
        cancelButton.setActionCommand("cancel");                               
        Border profilePanelBorder = BorderFactory.createLineBorder(Color.BLACK);
        scrollPane = new JScrollPane(backPanel); //ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
        
        // Set Profile panel to look like a sheet of paper
        profilePanel.setPreferredSize(new Dimension((int)pageFormat.getWidth(),(int)pageFormat.getHeight()));
        profilePanel.setMinimumSize(new Dimension(100,100));
        profilePanel.setMaximumSize(profilePanel.getPreferredSize());      
        profilePanel.setBorder(profilePanelBorder);                         
        profilePanel.setLayout(new GridBagLayout());               
        profilePanel.add(displayComponent,
            new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE, displayComponentInsets,0,0));   
        
        // Set page border from printer settings
        setInsets(); 
        
        // Assemble frame
        contentPane.setLayout(new GridBagLayout());
        backPanel.setBackground(Color.GRAY);
        backPanel.setLayout(new GridBagLayout());
        backPanel.add(profilePanel,new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.NONE, new Insets(10,10,10,10),0,0));
        scrollPane.setMinimumSize(new Dimension(600,500));
        // Set to same size as mainFrame
        this.setSize(Mvplan.prefs.getFrameSizeX(),Mvplan.prefs.getFrameSizeY());        
        contentPane.add(scrollPane, 
                new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, new Insets(10,10,0,10),0,0));        
        contentPane.add(buttonPanel, 
                new GridBagConstraints(0,1,1,1,1.0,0.0,GridBagConstraints.SOUTH,GridBagConstraints.HORIZONTAL, new Insets(0,10,0,10),0,0));               
        profilePanel.setBackground(Color.WHITE);          
        setResizable(true);
        // Place over parent .. for want of a better idea ...
        setLocation((int)parent.getLocation().getX()+parent.getWidth()/2-(int)getSize().getWidth()/2,
                (int)parent.getLocation().getY()+parent.getHeight()/2-(int)getSize().getHeight()/2);
        
        // Set ESCAPE key to close dialog
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction(){
            public void actionPerformed(ActionEvent e){
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE",escapeAction);  
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getRootPane().setDefaultButton(printButton);                
        setVisible(true);                  
    }

    /** Finds whether the display component supports changing a field */
    private boolean isMutator(Object obj, String method) {
        Class c = obj.getClass();        
        Method[] m=c.getMethods();
        for (int i=0;i<m.length;i++) {
            if(m[i].toString().contains(method))
                return true;
        }
        return false;                
    }
    
    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("print")) {
            // Do Print
            job.setPrintable(displayComponent,pageFormat);
            
            if (job.printDialog()) {
                try {                    
                    job.print();
                } catch (PrinterException pe) {                
                    JOptionPane.showMessageDialog((this),
                        Mvplan.getResource("mvplan.gui.TablePreviewDialog.error.text"),
                        Mvplan.getResource("mvplan.gui.TablePreviewDialog.error.title"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }  
            dispose();
        } else if(e.getActionCommand().equals("setup")) {
            pageFormat = job.pageDialog(pageFormat);
            profilePanel.setPreferredSize(new Dimension((int)pageFormat.getWidth(),(int)pageFormat.getHeight()));
            profilePanel.setSize(new Dimension((int)pageFormat.getWidth(),(int)pageFormat.getHeight()));
            setInsets();           
        }  else if(e.getActionCommand().equals("showColour")) {                
            Mvplan.prefs.setPrintColour(showColourCheckBox.isSelected()); 
            displayComponent.repaint();    
        }  else if(e.getActionCommand().equals("fontSize")) {     
            int i = ((Integer)printFontSizeCombo.getSelectedItem()).intValue();
            Mvplan.prefs.setPrintFontBodySize(i);
            Mvplan.prefs.setPrintFontHeaderSize(i-1);            
            displayComponent.repaint();   
        }  else if(e.getActionCommand().equals("showSeconds")) {            
            Mvplan.prefs.setShowSeconds(showSecondsCheckBox.isSelected());
            displayComponent.repaint();              
        }  else if(e.getActionCommand().equals("stopTime")) {            
            Mvplan.prefs.setShowStopTime(stopTimeCheckBox.isSelected());
            displayComponent.repaint(); 
        } else if(e.getActionCommand().equals("gasFirst")) {            
            Mvplan.prefs.setShowGasFirst(gasFirstCheckBox.isSelected());
            displayComponent.repaint();  
        } else if(e.getActionCommand().equals("colour")) {
            Color ch = JColorChooser.showDialog(this,Mvplan.getResource("mvplan.gui.TablePreviewDialog.colourChooser.title"), 
                        Mvplan.prefs.getBackgroundColour());
            if (ch != null) {
                Mvplan.prefs.setBackgroundColour(ch);
                colourButton.setBackground(ch);
                displayComponent.repaint();                 
            }
        } else {          
            // Do cancel 
            ((MainFrame)parent).setPageFormat(pageFormat);
            dispose();
        }       
    }
    /** Redraws preview with insets created from page imageable size */
    private void setInsets() {
        displayComponentInsets.set((int)pageFormat.getImageableY(),(int)pageFormat.getImageableX(),(int)pageFormat.getImageableY(),(int)pageFormat.getImageableX());
        // This works but is there a better way ?
        profilePanel.remove(displayComponent);
        profilePanel.add(displayComponent,
           new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE, displayComponentInsets,0,0));    
        profilePanel.revalidate();
    }
    
    /* Private class to implement a simple coloured button. Needed because OSX L&F does not
     * allow setting of the background colour
     */
    private class ColourButton extends JButton {
        public ColourButton() {
            super();
        }        
        // Override paintComponent()
        public void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D)g;
            // Fill background
            g2.setColor(this.getBackground());
            g2.fillRect(1,1,this.getWidth()-2,this.getHeight()-2);  
            // Draw border
            g2.setColor(getForeground());
            g2.drawRect(0,0,this.getWidth()-1,this.getHeight()-1);
        }
    }
}
