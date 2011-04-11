/*
 * ProfileDisplayComponent.java
 *
 * Displays single dive table in graphical (Graphics2D) form. 
 *
 * @author Guy Wittig
 * @version 06-Aug-2004
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
import java.util.ArrayList;
import mvplan.main.Mvplan;
import mvplan.segments.SegmentAbstract;

public class ProfileDisplayComponent extends AbstractDisplayComponent
{
  
    private ArrayList segments;

    /**
     * Constructor for objects of class profileDisplayComponent
     */
    public ProfileDisplayComponent(ArrayList outputSegments, String heading)
    {
        this.segments=outputSegments;
        this.heading=heading;

        numSegments=segments.size();
        
        setForeground(Color.BLACK);
        setStrings();   // Set up character strings
        
        // These are estimates at construction
        // TODO - calculate actuals from font metrics
        setSize(300,15*(numSegments+2)+2*(int)PADY );
    }
    
    
    /**
     * Paint method 
     */
    public void drawProfile(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
        Font bodyFont;
        Font headFont;
        Font symbolFont;
        float timeColumnWidth;
        float runTimeColumnWidth;
        LineMetrics bodyTextMetrics,headTextMetrics;
        float charWidth;       
        int sizeX,sizeY;
        float tableWidth;
        float tableHeight;
        float lineHeight;
        float c0,c1,c2,c3,c4,c5,c6;    // Column locations
        float textOffsetY;
        float startY;
        float x,y;
        int i;
        SegmentAbstract s;
        String symbol;  // Segment type unicode symbol
                    
        bodyFont=new Font(Mvplan.prefs.getPrintFontName(),Font.PLAIN,Mvplan.prefs.getPrintFontBodySize());
        headFont=new Font(Mvplan.prefs.getPrintFontName(),Font.PLAIN,Mvplan.prefs.getPrintFontHeaderSize());
        symbolFont=new Font(FONT_NAME,Font.PLAIN,Mvplan.prefs.getPrintFontHeaderSize());
                
        // Determine if stop times should show seconds
        if(Mvplan.prefs.isShowSeconds()) // && (Mvplan.prefs.stopTimeIncrement != (int)Mvplan.prefs.stopTimeIncrement))
            showSeconds=true;
        else 
            showSeconds=false;
                
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
        charWidth=(float)bodyFont.getStringBounds("X",frc).getWidth();        

        // Determine table's line height from full character height plus cell padding
        lineHeight=bodyTextMetrics.getHeight()+ (CELLPADX+CELLPADY);
        // Determine how high to set text origin over table boundary - because of descender
        textOffsetY = lineHeight-CELLPADY-bodyTextMetrics.getDescent();  
       
        // Determine column locations
        c0=PADX;
        c1=c0+charWidth*SYMBOLCOLUMN+2*CELLPADX;
        c2=c1+charWidth*DEPTHCOLUMNS+2*CELLPADX;
        c3=c2+charWidth*GASCOLUMNS+2*CELLPADX;
        c4=c3+charWidth*SPCOLUMNS+2*CELLPADX;
        c5=c4+charWidth*timeColumnWidth+2*CELLPADX;                
        c6=c5+charWidth*runTimeColumnWidth+2*CELLPADX;       
        tableWidth=c6;
        textOffsetY = CELLPADY+bodyTextMetrics.getDescent();

        tableHeight=(2+numSegments)*lineHeight;
        sizeX=(int)(PADX+c6);
        sizeY=(int)(PADY*2.+tableHeight);

        // Clear background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0,0,sizeX,sizeY);        
        g2.setPaint(getForeground());
        g2.fillRect((int)PADX,(int)PADY,(int)(c6-c0)+1,(int)lineHeight+1);
        
        // Print heading
        g2.setPaint(Color.WHITE);
        g2.drawString(heading,PADX+CELLPADX,PADY+lineHeight-textOffsetY);
        g2.setPaint(getForeground());
        
        // Draw table
        // Shade alternate lines        
        if(printColour)  {
            for (i=0; i<=numSegments+2; i++) {  
                if ( (i>2) & ((i/2)*2!=i) ){    // Every 2nd line
                    y=PADY+(i*lineHeight);     
                    g2.setPaint(Mvplan.prefs.getBackgroundColour());
                    g2.fillRect((int)PADX,(int)(y-lineHeight),(int)(c6-c0),(int)lineHeight+1 );
                    g2.setPaint(getForeground());
                }
            }
        }
        // Draw horizontal lines
        for (i=0; i<=numSegments+2; i++) {
            y=PADY+(i*lineHeight);
            drawLine(g2,PADX,y,tableWidth,y);
        }        
        
        // Fill table
        for (i=0;i<numSegments;i++) {
           y=PADY+((i+3)*lineHeight);           
           s=(SegmentAbstract)segments.get(i);
           
           switch(s.getType()) {           
            case SegmentAbstract.CONST: 
                symbol=DIVE_SYMBOL;
                break;
            case SegmentAbstract.ASCENT:
                symbol=ASCENT_SYMBOL;
                break;
            case SegmentAbstract.DESCENT:
                symbol=DESCENT_SYMBOL;
                break;
            case SegmentAbstract.DECO:
                symbol=DECO_SYMBOL;
                break;
            case SegmentAbstract.WAYPOINT:
                symbol=WAYPOINT_SYMBOL;
                break;
            default:
                symbol=" ";
                break;
            }
           // Draw table contents
           drawStringCenterAligned(g2,symbolFont ,symbol ,c0 ,c1 , y-textOffsetY);        // Symbol      
           drawStringRightAligned(g2, bodyFont,toConditionalIntString(s.getDepth()) , c2, y-textOffsetY);     // Depth       
           drawStringCenterAligned(g2, bodyFont,s.getGas().toString() , c2, c3, y-textOffsetY);     // Gas
           drawStringRightAligned(g2, bodyFont,Double.toString(s.getSetpoint()) , c4, y-textOffsetY);   // Setpoint 
           if(showSeconds){
                drawStringRightAligned(g2, bodyFont,doubleTimeToString(s.getTime()) , c5, y-textOffsetY);      // Time           
                drawStringRightAligned(g2, bodyFont,doubleTimeToString(s.getRunTime()) , c6, y-textOffsetY);   // Runtime                  
           } else {
                drawStringRightAligned(g2, bodyFont,toIntString(s.getTime()) , c5, y-textOffsetY);      // Time                
                drawStringRightAligned(g2, bodyFont,toIntString(s.getRunTime()) , c6, y-textOffsetY);   // Runtime   
           }
        }        
        drawLine(g2,c0,PADY,c0,PADY+tableHeight);
        drawLine(g2,c1,PADY+lineHeight,c1,PADY+tableHeight);
        drawLine(g2,c2,PADY+lineHeight,c2,PADY+tableHeight);
        drawLine(g2,c3,PADY+lineHeight,c3,PADY+tableHeight);
        drawLine(g2,c4,PADY+lineHeight,c4,PADY+tableHeight);
        drawLine(g2,c5,PADY+lineHeight,c5,PADY+tableHeight);
        drawLine(g2,c6,PADY,c6,PADY+tableHeight);  
        
        // Add headings
        g2.setFont(headFont);
        textOffsetY = CELLPADY+headTextMetrics.getDescent();         
        startY=PADY-textOffsetY+lineHeight*2;
        
        // Add headings - these come from string resources defined in the abstract class
        drawStringCenterAligned(g2,headFont,depthHead,c1,c2,startY);
        drawStringCenterAligned(g2,headFont,gasString,c2,c3,startY);
        drawStringCenterAligned(g2,headFont,spString,c3,c4,startY);
        drawStringCenterAligned(g2,headFont,stopChar,c4,c5,startY);        
        drawStringCenterAligned(g2,headFont,runString,c5,c6,startY);        
        
        setSize(sizeX,sizeY);     // Update object size
    }
 
}
