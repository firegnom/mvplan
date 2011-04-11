/**
 * ProgressIndicator.java 
 * 
 * Displays a circular progress indicator a'la Apple OSX. 
 * Able to show a small icon in the center.
 *
 *   Adapted from example InfiniteProgressIndicator.java
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

package mvplan.gui.components;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;


public class ProgressIndicator extends JComponent implements ImageObserver
{
    protected Area[]  ticker     = null;
    protected Thread  animationThread  = null;
    protected boolean animationStarted;
    protected int     barsCount;
    protected float   fps;      // Frames per second
    
    // Store image references. Image is the original, scaled image is scaled to fit inside
    private Image image, scaledImage; 
    // Scale factor sizes image to be maximum imageSize * IMAGE_SCALE_FACTOR
    private final static float IMAGE_SCALE_FACTOR = 0.4f;
    // Scale factors for tick. As a ratio of the component size
    private final static float TICK_SCALE_FACTOR_LENGTH = 5.0f;    
    private final static float TICK_SCALE_FACTOR_WIDTH = 2.0f;
        
    private int currentWidth,currentHeight;     // Current size is stored to determine if the component was resized.
    private int minDimension;                   // Minimum of currentWidth / currentHeight
    private int imageX, imageY;                 // Co-ordinate for draing image
    private boolean imageSized;                 // Flags that the image has been resized to fit
    private boolean imageReady;                 // Flags that the image is ready to paint
    
    protected RenderingHints hints = null;

    /** Minimal constructor. Uses the following defaults:
     *  Barcount: 12
     *  Frames per second: 15
     *  Image: null
     */
    public ProgressIndicator()
    {
        this(12, 15.0f,null);
    }

    /**
     * Constructor
     * @param barsCount Number of Bars on the progress indicator, e.g. 8-12
     * @param fps - frames per second, typical 15
     * @param image - image to display in the center of the component. Will be resized to fit.
     */ 
    public ProgressIndicator( int barsCount, float fps, Image image)
    {
        this.image = image;
        this.fps       = fps > 0.0f ? fps : 15.0f;
        this.barsCount = barsCount > 0 ? barsCount : 8;

        this.hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        this.hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        currentWidth = currentHeight = 0;
        imageSized=false;
        imageReady=false;
        animationStarted=false;
    }

    /** Starts animation */
    public void start()
    {
        if( !animationStarted) {        
            animationThread = new Thread(new Animator());
            animationThread.start();
        }
    }
    /** Stops animation */
    public void stop()
    {
        if (animationThread != null) {
	        animationThread.interrupt();
	        animationThread = null;
        }
        animationStarted=false;
        repaint();
    }
    /** TODO - do we need this ? */
    public void interrupt()
    {
        if (animationThread != null) {
            animationThread.interrupt();
            animationThread = null;
            setVisible(false);
        }
    }
    
    /** Provide an imageUpdate() method for the ImageObserver interface. This causes
     *  a repaint when a registered image (the ocon in our case) is loaded
     */
    public boolean imageUpdate( Image i, int flags, int x, int y, int w, int h) {
        if( ( flags & ImageObserver.ALLBITS) != 0) {
            repaint();  
            imageReady=true;
            return false;
        } else
            return true;
    } 
    
    /** Paints component -
     *  o Determines if image was resized since last paint(). If so rebuilds the ticker
     *  o If there is an image and it needs to have its size checked then do so
     *  o 
     *  @param g - graphics context
     */
    public void paintComponent(Graphics g)
    {   
        if (getWidth() <= 1 || getHeight()<=1) return;
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(hints);
        //Determine if component was resized. If so rebuild the ticker.
        if ( ticker == null || currentWidth  != getWidth() || currentHeight != getHeight())  {
            minDimension = getHeight() < getWidth() ? getHeight() : getWidth();      
            currentHeight = getHeight();
            currentWidth = getWidth();             
            ticker = buildTicker(); // Re-build ticker             
            imageSized=false;       // Signal that image size needs to be checked
        }
        
        // See if image needs to be resized
        if(!imageSized && image != null && image.getWidth(this) > 1){
            // Due to asynchronous loading of images we need to keep track of whether the image is ready
            imageReady=false;
            // Check and re-scale image
            if((image.getWidth(this)> minDimension*IMAGE_SCALE_FACTOR) || (image.getHeight(this) > minDimension * IMAGE_SCALE_FACTOR) ) {                
                // Need to rescale. Scale in proportion by using -1 parameter on getScaledImage()
                if(image.getWidth(this) >= image.getHeight(this))
                    // Scale based on fitting width
                    scaledImage = image.getScaledInstance((int)(minDimension*IMAGE_SCALE_FACTOR), -1 , Image.SCALE_SMOOTH ); 
                else
                    // Scale based on fitting height
                    scaledImage = image.getScaledInstance(-1, (int)(minDimension*IMAGE_SCALE_FACTOR) , Image.SCALE_SMOOTH ); 
            } else {
                // No scaling needed so just point to original image
                scaledImage = image;                
            }    
            // Organise a callback when the image loading is complete. Will not paint until it is ready
            Toolkit.getDefaultToolkit().prepareImage(scaledImage, -1, -1, this);
            imageSized=true;
        } 
        
        // Paint image first. Check that it is ready to paint.
        if(imageReady) {    
            // Place it in the middle.
            imageX = (int)((currentWidth-scaledImage.getWidth(this))/2.0f);
            imageY = (int)((currentHeight-scaledImage.getHeight(this))/2.0f);            
            g2.drawImage(scaledImage, imageX,imageY,this);
        }
        
        // Draw the ticker
        for (int i = 0; i < ticker.length; i++)
        {   // vary the brightness unless by tick it is stopped
            int channel = animationStarted ? 224 - 128 / (i + 1) : 200 ;
            g2.setColor(new Color(channel, channel, channel));
            g2.fill(ticker[i]);
        }           
    }

    /** Build ticked as an aray of areas 
     *  @return Area[] - the array of areas, each one representing a tick
     */
    private Area[] buildTicker()
    {
        // TODO - cater for insets
        Area[] array = new Area[barsCount];
        Point2D.Double center = new Point2D.Double((double) getWidth() / 2, (double) getHeight() / 2);
        // Determine angle between ticks (in radians)
        double fixedAngle = 2.0 * Math.PI / ((double) barsCount);
        // Determine size of a tick
        float tickLength = minDimension /TICK_SCALE_FACTOR_LENGTH;
        float tickWidth = tickLength / TICK_SCALE_FACTOR_WIDTH;
              
        for (double i = 0.0; i < (double) barsCount; i++)
        {
            Area tick = buildTick(tickLength, tickWidth);
            // To center of component
            AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
            // Place tick on centerline and move out to edge of component
            AffineTransform toBorder = AffineTransform.getTranslateInstance((minDimension/2)-tickLength-1, -tickWidth/2.0f);
            // Rotate tick around center
            AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());
            // Combine transforms
            AffineTransform toWheel = new AffineTransform();
            toWheel.concatenate(toCenter);
            toWheel.concatenate(toBorder);
            tick.transform(toWheel);
            tick.transform(toCircle);   
            // Add to area array
            array[(int) i] = tick;
        }
        return array;
    }
    /** Build a tick
     *  @param l - length
     *  @param w - width
     *  @returns Area - the tick
     */
    private Area buildTick(float l, float w)
    {
        Rectangle2D.Double body = new Rectangle2D.Double(0, 0, l, w);      
        Area tick = new Area(body);
        return tick;
    }
    /** 
     * Animator - internal class to manage animation 
     */
    protected class Animator implements Runnable
    {
        /** Default constructor */
        protected Animator() {   }
                
        /** Runs animation in thread */
        public void run() {
            Point2D.Double center = new Point2D.Double((double) getWidth() / 2, (double) getHeight() / 2);
            double fixedIncrement = 2.0 * Math.PI / ((double) barsCount);
            AffineTransform toCircle = AffineTransform.getRotateInstance(fixedIncrement, center.getX(), center.getY());  
            animationStarted = true;

            while (animationStarted){
                if(ticker!=null) {
                    for (int i = 0; i < ticker.length; i++)
                        ticker[i].transform(toCircle);  
                }
                repaint();                
                try {
                    Thread.sleep( (int) (1000 / fps));
                } catch (InterruptedException ie) {
                    break;
                }
                Thread.yield();
                }                
            }
    }

    /** Mutator for image 
     *  @param image - Image to be centered and scaled on component
     */
    public void setImage(Image image) {
        this.image = image;
        scaledImage=null;
        imageSized=false;
        imageReady=false;
        // If not animating force a repaint
        if( !animationStarted)  
            repaint();
    }
    /** Accessor for image 
     *  @return image
     */
    public Image getImage() {
        return image;
    }
    
}