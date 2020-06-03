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
import java.awt.Image;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import org.watto.Settings;
import org.watto.datatype.ImageResource;
import org.watto.event.WSClickableInterface;
import org.watto.event.WSSelectableInterface;
import org.watto.event.listener.WSSelectableListener;
import org.watto.plaf.ButterflyImageBackgroundPanelUI;
import org.watto.task.Task;
import org.watto.task.Task_ImagePreviewAnimation;
import org.watto.xml.XMLReader;

public class PreviewPanel_Image extends PreviewPanel implements WSClickableInterface, WSSelectableInterface {

  /** serialVersionUID */
  private static final long serialVersionUID = 1L;

  Image image;

  int imageWidth;

  int imageHeight;

  Image zoomImage = null;

  ImageResource imageResource = null;

  Task_ImagePreviewAnimation animation = null;

  /**
  **********************************************************************************************
  DO NOT USE - Only to generate a dummy panel for finding writable ViewerPlugins for this type
  **********************************************************************************************
  **/
  public PreviewPanel_Image() {
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public PreviewPanel_Image(Image imageIn, int widthIn, int heightIn) {
    super();

    image = imageIn;
    imageWidth = widthIn;
    imageHeight = heightIn;

    zoomImage = image;

    imageResource = null;

    createInterface();

    /*
    // 3.10 commented this out, and added createInterface above, because it's basically just duplicated code for some reason 
    ImageIcon icon = new ImageIcon(image);
    
    WSScrollPane scrollPane = new WSScrollPane(XMLReader.read("<WSScrollPane showBorder=\"true\" showInnerBorder=\"true\" opaque=\"false\"><WSPanel><WSLabel code=\"PreviewPanel_Image_ImageLabel\" opaque=\"true\" /></WSPanel></WSScrollPane>"));
    WSLabel imageLabel = (WSLabel) ComponentRepository.get("PreviewPanel_Image_ImageLabel");
    imageLabel.setIcon(icon);
    
    // Set the background color to whatever was last chosen
    WSPanel imageBackground = (WSPanel) ComponentRepository.get("PreviewPanel_Image_Background");
    if (imageBackground == null) {
      imageBackground = (WSPanel) imageLabel.getParent();
      if (imageBackground != null) {
        imageBackground.setObeyBackgroundColor(true);
    
        String backgroundColor = Settings.getString("PreviewPanel_Image_BackgroundColor");
        if (backgroundColor.equals("BLACK")) {
          imageBackground.setBackground(Color.BLACK);
        }
        else {
          imageBackground.setBackground(Color.WHITE);
        }
      }
    }
    
    add(scrollPane, BorderLayout.CENTER);
    */
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public PreviewPanel_Image(ImageResource imageResourceIn) {
    super();

    imageResource = imageResourceIn;

    image = imageResource.getImage();
    imageWidth = imageResource.getWidth();
    imageHeight = imageResource.getHeight();

    zoomImage = image;

    createInterface();
  }

  boolean fitToPanel = false;

  /**
   **********************************************************************************************
   
   **********************************************************************************************
   **/
  public void generateZoomImage() {
    //setFitToPanel(Settings.getBoolean("PreviewPanel_Image_FitToPanel"));

    if (!fitToPanel) {
      // no zoom - show full size
      zoomImage = image;
      return;
    }

    if (imageLabel == null || scrollPane == null) {
      // we haven't loaded the interface yet
      zoomImage = image;
      return;
    }

    // shrink the image to fit in the size of the panel

    // get the panel dimensions
    //int panelWidth = imageLabel.getWidth();
    //int panelHeight = imageLabel.getHeight();
    //Rectangle viewport = scrollPane.getViewportBorderBounds();
    int panelWidth = scrollPane.getViewport().getWidth();
    int panelHeight = scrollPane.getViewport().getHeight();

    if (panelWidth <= 0 || panelHeight <= 0) {
      // we haven't loaded the interface yet
      zoomImage = image;
      return;

    }

    if (imageWidth <= panelWidth && imageHeight <= panelHeight) {
      // no zoom - already fits in the panel
      zoomImage = image;
      return;
    }

    // work out which dimension needs to be shrunk the most
    int widthDifference = 0;

    int heightDifference = 0;

    if (imageWidth > panelWidth) {
      widthDifference = imageWidth - panelWidth;
    }
    if (imageHeight > panelHeight) {
      heightDifference = imageHeight - panelHeight;
    }

    // shrink according to the largest dimension difference
    if (widthDifference >= heightDifference) {
      // shrink on width
      zoomImage = image.getScaledInstance(panelWidth, -1, Image.SCALE_SMOOTH);
    }
    else {
      // shrink on height
      zoomImage = image.getScaledInstance(-1, panelHeight, Image.SCALE_SMOOTH);
    }

  }

  /**
   **********************************************************************************************
   
   **********************************************************************************************
   **/
  @Override
  public boolean onDeselect(JComponent c, Object e) {
    if (c instanceof WSCheckBox) { // WSCheckBox, not WSOptionCheckBox, because we've registered the listener on the checkbox
      WSCheckBox checkbox = (WSCheckBox) c;
      String code = checkbox.getCode();
      if (code.equals("PreviewPanel_Image_FitToPanel")) {
        setFitToPanel(false);
        generateZoomImage();
        reloadImage();
      }
      else if (code.equals("TiledPreviewBackground")) {
        repaint();
      }

      return true; // changing the Setting is handled by a separate listener on the WSObjectCheckbox class, so we can return true here OK
    }
    return false;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  @Override
  public boolean onSelect(JComponent c, Object e) {
    if (c instanceof WSCheckBox) { // WSCheckBox, not WSOptionCheckBox, because we've registered the listener on the checkbox
      WSCheckBox checkbox = (WSCheckBox) c;
      String code = checkbox.getCode();
      if (code.equals("PreviewPanel_Image_FitToPanel")) {
        setFitToPanel(true);
        generateZoomImage();
        reloadImage();
      }
      else if (code.equals("TiledPreviewBackground")) {
        repaint();
      }

      return true; // changing the Setting is handled by a separate listener on the WSObjectCheckbox class, so we can return true here OK
    }
    return false;
  }

  WSLabel imageLabel = null;

  WSScrollPane scrollPane = null;

  /**
  **********************************************************************************************
  Reloads the existing image (after, for example, doing a zoom)
  **********************************************************************************************
  **/
  public void reloadImage() {
    ImageIcon icon = new ImageIcon(zoomImage);
    imageLabel.setIcon(icon);
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public void createInterface() {

    setFitToPanel(Settings.getBoolean("PreviewPanel_Image_FitToPanel"));
    //generateZoomImage();

    //ImageIcon icon = new ImageIcon(zoomImage);

    scrollPane = new WSScrollPane(XMLReader.read("<WSScrollPane showBorder=\"true\" showInnerBorder=\"true\" opaque=\"false\"><WSPanel obeyBackgroundColor=\"true\" code=\"PreviewPanel_Image_Background\"><WSLabel code=\"PreviewPanel_Image_ImageLabel\" opaque=\"true\" /></WSPanel></WSScrollPane>"));

    imageLabel = (WSLabel) ComponentRepository.get("PreviewPanel_Image_ImageLabel");
    //imageLabel.setIcon(icon);

    // Set the background color to whatever was last chosen
    WSPanel imageBackground = (WSPanel) ComponentRepository.get("PreviewPanel_Image_Background");
    if (imageBackground == null) {
      imageBackground = (WSPanel) imageLabel.getParent();
    }

    if (imageBackground != null) {
      imageBackground.setObeyBackgroundColor(true);

      String backgroundColor = Settings.getString("PreviewPanel_Image_BackgroundColor");
      if (backgroundColor.equals("BLACK")) {
        imageBackground.setBackground(Color.BLACK);
      }
      else {
        imageBackground.setBackground(Color.WHITE);
      }

      // Sets the special renderer that will paint the background pattern
      imageBackground.setUI((ButterflyImageBackgroundPanelUI) ButterflyImageBackgroundPanelUI.createUI(imageBackground));
    }

    if (imageResource != null && imageResource.isAnimation()) {
      animation = new Task_ImagePreviewAnimation(imageResource.getNextFrame(), imageLabel);
      animation.setDirection(Task.DIRECTION_REDO);
      new Thread(animation).start();
    }

    // Don't need this listener - the events get passed down to the WSComponent anyway through the Event Chain
    //imageLabel.addMouseListener(new WSClickableListener(this)); // so we can change the background color on click

    add(scrollPane, BorderLayout.CENTER);

    if (imageResource != null && imageResource.isManualFrameTransition()) {
      // Add buttons to move to the next frame
      WSPanel buttonPanel = new WSPanel(XMLReader.read("<WSPanel obeyBackgroundColor=\"true\" code=\"PreviewPanel_Image_ManualFrameButtonsHolder\" layout=\"BorderLayout\"><WSPanel obeyBackgroundColor=\"true\" code=\"PreviewPanel_Image_ManualFrameButtons\" layout=\"GridLayout\" position=\"CENTER\" rows=\"1\" columns=\"2\"><WSButton code=\"PreviewPanel_Image_PreviousButton\" opaque=\"true\" showText=\"true\" /><WSButton code=\"PreviewPanel_Image_NextButton\" opaque=\"true\" showText=\"true\" /></WSPanel></WSPanel>"));
      add(buttonPanel, BorderLayout.SOUTH);
    }
    else {
      // remove the buttons for manual transition
    }

    WSOptionCheckBox fitToPanelCheckbox = new WSOptionCheckBox(XMLReader.read("<WSOptionCheckBox opaque=\"false\" code=\"PreviewPanel_Image_FitToPanel\" setting=\"PreviewPanel_Image_FitToPanel\" />"));
    WSOptionCheckBox transparencyPatternCheckbox = new WSOptionCheckBox(XMLReader.read("<WSOptionCheckBox opaque=\"false\" code=\"PreviewPanel_Image_TransparencyPattern\" setting=\"TiledPreviewBackground\" />"));

    //add a listener to the checkbox, so we can capture and process select/deselect
    WSSelectableListener selectableListener = new WSSelectableListener(this);
    fitToPanelCheckbox.addItemListener(selectableListener);
    transparencyPatternCheckbox.addItemListener(selectableListener);

    WSPanel topPanel = new WSPanel(XMLReader.read("<WSPanel showBorder=\"true\" layout=\"GridLayout\" rows=\"1\" columns=\"2\" />"));
    topPanel.add(fitToPanelCheckbox);
    topPanel.add(transparencyPatternCheckbox);

    add(topPanel, BorderLayout.NORTH);

    // this is now called by SidePanel_Preview AFTER the panel is added to the interface, so we know the panel size and can zoom the image appropriately

    // now that the interface is built, load the image
    generateZoomImage();
    ImageIcon icon = new ImageIcon(zoomImage);
    imageLabel.setIcon(icon);

  }

  public boolean isFitToPanel() {
    return fitToPanel;
  }

  public void setFitToPanel(boolean fitToPanel) {
    this.fitToPanel = fitToPanel;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public Image getImage() {
    return image;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public int getImageHeight() {
    return imageHeight;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public ImageResource getImageResource() {
    return imageResource;
  }

  /**
  **********************************************************************************************
  
  **********************************************************************************************
  **/
  public int getImageWidth() {
    return imageWidth;
  }

  /**
  **********************************************************************************************
  Toggle the background color behind the image when the user clicks on it
  **********************************************************************************************
  **/
  @Override
  public boolean onClick(JComponent source, MouseEvent event) {
    if (source instanceof WSComponent) {
      if (((WSComponent) source).getCode().equals("PreviewPanel_Image_ImageLabel")) {

        WSPanel imageBackground = (WSPanel) ComponentRepository.get("PreviewPanel_Image_Background");
        if (imageBackground == null) {
          imageBackground = (WSPanel) source.getParent();
          if (imageBackground == null) {
            return false;
          }
          else {
            imageBackground.setObeyBackgroundColor(true);
          }
        }

        if (imageBackground.getBackground().equals(Color.BLACK)) {
          imageBackground.setBackground(Color.WHITE);
          Settings.set("PreviewPanel_Image_BackgroundColor", "WHITE");
        }
        else {
          imageBackground.setBackground(Color.BLACK);
          Settings.set("PreviewPanel_Image_BackgroundColor", "BLACK");
        }
        return true;
      }
      else if (((WSComponent) source).getCode().equals("PreviewPanel_Image_NextButton")) {
        // show the next frame
        WSLabel imageLabel = (WSLabel) ComponentRepository.get("PreviewPanel_Image_ImageLabel");

        ImageResource nextFrame = imageResource.getNextFrame();
        if (nextFrame != null) {
          imageResource = nextFrame;

          image = imageResource.getImage(); // important, so the Export Preview button exports the right image
          generateZoomImage();
          imageLabel.setIcon(new ImageIcon(zoomImage));

          Settings.set("PreviewPanel_Image_CurrentFrame", Settings.getInt("PreviewPanel_Image_CurrentFrame") + 1);
        }
      }
      else if (((WSComponent) source).getCode().equals("PreviewPanel_Image_PreviousButton")) {
        // show the previous frame
        WSLabel imageLabel = (WSLabel) ComponentRepository.get("PreviewPanel_Image_ImageLabel");

        ImageResource previousFrame = imageResource.getPreviousFrame();
        if (previousFrame != null) {
          imageResource = previousFrame;

          image = imageResource.getImage(); // important, so the Export Preview button exports the right image
          generateZoomImage();
          imageLabel.setIcon(new ImageIcon(zoomImage));

          Settings.set("PreviewPanel_Image_CurrentFrame", Settings.getInt("PreviewPanel_Image_CurrentFrame") - 1);
        }
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
    image = null;
    zoomImage = image;

    if (animation != null) {
      animation.stop();
    }
  }

}