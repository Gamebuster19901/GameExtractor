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

package org.watto.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.watto.ErrorLogger;
import org.watto.Settings;
import org.watto.event.WSClickableInterface;
import org.watto.event.WSEnterableInterface;
import org.watto.event.WSSelectableInterface;
import org.watto.ge.helper.ImageFormatReader;
import org.watto.io.FileManipulator;
import org.watto.xml.XMLReader;

public class PreviewPanel_ImageInvestigator extends PreviewPanel_Image implements WSClickableInterface, WSSelectableInterface, WSEnterableInterface {

  /** serialVersionUID */
  private static final long serialVersionUID = 1L;

  File file = null;

  WSTextField offsetField;

  WSTextField widthField;

  WSTextField heightField;

  WSComboBox formatChooser;

  WSButton regenerateImageButton;

  WSCheckBox swizzleCheckbox;

  WSCheckBox verticalFlipCheckbox;

  WSRadioButton littleEndianRadioButton;

  WSRadioButton bigEndianRadioButton;

  /**
  **********************************************************************************************
  DO NOT USE - Only to generate a dummy panel for finding writable ViewerPlugins for this type
  **********************************************************************************************
  **/
  public PreviewPanel_ImageInvestigator() {
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public PreviewPanel_ImageInvestigator(File file) {
    super();

    createInterface();

    loadFile(file);

    /*
    ImageIcon icon = new ImageIcon(image);
    
    WSScrollPane scrollPane = new WSScrollPane(XMLReader.read("<WSScrollPane showBorder=\"true\" showInnerBorder=\"true\" opaque=\"false\"><WSPanel><WSLabel code=\"PreviewPanel_Image_ImageLabel\" opaque=\"true\" /></WSPanel></WSScrollPane>"));
    WSLabel imageLabel = (WSLabel) ComponentRepository.get("PreviewPanel_Image_ImageLabel");
    imageLabel.setIcon(icon);
    
    add(scrollPane, BorderLayout.CENTER);
    */
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public void loadFile(File file) {
    this.file = file;
    regenerateImage();
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public void createInterface() {
    WSScrollPane scrollPane = new WSScrollPane(XMLReader.read("<WSScrollPane showBorder=\"true\" showInnerBorder=\"true\" opaque=\"false\"><WSPanel obeyBackgroundColor=\"true\" code=\"PreviewPanel_ImageInvestigator_Background\"><WSLabel code=\"PreviewPanel_ImageInvestigator_ImageLabel\" opaque=\"true\" /></WSPanel></WSScrollPane>"));
    add(scrollPane, BorderLayout.CENTER);

    offsetField = new WSTextField(XMLReader.read("<WSTextField code=\"PreviewPanel_ImageInvestigator_OffsetField\" showLabel=\"true\" />"));
    widthField = new WSTextField(XMLReader.read("<WSTextField code=\"PreviewPanel_ImageInvestigator_WidthField\" showLabel=\"true\" />"));
    heightField = new WSTextField(XMLReader.read("<WSTextField code=\"PreviewPanel_ImageInvestigator_HeightField\" showLabel=\"true\" />"));

    String offsetValue = Settings.getString("PreviewPanel_ImageInvestigator_Offset");
    if (offsetValue == null || offsetValue.length() <= 0) {
      offsetValue = "0";
    }

    String widthValue = Settings.getString("PreviewPanel_ImageInvestigator_Width");
    if (widthValue == null || widthValue.length() <= 0) {
      widthValue = "256";
    }

    String heightValue = Settings.getString("PreviewPanel_ImageInvestigator_Height");
    if (heightValue == null || heightValue.length() <= 0) {
      heightValue = "256";
    }

    offsetField.setText(offsetValue);
    widthField.setText(widthValue);
    heightField.setText(heightValue);

    WSPanel controlsPanel = new WSPanel(XMLReader.read("<WSPanel layout=\"GridLayout\" rows=\"3\" columns=\"1\" />"));
    controlsPanel.add(offsetField);
    controlsPanel.add(widthField);
    controlsPanel.add(heightField);

    formatChooser = new WSComboBox(XMLReader.read("<WSComboBox code=\"PreviewPanel_ImageInvestigator_FormatChooser\" />"));
    regenerateImageButton = new WSButton(XMLReader.read("<WSButton code=\"PreviewPanel_ImageInvestigator_RegenerateImageButton\" />"));

    WSPanel formatChooserPanel = new WSPanel(XMLReader.read("<WSPanel code=\"PreviewPanel_ImageInvestigator_FormatChooserLabel\" showLabel=\"true\" showBorder=\"true\" />"));
    formatChooserPanel.add(formatChooser, BorderLayout.CENTER);
    formatChooserPanel.add(regenerateImageButton, BorderLayout.EAST);

    swizzleCheckbox = new WSCheckBox(XMLReader.read("<WSCheckBox code=\"PreviewPanel_ImageInvestigator_SwizzleCheckbox\" horizontal-alignment=\"left\" />"));
    swizzleCheckbox.setSelected(Settings.getBoolean("PreviewPanel_ImageInvestigator_SwizzleCheckboxSelected"));
    verticalFlipCheckbox = new WSCheckBox(XMLReader.read("<WSCheckBox code=\"PreviewPanel_ImageInvestigator_VerticalFlipCheckbox\" horizontal-alignment=\"left\" />"));
    verticalFlipCheckbox.setSelected(Settings.getBoolean("PreviewPanel_ImageInvestigator_VerticalFlipCheckboxSelected"));
    littleEndianRadioButton = new WSRadioButton(XMLReader.read("<WSRadioButton code=\"PreviewPanel_ImageInvestigator_LittleEndianRadioButton\" group=\"PreviewPanel_ImageInvestigator_EndianGroup\" opaque=\"false\" />"));
    littleEndianRadioButton.setSelected(Settings.getBoolean("PreviewPanel_ImageInvestigator_LittleEndianRadioButtonSelected"));
    bigEndianRadioButton = new WSRadioButton(XMLReader.read("<WSRadioButton code=\"PreviewPanel_ImageInvestigator_BigEndianRadioButton\" group=\"PreviewPanel_ImageInvestigator_EndianGroup\" opaque=\"false\" />"));
    bigEndianRadioButton.setSelected(Settings.getBoolean("PreviewPanel_ImageInvestigator_BigEndianRadioButtonSelected"));

    WSPanel checkboxPanel = new WSPanel(XMLReader.read("<WSPanel layout=\"GridLayout\" rows=\"2\" columns=\"1\" />"));
    checkboxPanel.add(swizzleCheckbox);
    checkboxPanel.add(verticalFlipCheckbox);

    WSPanel radioButtonPanel = new WSPanel(XMLReader.read("<WSPanel layout=\"GridLayout\" rows=\"2\" columns=\"1\" code=\"PreviewPanel_ImageInvestigator_EndianGroup\" showLabel=\"true\" showBorder=\"true\" />"));
    radioButtonPanel.add(littleEndianRadioButton);
    radioButtonPanel.add(bigEndianRadioButton);

    WSPanel bottomRightPanel = new WSPanel(XMLReader.read("<WSPanel layout=\"GridLayout\" rows=\"2\" columns=\"1\" />"));
    bottomRightPanel.add(checkboxPanel);
    bottomRightPanel.add(radioButtonPanel);

    WSPanel bottomPanel = new WSPanel(XMLReader.read("<WSPanel border-width=\"6\" />"));
    bottomPanel.add(controlsPanel, BorderLayout.CENTER);
    bottomPanel.add(bottomRightPanel, BorderLayout.EAST);

    WSPanel bottomPanelWithFormats = new WSPanel(XMLReader.read("<WSPanel />"));
    bottomPanelWithFormats.add(bottomPanel, BorderLayout.NORTH);
    bottomPanelWithFormats.add(formatChooserPanel, BorderLayout.CENTER);

    add(bottomPanelWithFormats, BorderLayout.SOUTH);

    loadImageFormats();

    String chosenFormat = Settings.getString("PreviewPanel_ImageInvestigator_Format");
    formatChooser.setSelectedItem(chosenFormat);
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public void loadImageFormats() {
    String[] imageFormats = new String[] {
        "DXT1",
        "DXT3",
        "DXT5",
        //"DXT1_BigEndian4",
        //"DXT3_BigEndian4",
        //"DXT5_BigEndian4",
        //"DXT1_BigEndian8",
        //"DXT3_BigEndian8",
        //"DXT5_BigEndian8",
        //"DXT3_BigEndian16",
        //"DXT5_BigEndian16",
        //"DXT5Swizzled",
        "BC5",
        "ETC2_RGBA8",
        "ARGB",
        "ABGR",
        "RGBA",
        "BGRA",
        "ARGB4444",
        "ABGR4444",
        "BARG4444",
        "RGBA4444",
        "BGRA4444",
        "GBAR4444",
        "RGB",
        "BGR",
        "RGB555",
        //"RGB555_BigEndian",
        "RGB565",
        //"RGB565_BigEndian",
        //        "RGB565_Switch",
        "BGR565",
        //"BGR565_BigEndian",
        "RGBA5551",
        //"RGBA5551_BigEndian",
        "ARGB1555",
        //"ARGB1555_BigEndian",
        "BGRA5551",
        //"BGRA5551_BigEndian",
        "RG",
        "G16R16",
        "R16G16",
        "L8A8",
        "A8L8",
        "U8V8",
        "16F16F16F16F_ARGB",
        "16F16F16F16F_ABGR",
        "16F16F16F16F_RGBA",
        "16F16F16F16F_BGRA",
        "32F32F32F32F_ARGB",
        "32F32F32F32F_ABGR",
        "32F32F32F32F_RGBA",
        "32F32F32F32F_BGRA",
        "4BitPaletted",
        "8BitPaletted"
    };

    formatChooser.setModel(new DefaultComboBoxModel(imageFormats));
  }

  /**
  **********************************************************************************************
  Toggle the background color behind the image when the user clicks on it
  **********************************************************************************************
  **/
  @Override
  public boolean onClick(JComponent source, MouseEvent event) {
    if (source instanceof WSComponent) {
      String code = ((WSComponent) source).getCode();
      if (code.equals("PreviewPanel_ImageInvestigator_ImageLabel")) {

        WSPanel imageBackground = (WSPanel) ComponentRepository.get("PreviewPanel_ImageInvestigator_Background");

        if (imageBackground.getBackground().equals(Color.BLACK)) {
          imageBackground.setBackground(Color.WHITE);
        }
        else {
          imageBackground.setBackground(Color.BLACK);
        }
        return true;
      }
      else if (code.equals("PreviewPanel_ImageInvestigator_RegenerateImageButton")) {
        regenerateImage();
        return true;
      }
      else if (code.equals("PreviewPanel_ImageInvestigator_SwizzleCheckbox")) {
        Settings.set("PreviewPanel_ImageInvestigator_SwizzleCheckboxSelected", swizzleCheckbox.isSelected());
        regenerateImage();
        return true;
      }
      else if (code.equals("PreviewPanel_ImageInvestigator_VerticalFlipCheckbox")) {
        Settings.set("PreviewPanel_ImageInvestigator_VerticalFlipCheckboxSelected", verticalFlipCheckbox.isSelected());
        regenerateImage();
        return true;
      }
      else if (code.equals("PreviewPanel_ImageInvestigator_LittleEndianRadioButton")) {
        Settings.set("PreviewPanel_ImageInvestigator_LittleEndianRadioButtonSelected", littleEndianRadioButton.isSelected());
        Settings.set("PreviewPanel_ImageInvestigator_BigEndianRadioButtonSelected", bigEndianRadioButton.isSelected());
        regenerateImage();
        return true;
      }
      else if (code.equals("PreviewPanel_ImageInvestigator_BigEndianRadioButton")) {
        Settings.set("PreviewPanel_ImageInvestigator_LittleEndianRadioButtonSelected", littleEndianRadioButton.isSelected());
        Settings.set("PreviewPanel_ImageInvestigator_BigEndianRadioButtonSelected", bigEndianRadioButton.isSelected());
        regenerateImage();
        return true;
      }

    }

    return false;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public void onCloseRequest() {
    // Flush the variables clear for garbage collection
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public boolean onDeselect(JComponent source, Object event) {
    return false;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public boolean onEnter(JComponent source, KeyEvent event) {

    if (source instanceof WSComponent) {
      String code = ((WSComponent) source).getCode();
      if (code.equals("PreviewPanel_ImageInvestigator_OffsetField")) {
        regenerateImage();
      }
      else if (code.equals("PreviewPanel_ImageInvestigator_WidthField")) {
        regenerateImage();
      }
      else if (code.equals("PreviewPanel_ImageInvestigator_HeightField")) {
        regenerateImage();
      }
      else {
        return false;
      }
      return true;
    }

    return false;
  }

  /**
   **********************************************************************************************
   * The event that is triggered from a WSSelectableListener when an item is selected
   * @param c the component that triggered the event
   * @param e the event that occurred
   **********************************************************************************************
   **/
  @Override
  public boolean onSelect(JComponent c, Object e) {
    if (c instanceof WSComponent) {
      String code = ((WSComponent) c).getCode();
      if (code.equals("PreviewPanel_ImageInvestigator_FormatChooser")) {
        regenerateImage();
      }
    }

    // catch-all to stop the combobox being caught by the SidePanel_Preview
    return true;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public void regenerateImage() {
    WSLabel imageLabel = (WSLabel) ComponentRepository.get("PreviewPanel_ImageInvestigator_ImageLabel");

    int offset = 0;

    try {
      imageWidth = Integer.parseInt(widthField.getText());
      imageHeight = Integer.parseInt(heightField.getText());
      offset = Integer.parseInt(offsetField.getText());
    }
    catch (Throwable t) {
      // ignore the errors for now
      ErrorLogger.log(t);
    }

    Settings.set("PreviewPanel_ImageInvestigator_Offset", offset);
    Settings.set("PreviewPanel_ImageInvestigator_Width", imageWidth);
    Settings.set("PreviewPanel_ImageInvestigator_Height", imageHeight);

    String format = (String) formatChooser.getSelectedItem();
    Settings.set("PreviewPanel_ImageInvestigator_Format", format);

    if (file == null) {
      return;
    }

    FileManipulator fm = new FileManipulator(file, false);
    fm.seek(offset);

    boolean doSwizzle = swizzleCheckbox.isSelected();
    boolean doVerticalFlip = verticalFlipCheckbox.isSelected();
    boolean bigEndian = bigEndianRadioButton.isSelected(); // little-endian implied otherwise

    try {
      imageResource = null;

      if (format.equals("DXT1")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readDXT1BigEndian(fm, imageWidth, imageHeight, 8);
        }
        else {
          imageResource = ImageFormatReader.readDXT1(fm, imageWidth, imageHeight);
        }
      }
      else if (format.equals("DXT3")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readDXT3BigEndian(fm, imageWidth, imageHeight, 8);
        }
        else {
          imageResource = ImageFormatReader.readDXT3(fm, imageWidth, imageHeight);
        }
      }
      else if (format.equals("DXT5")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readDXT5BigEndian(fm, imageWidth, imageHeight, 8);
        }
        else {
          imageResource = ImageFormatReader.readDXT5(fm, imageWidth, imageHeight);
        }
      }
      /*
      else if (format.equals("DXT1_BigEndian4")) {
        imageResource = ImageFormatReader.readDXT1BigEndian(fm, imageWidth, imageHeight, 4);
      }
      else if (format.equals("DXT3_BigEndian4")) {
        imageResource = ImageFormatReader.readDXT3BigEndian(fm, imageWidth, imageHeight, 4);
      }
      else if (format.equals("DXT5_BigEndian4")) {
        imageResource = ImageFormatReader.readDXT5BigEndian(fm, imageWidth, imageHeight, 4);
      }
      else if (format.equals("DXT1_BigEndian8")) {
        imageResource = ImageFormatReader.readDXT1BigEndian(fm, imageWidth, imageHeight, 8);
      }
      else if (format.equals("DXT3_BigEndian8")) {
        imageResource = ImageFormatReader.readDXT3BigEndian(fm, imageWidth, imageHeight, 8);
      }
      else if (format.equals("DXT5_BigEndian8")) {
        imageResource = ImageFormatReader.readDXT5BigEndian(fm, imageWidth, imageHeight, 8);
      }
      else if (format.equals("DXT3_BigEndian16")) {
        imageResource = ImageFormatReader.readDXT3BigEndian(fm, imageWidth, imageHeight, 16);
      }
      else if (format.equals("DXT5_BigEndian16")) {
        imageResource = ImageFormatReader.readDXT5BigEndian(fm, imageWidth, imageHeight, 16);
      }
      */
      //else if (format.equals("DXT5Swizzled")) {
      //  imageResource = ImageFormatReader.readDXT5Swizzled(fm, imageWidth, imageHeight);
      //}
      else if (format.equals("BC5")) {
        imageResource = ImageFormatReader.readBC5(fm, imageWidth, imageHeight);
      }
      else if (format.equals("ETC2_RGBA8")) {
        imageResource = ImageFormatReader.readETC2_RGBA8(fm, imageWidth, imageHeight);
      }
      else if (format.equals("ARGB")) {
        imageResource = ImageFormatReader.readARGB(fm, imageWidth, imageHeight);
      }
      else if (format.equals("ABGR")) {
        imageResource = ImageFormatReader.readABGR(fm, imageWidth, imageHeight);
      }
      else if (format.equals("RGBA")) {
        imageResource = ImageFormatReader.readRGBA(fm, imageWidth, imageHeight);
      }
      else if (format.equals("BGRA")) {
        imageResource = ImageFormatReader.readBGRA(fm, imageWidth, imageHeight);
      }
      else if (format.equals("ARGB4444")) {
        imageResource = ImageFormatReader.readARGB4444(fm, imageWidth, imageHeight);
      }
      else if (format.equals("ABGR4444")) {
        imageResource = ImageFormatReader.readABGR4444(fm, imageWidth, imageHeight);
      }
      else if (format.equals("BARG4444")) {
        imageResource = ImageFormatReader.readBARG4444(fm, imageWidth, imageHeight);
      }
      else if (format.equals("RGBA4444")) {
        imageResource = ImageFormatReader.readRGBA4444(fm, imageWidth, imageHeight);
      }
      else if (format.equals("BGRA4444")) {
        imageResource = ImageFormatReader.readBGRA4444(fm, imageWidth, imageHeight);
      }
      else if (format.equals("GBAR4444")) {
        imageResource = ImageFormatReader.readGBAR4444(fm, imageWidth, imageHeight);
      }
      else if (format.equals("RGB")) {
        imageResource = ImageFormatReader.readRGB(fm, imageWidth, imageHeight);
      }
      else if (format.equals("BGR")) {
        imageResource = ImageFormatReader.readBGR(fm, imageWidth, imageHeight);
      }
      else if (format.equals("RGB555")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readRGB555BigEndian(fm, imageWidth, imageHeight);
        }
        else {
          imageResource = ImageFormatReader.readRGB555(fm, imageWidth, imageHeight);
        }
      }
      else if (format.equals("RGB565")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readRGB565BigEndian(fm, imageWidth, imageHeight);
        }
        else {
          imageResource = ImageFormatReader.readRGB565(fm, imageWidth, imageHeight);
        }
      }
      //else if (format.equals("RGB565_Switch")) {
      //  imageResource = ImageFormatReader.readRGB565Switch(fm, imageWidth, imageHeight);
      //}
      else if (format.equals("BGR565")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readBGR565BigEndian(fm, imageWidth, imageHeight);
        }
        else {
          imageResource = ImageFormatReader.readBGR565(fm, imageWidth, imageHeight);
        }
      }
      else if (format.equals("RGBA5551")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readRGBA5551BigEndian(fm, imageWidth, imageHeight);
        }
        else {
          imageResource = ImageFormatReader.readRGBA5551(fm, imageWidth, imageHeight);
        }
      }
      else if (format.equals("ARGB1555")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readARGB1555BigEndian(fm, imageWidth, imageHeight);
        }
        else {
          imageResource = ImageFormatReader.readARGB1555(fm, imageWidth, imageHeight);
        }
      }
      else if (format.equals("BGRA5551")) {
        if (bigEndian) {
          imageResource = ImageFormatReader.readBGRA5551BigEndian(fm, imageWidth, imageHeight);
        }
        else {
          imageResource = ImageFormatReader.readBGRA5551(fm, imageWidth, imageHeight);
        }
      }
      else if (format.equals("RG")) {
        imageResource = ImageFormatReader.readRG(fm, imageWidth, imageHeight);
      }
      else if (format.equals("R16G16")) {
        imageResource = ImageFormatReader.readR16G16(fm, imageWidth, imageHeight);
      }
      else if (format.equals("G16R16")) {
        imageResource = ImageFormatReader.readG16R16(fm, imageWidth, imageHeight);
      }
      else if (format.equals("L8A8")) {
        imageResource = ImageFormatReader.readL8A8(fm, imageWidth, imageHeight);
      }
      else if (format.equals("A8L8")) {
        imageResource = ImageFormatReader.readA8L8(fm, imageWidth, imageHeight);
      }
      else if (format.equals("16F16F16F16F_ARGB")) {
        imageResource = ImageFormatReader.read16F16F16F16F_ARGB(fm, imageWidth, imageHeight);
      }
      else if (format.equals("16F16F16F16F_ABGR")) {
        imageResource = ImageFormatReader.read16F16F16F16F_ABGR(fm, imageWidth, imageHeight);
      }
      else if (format.equals("16F16F16F16F_RGBA")) {
        imageResource = ImageFormatReader.read16F16F16F16F_RGBA(fm, imageWidth, imageHeight);
      }
      else if (format.equals("16F16F16F16F_BGRA")) {
        imageResource = ImageFormatReader.read16F16F16F16F_BGRA(fm, imageWidth, imageHeight);
      }
      else if (format.equals("32F32F32F32F_ARGB")) {
        imageResource = ImageFormatReader.read32F32F32F32F_ARGB(fm, imageWidth, imageHeight);
      }
      else if (format.equals("32F32F32F32F_ABGR")) {
        imageResource = ImageFormatReader.read32F32F32F32F_ABGR(fm, imageWidth, imageHeight);
      }
      else if (format.equals("32F32F32F32F_RGBA")) {
        imageResource = ImageFormatReader.read32F32F32F32F_RGBA(fm, imageWidth, imageHeight);
      }
      else if (format.equals("32F32F32F32F_BGRA")) {
        imageResource = ImageFormatReader.read32F32F32F32F_BGRA(fm, imageWidth, imageHeight);
      }
      else if (format.equals("4BitPaletted")) {
        imageResource = ImageFormatReader.read4BitPaletted(fm, imageWidth, imageHeight);
      }
      else if (format.equals("8BitPaletted")) {
        imageResource = ImageFormatReader.read8BitPaletted(fm, imageWidth, imageHeight);
      }
      else if (format.equals("U8V8")) {
        imageResource = ImageFormatReader.readU8V8(fm, imageWidth, imageHeight);
      }

      fm.close();

      if (imageResource == null) {
        return;
      }

      // Apply checkboxes
      if (doSwizzle) {
        imageResource.setPixels(ImageFormatReader.unswizzle(imageResource.getPixels(), imageWidth, imageHeight, 1));
      }
      if (doVerticalFlip) {
        imageResource = ImageFormatReader.flipVertically(imageResource);
      }

      // Paint the imaghe onto the screen
      if (imageResource != null) {
        imageLabel.setIcon(new ImageIcon(imageResource.getImage()));
      }

      // Set the Image, so that the Preview can be saved.
      image = imageResource.getImage();
    }
    catch (Throwable t) {
      ErrorLogger.log(t);
    }

  }

}