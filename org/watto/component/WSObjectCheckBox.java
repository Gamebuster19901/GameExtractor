/*
 * Application:  Game Extractor
 * Author:       wattostudios
 * Website:      http://www.watto.org
 * Copyright:    Copyright (c) 2002-2020 wattostudios
 *
 * License Information:
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later versions. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranties
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License at http://www.gnu.org for more
 * details. For further information on this application, refer to the authors' website.
 */

package org.watto.component;

import org.watto.xml.XMLNode;

public class WSObjectCheckBox extends WSCheckBox {

  /** serialVersionUID */
  private static final long serialVersionUID = 1L;
  Object object;

  /**
  **********************************************************************************************

  **********************************************************************************************
  **/
  public WSObjectCheckBox(Object object) {
    super();
    this.object = object;
  }

  /**
  **********************************************************************************************

  **********************************************************************************************
  **/
  public WSObjectCheckBox(XMLNode node, Object object) {
    super(node);
    this.object = object;
  }

  /**
  **********************************************************************************************

  **********************************************************************************************
  **/
  public Object getObject() {
    return object;
  }

  /**
  **********************************************************************************************

  **********************************************************************************************
  **/
  @Override
  public String getText() {
    if (object == null) {
      return "";
    }
    return object.toString();
  }

  /**
  **********************************************************************************************

  **********************************************************************************************
  **/
  public void setObject(Object object) {
    this.object = object;
  }

}