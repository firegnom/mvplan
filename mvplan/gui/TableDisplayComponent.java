/*
 * TableDisplayComponent.java
 *
 * Displays multi-profile dive table in graphical (Graphics 2D) form.
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

/* Required by child class */
import mvplan.segments.SegmentAbstract;
import mvplan.dive.TableGeneratorModel;

public class TableDisplayComponent extends AbstractDisplayComponent
{
    final static float GASCOLUMNS=5;    // Override abstrract
    
    private SegmentAbstract [][] segments;
    private int longestProfile;
    private int numProfiles;
    private int ascentSegment;
    
    
    /**
     * Constructor for objects of class profileDisplayComponent
     */
    public TableDisplayComponent(TableGeneratorModel model, String heading)
    {
        this.segments=model.getSegmentArray();
        this.heading=heading;
        numSegments=model.getNumSegments();
        longestProfile=model.getLongestprofile();
        numProfiles=model.getNumProfiles();
        ascentSegment=model.getAscentRow();
        //Graphics2D g= (Graphics2D) getGraphics();
        
        setForeground(Color.BLACK);
        setStrings();   // Set up character strings
        
        // These are estimates at construction
        // TODO - calculate actuals from font metrics
        setSize(500,20*(numSegments+2)+2*(int)PADY );
    }
    

