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

package org.watto.task;

import java.io.File;
import java.util.Arrays;
import org.watto.Language;
import org.watto.SingletonManager;
import org.watto.component.ComponentRepository;
import org.watto.component.PreviewPanel;
import org.watto.component.WSDirectoryListHolder;
import org.watto.component.WSPopup;
import org.watto.datatype.Resource;
import org.watto.ge.helper.ShellFolderFile;
import org.watto.ge.plugin.ExporterPlugin;
import org.watto.ge.plugin.PluginFinder;
import org.watto.ge.plugin.RatedPlugin;
import org.watto.ge.plugin.ViewerPlugin;
import org.watto.ge.plugin.exporter.BlockQuickBMSExporterWrapper;
import org.watto.ge.plugin.exporter.Exporter_QuickBMSWrapper;
import org.watto.ge.plugin.exporter.Exporter_QuickBMS_Decompression;
import org.watto.io.DirectoryBuilder;
import sun.awt.shell.ShellFolder;

/**
**********************************************************************************************

**********************************************************************************************
**/
public class Task_ExportFiles extends AbstractTask {

  /** The direction to perform in the thread **/
  int direction = 1;

  File directory;

  Resource[] resources;

  ViewerPlugin[] converterPlugins = null;

  // so we can stop "file exported" popup from appearing when doing a preview
  boolean showPopups = true;

