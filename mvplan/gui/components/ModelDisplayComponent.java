/*
 * ModelDisplayComponent.java
 *
 * Displays tissue model in graphical form as a JComponent.
 *
 * TODO - implement model view controller pattern properly
 *
 * Created on 27 June 2006, 12:28
 * @author Guy
 * Copywrite 27 June 2006 Guy Wittig
 *
 */

package mvplan.gui.components;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import mvplan.main.Mvplan;
import mvplan.model.*;

public class ModelDisplayComponent  extends JComponent implements MouseMotionListener {
    mvplan.model.AbstractModel model;   // Points to model
    boolean complex=false;      // Complex layout for large displays

    Graphics2D g2;
    float maxP;         // Max compartment pressure
    float amb;         // Ambient pressure
    int i;
    float yScale;
    float xScale;
    float xSize;
    float ySize;
    Font infoFont; 
    int tissueOver=-1;
    
    /** Creates a new instance of ModelDisplayComponent */
    public ModelDisplayComponent(mvplan.model.AbstractModel model) {
        setModel(model);
        addMouseMotionListener(this);
        infoFont=new Font(Mvplan.prefs.getPrintFontName(),Font.BOLD,12);       
    }
    
    public synchronized void setModel(mvplan.model.AbstractModel model) {
        this.model=model;
        repaint();
    }
    
     public void paint(Graphics g)
    {
         String s;
         Compartment [] tissues;
         double  x,y;
         
        g2 = (Graphics2D)g;
        maxP=0.0f;
        yScale =0.0f;
        xSize = getSize().width ;       
        ySize = getSize().height;        
        
        // If large enough enable a more complex display
        complex = (ySize > 200 && xSize > 250);               
                
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        setBackground(Color.BLACK);      
                           
       // If no model, then return
        if(model==null) {
            // Draw bounding box
            setForeground(Color.BLACK);
            g2.fillRect(0,0,getSize().width, getSize().height);
            g2.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL));
            g2.setPaint(Color.GRAY);
            drawLine(g2,0.,0.,xSize,ySize);
            drawLine(g2, xSize,0.,0.,ySize);            
            return;
        }       
        tissues  = model.getTissues();
        // Determine scale.
        for(i=0; i<tissues.length; i++){
            if(tissues[i].getPpHe()+tissues[i].getPpN2() > maxP)
                maxP = (float)(tissues[i].getPpHe()+tissues[i].getPpN2());
        }
        
        if(maxP<Mvplan.prefs.getPAmb() )
            yScale = ySize / (int)(Mvplan.prefs.getPAmb()*1.5);
        else
            yScale = ySize / (int)((maxP)*1.2);
        xScale=  xSize / (tissues.length*2);
        Stroke standardStroke = new BasicStroke (xScale,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
        // Draw bounding box     
        setForeground(Color.BLACK);
        g2.fillRect(0,0,getSize().width, getSize().height);         
        //************PAINT BODY***************//
        if(g2.getClip().intersects(0,26,xSize-1, ySize-27)) {   // TODO - determine if we need a full paint or not     
            //System.out.println("Redraw: "+g2.getClip());
            g2.setStroke(standardStroke);
            g2.setPaint(Color.RED);        
            for(i=0; i<tissues.length; i++){ 
                g2.setPaint(Color.ORANGE);
                x= xScale*(((double)i)*2.0+1.0);
                drawLine(g2,
                        x,    
                        ySize,
                        x,
                        ySize - tissues[i].getPpN2()*yScale);
                g2.setPaint(Color.RED);
                drawLine(g2,
                        x,    
                        ySize- tissues[i].getPpN2()*yScale,
                        x,
                        ySize -(tissues[i].getPpHe()+tissues[i].getPpN2())*yScale);                                                          
            }   
            
            g2.setPaint(Color.WHITE);
            if( complex ) {
                g2.setFont(infoFont);
                for(i=0; i<tissues.length; i++){           
                    drawStringCenterAligned(g2, infoFont, String.valueOf(i+1), xScale*(i*2.0f+1.0f)-xScale/2,xScale*(i*2.0f+1.0f)+xScale/2 , ySize-2.0f );                
                } 
            }
        } 
        //*********PAINT DATA***************/     
        // Draw ambient pressure line  
        amb=(float)Mvplan.prefs.getPAmb()*yScale;
        g2.setPaint(Color.GREEN);
        g2.setStroke(new BasicStroke(2.0f));
        drawLine(g2,0,ySize-amb,xSize, ySize-amb); 
        if(complex) {
            g2.setFont(infoFont);
            g2.drawString("amb",2.0f,ySize-amb-2.0f );
            g2.setPaint(Color.WHITE);
            if(tissueOver >=0 && tissueOver<tissues.length) {
                // e.g. Compartment 03 - 1.20 bar 95%
                double tissuePressure = tissues[tissueOver].getPpHe()+tissues[tissueOver].getPpN2();    // Total absolute pressure in msw (fsw)
                double maxPressure = tissues[tissueOver].getMvalueAt(Mvplan.prefs.getPAmb());           // Maximom (M-value) pressure in msw (fws)
                s=String.format(Mvplan.getResource("mvplan.gui.components.ModelDisplayComponent.compartment.text")+
                        ": %1$02d - %2$4.2f "+
                        Mvplan.getResource("mvplan.bar.text")
                        , tissueOver+1, tissuePressure/Mvplan.prefs.getPConversion()
                        );
                g2.drawString(s, 20.0f, 20.0f);            
            }
        }               
     }
     // Determine which tissue compartment the mouse is over
     public void mouseMoved(MouseEvent e ){
         int x = e.getX();
         int y = e.getY(); 
         if(!complex || model==null) return;
         tissueOver = (int)( (x/xScale)/2.0f );   
         // Repaint just the text 
         repaint(0,0,(int)xSize,25); 
     }
     
     public void mouseDragged(MouseEvent e) {                 
    }
     
    /** Draws a string center aligned between x0,y and x1,y */
    private void drawStringCenterAligned(Graphics2D g2, Font f, String s, float x0, float x1 , float y) {
        java.awt.font.FontRenderContext frc=g2.getFontRenderContext();  
        float width=(float)f.getStringBounds(s,frc).getWidth();                
        float x=(x1-x0)/2 + x0 -width/2;
        g2.drawString(s,x,y);        
    }
     
      /** Draws line between x0,y0 and x1,y1 */
    protected void drawLine(Graphics2D g2, double x0, double y0, double x1, double y1) {
        Shape line = new java.awt.geom.Line2D.Double(x0,y0,x1,y1);
        g2.draw(line);
    }    
    
}
