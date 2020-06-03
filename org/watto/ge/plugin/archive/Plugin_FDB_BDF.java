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

package org.watto.ge.plugin.archive;

import java.io.File;
import org.watto.datatype.Resource;
import org.watto.ge.helper.FieldValidator;
import org.watto.ge.plugin.ArchivePlugin;
import org.watto.ge.plugin.ExporterPlugin;
import org.watto.ge.plugin.exporter.Exporter_ZLib;
import org.watto.io.FileManipulator;
import org.watto.task.TaskProgressManager;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class Plugin_FDB_BDF extends ArchivePlugin {

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Plugin_FDB_BDF() {

    super("FDB_BDF", "FDB_BDF");

    //         read write replace rename
    setProperties(true, false, false, false);

    setGames("Runes of Magic");
    setExtensions("fdb"); // MUST BE LOWER CASE
    setPlatforms("PC");

    // MUST BE LOWER CASE !!!
    //setFileTypes(new FileType("txt", "Text Document", FileType.TYPE_DOCUMENT),
    //             new FileType("bmp", "Bitmap Image", FileType.TYPE_IMAGE)
    //             );

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
      int versionByte = fm.readByte();
      String header = fm.readString(3);
      if (header.equals("BDF") && versionByte == 1) {
        rating += 50;
      }

      // Number Of Files
      if (FieldValidator.checkNumFiles(fm.readInt())) {
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

      ExporterPlugin exporter = Exporter_ZLib.getInstance();

      // RESETTING GLOBAL VARIABLES

      FileManipulator fm = new FileManipulator(path, false);

      long arcSize = fm.getLength();

      // 1 - Version (1)
      // 3 - Header (BDF)
      fm.skip(4);

      // 4 - Number of Files
      int numFiles = fm.readInt();
      FieldValidator.checkNumFiles(numFiles);

      // Read the offsets
      long[] offsets = new long[numFiles];

      // Loop through directory
      for (int i = 0; i < numFiles; i++) {
        // 4 - Unknown (1)
        // 8 - Hash?
        fm.skip(12);

        // 4 - File Offset
        int offset = fm.readInt();
        FieldValidator.checkOffset(offset, arcSize);
        offsets[i] = offset;
      }

      // Read the filenames
      fm.skip((numFiles * 4) + 4);

      String[] filenames = new String[numFiles];
      for (int i = 0; i < numFiles; i++) {
        // X - Filename
        // 1 - null Filename Terminator
        String filename = fm.readNullString();
        FieldValidator.checkFilename(filename);
        filenames[i] = filename;
      }

      // go through and read each file header
      fm.getBuffer().setBufferSize(128);

      Resource[] resources = new Resource[numFiles];
      TaskProgressManager.setMaximum(numFiles);

      // Loop through directory
      for (int i = 0; i < numFiles; i++) {
        fm.seek(offsets[i]);

        // 4 - File Length (including all these headers)
        int length = fm.readInt();
        FieldValidator.checkLength(length, arcSize);

        // 4 - Unknown (1)
        fm.skip(4);

        // 4 - Compression Flag (3=ZLib, 0=No Compression)
        int compFlag = fm.readInt();

        // 4 - Decompressed File Length
        int decompLength = fm.readInt();
        FieldValidator.checkLength(decompLength);

        // 4 - Compressed File Length (null if not compressed)
        if (compFlag != 0) {
          length = fm.readInt();
          FieldValidator.checkLength(length, arcSize);
        }
        else {
          fm.skip(4);
        }

        // 8 - Hash?
        fm.skip(8);

        // 4 - Filename Length (including null terminator)
        int filenameLength = fm.readInt();
        FieldValidator.checkFilenameLength(filenameLength);

        // X - Filename
        // 1 - null Filename Terminator
        fm.skip(filenameLength);

        // X - File Data
        long offset = fm.getOffset();

        String filename = filenames[i];

        if (compFlag == 3) {
          //path,name,offset,length,decompLength,exporter
          resources[i] = new Resource(path, filename, offset, length, decompLength, exporter);
        }
        else if (compFlag == 4) {
          //path,name,offset,length,decompLength,exporter
          resources[i] = new Resource(path, filename, offset, length, decompLength);
        }
        else {
          /*
          if (compFlag != 0) {
            System.out.println(compFlag + "\t" + filename);
          }
          */
          //path,name,offset,length,decompLength,exporter
          resources[i] = new Resource(path, filename, offset, length);
        }

        TaskProgressManager.setValue(i);
      }

      fm.close();

      return resources;

    }
    catch (Throwable t) {
      logError(t);
      return null;
    }
  }

}
