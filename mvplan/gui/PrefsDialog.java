
/*
 * PrefsDialog.java
 *
 * Displays Preferences Dialog.
 *
 * @author Guy Wittig
 * @version 28-Jul-2006
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

import mvplan.main.Mvplan;
import mvplan.prefs.Prefs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.math.BigDecimal;
import mvplan.util.PressureConverter;

public class PrefsDialog extends JDialog implements ActionListener, FocusListener
{
    /* Define some constants for error checking.      
     * Critical values are obtained from the Prefs object.
     * Less critical so are defined here to rule out stupid inputs.
     */

    final static double STMIN=0.1;  // Stop times
    final static double STMAX=5.0;
    final static double RMVMIN=0.0; // RMVs TODO - Imperial
    final static double RMVMAX=50.0;
    final static double CUFT = 28.3;    // Litres per CuFt
    
    // Set up the GUI
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();
    JPanel mainPanel = new JPanel();
    JPanel gfPanel = new JPanel();
    JPanel stopPanel = new JPanel();   
    JPanel divePanel = new JPanel();
    JPanel optionsPanel = new JPanel();
    JPanel outputPanel = new JPanel();
    JPanel gasPanel = new JPanel();     // For RMV values
    JPanel unitsPanel = new JPanel();    
    JTextField gfLow = new JTextField();
    JTextField gfHigh = new JTextField();
    JTextField stopDepthIncrement = new JTextField();
    JTextField lastStopDepth = new JTextField();
    JTextField stopTimeIncrement = new JTextField();
    JTextField diveRMV = new JTextField();
    JTextField decoRMV = new JTextField();
    JTextField ascentRate = new JTextField();
    JTextField descentRate = new JTextField();
    JTextField altitude = new JTextField();
    JTextField message = new JTextField();    
    JCheckBox runTimeCB = new JCheckBox();
    JCheckBox forceStopsCB = new JCheckBox();
    JCheckBox extendedOutputCB = new JCheckBox();
    JCheckBox mvMultilevelModeCB = new JCheckBox();
    JRadioButton metricButton, imperialButton;
    // TODO use reflection ?
    JComboBox cmbModel = new JComboBox(new String[] {"ZHL16B","ZHL16C"});

    int outputStyle;    // For prefs checkbox
    int currentUnits;   // What units are we displaying ?
    double stopDepthMax;
    double stopDepthMin;
    double descentRateMax;
    double descentRateMin;
    double ascentRateMax;
    double ascentRateMin;
    
    // Constructor for Prefs dialog
    /**
     * Constructor for Prefs Dialog
     * @param frame Parent frame
     */
    public PrefsDialog(Frame frame)
    {
        super(frame,true);
        this.setTitle(Mvplan.getResource("mvplan.gui.PrefsDialog.title"));
        Container cont = getContentPane();        
        currentUnits = Mvplan.prefs.METRIC;     // Metric unless otherwise advised 
        
        // Get limits for input verifiers
        stopDepthMax = Mvplan.prefs.getStopDepthMax();
        stopDepthMin = Mvplan.prefs.getStopDepthMin();
        ascentRateMax = -Mvplan.prefs.getAscentRateMax();   // Note sign
        ascentRateMin = -Mvplan.prefs.getAscentRateMin();  
        descentRateMax = Mvplan.prefs.getDescentRateMax();
        descentRateMin = Mvplan.prefs.getDescentRateMin();  
        
        cont.add(mainPanel);
        
        // GF Panel
        Border gfBorder = BorderFactory.createEtchedBorder();
        gfPanel.setBorder(BorderFactory.createTitledBorder(gfBorder,Mvplan.getResource("mvplan.gui.PrefsDialog.GfBorder.text")));
        gfPanel.setLayout(new GridLayout(2,2));
        gfLow.setColumns(5);
        gfHigh.setColumns(5);
        gfLow.setText(String.valueOf((int)(Mvplan.prefs.getGfLow()*100.)));
        gfLow.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.gfLow.tip"));
        gfLow.addFocusListener(this);
        gfLow.setInputVerifier(new GfVerifier());
        gfHigh.setText(String.valueOf((int)(Mvplan.prefs.getGfHigh()*100.)));
        gfHigh.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.gfHigh.tip"));
        gfHigh.addFocusListener(this);
        gfHigh.setInputVerifier(new GfVerifier());        
        gfPanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.gfLowLabel.text")));
        gfPanel.add(gfLow);
        gfPanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.gfHighLabel.text")));
        gfPanel.add(gfHigh);
        gfPanel.setPreferredSize(new Dimension(150,80));

        // Stop Panel
        Border stopBorder = BorderFactory.createEtchedBorder();
        stopPanel.setBorder(BorderFactory.createTitledBorder(stopBorder,
                Mvplan.getResource("mvplan.gui.PrefsDialog.stopBorder.text")));
        stopPanel.setLayout(new GridLayout(4,2));
        stopDepthIncrement.setColumns(5);
        lastStopDepth.setColumns(5);
        stopTimeIncrement.setColumns(5);
        stopDepthIncrement.setText(String.valueOf((int)Mvplan.prefs.getStopDepthIncrement()));
        stopDepthIncrement.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.stopDepthIncrement.tip"));
        stopDepthIncrement.addFocusListener(this);
        stopDepthIncrement.setInputVerifier(new SdVerifier()); 
        lastStopDepth.setText(String.valueOf(Mvplan.prefs.getLastStopDepth()));
        lastStopDepth.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.lastStopDepth.tip"));
        lastStopDepth.addFocusListener(this);
        lastStopDepth.setInputVerifier(new SdVerifier());
        stopTimeIncrement.setText(String.valueOf(Mvplan.prefs.getStopTimeIncrement()));
        stopTimeIncrement.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.stopTimeIncrement.tip"));
        stopTimeIncrement.addFocusListener(this);
        stopTimeIncrement.setInputVerifier(new StVerifier());
        stopPanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.depthLabel.text")));
        stopPanel.add(stopDepthIncrement);
        stopPanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.lastLabel.text")));
        stopPanel.add(lastStopDepth);
        stopPanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.timeLabel.text")));
        stopPanel.add(stopTimeIncrement);
        stopPanel.setPreferredSize(new Dimension(150,100));

        // Dive Panel
        Border diveBorder = BorderFactory.createEtchedBorder();
        divePanel.setBorder(BorderFactory.createTitledBorder(diveBorder,Mvplan.getResource("mvplan.gui.PrefsDialog.diveBorder.text")));
        divePanel.setLayout(new GridLayout(4,2));
        ascentRate.setColumns(5);
        ascentRate.setText(String.valueOf(-Mvplan.prefs.getAscentRate()));
        ascentRate.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.ascentRate.tip"));
        ascentRate.addFocusListener(this);    
        ascentRate.setInputVerifier(new RateVerifier());
        ascentRate.setName("ascentRate");   // For verifier to identify component
        descentRate.setColumns(5); 
        descentRate.setText(String.valueOf(Mvplan.prefs.getDescentRate()));
        descentRate.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.descentRate.tip"));          
        descentRate.addFocusListener(this);
        descentRate.setInputVerifier(new RateVerifier()); 
        descentRate.setName("descentRate");   // For verifier to identify component
        altitude.setColumns(5);
        altitude.setText(String.valueOf(Mvplan.prefs.getAltitude()));
        altitude.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.altitude.tip"));
        altitude.addFocusListener(this);
        altitude.setName("altitude");
        altitude.setInputVerifier(new AltitudeVerifier());   
        divePanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.ascentLabel.text")));
        divePanel.add(ascentRate);        
        divePanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.descentLabel.text")));        
        divePanel.add(descentRate);
        divePanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.altitudeLabel.text")));        
        divePanel.add(altitude);
        divePanel.setPreferredSize(new Dimension(150,100));        
        
        // Options Panel
        Border optionsBorder = BorderFactory.createEtchedBorder();
        optionsPanel.setBorder(BorderFactory.createTitledBorder(optionsBorder,Mvplan.getResource("mvplan.gui.PrefsDialog.prefsBorder.text")));
        optionsPanel.setLayout(new GridLayout(4,1));
        forceStopsCB.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.forceStopsCB.text"));
        forceStopsCB.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.forceStopsCB.tip"));
        forceStopsCB.setSelected(Mvplan.prefs.getForceAllStops());
        forceStopsCB.setEnabled(false);
        runTimeCB.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.runTimeCB.text"));
        runTimeCB.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.runTimeCB.tip"));
        runTimeCB.setSelected(Mvplan.prefs.getRuntimeFlag());
        extendedOutputCB.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.extendedOutputCB.tip"));
        extendedOutputCB.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.extendedOutputCB.text"));
        if (Mvplan.prefs.getOutputStyle()==Prefs.BRIEF)
            extendedOutputCB.setSelected(false);
        else
            extendedOutputCB.setSelected(true);   
        mvMultilevelModeCB.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.mvMultilevelModeCB.text"));
        mvMultilevelModeCB.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.mvMultilevelModeCB.tip"));
        mvMultilevelModeCB.setSelected(Mvplan.prefs.getGfMultilevelMode());


        cmbModel.setSelectedItem(Mvplan.prefs.getModelClassName());
        cmbModel.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.cmbModel.tip"));


        //optionsPanel.add(forceStopsCB);
        optionsPanel.add(runTimeCB);
        optionsPanel.add(extendedOutputCB);
        optionsPanel.add(mvMultilevelModeCB);
        optionsPanel.add(cmbModel);
        optionsPanel.setPreferredSize(new Dimension(150,140));

        // Gas Panel
        Border gasBorder = BorderFactory.createEtchedBorder();
        gasPanel.setBorder(BorderFactory.createTitledBorder(gasBorder,Mvplan.getResource("mvplan.gui.PrefsDialog.gasBorder.text")));
        gasPanel.setLayout(new GridLayout(2,2));
        diveRMV.setColumns(5);
        decoRMV.setColumns(5);
        diveRMV.setText(String.valueOf(Mvplan.prefs.getDiveRMV()));
        diveRMV.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.diveRMV.tip"));
        diveRMV.addFocusListener(this);
        diveRMV.setInputVerifier(new RmvVerifier());
        decoRMV.setText(String.valueOf(Mvplan.prefs.getDecoRMV()));
        decoRMV.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.decoRMV.tip"));
        decoRMV.addFocusListener(this);
        decoRMV.setInputVerifier(new RmvVerifier());        
        gasPanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.diveRMV.text")));
        gasPanel.add(diveRMV);
        gasPanel.add(new JLabel(Mvplan.getResource("mvplan.gui.PrefsDialog.decoRMV.text")));
        gasPanel.add(decoRMV);
        gasPanel.setPreferredSize(new Dimension(150,80));
        
        // Units panel        
        Border unitsBorder = BorderFactory.createEtchedBorder();
        unitsPanel.setBorder(BorderFactory.createTitledBorder(unitsBorder,Mvplan.getResource("mvplan.gui.PrefsDialog.unitsBorder.text")));
        unitsPanel.setLayout(new GridLayout(2,1));
        metricButton = new JRadioButton(Mvplan.getResource("mvplan.gui.PrefsDialog.metricButton.text"));
        imperialButton = new JRadioButton(Mvplan.getResource("mvplan.gui.PrefsDialog.imperialButton.text"));
        ButtonGroup unitsButtons = new ButtonGroup();
        unitsButtons.add(metricButton);
        unitsButtons.add(imperialButton);
        unitsPanel.add(metricButton);
        unitsPanel.add(imperialButton);
        metricButton.setActionCommand("metric");
        metricButton.addActionListener(this);
        metricButton.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.metricButton.tip"));
        imperialButton.setActionCommand("imperial");
        imperialButton.addActionListener(this);
        imperialButton.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.imperialButton.tip"));        
        
        if(Mvplan.prefs.getUnits()==Mvplan.prefs.METRIC) {
            metricButton.setSelected(true); 
            currentUnits = Mvplan.prefs.METRIC;            
        } else { 
            imperialButton.setSelected(true);  
            currentUnits = Mvplan.prefs.IMPERIAL;
        }
                
        // Button Panel
        buttonPanel.setLayout(new GridBagLayout());
        message.setColumns(50);
        message.setEditable(false);
        message.setBorder(null);
        message.setForeground(Color.RED);  
        message.setBackground(this.getBackground());
        okButton.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.okButton.text"));
        okButton.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.okButton.tip"));
        okButton.setActionCommand("ok");
        cancelButton.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.cancelButton.text"));
        cancelButton.setToolTipText(Mvplan.getResource("mvplan.gui.PrefsDialog.cancelButton.tip"));
        cancelButton.setActionCommand("cancel");
        okButton.setPreferredSize(new Dimension(80,25));
        cancelButton.setPreferredSize(new Dimension(80,25));
        buttonPanel.add(message, new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0,0,0,0),0,0));
        buttonPanel.add(okButton, new GridBagConstraints(1,0,1,1,0.1,1.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,10,0,10),0,0));
        buttonPanel.add(cancelButton, new GridBagConstraints(2,0,1,1,0.1,1.0,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0,10,0,10),0,0));
        buttonPanel.setPreferredSize(new Dimension(200,25));
        cancelButton.addActionListener(this);
        okButton.addActionListener(this);

        mainPanel.setLayout(new GridBagLayout());
        mainPanel.add(gfPanel,
            new GridBagConstraints(0,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, 
            new Insets(10,10,10,10),0,0));  
        mainPanel.add(gasPanel,
            new GridBagConstraints(1,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, 
            new Insets(10,10,10,10),0,0));   
        //mainPanel.add(printPanel,
            //new GridBagConstraints(2,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, 
            //new Insets(10,10,10,10),0,0));         
        mainPanel.add(stopPanel,
            new GridBagConstraints(0,1,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, 
            new Insets(10,10,10,10),0,0)); 
        mainPanel.add(divePanel,
            new GridBagConstraints(1,1,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, 
            new Insets(10,10,10,10),0,0));  
         mainPanel.add(unitsPanel,
            new GridBagConstraints(2,0,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, 
            new Insets(10,10,10,10),0,0));          
        mainPanel.add(optionsPanel,
            new GridBagConstraints(2,1,1,1,1.0,1.0,GridBagConstraints.NORTH,GridBagConstraints.BOTH, 
            new Insets(10,10,10,10),0,0)); 

        mainPanel.add(buttonPanel,
            new GridBagConstraints(0,2,3,1,1.0,0.1,GridBagConstraints.CENTER,GridBagConstraints.BOTH, 
            new Insets(10,10,10,10),0,0)); 

        // Set up keystrokes to fire actions
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        KeyStroke limits = KeyStroke.getKeyStroke(KeyEvent.VK_L,InputEvent.CTRL_MASK);
        Action escapeAction = new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    dispose();
                }
        };
        Action limitsAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setLimits();               
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE",escapeAction);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(limits, "LIMITS");
        getRootPane().getActionMap().put("LIMITS",limitsAction);
        
        setResizable(false);
        pack();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(frame);
        getRootPane().setDefaultButton(okButton);
        setVisible(true);

    }

    /**
     * Alters dive limits for depth, gradient factors, segment times. Allows the enabling
     * of "Expedition Mode" where these limits are extended. This function is not advertised.
     */
    private void setLimits(){
        if(Mvplan.prefs.getExtendedLimits()) {
            if(JOptionPane.showConfirmDialog(this,Mvplan.getResource("mvplan.gui.PrefDialog.LimitsDialog.clear.text"),
                    Mvplan.getResource("mvplan.gui.PrefDialog.LimitsDialog.title"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION)
                Mvplan.prefs.setExtendedLimits(false);
        } else {
            if(JOptionPane.showConfirmDialog(this,
                    Mvplan.getResource("mvplan.gui.PrefDialog.LimitsDialog.confirm.text"),
                    Mvplan.getResource("mvplan.gui.PrefDialog.LimitsDialog.title"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)== JOptionPane.YES_OPTION)
                Mvplan.prefs.setExtendedLimits(true);
        }                    
    }
    
    /**
     * Handles focus gained events. Selects all text to make it easier to edit.
     * @param e Focus gained event
     */
    public void focusGained(FocusEvent e) {
        JTextField tf = (JTextField)(e.getComponent());
        tf.selectAll();        
    }
    
    /**
     * Handles focus lost events. 
     * Required for interface. Does nothing
     * @param e Event   
     */
    public void focusLost(FocusEvent e) {
    }

    /**
     * Handle clicks on the Set and Cancel buttons.
     * Performs field validation using the input verifiers where possible, 
     * and saves the results in the Prefs object.
     * @param e Action Performed event.
     */
    public void actionPerformed(ActionEvent e) {
        boolean inputError=false;
        double d;
        int i;
        
        if ("ok".equals(e.getActionCommand())) {            
            // Store preferences back into the Prefs object
            try {
                // Units 
                // TODO - Only if changed ?
                if(metricButton.isSelected())
                    Mvplan.prefs.setUnitsTo(Mvplan.prefs.METRIC);
                else    
                    Mvplan.prefs.setUnitsTo(Mvplan.prefs.IMPERIAL);   
                
                // Use input verifier to check the GF fields
                if( new GfVerifier().verify(gfLow)== true )
                    Mvplan.prefs.setGfLow((double)(Integer.parseInt(gfLow.getText()))/100.0);
                else
                    inputError=true;                                  
                 if( new GfVerifier().verify(gfHigh)== true )
                    Mvplan.prefs.setGfHigh((double)(Integer.parseInt(gfHigh.getText()))/100.0);
                else 
                    inputError=true;               
                
                // Use input verifier to check stop depths
                if( new SdVerifier().verify(stopDepthIncrement)== true )
                    // Force this to be an integer
                    Mvplan.prefs.setStopDepthIncrement( (double)( Integer.parseInt(stopDepthIncrement.getText())));
                else
                    inputError=true;
                if( new SdVerifier().verify(lastStopDepth)== true )
                    Mvplan.prefs.setLastStopDepth( Double.parseDouble(lastStopDepth.getText()));
                else
                    inputError=true;
                
                // Use input verifier to check stop time
                if( new StVerifier().verify(stopTimeIncrement)== true )
                    Mvplan.prefs.setStopTimeIncrement( Double.parseDouble(stopTimeIncrement.getText()));
                else
                    inputError=true;       
                
                // Use input verifier to check ascent/descent rate. 
                if( new RateVerifier().verify(ascentRate)== true )
                    Mvplan.prefs.setAscentRate( -Double.parseDouble(ascentRate.getText())); // Note sign
                else
                    inputError=true;
                
                if( new RateVerifier().verify(descentRate)== true )
                    Mvplan.prefs.setDescentRate( Double.parseDouble(descentRate.getText()));
                else
                    inputError=true;    

                // Use input verifier to check RMV
                if( new RmvVerifier().verify(diveRMV)== true )
                    Mvplan.prefs.setDiveRMV( Double.parseDouble(diveRMV.getText()));
                else
                    inputError=true;    
                if( new RmvVerifier().verify(decoRMV)== true )
                    Mvplan.prefs.setDecoRMV( Double.parseDouble(decoRMV.getText()));
                else
                    inputError=true;   
                
                // Check altitude
                if( new AltitudeVerifier().verify(altitude)== true) {
                    d = Double.parseDouble(altitude.getText());
                    // If necessary display warning message regarding depth gauge calibration
                    if (d>0 && Mvplan.prefs.getAltitude()==0.0) {                    
                        JOptionPane.showMessageDialog((this),
                            Mvplan.getResource("mvplan.gui.PrefsDialog.altitudeWarning.text"),
                            Mvplan.getResource("mvplan.gui.PrefsDialog.altitudeWarning.title"),
                            JOptionPane.WARNING_MESSAGE);     
                    }
                    Mvplan.prefs.setAltitude(d);  
                } else 
                    inputError=true;    
                   
                // Store all the check box preferences
                Mvplan.prefs.setRuntimeFlag(runTimeCB.isSelected());
                Mvplan.prefs.setForceAllStops(forceStopsCB.isSelected());                
                if (extendedOutputCB.isSelected())
                    Mvplan.prefs.setOutputStyle(Prefs.EXTENDED);
                else
                    Mvplan.prefs.setOutputStyle(Prefs.BRIEF);
                Mvplan.prefs.setGfMultilevelMode(mvMultilevelModeCB.isSelected());
                Mvplan.prefs.setModelClassName(cmbModel.getSelectedItem().toString());
                
            } catch (NumberFormatException ex) {
                inputError=true;                
            }
            
            if (!inputError) {
                    Mvplan.prefs.validatePrefs();                
                    dispose();
            }
            
        } else if("metric".equals(e.getActionCommand())) {            
            if(currentUnits != Mvplan.prefs.METRIC) {
                // Convert units to metric
                // Field validation to  metric
                stopDepthMax = stopDepthMax / Mvplan.prefs.METERS_TO_FEET;
                stopDepthMin = stopDepthMin / Mvplan.prefs.METERS_TO_FEET;
                ascentRateMax = ascentRateMax / Mvplan.prefs.METERS_TO_FEET;
                ascentRateMin = ascentRateMin / Mvplan.prefs.METERS_TO_FEET; 
                descentRateMax = descentRateMax / Mvplan.prefs.METERS_TO_FEET;
                descentRateMin = descentRateMin / Mvplan.prefs.METERS_TO_FEET;   
                try {
                    stopDepthIncrement.setText(String.valueOf(  (int)roundDouble(0,  ( Double.parseDouble(stopDepthIncrement.getText()) / Mvplan.prefs.METERS_TO_FEET+0.01)) )) ;
                    lastStopDepth.setText(String.valueOf(  roundDouble( 0, Double.parseDouble(lastStopDepth.getText()) / Mvplan.prefs.METERS_TO_FEET) )) ; 
                    descentRate.setText(String.valueOf(  roundDouble( 0,  Double.parseDouble(descentRate.getText()) / Mvplan.prefs.METERS_TO_FEET) )) ;
                    ascentRate.setText(String.valueOf(  roundDouble( 0,  Double.parseDouble(ascentRate.getText()) / Mvplan.prefs.METERS_TO_FEET) )) ;
                    diveRMV.setText(String.valueOf(  roundDouble( 0,  Double.parseDouble(diveRMV.getText()) * CUFT) ) );
                    decoRMV.setText(String.valueOf(  roundDouble( 0,  Double.parseDouble(decoRMV.getText()) * CUFT) ) ) ;
                    altitude.setText(String.valueOf(  roundDouble( 0,  Double.parseDouble(altitude.getText()) / Mvplan.prefs.METERS_TO_FEET) )) ; 
                } catch (NumberFormatException ex) {
                    // No action
                }
                currentUnits = Mvplan.prefs.METRIC;
            }
        } else if("imperial".equals(e.getActionCommand())) {
            if(currentUnits != Mvplan.prefs.IMPERIAL) {
                // Convert units to imperial
                // Field validation to  imperial
                stopDepthMax = stopDepthMax * Mvplan.prefs.METERS_TO_FEET;
                stopDepthMin = stopDepthMin * Mvplan.prefs.METERS_TO_FEET;
                ascentRateMax = ascentRateMax * Mvplan.prefs.METERS_TO_FEET;
                ascentRateMin = ascentRateMin * Mvplan.prefs.METERS_TO_FEET; 
                descentRateMax = descentRateMax * Mvplan.prefs.METERS_TO_FEET;
                descentRateMin = descentRateMin * Mvplan.prefs.METERS_TO_FEET;     
                try {
                    stopDepthIncrement.setText(String.valueOf(  (int)roundDouble(0, ( Double.parseDouble(stopDepthIncrement.getText()) * Mvplan.prefs.METERS_TO_FEET+0.01)) )) ;
                    lastStopDepth.setText(String.valueOf(  roundDouble(0,   Double.parseDouble(lastStopDepth.getText()) * Mvplan.prefs.METERS_TO_FEET) )) ;
                    descentRate.setText(String.valueOf(  roundDouble(0,   Double.parseDouble(descentRate.getText()) * Mvplan.prefs.METERS_TO_FEET) )) ;
                    ascentRate.setText(String.valueOf(  roundDouble(0,   Double.parseDouble(ascentRate.getText()) * Mvplan.prefs.METERS_TO_FEET) )) ;
                    diveRMV.setText(String.valueOf(  roundDouble( 2, Double.parseDouble(diveRMV.getText()) / CUFT) )) ;
                    decoRMV.setText(String.valueOf(  roundDouble( 2 ,Double.parseDouble(decoRMV.getText()) / CUFT) )) ;
                    altitude.setText(String.valueOf(  roundDouble( 0,  Double.parseDouble(altitude.getText()) * Mvplan.prefs.METERS_TO_FEET) )) ;
                } catch (NumberFormatException ex) {
                    // No action
                }
                currentUnits = Mvplan.prefs.IMPERIAL;
            }            
            
        } else if("cancel".equals(e.getActionCommand())) {
            dispose();
        }
           
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
    
    ////////////// INPUT VERIFIER HELPER CLASSES ////////////////////
    
     /*
     * Input verifier for descentRates
     */
    class RateVerifier extends InputVerifier {
        public boolean verify (JComponent comp) 
        {
            // TODO: If used for Ascent and Descent need to check field type !
            JTextField field = (JTextField)comp;            
            double min,max; // Rates
            if(comp.getName().equals("ascentRate")) {
                min=ascentRateMin;
                max=ascentRateMax;
            } else {
                min=descentRateMin;
                max=descentRateMax;
            }
                
            boolean passed=false;
            try {
                // Check specified field and control field focus
                double d=Double.parseDouble(field.getText());
                passed = (d>=min && d <=max);
                message.setText("");
            } catch (NumberFormatException e) {}
            if (! passed) {
                comp.getToolkit().beep();
                message.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.Verifier.errorMessage.text")+
                        " "+min+" - "+max);
                field.selectAll();
            }
            
            return passed;
        }
    }   
    
    /*
     * Input verifier for altitude 
     */
    class AltitudeVerifier extends InputVerifier {
        public boolean verify (JComponent comp) 
        {
            JTextField field = (JTextField)comp;
            double altitudeMax= Mvplan.prefs.ALTITUDE_MAX;            
            boolean passed=false;
            
            // Modify maximum for imperial if required
            if(currentUnits==Mvplan.prefs.IMPERIAL) altitudeMax=altitudeMax*Mvplan.prefs.METERS_TO_FEET;
            //System.out.println("Altitude verifier");
            try {
                // Check specified field and control field focus
                double d=Double.parseDouble(field.getText());
                passed= (d>=0.0 && d <=altitudeMax);
                message.setText("");
            } catch (NumberFormatException e) {}
            if (! passed) {
                comp.getToolkit().beep();
                message.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.Verifier.errorMessage.text")+
                        " "+0+" - "+altitudeMax);
                field.selectAll();
            }
            return passed;
        }
    }   
    
    /*
     * Input verifier for stop time
     */
    class StVerifier extends InputVerifier {
        public boolean verify (JComponent comp) 
        {
            JTextField field = (JTextField)comp;
            boolean passed=false;
            try {
                // Check specified field and control field focus
                double d=Double.parseDouble(field.getText());
                passed= (d>=STMIN && d <=STMAX);
                message.setText("");
            } catch (NumberFormatException e) {}
            if (! passed) {
                comp.getToolkit().beep();
                 message.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.Verifier.errorMessage.text")+
                        " "+STMIN+" - "+STMAX);
                field.selectAll();
            }
            return passed;
        }
    }    
    
     /*
     * Input verifier for RMV
     */
    class RmvVerifier extends InputVerifier {
        public boolean verify (JComponent comp) 
        {
            JTextField field = (JTextField)comp;
            boolean passed=false;
            try {
                // Check specified field and control field focus
                double d=Double.parseDouble(field.getText());
                passed= (d>=RMVMIN && d <=RMVMAX);
                message.setText("");
            } catch (NumberFormatException e) {}
            if (! passed) {
                comp.getToolkit().beep();
                message.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.Verifier.errorMessage.text")+
                        " "+RMVMIN+" - "+RMVMAX);
                field.selectAll();
            }
            return passed;
        }
    }
    /*
     * Input verifier for stop depths
     */
    class SdVerifier extends InputVerifier {
        public boolean verify (JComponent comp) 
        {
            JTextField field = (JTextField)comp;
            boolean passed=false;
            try {                
                if (field==stopDepthIncrement) {
                    // Check Stop Depth Increment as int. Accept as double and strip decimal.   
                    double d=Double.parseDouble(field.getText());
                    int i=(int)d;
                    field.setText(String.valueOf(i));
                    passed= (i>=stopDepthMin && i <=stopDepthMax);                    
                } else {               
                    // Check other field as double
                    double d=Double.parseDouble(field.getText());
                    passed= (d>=stopDepthMin && d <=stopDepthMax);
                }
                message.setText("");
            } catch (NumberFormatException e) {}
            if (! passed) {
                comp.getToolkit().beep();
                message.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.Verifier.errorMessage.text")+
                        " "+stopDepthMin+" - "+stopDepthMax);
                field.selectAll();
            }
            return passed;
        }
    }
    /*
     * Input verifier for gradient factors
     */
    class GfVerifier extends InputVerifier {
        public boolean verify (JComponent comp) 
        {
            JTextField field = (JTextField)comp;
            boolean passed=false;
            try {
                // Check specified field and control field focus
                int n=Integer.parseInt(field.getText());
                passed= (n>=Mvplan.prefs.getGfMin()*100 && n <=Mvplan.prefs.getGfMax()*100);
                message.setText("");
            } catch (NumberFormatException e) {}
            if (! passed) {
                comp.getToolkit().beep();
                message.setText(Mvplan.getResource("mvplan.gui.PrefsDialog.Verifier.errorMessage.text")+
                        " "+(int)(Mvplan.prefs.getGfMin()*100)+" - "+(int)(Mvplan.prefs.getGfMax()*100));
                field.selectAll();
            }
            return passed;
        }
    }
}
