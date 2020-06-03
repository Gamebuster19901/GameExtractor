
package org.watto.ge.plugin.archive;

import java.io.File;
import org.watto.Language;
import org.watto.Settings;
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
public class Plugin_CAM_CYLBPC_3 extends ArchivePlugin {

  /**
  **********************************************************************************************

  **********************************************************************************************
  **/
  public Plugin_CAM_CYLBPC_3() {

    super("CAM_CYLBPC_3", "Playboy Mansion CAM (no filenames)");

    //         read write replace rename
    setProperties(true, false, true, false);

    setGames("Playboy: The Mansion");
    setExtensions("cam");
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
      if (fm.readString(8).equals("CYLBPC  ")) {
        rating += 50;
      }

      // Version Main
      if (fm.readShort() == 1) {
        rating += 5;
      }

      // Version Sub
      if (fm.readShort() == 1) {
        rating += 5;
      }

      // Number Of File Types
      int numFiles = fm.readInt();
      if (FieldValidator.checkNumFiles(numFiles)) {
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

      // 8 - Header (CYLBPC  )
      // 2 - Version Main (1)
      // 2 - Version Sub (1)
      fm.skip(12);

      // 4 - Number Of File Types
      int numFileTypes = fm.readInt();
      FieldValidator.checkNumFiles(numFileTypes);

      // 4 - Directory Length?
      fm.skip(4);

      String[] fileTypes = new String[numFileTypes];
      int[] dirOffsets = new int[numFileTypes];

      for (int i = 0; i < numFileTypes; i++) {
        // 4 - File Type Extension/Description
        fileTypes[i] = fm.readNullString(4);

        // 4 - Offset to the directory for this file type
        dirOffsets[i] = fm.readInt();
      }

      int numFiles = Archive.getMaxFiles(4);

      long arcSize = fm.getLength();

      Resource[] resources = new Resource[numFiles];
      TaskProgressManager.setMaximum(arcSize);

      // Loop through directory
      int realNumFiles = 0;
      for (int i = 0; i < numFileTypes; i++) {
        // go to the fileTypeDir Offset
        fm.seek(dirOffsets[i]);

        // 4 - Number Of Files in this directory
        int numFilesOfType = fm.readInt();
        FieldValidator.checkNumFiles(numFilesOfType);

        // 4 - null
        fm.skip(4);

        for (int j = 0; j < numFilesOfType; j++) {

          // 20 - Filename? File ID?
          String filename = fm.readNullString(20) + "." + fileTypes[i];

          // 4 - File Offset
          long offset = fm.readInt();
          FieldValidator.checkOffset(offset, arcSize);

          // 4 - File Size
          long length = fm.readInt();
          FieldValidator.checkLength(length, arcSize);

          //path,id,name,offset,length,decompLength,exporter
          resources[realNumFiles] = new Resource(path, filename, offset, length);

          TaskProgressManager.setValue(offset);
          realNumFiles++;
        }

      }

      fm.close();

      resources = resizeResources(resources, realNumFiles);

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

      FileManipulator fm = new FileManipulator(path, true);
      FileManipulator src = new FileManipulator(new File(Settings.getString("CurrentArchive")), false);

      int numFiles = resources.length;
      TaskProgressManager.setMaximum(numFiles);

      TaskProgressManager.setMessage(Language.get("Progress_WritingDirectory"));

      // 8 - Header (CYLBPC  )
      // 2 - Version Main (1)
      // 2 - Version Sub (1)
      fm.writeBytes(src.readBytes(12));

      // 4 - Number Of File Types
      int numFileTypes = src.readInt();
      fm.writeInt(numFileTypes);

      // DETERMINE THE FIRST FILE OFFSET
      long offset = 20 + (numFileTypes * 16) + (numFiles * 28);

      // 4 - Directory Length [+20 +numTypes*8]
      fm.writeInt((int) offset - 20 - (numFiles * 8));
      src.skip(4);

      // for each type
      // 4 - File Type Extension/Description
      // 4 - Offset to the directory for this file type
      fm.writeBytes(src.readBytes(numFileTypes * 8));

      // for each type
      int currentFileNum = 0;
      for (int t = 0; t < numFileTypes; t++) {
        // 4 - Number Of Files
        int numFilesOfType = src.readInt();
        fm.writeInt(numFilesOfType);

        // 4 - null
        fm.writeBytes(src.readBytes(4));

        // for each file of this type
        for (int i = 0; i < numFilesOfType; i++) {
          long length = resources[currentFileNum].getLength();

          // 20 - Filename? File ID?
          fm.writeBytes(src.readBytes(20));

          // 4 - File Offset
          // 4 - File Size
          src.skip(8);
          fm.writeInt((int) offset);
          fm.writeInt((int) length);
          offset += length;

          currentFileNum++;

        }

      }

      TaskProgressManager.setMessage(Language.get("Progress_WritingFiles"));
      // X - File Data
      write(resources, fm);

      src.close();

      fm.close();

    }
    catch (Throwable t) {
      logError(t);
    }
  }

}
