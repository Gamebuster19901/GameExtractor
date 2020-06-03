
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
import org.watto.io.converter.IntConverter;
import org.watto.io.converter.ShortConverter;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class Plugin_SCE_MAP extends ArchivePlugin {

  /**
  **********************************************************************************************

  **********************************************************************************************
  **/
  public Plugin_SCE_MAP() {

    super("SCE_MAP", "SCE_MAP");

    //         read write replace rename
    setProperties(true, false, false, false);

    setGames("Zero Population Count: No Flesh Shall Be Spared");
    setExtensions("sce");
    setPlatforms("PC");

    setFileTypes("lins", "Audio File",
        "pict", "Picture File");

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

      // Unknown (2)
      if (ShortConverter.changeFormat(fm.readShort()) == 2) {
        rating += 5;
      }

      // Unknown (1)
      if (ShortConverter.changeFormat(fm.readShort()) == 1) {
        rating += 5;
      }

      // Header
      if (fm.readString(64).equals("MAP")) {
        rating += 50;
      }

      fm.skip(4);

      long arcSize = fm.getLength();

      // Directory Offset
      if (FieldValidator.checkOffset(IntConverter.changeFormat(fm.readInt()), arcSize)) {
        rating += 5;
      }

      // Number Of Files
      if (FieldValidator.checkNumFiles(ShortConverter.changeFormat(fm.readShort()))) {
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
  @SuppressWarnings("unused")
  public Resource[] read(File path) {
    try {

      addFileTypes();

      FileManipulator fm = new FileManipulator(path, false);

      long arcSize = fm.getLength();

      // 2 - Unknown (2)
      // 2 - Unknown (1)
      // 64 - File Description? (MAP) (null)
      // 4 - Unknown
      fm.skip(72);

      // 4 - Directory Offset
      long dirOffset = IntConverter.changeFormat(fm.readInt());
      FieldValidator.checkOffset(dirOffset, arcSize);

      // 2 - Number Of Files
      int numFiles = ShortConverter.changeFormat(fm.readShort());
      FieldValidator.checkNumFiles(numFiles);

      // 2 - Unknown
      // 2 - Unknown
      // 2 - Unknown
      // 44 - null

      fm.seek(dirOffset);

      Resource[] resources = new Resource[numFiles];
      TaskProgressManager.setMaximum(numFiles);

      // Loop through directory
      for (int i = 0; i < numFiles; i++) {
        // 4 - File Offset
        long offset = IntConverter.changeFormat(fm.readInt());
        FieldValidator.checkOffset(offset, arcSize);

        // 4 - File Length
        long length = IntConverter.changeFormat(fm.readInt());
        FieldValidator.checkLength(length, arcSize);

        // 2 - File ID (Incremental, starting at 0)
        int fileID = ShortConverter.changeFormat(fm.readShort());

        // 2 - Unknown (0/2/4)
        // 2 - Unknown (0/4)
        // 4 - Unknown (0/1/3/20/28)
        fm.skip(8);

        // 66 - Filename (null)
        String filename = fm.readNullString(66);

        //// 4 - File Type / Extension
        //String extension = fm.readString(4);
        //System.out.println(extension);

        //path,id,name,offset,length,decompLength,exporter
        resources[i] = new Resource(path, filename, offset, length);

        TaskProgressManager.setValue(i);
      }

      for (int i = 0; i < numFiles; i++) {
        fm.seek(resources[i].getOffset());

        // 4 - File Extension/Type
        String extension = fm.readString(4);

        resources[i].setName(resources[i].getName() + "." + extension.toLowerCase());
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
