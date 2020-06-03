
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
public class Plugin_SND_2 extends ArchivePlugin {

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Plugin_SND_2() {

    super("SND_2", "SND_2");

    //         read write replace rename
    setProperties(true, false, true, false);

    setExtensions("snd");
    setGames("Star Trek: Birth Of The Federation");
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

      // 4 - dirOffset
      long dirOffset = fm.readInt();
      FieldValidator.checkOffset(dirOffset, arcSize);

      fm.seek(dirOffset);

      int numFiles = Archive.getMaxFiles(4);//guess

      Resource[] resources = new Resource[numFiles];
      TaskProgressManager.setMaximum(arcSize);

      int i = 0;
      int currentPos = 4;
      while (fm.getOffset() < fm.getLength()) {
        // 4 - offset
        long offset = fm.readInt();
        FieldValidator.checkOffset(offset, arcSize);

        // 4 - fileLength
        long length = fm.readInt();
        FieldValidator.checkLength(length, arcSize);

        // 4 - Unknown
        // 4 - Unknown
        fm.skip(8);

        currentPos += length;

        String filename = Resource.generateFilename(i);

        //path,id,name,offset,length,decompLength,exporter
        resources[i] = new Resource(path, filename, offset, length);

        TaskProgressManager.setValue(currentPos);
        i++;
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

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public void write(Resource[] resources, File path) {
    try {

      int numFiles = resources.length;
      TaskProgressManager.setMaximum(numFiles);

      FileManipulator fm = new FileManipulator(path, true);
      FileManipulator src = new FileManipulator(new File(Settings.getString("CurrentArchive")), false);

      // 4 - dirOffset (don't know it yet)
      fm.setLength(8);
      fm.seek(4);
      src.skip(4);

      TaskProgressManager.setMessage(Language.get("Progress_WritingFiles"));
      write(resources, fm);

      long dirOffset = (int) fm.getOffset();

      long offset = 4;

      TaskProgressManager.setMessage(Language.get("Progress_WritingDirectory"));
      for (int i = 0; i < numFiles; i++) {
        Resource fd = resources[i];
        long length = fd.getDecompressedLength();

        // 4 - offset
        // 4 - fileLength
        src.skip(8);
        fm.writeInt((int) offset);
        fm.writeInt((int) length);

        // 4 - Unknown
        // 4 - Unknown
        fm.writeBytes(src.readBytes(8));

        offset += length;
      }

      // we know the dirOffset now, so put it at the start of the file
      fm.seek(0);
      fm.writeInt((int) dirOffset);

      src.close();

      fm.close();

    }
    catch (Throwable t) {
      logError(t);
    }
  }

}