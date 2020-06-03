
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
public class Plugin_WAD_2 extends ArchivePlugin {

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Plugin_WAD_2() {

    super("WAD_2", "WAD_2");

    //         read write replace rename
    setProperties(true, false, false, false);

    setExtensions("wad");
    setGames("Tomb Raider 3");
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
      // 536 - Unknown
      fm.skip(536);

      int numFiles = Archive.getMaxFiles(4);

      long arcSize = fm.getLength();

      Resource[] resources = new Resource[numFiles];
      TaskProgressManager.setMaximum(arcSize);

      int i = 0;
      boolean hasNext = true;
      while (hasNext) {
        // 260 - Filename
        String filename = fm.readNullString(260);
        if (filename.equals("")) {
          hasNext = false;
        }
        else {
          FieldValidator.checkFilename(filename);

          // 4 - fileLength
          long length = fm.readInt();
          FieldValidator.checkLength(length, arcSize);

          // 4 - offset
          long offset = fm.readInt();
          if (offset > 0) {

            //path,id,name,offset,length,decompLength,exporter
            resources[i] = new Resource(path, filename, offset, length);

            TaskProgressManager.setValue(offset);
            i++;
          }
        }
      }

      resources = resizeResources(resources, i);

      fm.close();

      return resources;

    }
    catch (Throwable t) {
      logError(t);
      return null;
    }
  }

}