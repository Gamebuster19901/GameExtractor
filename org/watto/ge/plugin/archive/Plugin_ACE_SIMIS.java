
package org.watto.ge.plugin.archive;

import java.io.File;
import org.watto.Language;
import org.watto.task.TaskProgressManager;
import org.watto.datatype.Resource;
import org.watto.ge.helper.FieldValidator;
import org.watto.ge.plugin.ArchivePlugin;
////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                            //
//                                       GAME EXTRACTOR                                       //
//                               Extensible Game Archive Editor                               //
//                                http://www.watto.org/extract                                //
//                                                                                            //
//                           Copyright (C) 2002-2009  WATTO Studios                           //
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
import org.watto.ge.plugin.ExporterPlugin;
import org.watto.ge.plugin.exporter.Exporter_ZLib;
import org.watto.io.FileManipulator;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class Plugin_ACE_SIMIS extends ArchivePlugin {

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Plugin_ACE_SIMIS() {

    super("ACE_SIMIS", "ACE_SIMIS");

    //         read write replace rename
    setProperties(true, true, true, false);

    setGames("Microsoft Train Simulator");
    setExtensions("ace");
    setPlatforms("PC");

    setFileTypes("ace", "Ace Image");

  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public int getMatchRating(FileManipulator fm) {
    try {

      int rating = 0;

      if (FieldValidator.checkExtension(fm, extensions)) {
        rating += 25;
      }

      // Header
      if (fm.readString(8).equals("SIMISA@F")) {
        rating += 50;
      }

      return rating;

    }
    catch (Throwable t) {
      return 0;
    }
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public Resource[] read(File path) {
    try {

      ExporterPlugin exporter = Exporter_ZLib.getInstance();
      addFileTypes();

      FileManipulator fm = new FileManipulator(path, false);

      // 8 - Header (SIMISA@F)
      fm.skip(8);

      // 4 - Decompressed Size?
      int decompSize = fm.readInt();

      // 4 - Padding (all 64's)
      // X - File Data (compressed)

      long arcSize = fm.getLength();

      Resource[] resources = new Resource[1];
      TaskProgressManager.setMaximum(1);

      //path,id,name,offset,length,decompLength,exporter
      resources[0] = new Resource(path, path.getName(), 16, arcSize - 16, decompSize, exporter);

      TaskProgressManager.setValue(1);

      fm.close();

      return resources;

    }
    catch (Throwable t) {
      logError(t);
      return null;
    }
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public void write(Resource[] resources, File path) {
    try {

      ExporterPlugin exporter = Exporter_ZLib.getInstance();

      FileManipulator fm = new FileManipulator(path, true);

      // 8 - Header (SIMISA@F)
      fm.writeString("SIMISA@F");

      // 4 - Decompressed Size?
      fm.writeInt((int) resources[0].getDecompressedLength());

      // 4 - Padding (all 64's)
      for (int i = 0; i < 4; i++) {
        fm.writeByte(64);
      }

      TaskProgressManager.setMaximum(1);

      TaskProgressManager.setMessage(Language.get("Progress_WritingFiles"));
      write(exporter, resources[0], fm);

      fm.close();

    }
    catch (Throwable t) {
      logError(t);
    }
  }

}
