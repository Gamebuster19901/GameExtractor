
package org.watto.ge.plugin.archive;

import java.io.File;
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
import org.watto.io.FileManipulator;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class Plugin_LIB_TCT extends ArchivePlugin {

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Plugin_LIB_TCT() {

    super("LIB_TCT", "LIB_TCT");

    //         read write replace rename
    setProperties(true, false, false, false);

    setExtensions("lib");
    setGames("Battle Isle 2");
    setPlatforms("PC");

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

      long arcSize = fm.getLength();

      // Directory Offset
      if (FieldValidator.checkOffset(fm.readInt(), arcSize)) {
        rating += 5;
      }

      // Header
      if (fm.readString(4).equals("TCT ")) {
        rating += 50;
      }

      // Number Of Files
      if (FieldValidator.checkNumFiles(fm.readShort())) {
        rating += 5;
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

      addFileTypes();

      FileManipulator fm = new FileManipulator(path, false);
      long arcSize = fm.getLength();

      // 4 - Directory Offset
      long dirOffset = fm.readInt();
      FieldValidator.checkOffset(dirOffset, arcSize);

      // 4 - Header (TCT )
      fm.skip(4);

      // 2 - numFiles
      int numFiles = fm.readShort();
      FieldValidator.checkNumFiles(numFiles);

      Resource[] resources = new Resource[numFiles];
      TaskProgressManager.setMaximum(numFiles);

      // 2 - Unknown

      fm.seek(dirOffset);
      for (int i = 0; i < numFiles; i++) {
        // 8 - Filename (null)
        String filename = fm.readNullString(8);
        FieldValidator.checkFilename(filename);

        // 4 - Offset
        long offset = fm.readInt();
        FieldValidator.checkOffset(offset, arcSize);

        //path,id,name,offset,length,decompLength,exporter
        resources[i] = new Resource(path, filename, offset);

        TaskProgressManager.setValue(i);
      }

      fm.close();

      calculateFileSizes(resources, arcSize);

      return resources;

    }
    catch (Throwable t) {
      logError(t);
      return null;
    }
  }

}