  // so we can stop "file being exported" progress popup from appearing when doing a preview
  boolean showProgressPopups = true;

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Task_ExportFiles(File directory, Resource resource) {
    this(directory, new Resource[] { resource });
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Task_ExportFiles(File directory, Resource[] resources) {
    this.directory = directory;
    this.resources = resources;
    showPopups = true;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public void redo() {
    if (!TaskProgressManager.canDoTask()) {
      return;
    }

    if (resources.length <= 0) {
      if (showPopups) {
        if (isShowPopups()) {
          WSPopup.showError("ExportFiles_NoFilesSelected", true);
        }
        TaskProgressManager.stopTask();
      }
      return;
    }

    // Progress dialog

    if (showProgressPopups) {
      TaskProgressManager.show(2, 0, Language.get("Progress_ExportingFiles")); // 2 progress bars
      TaskProgressManager.setIndeterminate(true, 0); // first 1 is indeterminate
      TaskProgressManager.setMaximum(resources.length, 1); // second one shows how many files are done

      TaskProgressManager.startTask();
    }

    // check the directory exists
    if (directory.exists()) {
      if (directory.isFile()) {
        directory = directory.getParentFile();
      }
    }

    try {
      if (directory instanceof ShellFolder || directory instanceof ShellFolderFile) {
        directory = directory.getCanonicalFile();
      }
    }
    catch (Throwable t) {
    }

    DirectoryBuilder.buildDirectory(directory, true);

    // SEE BELOW THIS CODE BLOCK FOR THE CHANGE...
    /*
    // If we're using the QuickBMS Plugin, we want to do the exports in bulk
    boolean quickBMS = false;
    if (resources.length > 1) {
      Resource firstResource = resources[0];
      //if (firstResource.getExporter() instanceof Exporter_QuickBMSWrapper) {
      ExporterPlugin exporter = firstResource.getExporter();
      if (exporter instanceof Exporter_QuickBMSWrapper || exporter instanceof Exporter_QuickBMS_Decompression) {
        quickBMS = true;
    
        Task_QuickBMSBulkExport task = new Task_QuickBMSBulkExport(resources, directory);
        task.redo(); // run it within this Thread, not as a new one
      }
    }
    
    // otherwise, for all normal cases, we want to extract files using Game Extractor
    if (!quickBMS) {
      for (int i = 0; i < resources.length; i++) {
        resources[i].extract(directory);
        TaskProgressManager.setValue(i, 1); // update the value of the second progress bar
      }
    }
    */
    // Unlike the above...
    // Because files in an archive can have different compressions (eg some with ZLib, some the XYZ, some with no compression), we actually
    // need to collect all the QuickBMS ones for a bulk extract, and all the others can be extracted normally.
    int numResources = resources.length;
    Resource[] bulkResources = new Resource[numResources];
    int numBulkResources = 0;

    for (int i = 0; i < numResources; i++) {
      Resource resource = resources[i];
      ExporterPlugin exporter = resource.getExporter();
      if (exporter instanceof Exporter_QuickBMSWrapper || exporter instanceof Exporter_QuickBMS_Decompression || exporter instanceof BlockQuickBMSExporterWrapper) {
        // add it to the Bulk list
        bulkResources[numBulkResources] = resource;
        numBulkResources++;
      }
      else {
        // extract it normally
        resources[i].extract(directory);
        TaskProgressManager.setValue(i, 1); // update the value of the second progress bar
      }
    }
    // Now run the bulk extract
    if (numBulkResources > 0) {
      if (numBulkResources != numResources) {
        Resource[] oldResources = bulkResources;
        bulkResources = new Resource[numBulkResources];
        System.arraycopy(oldResources, 0, bulkResources, 0, numBulkResources);
      }

      Task_QuickBMSBulkExport task = new Task_QuickBMSBulkExport(bulkResources, directory);
      task.redo(); // run it within this Thread, not as a new one
    }

    // Now that we have exported all the files, see whether they chose to do conversions (eg convert images --> *.PNG).
    int numConverters = 0;
    if (converterPlugins != null) {
      numConverters = converterPlugins.length;
    }

    if (numConverters > 0) {

      TaskProgressManager.setMessage(Language.get("Progress_ConvertingFiles"));
      TaskProgressManager.setValue(0, 1); // update the value of the second progress bar

      // Go through all the files that were extracted and look for a Viewer that will handle it.
      for (int r = 0; r < resources.length; r++) {
        Resource resource = resources[r];
        File path = resource.getExportedPath();

        // Analyse the file for compatible Viewers
        RatedPlugin[] plugins = PluginFinder.findPlugins(path, ViewerPlugin.class);

        // Try each matching Viewer until we find one that succeeds in previewing the file completely
        if (plugins != null && plugins.length > 0) {
          Arrays.sort(plugins);

          SingletonManager.set("CurrentResource", resource);

          // try to open the preview using each plugin
          for (int i = 0; i < plugins.length; i++) {
            ViewerPlugin previewPlugin = (ViewerPlugin) plugins[i].getPlugin();
            PreviewPanel previewPanel = previewPlugin.read(path);

            if (previewPanel != null) {
              // Found a Viewer, which was able to generate a PreviewPanel for the file.
              // If the type of PreviewPanel matches with a chosen Conversion type, perform the conversion.

              // Loop through all the chosen converters until we find the matching one
              for (int c = 0; c < numConverters; c++) {
                ViewerPlugin converterPlugin = converterPlugins[c];
                if (converterPlugin.canWrite(previewPanel)) {
                  // found the converter for this type, so run the conversion

                  File destination = new File(path.getAbsolutePath() + "." + converterPlugin.getExtension(0));
                  converterPlugin.write(previewPanel, destination);

                  // skip all the other converters - we've found the right one already
                  c = numConverters;
                }
              }

              // skip the rest of the plugins - we're done with this file
              i = plugins.length;
            }

          }
        }

        TaskProgressManager.setValue(r, 1); // update the value of the second progress bar
      }
    }

    TaskProgressManager.stopTask();

    // Reload the DirectoryList if it's changed
    ((WSDirectoryListHolder) ComponentRepository.get("SidePanel_DirectoryList_DirectoryListHolder")).reload();

    if (showPopups) {
      if (isShowPopups()) {
        WSPopup.showMessage("ExportFiles_FilesExported", true);
      }
    }

  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public void setConverterPlugins(ViewerPlugin[] converterPlugins) {
    this.converterPlugins = converterPlugins;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public void setShowPopups(boolean showPopups) {
    this.showPopups = showPopups;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public void setShowProgressPopups(boolean showProgressPopups) {
    this.showProgressPopups = showProgressPopups;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  @SuppressWarnings("rawtypes")
  public String toString() {
    Class cl = getClass();
    String name = cl.getName();
    Package pack = cl.getPackage();

    if (pack != null) {
      name = name.substring(pack.getName().length() + 1);
    }

    return Language.get(name);
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public void undo() {
    if (!TaskProgressManager.canDoTask()) {
      return;
    }
  }

}
