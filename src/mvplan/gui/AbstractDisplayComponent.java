/*
 * ProfileDisplayComponent.java
 *
 * Abstract class for dive table display components
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
import java.awt.font.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.text.MessageFormat;
import java.awt.print.*;
import mvplan.main.Mvplan;
import mvplan.segments.SegmentAbstract;


/**
 * Component for display of profile output.
 * 
 * @author Guy Wittig 
 * @version 6-Aug-2004
 */
public abstract class AbstractDisplayComponent extends JComponent implements Printable
{
    final static float PADX=1;   // Pad table out from edge
    final static float PADY=1;
    final static float CELLPADX=2;
    final static float CELLPADY=1;
    final static float SYMBOLCOLUMN=2;
    final static float DEPTHCOLUMNS=3;
    final static float GASCOLUMNS=10;
    final static float SPCOLUMNS=3;

    final static String FONT_NAME="Dialog";
    final static String ASCENT_SYMBOL="\u25B2";
    final static String DESCENT_SYMBOL="\u25BC";
    final static String DIVE_SYMBOL="\u25BA";
    final static String WAYPOINT_SYMBOL="\u25AA";
    final static String DECO_SYMBOL="\u25AC";
    
    protected String depthHead;
    protected String gasString, spString, stopChar, runString, runChar;
    
     protected boolean showSeconds=false; 
     protected boolean showRunTime=true;
     protected boolean showStopTime=false;
     protected boolean showGasFirst=false;
     protected boolean printColour=true;
     
     //protected int componentSizeX,componentSizeY;  
    
    //private ArrayList segments;
     protected String heading;       
     protected int numSegments;
    
    
    /************ COMPONENT METHODS ************/
    /* Sets character strings for display from resource file */
     public void setStrings() {
         // Get unit specific strings from preferences as they chage
         depthHead = Mvplan.prefs.getDepthShortString();
         gasString = Mvplan.getResource("mvplan.gas.shortText");
         spString = Mvplan.getResource("mvplan.sp.shortText");
         stopChar = Mvplan.getResource("mvplan.stop.char");
         runString = Mvplan.getResource("mvplan.run.shortText");
         runChar = Mvplan.getResource("mvplan.run.char");                 
     }
     
   public void paintComponent(Graphics g) {
       drawProfile(g);
   }

   /**  Prints the component to graphics context
    *   @returns PAGE_EXISTS
    */
   public int  print(Graphics g, PageFormat pf , int index) {       
       // Only print the first page
       if(index>0) return Printable.NO_SUCH_PAGE;
       // Reposition to the top left imageable area
       g.translate((int)pf.getImageableX(), (int)pf.getImageableY());
       // Disable double buffering
       RepaintManager mgr= RepaintManager.currentManager(this);
       mgr.setDoubleBufferingEnabled(false);
       // Draw image
       drawProfile(g);
       mgr.setDoubleBufferingEnabled(true);
       return PAGE_EXISTS;            
   }
   
   /** Gets preferred size. Typically is the same as size.
    *   @return Dimension x,y
    */
    public Dimension getPreferredSize(){
        return getSize();
    }
    
   /** Gets minimum size. Typically is the same as size.
    *   @return Dimension x,y
    */    
    public Dimension getMinimumSize() {
        return getSize();
    }
    
    /**
     * Draws profile. Is called by Paint and Print 
     */
    public abstract void drawProfile(Graphics g);       
    

    
    
    /*********** UTILITY METHODS ************/
    
    /** Convert a (double) time to mm:ss 
     *  @return String with time a mm:ss
     */
    protected String doubleTimeToString (double time) {
        int timeMins,timeSeconds;
        double timeSecondsD;
        
        // All sorts of crap to ensure we don't have wierd rounding errors
        timeMins=(int)time;
        timeSecondsD = ((time - (double)timeMins)*60.0);
        if((timeSecondsD-(int)timeSecondsD) > 0.5)
            timeSeconds=(int)timeSecondsD+1;
        else
            timeSeconds=(int)timeSecondsD;
        if(timeSeconds==60) {
            timeSeconds=0;
            timeMins+=1;
        }
        Object [] obs={new Integer(timeMins),new Integer(timeSeconds)};
        return MessageFormat.format("{0,number,#0}:{1,number,00}",obs);
    }
    
    /** Draws a string right aligned at x,y */
    protected void drawStringRightAligned(Graphics2D g2, Font f, String s, float x, float y) {
        FontRenderContext frc=g2.getFontRenderContext();  
        float width=(float)f.getStringBounds(s,frc).getWidth();                
        g2.drawString(s,x-width-CELLPADY,y);
        
    }
    /** Draws a string center aligned between x0,y and x1,y */
    protected void drawStringCenterAligned(Graphics2D g2, Font f, String s, float x0, float x1 , float y) {
        FontRenderContext frc=g2.getFontRenderContext();  
        float width=(float)f.getStringBounds(s,frc).getWidth();                
        float x=(x1-x0)/2 + x0 -width/2;
        g2.drawString(s,x,y);
        
    }
    /** Converts an integer into a string with template ##0
     *  @return String representation of integer with template ##0
     */
    protected String toIntString(double d) {
        Object[] obs={new Double(d)};
        return MessageFormat.format("{0,number,##0}",obs);
        
    }
    /** Conditionally converts a double into a formatted string. 
     *  If d<10 uses template 0.#
     *  If d>10 uses template ##0
     *  @return String representation of double
     */
    protected String toConditionalIntString(double d) {
        Object[] obs={new Double(d)};
        if (d<10.0)
            return MessageFormat.format("{0,number,0.#}",obs);
        else
            return MessageFormat.format("{0,number,##0}",obs);
        
    }    
    /** Draws line between x0,y0 and x1,y1 */
    protected void drawLine(Graphics2D g2, double x0, double y0, double x1, double y1)
    {
        Shape line = new java.awt.geom.Line2D.Double(x0,y0,x1,y1);
        g2.draw(line);
    }

    /************ ACCESSOR / MUTATOR METHODS *********/
    
    public boolean isShowSeconds() {
        return showSeconds;
    }

    public void setShowSeconds(boolean showSeconds) {        
        this.showSeconds = showSeconds;
    }

    public boolean isShowRunTime() {
        return showRunTime;
    }

    public void setShowRunTime(boolean showRunTime) {
        this.showRunTime = showRunTime;
    }

    public boolean isShowStopTime() {
        return showStopTime;
    }
  

    public void setShowStopTime(boolean showStopTime) {
        this.showStopTime = showStopTime;
    }

    public boolean isShowGasFirst() {
        return showGasFirst;
    }

    public void setShowGasFirst(boolean showGasFirst) {
        this.showGasFirst = showGasFirst;
    }
}