    /**
     * Paint method 
     */
    public void drawProfile(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
        Font bodyFont;
        Font headFont;
        Font bodyFontBold;
        Font symbolFont;
        float timeColumnWidth;
        float runTimeColumnWidth;
        LineMetrics bodyTextMetrics,headTextMetrics;
        float charWidth;       
        int sizeX,sizeY;
        float tableWidth;
        float tableHeight;
        float lineHeight;
        
        float cols [];                  // Holds column x posn
                
        float textOffsetY;
        float startY;
        float x,y;
        int i,j,cp;
        int cn; // Used to manage number of columns to draw
        
        SegmentAbstract s;
        String symbol;  // Segment type unicode symbol
        
        Stroke thinStroke = new BasicStroke(0.5f);
        Stroke thickStroke = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        Stroke standardStroke = new BasicStroke (1.0f);
        
        bodyFont=new Font(Mvplan.prefs.getPrintFontName(),Font.PLAIN,Mvplan.prefs.getPrintFontBodySize());
        headFont=new Font(Mvplan.prefs.getPrintFontName(),Font.PLAIN,Mvplan.prefs.getPrintFontHeaderSize());
        bodyFontBold=new Font(Mvplan.prefs.getPrintFontName(),Font.BOLD,Mvplan.prefs.getPrintFontBodySize());
        symbolFont=new Font(FONT_NAME,Font.PLAIN,Mvplan.prefs.getPrintFontHeaderSize());
        
        // Determine if stop times should show seconds
        // TODO - seconds not supported
        showSeconds=false;
        /*
        if(Mvplan.prefs.showSeconds) // && (Mvplan.prefs.stopTimeIncrement != (int)Mvplan.prefs.stopTimeIncrement))
            showSeconds=true;
        else 
            showSeconds=false;
        */
        
        if(showSeconds) {
            timeColumnWidth=5;
            runTimeColumnWidth=6;
        } else {
            timeColumnWidth=2;
            runTimeColumnWidth=3;            
        }        
        // Load printing defaults 
        showStopTime = Mvplan.prefs.isShowStopTime();
        showRunTime  = Mvplan.prefs.isShowRunTime();
        showGasFirst = Mvplan.prefs.isShowGasFirst();
        printColour  = Mvplan.prefs.isPrintColour();
                
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(bodyFont);
        FontRenderContext frc=g2.getFontRenderContext();
        
        bodyTextMetrics=bodyFont.getLineMetrics(heading,frc);
        headTextMetrics=headFont.getLineMetrics(heading,frc);    
        
        // Establish representative character width for column spacing
        charWidth=(float)bodyFont.getStringBounds("X",frc).getWidth()*1.1f;        

        // Determine table's line height from full character height plus cell padding
        lineHeight=bodyTextMetrics.getHeight()+ (CELLPADX+CELLPADY);
        // Determine how high to set text origin over table boundary - because of descender
        textOffsetY = lineHeight-CELLPADY-bodyTextMetrics.getDescent();  
       
        /****************************/
        // Determine column locations
        // No columns = 4 + (numProfiles * cn)
        // This is done in this wierd and complex way to allow the movement of the gas columns to the end.
        cn = showStopTime ? 2 : 1;       
        cp=0;    // Column pointer (cp) = First Column
        cols = new float [4+numProfiles*cn];
        cols[cp++]=PADX;            
        cols[cp]=cols[cp-1]+charWidth*DEPTHCOLUMNS+2*CELLPADX;
        cp++;        
        if(showGasFirst) {
            cols[cp]=cols[(cp++)-1]+charWidth*GASCOLUMNS+2*CELLPADX;            
            cols[cp]=cols[(cp++)-1]+charWidth*SPCOLUMNS+2*CELLPADX;            
        }        
        for(i=0;i<numProfiles*cn;i+=cn){
            if(showStopTime) {
                cols[cp] = cols[(cp++)-1]+charWidth*timeColumnWidth+2*CELLPADX;                
                cols[cp] = cols[(cp++)-1]+charWidth*runTimeColumnWidth+2*CELLPADX;               
            } else
                cols[cp] = cols[(cp++)-1]+charWidth*runTimeColumnWidth+2*CELLPADX;                                
        }
        if(!showGasFirst) {
            cols[cp]=cols[cp-1]+charWidth*GASCOLUMNS+2*CELLPADX;
            cp++;
            cols[cp]=cols[cp-1]+charWidth*SPCOLUMNS+2*CELLPADX;            
        }
        
         // Calculate table sizes   
        tableWidth=cols[cols.length-1];        
        textOffsetY = CELLPADY+bodyTextMetrics.getDescent();
        tableHeight=(2+numSegments)*lineHeight;
        sizeX=(int)(PADX*2.0f+tableWidth);
        sizeY=(int)(PADY*2.0f+tableHeight);

        // Clear background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0,0,sizeX,sizeY);        
        g2.setPaint(getForeground());
        
        // Shade alternate columns          
        if(printColour)  {
            int shadeWidth = showStopTime ? (int)(charWidth*(timeColumnWidth+runTimeColumnWidth)+4*CELLPADX) : (int)(charWidth*(runTimeColumnWidth)+2*CELLPADX);
            cp= showGasFirst ? 3 : 1;   
            g2.setPaint(Mvplan.prefs.getBackgroundColour());             
            for(i=0;i<numProfiles;i+=2) {      
                g2.fillRect((int)(cols[cp]+1),(int)(PADY+lineHeight+1),shadeWidth,(int)(tableHeight-lineHeight) ); 
                cp+=cn*2;
            }
            g2.setPaint(getForeground());
        }
            /* Shade every second line - NOT USED
            for (i=0; i<=numSegments+2; i++) {  
                if ( (i>2) & ((i/2)*2!=i) ){    // Every 2nd line
                    y=PADY+(i*lineHeight);     
                    g2.setPaint(BackgroundShade);
                    g2.fillRect((int)PADX,(int)(y-lineHeight),(int)(tableWidth-cols[0]),(int)lineHeight+1 );
                    g2.setPaint(getForeground());
                }
            } */
        
        // Print MV-Plan heading
        g2.fillRect((int)PADX,(int)PADY,(int)(tableWidth-cols[0])+1,(int)lineHeight+1);
        g2.setFont(bodyFontBold);
        g2.setPaint(Color.WHITE);
        g2.drawString(heading,PADX+CELLPADX,PADY+lineHeight-textOffsetY);
        g2.setPaint(getForeground());        
        
         g2.setStroke(thinStroke);
        // Draw horizontal lines
        for (i=0; i<=numSegments+2; i++) {           
            y=PADY+(i*lineHeight);
            if(i==ascentSegment+2) {    // Draw thick line under last dive segment.
                g2.setStroke(thickStroke);
                drawLine(g2,PADX,y,tableWidth,y);
                g2.setStroke(thinStroke);
            } if (i==0 || i == numSegments+2) {
                g2.setStroke(standardStroke);
                drawLine(g2,PADX,y,tableWidth,y);
                g2.setStroke(thinStroke);                
            } else
                drawLine(g2,PADX,y,tableWidth,y);
        }        
        
        // Fill table
        for (i=0;i<numSegments;i++) {
           y=PADY+((i+3)*lineHeight);           
           s=(SegmentAbstract)segments[longestProfile][i];
                      
           // Draw table contents
           //drawStringCenterAligned(g2,symbolFont ,symbol ,c0 ,c1 , y-textOffsetY);        // Symbol   
           g2.setFont(bodyFontBold);
           cp=0; // Column Pointer = First Column           
           drawStringRightAligned(g2, bodyFont,toConditionalIntString(s.getDepth()) , cols[cp+1], y-textOffsetY);     // Depth     
           cp++;
           g2.setFont(bodyFont);
           if(showGasFirst) {
                drawStringCenterAligned(g2, bodyFont,s.getGas().getShortName() , cols[cp], cols[1+cp++], y-textOffsetY);     // Gas
                drawStringRightAligned(g2, bodyFont,Double.toString(s.getSetpoint()) , cols[1+cp++], y-textOffsetY);   // Setpoint 
           }
           for(j=0; j<numProfiles;j++) {
               s=(SegmentAbstract)segments[j][i];
               if(s!=null){
                   if(showStopTime)        drawStringRightAligned(g2, bodyFont,toIntString(s.getTime()) , cols[1+cp++], y-textOffsetY);      // Time                     
                   if(i==ascentSegment-1)   g2.setFont(bodyFontBold);             
                    drawStringRightAligned(g2, bodyFont,toIntString(s.getRunTime()) , cols[1+cp++], y-textOffsetY);   // Runtime
                    g2.setFont(bodyFont);                  
               } else
                   cp += showStopTime ? 2 : 1;
           }
           if(!showGasFirst) {
                // Get gas details again from longest profile to avoid null segments
                s=(SegmentAbstract)segments[longestProfile][i];
                drawStringCenterAligned(g2, bodyFont,s.getGas().getShortName() , cols[cp], cols[1+cp++], y-textOffsetY);     // Gas
                drawStringRightAligned(g2, bodyFont,Double.toString(s.getSetpoint()) , cols[1+cp++], y-textOffsetY);   // Setpoint 
           }           
           
           
           
           /* This isn't really practical
           if(showSeconds){
                drawStringRightAligned(g2, bodyFont,doubleTimeToString(s.getTime()) , c5, y-textOffsetY);      // Time           
                drawStringRightAligned(g2, bodyFont,doubleTimeToString(s.getRunTime()) , c6, y-textOffsetY);   // Runtime                  
           } else {
                drawStringRightAligned(g2, bodyFont,toIntString(s.getTime()) , c5, y-textOffsetY);      // Time                
                drawStringRightAligned(g2, bodyFont,toIntString(s.getRunTime()) , c6, y-textOffsetY);   // Runtime   
           } */
        }   
             
        // Draw vertical Lines
        cp=1;
        if(showGasFirst) {
            for (i=1;i<3;i++)
                drawLine(g2, cols[cp], PADY+lineHeight, cols[cp++], PADY+tableHeight);               
        }
        for (i=0;i<numProfiles;i++) {
            drawLine(g2, cols[cp], PADY+lineHeight, cols[cp], PADY+tableHeight);                        
            cp+=cn;
        }
        if(!showGasFirst) {
            for (i=1;i<=3;i++)
                drawLine(g2, cols[cp], PADY+lineHeight, cols[cp++], PADY+tableHeight);               
        }                    
        // Surrounding box
        g2.setStroke(standardStroke);
        drawLine(g2, cols[0],PADY,cols[0],PADY+tableHeight);
        drawLine(g2, cols[cols.length-1], PADY, cols[cols.length-1],PADY+tableHeight);
        
            
        // // Add headings - these come from string resources defined in the abstract class
        g2.setFont(headFont);
        textOffsetY = CELLPADY+headTextMetrics.getDescent();         
        startY=PADY-textOffsetY+lineHeight*2;
        cp=0;   // Initialise colomn pointer
        drawStringCenterAligned(g2,headFont,depthHead,cols[cp],cols[1+cp++],startY);
        if(showGasFirst) {
            drawStringCenterAligned(g2,headFont,gasString,cols[cp],cols[1+cp++],startY);
            drawStringCenterAligned(g2,headFont,spString,cols[cp],cols[1+cp++],startY);
        }
        for(i=0;i<numProfiles;i++) {
            if(showStopTime)    drawStringRightAligned(g2,headFont,stopChar,cols[1+cp++],startY);        
            drawStringRightAligned(g2,headFont,runChar,cols[1+cp++],startY);        
        }
        if(!showGasFirst) {
            drawStringCenterAligned(g2,headFont,gasString,cols[cp],cols[1+cp++],startY);
            drawStringCenterAligned(g2,headFont,spString,cols[cp],cols[1+cp++],startY);
        }        
        setSize(sizeX,sizeY);     // Update object size and fire event
    }
      
    
}
