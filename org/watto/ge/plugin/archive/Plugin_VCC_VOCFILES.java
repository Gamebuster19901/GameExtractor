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
import org.watto.Language;
import org.watto.datatype.Resource;
import org.watto.ge.helper.FieldValidator;
import org.watto.ge.plugin.ArchivePlugin;
import org.watto.io.FileManipulator;
import org.watto.task.TaskProgressManager;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class Plugin_VCC_VOCFILES extends ArchivePlugin {

  /**
  **********************************************************************************************

  **********************************************************************************************
  **/
  public Plugin_VCC_VOCFILES() {

    super("VCC_VOCFILES", "VCC_VOCFILES");

    //         read write replace rename
    setProperties(true, true, true, true);

    setGames("Rational Systems", "Star Trek: Judgement Rights");
    setExtensions("vcc"); // MUST BE LOWER CASE
    setPlatforms("PC");

    //setFileTypes("","",
    //             "",""
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
      if (fm.readString(8).equals("VOCFILES")) {
        rating += 50;
      }

      long arcSize = fm.getLength();

      // Directory Offset
      if (FieldValidator.checkOffset(fm.readInt(), arcSize)) {
        rating += 5;
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

      //ExporterPlugin exporter = Exporter_ZLib.getInstance();

      // RESETTING GLOBAL VARIABLES

      FileManipulator fm = new FileManipulator(path, false);

      long arcSize = fm.getLength();

      // 8 - Header (VOCFILES)
      fm.skip(8);

      // 4 - Directory Offset
      long dirOffset = fm.readInt();
      FieldValidator.checkOffset(dirOffset, arcSize);

      // 4 - Number Of Files
      int numFiles = fm.readInt();
      FieldValidator.checkNumFiles(numFiles);

      fm.seek(dirOffset);

      Resource[] resources = new Resource[numFiles];
      TaskProgressManager.setMaximum(numFiles);

      // Loop through directory
      for (int i = 0; i < numFiles; i++) {
        // 8 - Filename (no extension) (null terminated)
        String filename = fm.readNullString(8);
        FieldValidator.checkFilename(filename);

        // 4 - File Offset
        long offset = fm.readInt();
        FieldValidator.checkOffset(offset, arcSize);

        // 4 - File Length
        long length = fm.readInt();
        FieldValidator.checkLength(length, arcSize);

        //path,name,offset,length,decompLength,exporter
        resources[i] = new Resource(path, filename, offset, length);

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

  /**
   **********************************************************************************************
   * Writes an [archive] File with the contents of the Resources
   **********************************************************************************************
   **/
  @Override
  public void write(Resource[] resources, File path) {
    try {

      FileManipulator fm = new FileManipulator(path, true);
      int numFiles = resources.length;
      TaskProgressManager.setMaximum(numFiles);

      // Calculations
      TaskProgressManager.setMessage(Language.get("Progress_PerformingCalculations"));

      long dirOffset = 16;
      for (int i = 0; i < numFiles; i++) {
        dirOffset += resources[i].getDecompressedLength();
      }

      // Write Header Data

      // 8 - Header (VOCFILES)
      fm.writeString("VOCFILES");

      // 4 - Directory Offset
      fm.writeInt((int) dirOffset);

      // 4 - Number Of Files
      fm.writeInt(numFiles);

      // Write Files
      TaskProgressManager.setMessage(Language.get("Progress_WritingFiles"));
      write(resources, fm);

      // Write Directory
      TaskProgressManager.setMessage(Language.get("Progress_WritingDirectory"));
      long offset = 16;
      for (int i = 0; i < numFiles; i++) {
        Resource resource = resources[i];
        long decompLength = resource.getDecompressedLength();

        // 8 - Filename (no extension) (null terminated)
        String filename = resource.getName();
        if (filename.length() > 8) {
          filename = filename.substring(0, 8);
        }
        while (filename.length() < 8) {
          filename += (char) 0;
        }
        fm.writeString(filename);

        // 4 - File Offset
        fm.writeInt((int) offset);

        // 4 - File Length
        fm.writeInt((int) decompLength);

        offset += decompLength;
      }

      fm.close();

    }
    catch (Throwable t) {
      logError(t);
    }
  }

}
