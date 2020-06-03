
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
public class Plugin_QFM extends ArchivePlugin {

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Plugin_QFM() {

    super("QFM", "QFM");

    //         read write replace rename
    setProperties(true, false, false, false);

    setGames("Football Manager 2006");
    setExtensions("qfm");
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

      // Unknown (258)
      if (fm.readShort() == 258) {
        rating += 5;
      }

      // Header
      if (fm.readString(4).equals("mfq.")) {
        rating += 50;
      }

      // Unknown (3)
      if (fm.readShort() == 3) {
        rating += 5;
      }

      // Unknown (256)
      if (fm.readShort() == 256) {
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

      // 2 - Unknown (258)
      // 4 - Header (mfq.)
      // 2 - Unknown (3)
      // 2 - Unknown (256)
      fm.skip(10);

      int numFiles = Archive.getMaxFiles();

      Resource[] resources = new Resource[numFiles];

      TaskProgressManager.setMaximum(arcSize);

      // Loop through directory
      int realNumFiles = 0;
      while (fm.getOffset() < arcSize) {
        // X - Filename (unicode text)
        // 2 - null Filename Terminator
        String filename = fm.readNullString();
        fm.skip(1);
        filename = new String(filename.getBytes(), "UTF-16LE");
        FieldValidator.checkFilename(filename);

        // 484 - null Padding
        fm.skip(484);

        // 4 - File Extension Length [*2 for unicode]
        // 8 - File Extension (unicode text) (including ".") (reversed?)
        // 2 - null File Extension Terminator
        fm.skip(14);

        // 4 - File Length
        long length = fm.readInt();
        FieldValidator.checkLength(length, arcSize);

        // X - File Data
        long offset = fm.getOffset();
        fm.skip(length);

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
