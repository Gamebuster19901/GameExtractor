
package org.watto.ge.plugin.archive;

import java.io.File;
import org.watto.task.TaskProgressManager;
import org.watto.datatype.Archive;
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
public class Plugin_LVL_UCFB_2 extends ArchivePlugin {

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Plugin_LVL_UCFB_2() {

    super("LVL_UCFB_2", "LVL_UCFB_2");

    //         read write replace rename
    setProperties(true, false, false, false);

    setGames("Star Wars Battlefront");
    setExtensions("lvl");
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

      // Header
      if (fm.readString(4).equals("ucfb")) {
        rating += 50;
      }

      long arcSize = fm.getLength();

      // Archive Size
      if (fm.readInt() + 8 == arcSize) {
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
   * Reads an [archive] File into the Resources
   **********************************************************************************************
   **/
  @Override
  public Resource[] read(File path) {
    try {

      // NOTE - Compressed files MUST know their DECOMPRESSED LENGTH
      //      - Uncompressed files MUST know their LENGTH

      addFileTypes();

      // RESETTING THE GLOBAL VARIABLES

      FileManipulator fm = new FileManipulator(path, false);

      long arcSize = (int) fm.getLength();

      // 4 - Header (ucfb)
      // 4 - Archive Size [+8]
      fm.skip(8);

      int numFiles = Archive.getMaxFiles();

      Resource[] resources = new Resource[numFiles];

      TaskProgressManager.setMaximum(arcSize);

      // Loop through directory
      int realNumFiles = 0;
      while (fm.getOffset() < arcSize - 8) {
        // 4 - Unknown (1538883361) (null for Padding files)
        fm.skip(4);

        // 4 - File Size (including all these fields) (null for Padding files)
        long length = fm.readInt();
        if (length != 0) {
          length -= 16;
        }
        FieldValidator.checkLength(length, arcSize);

        // 4 - File Type Code/Extension ("emo_","_pad")
        String extension = fm.readString(4);

        // 4 - File Size (not including the 2 fields above) (null for Padding files)
        fm.skip(4);

        // X - File Data (does not exist for Padding files)
        long offset = fm.getOffset();
        fm.skip(length);

        String filename = Resource.generateFilename(realNumFiles) + "." + extension;

        //path,id,name,offset,length,decompLength,exporter
        resources[realNumFiles] = new Resource(path, filename, offset, length);

        TaskProgressManager.setValue(offset);
        realNumFiles++;
      }

      resources = resizeResources(resources, realNumFiles);

      fm.close();

      return resources;

    }
    catch (Throwable t) {
      logError(t);
      return null;
    }
  }

}
