## 3.57.0
### Sun Mar 10 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Added an optional third control point to leaded components and some schematic symbols that can be used to set component label position. This feature is off by default to keep things backwards compatible and is driven by "Movable Label" property
#### New Feature
Added mini toolbar in the top-right corner of the screen with transform actions exposed for quick access
#### New Feature
Added "Auto-Wrap Label" component
#### New Feature
Auto-save plugin now works by periodically creating project backups in user\.diylc\backup directory and maintains file history, keeping the last X versions of the file. Maximum size for all backups combined is configurable and pre-set to 64MB
#### Improvement
Ability to show labels for standing components and control the position
#### Improvement
Assign a new name for pasted component if the project already contains components under that name
#### Improvement
Output SPICE node names in the "Nxxx" format
#### Improvement
Improve "Expand Selection" feature to use the same algorithm as netlists for better precision
#### Improvement
Improve performance of netlist calculations
#### Improvement
Honor "Terminal Strip" connectivity when creating a netlist
#### Improvement
Honor all possible combinations of "DIP Switch" positions when creating a netlist
#### Improvement
Merge switch configurations that yield with the same effective wiring for "Analyze Guitar Wirings" module, for a more compact report
#### Improvement
Scan "library" folder for any additional JAR files that may contain more components
#### Bug Fix
When pasting a group of components that contain at least one "PCB Text" instance, some of the other components get rendered mirrored while position the pasted components
#### Bug Fix
Some schematic symbols (transistors, potentiometer...) are not rendered properly when rotated and flipped at the same time
#### Bug Fix
Z-order of components sometimes gets wrong when importing a DIY file into another file
## 3.56.0
### Sun Mar 10 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Ability to tag components and building blocks as favorites and recall them quickly from a separate folder in the component tree
#### New Feature
Generate SPICE-compatible netlist (still in beta stage)
#### New Feature
Added pinout property to all transistor components
#### New Feature
Ability to enter arbitrary angle of rotation for "Open 1/4" Jack" and "Pilot Lamp Holder"
#### Improvement
When in "Highlight Connected Area" mode, the status bar shows useful information making it harder to forget what the mode does
#### Improvement
Cleaner and more compact XML format for .DIY files resulting in files about half the size of originals. Backward compatible with older files, but will introduce issues when opening files from the new version using an old version!
#### Improvement
Detect when a file is created with a newer version of DIYLC and show a warning
#### Bug Fix
"Highlight Connected Areas" doesn't follow multiple chained wires and jumpers
#### Bug Fix
"Mini Toggle Switch" doesn't reset body size after changing terminal spacing
#### Bug Fix
"Ground Symbol" jumps around when being copy-pasted together with other components
## 3.55.1
### Sat Mar 02 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### Bug Fix
Z-order of components is not retained in building blocks
## 3.55.0
### Sat Mar 02 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
"Analyze Guitar Wiring" can figure out if wiring is hum-cancelling and in or out of phase for each position, split-coil wiring, series/parallel humbucking mode, volume and tone potentiometers
#### New Feature
Added two or four terminal hookup to bass pickups types
#### New Feature
Added 4-position Tele switch type to the "Lever Switch" component
#### New Feature
Show wire diameter in mm and in next to AWG gauge
#### Improvement
Increase the range of zoom slider for images to 500%
#### Improvement
Increase the number of recently used files from 10 to 20
#### Bug Fix
Netlist algorithm would sometimes create overlapping groups of nodes which are supposed to be merged
#### Bug Fix
Fixed bug with "Highlight Connected Areas" that makes random components conductive
#### Bug Fix
Fixed rendering issue with "Lever Switch" when rotated
## 3.54.0
### Tue Feb 26 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
"Create Netlist" feature added under File -> Analyze
#### New Feature
"Analyze Guitar Wiring" feature added under File -> Analyze
#### New Feature
Guitar pickups now have separate terminals for start and finish point of each coil. Single coil pickups have polarity property added
#### New Feature
Added center-OFF options for "Mini Toggle Switch"
#### New Feature
Added ability to mark common lugs on all "Lever Switch" types with a different color
#### Improvement
Improve performance of "Image" component and greatly reduce file size when images are present
#### Improvement
Allow a file to be loaded if it references a missing component property, but show a warning
#### Bug Fix
Version 3.53 would not start unless there was an older version running on the same machine before
## 3.53.0
### Sat Feb 09 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Added extra work space around the layout that can be used to store additional components and helpers, but does not get exported to a file. Configurable from the menu
#### New Feature
Ability to export and import building blocks and variants from the "File" menu
#### New Feature
Show mouse cursor coordinates in the status bar
#### New Feature
"RCA Jack" added
#### New Feature
Add switched type to "Open 1/4" Jack" component
#### New Feature
Selectable coordinate origin point for all perforated boards
#### Improvement
Vast improvement of rendering performance and memory management, especially with large layouts
#### Improvement
Create a backup of config file when there is an issue with loading (for example running two DIYLC versions on the same machine) to avoid data loss
#### Improvement
Automatically select dropped components from the component tree after they get placed
#### Improvement
Better rendering of IC and transistor pins when zoomed in
#### Improvement
Better rendering of guitar pickups when dragging and in outline mode
#### Improvement
Better rendering of TO-3 transistor package
#### Bug Fix
Drag&Drop from the component tree sometimes picks up a wrong component type
#### Bug Fix
"Select All" menu option from the component tree context menu doesn't work
#### Bug Fix
Components do not load if the installation directory name contains a special character, like '!'
#### Bug Fix
Do not allow zero for size and spacing properties that could lead to crashes or unexpected behavior
## 3.52.0
### Sun Feb 03 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Ability to drag components directly from the tree to the canvas for faster process
#### New Feature
"Twisted Leads" added (DIYLC v2 fans rejoice)
#### New Feature
"Fuse Symbol" added
#### Improvement
Better rendering precision when exporting to PDF and PNG files
#### Improvement
Added confirmation dialog before deleting a building block
#### Bug Fix
"PCB Text" components are not included in trace mask exports
## 3.51.0
### Mon Jan 28 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### Improvement
Better selection rendering for a few components, mostly electro-mechanical
#### Bug Fix
Jumper color switches to red on its own
#### Bug Fix
Lead alignment sometimes gets rendered off by 1px for resistor and capacitor schematic symbol
#### Bug Fix
Reverted Ohm symbol changes that made it render wrong on some systems
#### Bug Fix
Update dialog doesn't show up when clicked on the light bulb icon in the status bar
## 3.50.0
### Wed Jan 23 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
"Signal Transformer" added
#### New Feature
"Pilot Lamp Holder" added
#### New Feature
"Radial Inductor" added
#### New Feature
"Toroidal Inductor" added
#### New Feature
Configurable graphics hardware acceleration
#### New Feature
More flexibility with coordinate display for boards
#### New Feature
"Recent Updates" added to status bar and Help menu
#### Improvement
Fixed the issue with Ohm symbol rendering with some fonts and changed default project font back to "Square721 BT"
#### Improvement
Better color band rendering on resistors and smarter label positioning
#### Improvement
Added few pedal enclosure variants
#### Improvement
Chrome-style cursors for mouse scrolling with arrows showing the exact scrolling direction
#### Improvement
Do not render various jack components as completely transparent while dragging
#### Bug Fix
Since 3.49, straight traces are rendered 1px thinner than curved traces of the same thickness
#### Bug Fix
Since 3.49, some capacitor variants have disappeared. The fix brings them back
## 3.49.0
### Wed Jan 16 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Middle mouse click activates mouse movement scroll mode, similar to web browsers
#### New Feature
When "High Quality Rendering" option is checked, some components are rendered with 3D shading for better looks
#### New Feature
"Fuse Holder" added
#### New Feature
"Bridge Rectifier" added
#### New Feature
Changed default project font again to "Lucida Console" because "Square721 BT" doesn't render Ohm symbol properly
#### New Feature
Added optional arrow to the "Line" component together with configurable thickness
#### New Feature
Ability to flip polarity of IC symbol
#### Improvement
Use hardware acceleration for rendering to improve performance and reduce memory usage
#### Improvement
Use transparency instead of rendering pins on top of transistor body
#### Improvement
Leads and wires are rendered better, especially when zoomed in
## 3.48.0
### Sun Jan 13 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Added "Label Orientation" property to leaded components that can be used to force component labels to be rendered horizontally instead of following component placement direction
#### New Feature
"Jazz Bass Pickup" added
#### New Feature
"P-Bass Pickup" added
#### New Feature
Filtertron pickup type added within "Humbucker Pickup" as well as screw pole pieces for all humbucker types
#### New Feature
"DIP Switch" added
#### New Feature
"Tantalum Capacitor" added
#### New Feature
"IEC Socket" added
#### New Feature
"Logic Gate" added with 8 main gate types
#### New Feature
Ability to have "Single-coil Pickup" and "Humbucker Pickup" with no poles, similar to EMG, Lace Sensor...
#### Improvement
Render resistor color bands and capacitor polarity markings properly when zoomed in
#### Improvement
Do not allow negative values for properties indicating a size
#### Bug Fix
Project font gets reverted to "Square721 BT" on Undo
#### Bug Fix
Some actions do not register with Undo/Redo mechanism: project font, layer settings, group/ungroup
#### Bug Fix
When loaded from an existing file, "Potentiometer Symbol" doesn't retain the specified orientation
## 3.47.0
### Wed Jan 09 01:00:00 EET 2019
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Multi-section Electrolytic "Can" Capacitor added
#### New Feature
Added visual indicator to the component tree showing how many variants, if any, exist for a component
#### New Feature
DIYLC now comes with predefined component variants out-of-the box, in addition to user-specific variants
#### Improvement
Changed default font to "Square721 BT" that looks more technical and is packaged with the app
#### Improvement
Right-click rotate while placing components on the canvas now applies to tube sockets as well
#### Bug Fix
Fix "Mouse Wheel Zoom" feature that got broken in 3.46.1
#### Bug Fix
Include Themes (under Config menu) in the Windows installer version
#### Bug Fix
Covered humbucker pickup throwing errors when rotated
## 3.46.1
### Wed Dec 26 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### Bug Fix
Revert unintentional change for MacOs for "command" and "control" keys for zooming and selection
## 3.46.0
### Wed Dec 26 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Oval solder pads, available as new types of regular "Solder Pad" components
#### Improvement
Faster click and popup menu response due to improved performance of selection area calculations, especially with bigger layouts
#### Improvement
Do not group multiple components automatically after being pasted
#### Improvement
Better error handing in V2 file importer
#### Improvement
Rotate commands now applies to "Label" and "PCB Text" components
#### Bug Fix
Clicking on a building block in the component tree sometimes makes the components go back to the top-left corner of the layout
#### Bug Fix
Ctrl + click doesn't add components to the selection in MacOS
## 3.45.0
### Tue Dec 25 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### Improvement
Changed the way fonts are initialized, hoping to reduce a chance of dialogs hanging
#### Bug Fix
Fixed selection issues on right click in OSX that make it impossible to use Variants and other features
#### Bug Fix
Reverted undesired change for three-point curved traces and wires which made them slightly more rounded than before version 3.43.0
#### Bug Fix
"Recent Files" causes the whole menu to be rendered at two places under MacOS
## 3.44.1
### Fri Dec 21 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### Bug Fix
Fixed OSX "No compatible version of Java 1.8+ is available." error
## 3.44.0
### Tue Dec 18 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Added few chassis-related components under "Electromechanical" category
#### Improvement
All file export actions (PNG, PDF, BOM...) take the current file name as a default
#### Improvement
Center visible area to the selection when using mouse wheel to zoom in and out
#### Improvement
Change mouse cursor when in "Highlight Connected Areas" mode to avoid confusion
#### Bug Fix
Thin dashed and dotted lines turn to solid when zoomed in
## 3.43.0
### Mon Dec 17 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Added "Dashed" and "Dotted" style to "Hookup Wire", "Jumper" and "Line" components
#### New Feature
Added five and seven control point options to curved components for more flexibility (literally)
#### New Feature
Added "Turret Lug" component
## 3.42.1
### Sun Dec 16 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### Improvement
Added usage hints for new functionality
## 3.42.0
### Sun Dec 16 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
New in "Config" menu - highlight connected areas through copper traces, leads, jumpers, etc...
#### New Feature
Holding "shift" key switches mouse wheel scrolling function from vertical to horizontal
#### New Feature
Three new trimmer potentiometer types
#### Improvement
Preserve z-order of components when copy-pasting
#### Bug Fix
File chooser dialogs don't append file extension if folder path has a dot character in it
#### Bug Fix
"Cut Line" component sometimes disappears
#### Bug Fix
Measurement tool shows incorrect readings when zoomed in or out
#### Bug Fix
Components in the tree sometimes stop responding until deselected and selected again
#### Bug Fix
Fixed BOM export to Excel
## 3.41.1
### Tue Dec 11 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Automatically delete logs older than a week
#### Bug Fix
Fixed building blocks that stopped working in version 3.40.0
## 3.41.0
### Tue Dec 11 01:00:00 EET 2018
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Using right click to draw a selection rectangle doesn't move objects under the cursor (useful for selecting multiple components that have a big board below)
#### New Feature
"Cut Line" component added to designate where a custom eyelet/turret boards needs to be cut
#### Improvement
Increased padding between schematic symbols and their labels
#### Improvement
Better label position on resistors
#### Bug Fix
Auto-save dialog pops up when running multiple instances of the app at the same time
#### Bug Fix
Left mouse click on a component in the component tree sometimes has no effect
## 3.40.0
### Mon May 08 02:00:00 EEST 2017
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Import files created in DIYLC v2
#### New Feature
Ability to force components to the front or back of other components even outisde their designated layers, allowing placing components or jumpers below boards, etc
#### New Feature
'Tilde' key repeats the last added component
#### New Feature
Duplicate selection option in Edit menu and popup menu (Ctrl+D)
#### New Feature
Added bitchin new splash screen
#### New Feature
Potentiometers can show an optional shaft, can have either solder lugs or PCB pins and rendering includes the wafer
#### New Feature
Ability to change PCB board shape from rectangular to oval.
#### Improvement
Do not block the UI while checking for new version at startup (can be problematic when there's connectivity issues)
#### Improvement
DIL IC label rotates to fit the longer side of the component. Name and value are displayed in separate lines
#### Improvement
Better axial electrolytic rendering. Changed default color of all electrolytic capacitors
#### Improvement
Editable toggle switch color
#### Improvement
Changed border color of memory status bar in bottom-right corner not to be red under OSX
## 3.39.0
### Tue Apr 25 02:00:00 EEST 2017
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Assign F1-F12 keys as shortcuts for frequently used component types or building blocks
#### New Feature
Type-in selection nudge
#### New Feature
Show and hide each individual layer
#### New Feature
Red ticks on both rulers mark selection bounds, blue tick tracks cursor position
#### Improvement
Improve precision for Solder Pad and Copper Trace by not rounding the size up or down
#### Improvement
Show selection size in both inches and centimeters
#### Improvement
Use standard OSX/Mac "command" key for menu shortcuts and to un-stuck components
#### Improvement
Use balloon to show announcements and update notifications
#### Improvement
Moved logs to user's home directory
#### Bug Fix
Issues with DIL and SIL IC pin alignment when using metric grid and pin spacing
#### Bug Fix
Resistor changes shape on its own
## 3.38.0
### Thu Apr 20 22:00:00 EEST 2017
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Added 'Recent Files' to the main menu to keep track of the previous 10 files
#### New Feature
Hold Control key to zoom with mouse wheel. Zooming (somewhat) tracks mouse cursor position
#### New Feature
Ability to set project-wide default font through 'Edit Project Settings' dialog
#### New Feature
Added wizard installer for Windows that associates the app with .DIY files automatically
#### Improvement
Moved config and auto-save file locations to standard user directory instead of using the app directory
#### Improvement
Do not allow creating zero-length components (e.g. traces, lines, etc)
#### Improvement
Hookup wire can be sent to back behind boards
#### Improvement
Solder pads can be sent behind copper traces to allow creating white border around traces on top of a ground plane
#### Improvement
Ability to edit color of copper traces, curved traces and solder pads at the same time when they are all selected
#### Improvement
Tooltip doesn't cover buttons in the right side of the status bar
#### Bug Fix
Component library empty if the app is installed in a folder that contains special characters in the name
## 3.37.0
### Mon Apr 10 22:00:00 EEST 2017
(https://github.com/bancika/diy-layout-creator/releases)
#### Bug Fix
Thumbnail sometimes gets very small when uploading a project to the cloud
#### Improvement
Ability to change resistor shape (tubular or standard "dog bone")
#### Improvement
Added more control over Breadboard size, orientation and appearance
## 3.36.0
### Thu Jan 26 21:00:00 EET 2017
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Ability to quickly save a group of components as a building block and use it later
#### New Feature
Show/hide rulers
#### New Feature
Show/hide the grid
#### Improvement
Ability to type in hex value in color editor
#### Improvement
Ability to rotate tube sockets for arbitrary angle and change color
#### Improvement
Flip tube schematic symbols
#### Improvement
Renamed "template" to "variant" as it is describes the meaning more closely
#### Improvement
Applying a variant/template shouldn't affect component placement
#### Improvement
Improved rotation and mirroring of multiple components at the same time
#### Improvement
Improved resistor rendering to look more realistic
#### Improvement
Editable single coil pickup pole pieces (rods or rails) and pole piece color
#### Improvement
Editable humbucker pickup pole pieces (rods or rails) and pole piece color
#### Improvement
Editable P90 pickup pole piece color
#### Bug Fix
Component type gets selected while expanding folders in the component tree
## 3.35.0
### Thu Jan 05 21:00:00 EET 2017
(https://github.com/bancika/diy-layout-creator/releases)
#### Bug Fix
Cannot type "q" letter in any of the boxes around the app
#### New Feature
Mirror selection horizontally and vertically
#### New Feature
Ability to set a default template for component type (grey "pin" icon in the template popup)
#### Improvement
Default focus on "Text" field for label
#### Improvement
Consolidated popup menu items with "Edit" menu
#### Improvement
Renamed menu actions for renumbering to be clearer
## 3.34.0
### Fri Dec 30 21:00:00 EET 2016
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Searchable component tree instead of the old toolbar (can be changed back from "Config" menu)
#### New Feature
Open 1/4" Switchcraft-style jack added
#### New Feature
LP-style switch added
#### New Feature
P-90 pickup in both "dog ear" and "soap bar" variations added
#### New Feature
Telecaster bridge pickup added as a variation of single coil pickup
#### New Feature
Mini humbucker pickup added as a variation of Humbucker pickup
#### Improvement
X-axis coordinates for all perfboards are now numberical
#### Improvement
Improved trace cut rendering to look more realistic
#### Improvement
Improved guitar pickup rendering to look more realistic
#### Bug Fix
Using unicode characters (like micro character) in template names breaks the whole config file
#### Bug Fix
Fixed broken links in the Help menu
#### Bug Fix
TriPad board didn't show coordinate labels even when configured to do so
## 3.33.0
### Fri Dec 23 21:00:00 EET 2016
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Terminal strip component added
#### New Feature
Battery schematic symbol added
#### New Feature
Glass diode component added
#### New Feature
SMD capacitor and resistor added
#### Improvement
Fixed the issue with zooming in on traces and similar components that would not change on zooming
#### Improvement
Tube, transistor and potentiometer symbols can be rotated
#### Improvement
Added percent text box next to the slider for editors like Alpha or Scale
#### Improvement
Better object line representation when zoomed in
#### Improvement
Increased maximum zoom range from 200% to 300%
#### Improvement
Improved pentode symbol without the suppressor grid
## 3.32.0
### Sat Nov 12 21:00:00 EET 2016
(https://github.com/bancika/diy-layout-creator/releases)
#### New Feature
Project Cloud feature introduced with global project sharing functionality
#### New Feature
Transformer core and coil schematic symbols added that can be used to put together custom transformer symbols
#### New Feature
Added Public Announcements feature
#### Improvement
Potentiometer and transistor symbols can be flipped horizontally
#### Improvement
Reduced DIYLC package size
#### Improvement
Auto save and recovery made a bit smarter
#### Improvement
Popup dialogs work better with keyboard - Enter confirms, Escape dismisses dialogs
#### Improvement
Make order of items in the 'Edit Project' dialog more logical
#### Improvement
Better support for old V1 file import
#### Improvement
Toggle switch terminal spacing can be changed
#### Improvement
Allow multi-line project description
#### Bug Fix
Changing Display to one trimmer affects all trimmers and does not get saved to the file
#### Bug Fix
Solder pad component should not allow negative size
#### Bug Fix
Fixed silent error when having a resistor without a valid value (e.g. missing units)
## 3.31.0
### Mon May 09 22:00:00 EEST 2016
(https://github.com/bancika/diy-layout-creator/releases)
#### Bug Fix
Undo works only one level back (bug introduced in 3.29)
## 3.30.0
### Mon Apr 25 22:00:00 EEST 2016
(https://github.com/bancika/diy-layout-creator/releases)
#### Improvement
Improved potentiometer symbol appearance
## 3.29.0
### Wed Apr 20 22:00:00 EEST 2016
(https://github.com/bancika/diy-layout-creator/releases)
#### Bug Fix
Issue with losing saved user configuration
#### Bug Fix
Issue with disabled "Save as Template" context menu item problem, as well as issue with strange rotation and probably few more
#### Bug Fix
Undo steps kept after opening a new file
#### New Feature
Transistors can be rotated and flipped vertically
#### New Feature
DIL ICs can show pin numbers
#### New Feature
Display both component value and name or neither of them
#### New Feature
Potentiometer schematic symbol added
## 3.28.0
### Thu Aug 15 22:00:00 EEST 2013
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Issue with dragging multiple components
#### Bug Fix
Issue with component selection
#### New Feature
Select all within a layer
## 3.27.0
### Tue Apr 30 22:00:00 EEST 2013
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Moving multiple components doesn't honor snap to grid OFF setting
#### Bug Fix
Do not change selection on right click when right clicking on one of the selected components
#### Bug Fix
Right click rotation during component creation throws an error
#### New Feature
Ability to import existing DIY files into a project
#### New Feature
Added primitive shapes - ellipse, polygon and (rounded) rectangle
#### New Feature
Added tri-pad board (thanks Hauke!)
#### New Feature
Apply template to one or more existing components from the context menu
#### Improvement
Trace cuts can be placed between two holes for tighter layouts (double click trace to find "Cut between holes")
#### Improvement
Moved all schematic symbols to a dedicated category
#### Improvement
Allow grid spacing as little as 0.1mm
## 3.26.0
### Thu Apr 25 22:00:00 EEST 2013
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Large components disappear when none of the edges is in the visible area of the screen
#### New Feature
Ability to rotate selection
#### Improvement
More predictable movement of multiple components at the same time
## 3.25.0
### Mon Apr 22 22:00:00 EEST 2013
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Large components disappear when none of the edges is in the visible area of the screen
#### New Feature
Breadboard component
## 3.24.0
### Mon Mar 04 21:00:00 EET 2013
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Snap to grid after releasing Ctrl button and creating a new component
#### Bug Fix
Paste throws an error
#### Bug Fix
Potentiometer doesn't update when size only is changed
#### Improvement
Faster startup when using image component templates
## 3.23.0
### Mon Oct 08 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
TO-3 transistor rotation is wrong for 180 degrees
#### Bug Fix
Pasted components are placed below traces
#### Bug Fix
Right click changes selection
#### New Feature
SIP IC component
#### Improvement
Editable colors for DIP IC
#### Improvement
Clearer standing component (mainly diode) reversion
#### Improvement
Ctrl + right click on component type in toolbox appends components of that type to selection
#### Improvement
Editable lead color; changed default lead color
## 3.22.0
### Mon Oct 08 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Two Voltage properties for ceramic capacitors
#### Bug Fix
Cannot create ceramic capacitor if there's a default value for voltage
#### Improvement
Copy paste functionality improved
#### Improvement
Component templates save shape of the component (for wires, traces and such)
## 3.21.0
### Mon Oct 01 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Configuration and component templates are lost when restarting the app
## 3.20.0
### Mon Sep 24 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
BOM doesn't work when there's a component without a value set
#### Bug Fix
Editing multiple components doesn't work there's a component without a value set
#### New Feature
Diode symbol
#### New Feature
Zener diode symbol
#### New Feature
Schottky diode symbol
#### New Feature
LED symbol
#### Improvement
Bring back default check boxes in the editor
## 3.19.0
### Mon Sep 24 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Cannot save a component template if value is not set
#### New Feature
Mirrored text for PCB artwork
## 3.18.0
### Mon Sep 24 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Components don't show up in the toolbox on OSX
#### New Feature
Inductor symbol
#### Improvement
Component values (resistance, capacitance, etc) are created blank instead of defaulting to some values
## 3.17.0
### Mon Sep 24 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Triode tube symbol
#### New Feature
Pentode tube symbol
#### New Feature
Diode tube symbol
#### Improvement
Editable label color for leaded components
#### Improvement
Outline mode for switches, jacks, trimmers, potentiometers and transistors
## 3.16.0
### Mon Sep 17 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Ability to rotate text
#### New Feature
Connector dot component
#### Bug Fix
Radial components become transparent by themself
#### Bug Fix
Context menu doesn't show on MacOS
#### Improvement
Ability to flip standing diodes
#### Improvement
Allow zero sized holes on solder pads
## 3.15.0
### Fri Aug 31 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Added support for component templates
#### Improvement
Hidden "default" checkboxes from the editor because templates cover that functionality. If you need them back however, uncomment org.diylc.swing.gui.editor.PropertyEditorDialog.SHOW_DEFAULT_BOXES=true in the config.properties file
#### Improvement
Ability to rotate toggle switch
## 3.14.0
### Mon Aug 27 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Ruler jumps when scrolling
#### Bug Fix
Error when importing V1 files that contain potentiometers
#### New Feature
Miniature and ultra-miniature PCB mount relays
#### New Feature
TO-3 transistors
#### Improvement
Folded electrolytic capacitors
#### Improvement
Folded TO-220 transistor
#### Improvement
Label vertical and horizontal alignment can be changed
#### Improvement
Editable pin spacing for all radial components
## 3.13.0
### Thu Aug 23 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Problems opening older files that have BOM
#### New Feature
Polygon Ground Fill component
#### New Feature
TO-1 transistor component
#### New Feature
Multi-layer PCB support; outputs each layer's trace mask to a separate page
#### New Feature
Editable BOM color
#### Improvement
Show voltage and power ratings in the BOM where applicable
#### Improvement
Folded TO-92 transistors
#### Improvement
Block user actions during long operations (print, export, etc)
## 3.12.0
### Thu Aug 23 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Rectangle selection triggers file (modified) state
#### Bug Fix
Two components are created instead of one
#### Bug Fix
Undo-Redo repeated several times messes up selection
#### Bug Fix
Loading DIY file that contains images sometimes breaks
#### Bug Fix
Image disappears while scrolling if not completely in the visible area
#### Improvement
Auto-Edit opens the editor on for components that make sense (e.g. NOT for traces, solder pads, etc)
#### Improvement
Better sorting in the BOM (e.g. R2 should go before R10)
## 3.11.0
### Tue Aug 21 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Improvement
Some components (like Image) can be placed in any layer using Send to Back/Bring to Front
#### Improvement
Replaced deep cloning library with manual implementation
## 3.10.0
### Mon Aug 20 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Improvement
Improved overall performance
#### Bug Fix
Should be more stable under OSX and not crash unexpectedly
## 3.9.0
### Mon Aug 20 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Move selected components with arrow keys (and ctrl/shift)
#### Improvement
Right-click in the toolbar to select all components of the same type
#### Improvement
Click on a component in the toolbar sometimes doesn't register
#### Improvement
Remove junk from BOM (eyelets, wires, traces)
#### Improvement
Improved "Recently Used" toolbox, doesn't jump as you click
#### Improvement
Control number of "Recently Used" items from config.properties
#### Improvement
Faster component placement, auto-focus on "Value" box
#### Improvement
Transistors can show name or value
#### Improvement
UNICODE font export to PDF files
#### Improvement
More intuitive unsaved file warning message
## 3.8.0
### Thu Aug 16 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Right click rotates component while laying out on the canvas
#### New Feature
Added DIY file association for Windows (run associations.bat)
#### Improvement
More usable "Bring to Forward" and "Send to Backward"
#### Improvement
Faster copy-paste functionality
#### Improvement
Improved auto-save not to slow down the app
## 3.7.0
### Tue Mar 27 22:00:00 EEST 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Lever guitar switch
#### New Feature
9V battery snap
## 3.6.0
### Wed Mar 21 21:00:00 EET 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Improvement
Fonts are exported to PDF
#### New Feature
Plastic DC jack
#### New Feature
Humbucker pickup
#### New Feature
Single coil pickup
## 3.5.0
### Thu Mar 15 21:00:00 EET 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Generic closed 1/4" jack
#### New Feature
Cliff-style closed 1/4" jack
#### New Feature
Ground Symbol
## 3.4.0
### Fri Mar 09 21:00:00 EET 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
IC Symbol
#### New Feature
BJT Symbol
#### New Feature
MOSFET Symbol
#### New Feature
JFET Symbol
#### New Feature
Line connector
#### Improvement
IC component can display name or value
#### Bug Fix
"Index out of bounds" exception logs when selecting components
#### Bug Fix
Loading files fails because encoding is not recognized
#### Bug Fix
Application breaks when resistance is set to 0 ohms
## 3.3.0
### Sat Feb 25 21:00:00 EET 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Expand selection feature, useful for debugging
#### Improvement
Limit length of text in the status bar using "and X more" suffix
## 3.2.0
### Sun Feb 19 21:00:00 EET 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### New Feature
Select components from context menu even when they are under other components
#### New Feature
Ctrl+Shift toggles "Snap to Grid" option
#### New Feature
Perfboards can show hole coordinates
#### Improvement
Smarter resistor labels; try not to overlap label with color code bands
#### Improvement
Improved import from v1 file format
#### Improvement
Imperial ruler is divided to .1" instead of 1/4"
#### Improvement
Allow finer grid spacing up to 0.5mm (0.02")
#### Improvement
Do not hard-code voltage and power ratings
## 3.1.0
### Wed Feb 01 21:00:00 EET 2012
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Selection sometimes highlights wrong component(s)
#### Bug Fix
Renumber function sometimes doesn't work as expected
#### Improvement
Increased PNG export resolution to 300dpi
#### Improvement
Select All should not include locked components
#### Improvement
Better lead and label rendering when lead spacing is smaller than component body
#### Improvement
Allow more than 32 pins for IC
#### New Feature
Adjustable solder pad hole size
## 3.0.9
### Sun Aug 28 22:00:00 EEST 2011
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Auto-created solder pads aren't assigned with default settings and name
#### Improvement
Component edit dialog allows changing property values even when they are not the same. Boxes are marked yellow to designate multi-valued state.
#### Improvement
Component edit dialog should show mutual properties when components of different types are selected (e.g. curved trace and straight trace width)
#### Improvement
Ctrl+click should not clear selection when clicked on the canvas
#### Improvement
Increased PNG export resolution
#### Improvement
Auto-increment component names when copy-pasting
#### Improvement
Do not attempt to write auto-save files if user does not have permissions to create files
#### Improvement
Allow zero ohm resistors
#### New Feature
Outline mode that resembles PCB silkscreen
#### New Feature
Double click on a component button in the toolbar selects all components of that type
#### New Feature
Renumber selected components by X or Y axis
#### New Feature
Recently used components in the toolbar for easy navigation
## 3.0.8
### Thu Mar 03 21:00:00 EET 2011
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Sometimes it's not possible to click on a component, especially when layout is large
#### Improvement
Curved traces and wires can have 2, 3 and 4 control points
#### New Feature
Switchable wheel zoom
#### New Feature
Automatic standing resistors, capacitors and diodes
#### New Feature
Vertical vero board
#### New Feature
Label has editable font
## 3.0.7
### Sun Feb 27 21:00:00 EET 2011
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Resistor and capacitor symbols are not clickable
#### Bug Fix
Boards sometimes render holes/pads on the edge
#### Bug Fix
Download link doesn't work on Mac
#### Bug Fix
Ruler ticks don't always match with the grid
#### Bug Fix
Micro symbol doesn't get exported to PDF
#### Improvement
DIL IC now has more pin options
#### Improvement
Send to Back/Bring to Front are more flexible and renamed to match what they do
#### New Feature
Auto-save project and restore if app crashes
#### New Feature
Added support for themes
#### New Feature
Added tube socket
#### New Feature
Sticky points may be turned off
#### New Feature
Snap to Grid may be turned off
#### New Feature
Auto-create solder pads
#### New Feature
Auto-edit mode
#### New Feature
Continuous component creation
## 3.0.6
### Mon Feb 07 21:00:00 EET 2011
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Copy/paste doesn't work for some components (potentiometer, transistor, etc)
#### Bug Fix
Resistor color codes are sometimes wrong
#### Improvement
Resistors can specify power handling
#### Improvement
Capacitors can specify voltage handling
#### Improvement
Fixed lead alignment issue
#### Improvement
Better scaling, grid lines are more precise
#### Improvement
Improved performance
#### New Feature
Added resistor schematic symbol
#### New Feature
Added capacitor schematic symbol
#### New Feature
Added eyelet/turret component
#### New Feature
Added Marshall/Trainwreck style boards
#### New Feature
Added few types of trimmers
#### New Feature
Configurable rendering quality to improve speed
## 3.0.5
### Sun Jan 30 21:00:00 EET 2011
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Curved trace does not show control points
#### Bug Fix
Potentiometer draws wrong at 180 degrees
#### Improvement
Reset component selection when tab is changed
#### Improvement
Resistor can show color bands
#### Improvement
Electrolytic capacitor has a new icon and new looks
#### Improvement
Solder pad can be square
#### Improvement
Added keyboard shortcuts to some menu options
#### Improvement
V1 file importer can recognize hookup wire and transistors
#### New Feature
Added support for creating and loading templates
#### New Feature
Added TO-92 transistor
#### New Feature
Added TO-220 transistor
#### New Feature
Added LED
#### New Feature
Added axial electrolytic capacitor
## 3.0.4
### Thu Jan 27 22:00:00 EET 2011
(http://code.google.com/p/diy-layout-creator/downloads/list)
#### Bug Fix
Copy/paste between two instances of the application
#### Bug Fix
Parsing numerical values in countries that use comma as decimal separator
#### Bug Fix
Dragging sticky points affects selectability of objects
#### Bug Fix
Click is sometimes not registered while adding a new component
#### Bug Fix
Fixed NPE when using default values
#### Improvement
Allow shape control points of curves to overlap
#### Improvement
Added DIL IC first pin indentation
#### Improvement
Added transparency to the potentiometer
#### Improvement
Reduced number of layers
#### Improvement
Import from v1 files can now read label and electrolytic capacitor components and trace/pad colors
#### Improvement
Added worker thread for longer file operation, so GUI doesn't freeze
#### New Feature
Added right click menu on the canvas
#### New Feature
Added "send to back" and "bring to front" options
#### New Feature
Added label component
#### New Feature
Added image component
