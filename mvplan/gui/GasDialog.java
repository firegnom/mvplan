/*
 * GasDialog.java
 *
 * Displays the Gas dialog. 
 * Permits editing a gas object (fHe, fO2, MOD). Validates fields
 * to ensure that fHe + fO2 sum to 0-100%, and that MOD is within limits. 
 * 
 * @author Guy Wittig
 * @version 19-Mar-2003
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
import java.math.BigDecimal;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.lang.Math.*;
import mvplan.gas.Gas;
import mvplan.main.*;

public class GasDialog extends JDialog
                        implements ActionListener, FocusListener, ChangeListener
{
    int he;
    int o2;
    int mod;
    double maxMOD;
    double sliderMOD;
    
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();
    JPanel mainPanel = new JPanel();
    JPanel fieldPanel = new JPanel();
    JPanel buttonPanel = new JPanel();
    JPanel sliderPanel = new JPanel();
    JTextField inputHe = new JTextField(5);
    JTextField inputO2 = new JTextField(5);
    JTextField inputMod = new JTextField(5);
    Gas gas;
    JLabel labelHe = new JLabel();
    JLabel labelO2 = new JLabel();
    JLabel labelMod = new JLabel();  
    JLabel labelPpO2 = new JLabel(); 
    
    JSlider ppO2Slider = new JSlider(JSlider.HORIZONTAL);
    
    public GasDialog(Frame frame, Gas g)
    {
        // Create the diaog
        super(frame,true);
        Container cont = getContentPane();      // Get container
        gas=g;                                  // Need this for method scope        
        maxMOD = Mvplan.prefs.getMaxMOD();      // Get max pO2 
        
        if(gas.getMod()>0.0) 
            sliderMOD=Gas.getppO2(gas.getFO2(),gas.getMod());
        else
            sliderMOD=maxMOD;
        
        // Create components      
        this.setTitle( Mvplan.getResource("mvplan.gui.GasDialog.title") );
        labelHe.setText(Mvplan.getResource("mvplan.helium.shortText")+ "%");
        labelO2.setText(Mvplan.getResource("mvplan.oxygen.shortText")+ "%");
        labelMod.setText(Mvplan.getResource("mvplan.mod.shortText"));
        okButton.setText(Mvplan.getResource("mvplan.gui.GasDialog.okButton.text"));        
        cancelButton.setText(Mvplan.getResource("mvplan.gui.GasDialog.cancelButton.text"));
        labelPpO2.setText(Mvplan.getResource("mvplan.ppO2.shortText"));
        labelPpO2.setLabelFor(ppO2Slider);
        

        labelHe.setHorizontalAlignment(SwingConstants.RIGHT);
        labelHe.setLabelFor(inputHe);
        labelO2.setHorizontalAlignment(SwingConstants.RIGHT);
        labelO2.setLabelFor(inputO2);
        labelMod.setHorizontalAlignment(SwingConstants.RIGHT);
        labelMod.setLabelFor(inputMod);
        labelPpO2.setHorizontalAlignment(SwingConstants.CENTER);
        okButton.setActionCommand("ok");
        okButton.addActionListener(this);
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("cancel");

        fieldPanel.setLayout(new GridLayout(3,2));
        fieldPanel.add(labelHe);
        fieldPanel.add(inputHe);
        fieldPanel.add(labelO2);
        fieldPanel.add(inputO2);
        fieldPanel.add(labelMod);
        fieldPanel.add(inputMod);
        fieldPanel.setPreferredSize(new Dimension(80,100));
       
       
        //sliderPanel
        sliderPanel.setLayout(new GridLayout(2,1));
        sliderPanel.add(labelPpO2);
        sliderPanel.add(ppO2Slider);
        sliderPanel.setPreferredSize(new Dimension(200,80));  
        ppO2Slider.setMajorTickSpacing(20);
        ppO2Slider.setMinorTickSpacing(5);
        ppO2Slider.setPaintTicks(true);
        ppO2Slider.setPaintLabels(true);
        ppO2Slider.setSnapToTicks(true);
        ppO2Slider.setMinimum(80);
        ppO2Slider.setMaximum((int)Math.round(maxMOD*100) );
        ppO2Slider.setValue((int)Math.round(sliderMOD*100));
        
        Hashtable sliderLabels = new Hashtable();
        // Use the string formatter to ensure labels are localised.
        sliderLabels.put( new Integer(80), new JLabel( String.format("%1$3.1f",0.8)));
        sliderLabels.put( new Integer(120), new JLabel(String.format("%1$3.1f",1.2)));
        sliderLabels.put( new Integer(100), new JLabel(String.format("%1$3.1f",1.0)));
        sliderLabels.put( new Integer(140), new JLabel(String.format("%1$3.1f",1.4)));
        sliderLabels.put( new Integer(160), new JLabel(String.format("%1$3.1f",1.6)));
        ppO2Slider.setLabelTable(sliderLabels);
        ppO2Slider.setToolTipText(Mvplan.getResource("mvplan.gui.GasDialog.ppO2slider.tip"));
        
       
        buttonPanel.setLayout(new GridLayout(1,2,10,0)); // TODO - layout that gives uniform size ?
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.add(fieldPanel,new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.NORTH,GridBagConstraints.NONE,new Insets(10,10,0,10),0,0));
        mainPanel.add(sliderPanel,new GridBagConstraints(0,1,1,1,1,0,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(0,10,0,10),0,0));
        mainPanel.add(buttonPanel,new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.SOUTH,GridBagConstraints.HORIZONTAL,new Insets(10,10,10,10),0,0));
        cont.add(mainPanel);
        
        // Add listeners
        inputHe.addFocusListener(this);
        inputO2.addFocusListener(this);
        inputMod.addFocusListener(this);
        ppO2Slider.addChangeListener(this);
        
        
        // Set input verifiers
        inputHe.setInputVerifier( new GasVerifier() );
        inputO2.setInputVerifier( new GasVerifier() );
        inputMod.setInputVerifier( new GasVerifier() );
        
        // Get existing settings. May be zero if this is a new gas.
        he=(int)Math.round(gas.getFHe()*100.0);
        o2=(int)Math.round(gas.getFO2()*100.0);
        mod=(int)Math.round(gas.getMod());

        inputHe.setText(String.valueOf(he));
        inputO2.setText(String.valueOf(o2));
        inputMod.setText(String.valueOf(mod));
        inputMod.setToolTipText(Mvplan.getResource("mvplan.gui.GasDialog.mod.tip"));
        inputO2.setToolTipText(Mvplan.getResource("mvplan.gui.GasDialog.oxygen.tip"));
        inputHe.setToolTipText(Mvplan.getResource("mvplan.gui.GasDialog.helium.tip"));

        // Set ESCAPE key to close dialog
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction(){
                public void actionPerformed(ActionEvent e){
                    dispose();
                }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE",escapeAction);
        
        getRootPane().setDefaultButton(okButton);
        pack(); 
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(frame);
        setVisible(true);            
    }

   /*
    * Handle clicks on the Set and Cancel buttons.
    */
    public void actionPerformed(ActionEvent e) {
        boolean canClose=true;
        boolean formatError=false;
        double h=0.0,o=0.0,m=0.0;
        //System.out.println(e.getActionCommand());
        if ("ok".equals(e.getActionCommand())) {
            try {                
                canClose = Gas.validate( 
                                h = Double.parseDouble(inputHe.getText())/100.0,
                                o = Double.parseDouble(inputO2.getText())/100.0,
                                m=  Double.parseDouble(inputMod.getText()));                                     
            } catch (NumberFormatException ex) {
                if(Mvplan.DEBUG > 0) System.out.println("Number format exception parsing gas dialog.");                
                canClose=false;
            }
            if(canClose) {
                gas.setGas(h,o,m); 
                setVisible(false);
            } else {
                okButton.setEnabled(false);             
                this.getToolkit().beep();
            }
        } else if("cancel".equals(e.getActionCommand()))
            setVisible(false);
    }

    public void focusGained(FocusEvent e) {
        JTextField tf = (JTextField)(e.getComponent());
        double o=0.0;
        if(tf==(inputMod)) {
            try {
                o = Double.parseDouble(inputO2.getText())/100.0;
            } catch (NumberFormatException ex) { }
            if (o>0.0) {                
                inputMod.setText(String.valueOf( (int)Math.round(Gas.getMod(o,sliderMOD))));                
            }
            validateForm();
        }
        tf.selectAll();        
    }
    
    public void focusLost(FocusEvent e) {
        JTextField tf = (JTextField)(e.getComponent());
        double fO2=0.0, mod=0.0;
        if(tf==(inputMod)) {
            try {
                fO2 = Double.parseDouble(inputO2.getText())/100.0;
                mod = Double.parseDouble(inputMod.getText());
            } catch (NumberFormatException ex) { }
            if (fO2>0.0) {
                sliderMOD=Gas.getppO2(fO2,mod);
                //System.out.println("focusLost(): set sliderMOD="+sliderMOD);
                ppO2Slider.setValue((int)Math.round(sliderMOD*100));
            }
        }             
    }
    
    public boolean validateForm() {
        double h,o,m;
        boolean check;
        // Check all numbers make sense and enable/disable OK button
        try {
            h = Double.parseDouble(inputHe.getText())/100.0;
            o = Double.parseDouble(inputO2.getText())/100.0;
            m = Double.parseDouble(inputMod.getText());
        } catch (NumberFormatException ex) {
            okButton.setEnabled(false); 
            return false;
        }        
        check = Gas.validate(h,o,m);
        okButton.setEnabled(check);
        return check;
    }

        
    /* 
     * Verify inputs on field exits. Disable OK button as required
     */
    class GasVerifier extends InputVerifier {
        public boolean verify (JComponent comp) 
        {
            JTextField field = (JTextField)comp;
            boolean fieldPassed=false;
            try {
                // Check specified field and control field focus
                // Parse as integer because we want integer numbers input
                int n=Integer.parseInt(field.getText());
                if (field==(inputMod))
                    fieldPassed = Gas.validate("mod",(double)n);
                else if (field == inputHe)
                    fieldPassed = Gas.validate("fHe",((double)n)/100);
                else if (field == inputO2)
                    fieldPassed = Gas.validate("fO2",((double)n)/100);                                                
            } catch (NumberFormatException e) {
                fieldPassed=false;
            }
            // Validate all fields and set OK button state
           if ( !validateForm() )
               comp.getToolkit().beep();
            
            if (!fieldPassed) {
                comp.getToolkit().beep();
                field.selectAll();
            }
            return fieldPassed;
        }
    }
    /*
     * Manage changes to the ppO2 slider
     */
    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider)e.getSource();
        double o=0.0;
        int value = slider.getValue();
        sliderMOD=(double)value/100.0;
        try {        
            o = Double.parseDouble(inputO2.getText())/100.0;
        } catch (NumberFormatException ex) {
            o = 0.0;
        }
        if (o>0.0) {
            inputMod.setText(String.valueOf( (int)Math.round(Gas.getMod(o,sliderMOD))));
            //System.out.println("stateChanged(): sliderMOD="+sliderMOD+" inputMod:"+inputMod.getText());
            validateForm();
        }
        //if(Mvplan.DEBUG > 0) System.out.println("Slider:"+value+" "+sliderMOD);

    }
    
    

}
