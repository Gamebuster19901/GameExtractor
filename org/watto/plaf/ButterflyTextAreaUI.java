////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                            //
//                                       WATTO STUDIOS                                        //
//                             Java Code, Programs, and Software                              //
//                                    http://www.watto.org                                    //
//                                                                                            //
//                           Copyright (C) 2004-2010  WATTO Studios                           //
//                                                                                            //
// This program is free software; you can redistribute it and/or modify it under the terms of //
// the GNU General Public License published by the Free Software Foundation; either version 2 //
// of the License, or (at your option) any later versions. This program is distributed in the //
// hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranties //
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License //
// at http://www.gnu.org for more details. For updates and information about this program, go //
// to the WATTO Studios website at http://www.watto.org or email watto@watto.org . Thanks! :) //
//                                                                                            //
////////////////////////////////////////////////////////////////////////////////////////////////

package org.watto.plaf;

import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;


/***********************************************************************************************
Used to paint the GUI for <code>WSTextArea</code>s
***********************************************************************************************/
public class ButterflyTextAreaUI extends BasicTextAreaUI {

  /** the <code>JTextArea</code> to be painted **/
  JTextArea textArea;


  /***********************************************************************************************
  Sets up the painting properties for painting on the <code>Component</code>
  @param component the <code>Component</code> that will be painted
  ***********************************************************************************************/
  public void installUI(JComponent component){
    super.installUI(component);
    textArea = (JTextArea)component;
  }


  /***********************************************************************************************
  Paints the <code>component</code> on the <code>graphics</code>
  @param graphics the <code>Graphics</code> to paint the <code>component</code> on
  @param component the <code>Component</code> to paint
  ***********************************************************************************************/
  public void paintBackground(Graphics graphics){
    if (textArea.isOpaque()) {
      ButterflyPainter.paintOpaque(graphics,textArea);
    }
  }


  /***********************************************************************************************
  Creates a <code>ButterflyTextAreaUI</code> instance for rendering the <code>component</code>
  @param component the <code>Component</code> to get the painter for
  @return a new <code>ButterflyTextAreaUI</code> instance
  ***********************************************************************************************/
  public static ComponentUI createUI(JComponent component){
    return new ButterflyTextAreaUI();
  }
}