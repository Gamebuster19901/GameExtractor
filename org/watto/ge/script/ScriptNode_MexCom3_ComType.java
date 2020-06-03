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

package org.watto.ge.script;

import javax.script.ScriptException;
import org.watto.ErrorLogger;
import org.watto.ge.plugin.exporter.Exporter_ZLib;

/**
 **********************************************************************************************
 * A MexCom3 ScriptNode for command ComType
 **********************************************************************************************
 **/
public class ScriptNode_MexCom3_ComType extends ScriptNode {

  String compressionType;

  /**
   **********************************************************************************************
   * Constructor
   **********************************************************************************************
   **/
  public ScriptNode_MexCom3_ComType(String compressionType) {
    super("ComType");
    this.compressionType = compressionType;
  }

  /**
   **********************************************************************************************
   * Runs the command
   **********************************************************************************************
   **/
  @Override
  @SuppressWarnings("static-access")
  public void run() {
    try {
      if (!checkErrors()) {
        return;
      }

      if (compressionType.equals("ZLib1")) {
        var.setExporter(Exporter_ZLib.getInstance());
        var.setCompressed(true);
      }
      else {
        var.setCompressed(false);
        throw new ScriptException("The compression type " + compressionType + " is not valid.");
      }

    }
    catch (Throwable t) {
      ErrorLogger.log("SCRIPT", t);
      errorCount++;
    }
  }

  /**
   **********************************************************************************************
   Checks the syntax of this command for any errors.
   return <b>null</b> if no errors, otherwise returns the error message
   **********************************************************************************************
   **/
  public String checkSyntax() {
    if (compressionType.equals("ZLib1")) {
      return null;
    }
    else {
      return "Invalid Compression Type: " + compressionType;
    }
  }

}