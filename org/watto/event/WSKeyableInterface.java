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
package org.watto.event;

import java.awt.event.KeyEvent;
import javax.swing.JComponent;

/***********************************************************************************************
A class that reacts to <code>KeyEvent</code> pressed events
@see java.awt.event.KeyEvent
@see org.watto.event.listener.WSKeyableListener
***********************************************************************************************/
public interface WSKeyableInterface {

  /***********************************************************************************************
  Performs an action when a <code>KeyEvent</code> pressed event is triggered
  @param source the <code>JComponent</code> that triggered the event
  @param event the <code>KeyEvent</code>
  @return <b>true</b> if the event was handled by this class<br />
          <b>false</b> if the event wasn't handled by this class, and thus should be passed on to
          the parent class for handling.
  ***********************************************************************************************/
  public boolean onKeyPress(JComponent source, KeyEvent event);
}