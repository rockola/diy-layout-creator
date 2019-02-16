/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.images;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Loads image resources as Icons.
 * 
 * @author Branislav Stojkovic
 */
public enum IconLoader {

  Delete("delete.png"), Add("add.png"), FolderOut("folder_out.png"), Garbage("garbage.png"), DiskBlue("disk_blue.png"), SaveAs(
      "save_as.png"), Exit("exit.png"), DocumentPlainYellow("document_plain_yellow.png"), PhotoScenery(
      "photo_scenery.png"), LightBulbOn("lightbulb_on.png"), LightBulbOff("lightbulb_off.png"), NotebookAdd(
      "notebook_add.png"), FormGreen("form_green.png"), Gears("gears.png"), About("about.png"), WindowColors(
      "window_colors.png"), WindowGear("window_gear.png"), NavigateCheck("navigate_check.png"), Undo("undo.png"), Error(
      "error.png"), Warning("warning.png"), ZoomSmall("zoom_small.png"), MoveSmall("move_small.png"), Print("print.png"), PDF(
      "pdf.png"), Excel("excel.png"), CSV("csv.png"), HTML("html.png"), Image("image.png"), Cut("cut.png"), Copy(
      "copy.png"), Paste("paste.png"), Selection("selection.png"), BOM("bom.png"), BlackBoard("blackboard.png"), IdCard(
      "id_card.png"), IdCardAdd("id_card_add.png"), Chest("chest.png"), Upload("upload.png"), Wrench("wrench.png"), Group(
      "group.png"), Ungroup("ungroup.png"), TraceMask("trace_mask.png"), Faq("faq.png"), Component("component.png"), Plugin(
      "plugin.png"), Manual("manual.png"), Donate("donate.png"), Bug("bug.png"), IconLarge("icon_large.png"), IconMedium(
      "icon_medium.png"), IconSmall("icon_small.png"), DocumentEdit("document_edit.png"), EditComponent(
      "edit_component.png"), Size("size.png"), Front("front.png"), Back("back.png"), Pens("pens.png"), Sort("sort.png"), ElementsSelection(
      "elements_selection.png"), BranchAdd("branch_add.png"), BriefcaseAdd("briefcase_add.png"), BriefcaseInto(
      "briefcase_into.png"), RotateCW("rotate_cw.png"), RotateCCW("rotate_ccw.png"), ElementInto("element_into.png"), Arrow(
      "arrow.png"), Cloud("cloud.png"), CloudUp("cloud_up.png"), CloudGear("cloud_gear.png"), IdCardEdit(
      "id_card_edit.png"), KeyEdit("key_edit.png"), Find("find.png"), Dashboard("dashboard.png"), DataFind(
      "data_find.png"), CloudDownload("cloud_download.png"), CloudDelete("cloud_delete.png"), CloudEdit(
      "cloud_edit.png"), CloudUpload("cloud_upload.png"), NavLeftBlue("nav_left_blue.png"), NavRightBlue(
      "nav_right_blue.png"), MissingImage("missing_image.png"), CloudBg("cloud_bg.png"), CloudBig("cloud_big.png"), CloudWait(
      "cloud_wait.png"), Spinning("spinning.gif"), Megaphone("megaphone.png"), Download("download.png"), Eye("eye.png"), Messages(
      "messages.png"), SearchBox("search-box.png"), Screwdriver("screwdriver.png"), Hammer("hammer.png"), FlipHorizontal(
      "flip_horizontal.png"), FlipVertical("flip_vertical.png"), MagicWand("magic_wand.png"), PinGrey("pin_grey.png"), PinGreen(
      "pin_green.png"), CoffeebeanEdit("coffeebean_edit.png"), ApplicationEdit("application_edit.png"), ComponentAdd(
      "component_add.png"), History("history.png"), DocumentPlain("document_plain.png"), FitToSize("fit_to_size.png"), DocumentsGear(
      "documents_gear.png"), SplashResistor("splash_resistor.png"), SplashCeramic("splash_ceramic.png"), SplashElectrolytic(
      "splash_electrolytic.png"), SplashFilm("splash_film.png"), Splash("splash.png"), Help("help2.png"), ScrollInformation("scroll_information.png"),
      Node("node.png"), Web("web.png"), Guitar("guitar.png"), Scientist("scientist.png");

  protected String name;

  private IconLoader(String name) {
    this.name = name;
  }

  public Icon getIcon() {
    java.net.URL imgURL = getClass().getResource(name);
    if (imgURL != null) {
      return new ImageIcon(imgURL, name);
    } else {
      System.err.println("Couldn't find file: " + name);
      return null;
    }
  }

  public Image getImage() {
    BufferedImage img = null;
    try {
      img = ImageIO.read(getClass().getResourceAsStream(name));
    } catch (IOException e) {
      System.err.println("Couldn't find file: " + name);
    }
    return img;
  }
}
