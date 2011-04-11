/**
 * ModelDAO.java
 *
 *   Data Access Object for the ZHL16BModel class. Manages serialisation of ZHL16BModel objects to XML using the XMLDecoder and XMLEncoder classes.
 *
 *   @author Guy Wittig
 *   @version 18-Jun-2006
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
package mvplan.model;

import java.io.*;
import java.beans.*;

public class ModelDAO {
    
    /** Return value. Indicates Success  */
    public static final int SUCCESS=0;
    /** Return value. Inidcates file error. */
    public static final int FILE_ERROR=1;
    
    /**
     * Empty constructor. Creates a new instance of ModelDAO
     */
    public ModelDAO() {
    }
    
    /**
     * Saves a model to the filename specified as an XML encoded object.
     * @param m ZHL16BModel to be saved
     * @param fileName Filename to save to
     * @return Return code: SUCCESS or FILE_ERROR
     */
    public int saveModel(AbstractModel m, String fileName){
        try {
            XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(
                new FileOutputStream(fileName)) );
            encoder.setExceptionListener(new ExceptionListener() {
                public void exceptionThrown(Exception exception) {
                    exception.printStackTrace();
                }
            });
            encoder.writeObject(m);
            encoder.close();   
            return SUCCESS;
            //System.out.println("ZHL16BModel saved to "+fileName);
        } catch (Exception ex) {
            System.out.println("Error writing model: "+ex); 
            return FILE_ERROR;
        }
    }
    
    /**
     * Loads model from filename specified as XML Encoded object.
     * @param fileName Filename to load from
     * @return ZHL16BModel object constructed from the XML.
     */
    public AbstractModel loadModel(String fileName){
        try{
            XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(
                new FileInputStream(fileName)));
            AbstractModel m = (AbstractModel)decoder.readObject();
            decoder.close(); 
            // Validate model
            if(m.validateModel()== AbstractModel.MODEL_VALIDATION_SUCCESS)
                return m;
            else
                return null;
        } catch (Exception ex) {
            System.out.println("Error reading model: "+ex);
            return null;
        }        
    }    
}
