<p align="justify"><strong>DIYLC is provided free of charge and can be used and redistributed freely. Please note that it takes a lot of time and effort to build, test and maintain it and to interact with the growing community of users. If you find DIYLC useful and want to support further development, please consider making a small PayPal donation. Every little bit counts! If you have some extra parts, transistors, tubes, ICs, caps...anything useful for a fellow DIYer, I'll gladly accept a donation in parts.</strong></p>
<p align="center"><a href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=U6GLS8HLTSU88" rel="nofollow"><img src="https://www.paypal.com/en_US/i/btn/btn_donateCC_LG.gif"></a> </p>
<a href="https://github.com/bancika/diy-layout-creator/releases/latest">Click here</a> to get the latest version for all platforms.</a><br>
<a href="https://github.com/bancika/diy-layout-creator/tree/wiki">Click here</a> to read the user documentation as well as developer documentation.<br>
<b>OSX users</b>: make sure that <i>3rd party apps are allowed</i> on your Mac before installing DIYLC. <a href="https://support.apple.com/en-us/HT202491">This document</a> covers how to do it.
<h3><a name="News"></a>Introduction<a href="#Introduction" class="section_anchor"></a></h3>
<p align="justify">DIY Layout Creator (DIYLC in short) is a freeware drawing tool developed with help of the large online community of DIY electronics enthusiasts. It incorporates many ideas that came from people using older versions of the application. The goal is to provide a simple interface and enough power to let the user draw schematics, board/chassis layouts and guitar wiring diagrams quickly and without a steep learning curve. Also, it is buitd around a flexible open source framework that may be used to draw pretty much anything. Below is a sample board layout drawn in DIYLC3. </p>
<p></p>
<p align="center"><a href="http://diy-fever.com/wordpress/wp-content/gallery/diylc/diylc_3_36_rangemaster.png" rel="nofollow"><img src="http://diy-fever.com/nextgen-image/833/480x0x90/a93c6f7598bc16fce4f4aad480631523"></a></p>
<p></p>
<h3><a name="Key Features"></a>Features<a href="#Features" class="section_anchor"></a></h3>
<ul>
   <li>platform independence, will run on any machine having Java JRE/JDK 1.6.0_45 or newer </li>
   <li>easy to use interface, most of the operations can be done using mouse </li>
   <li>can be used to draw PCB, perfboard, stripboard or point-to-point circuit layouts layouts, schematics and guitar wiring diagrams </li>
   <li>high flexibility, has the API to allow plug-ins and new components to be added without much trouble </li>
   <li>improved performance and reduced memory consumption compared to the previous versions </li>
   <li>save default values for each property of a component individually </li>
   <li>group components together and treat them as one, e.g. move, edit or delete </li>
   <li>export the output to image, PDF or printer </li>
   <li>export PCB trace mask suitable for toner transfer </li>
   <li>create a Bill of Materials as a part of the project or export it to few different file formats </li>
   <li>zoom in/out feature </li>
   <li>configurable grid spacing on the project level </li>
   <li>auto update checks for new versions </li>
   <li>import files created with older versions of the application </li>
   <li>create and load project templates </li>
   <li>cloud feature - share your projects, search through the cloud and download other users' projects </li>
   <li>highlight connected areas to simplify validation and circuit debugging</li>
</ul>
<p>To report bugs or request a new feature go to <a href="https://github.com/bancika/diy-layout-creator/issues" rel="nofollow">Issues</a> page and create a new issue describing your problem or request. </p>
<p>To join the discussion or request technical assistance, use the dedicated <a href="http://groups.google.com/group/diy-layout-creator" rel="nofollow">Discussion Group</a>. </p>
<h3><a name="Want_to_contribute?"></a>Want to contribute?<a href="#Want_to_contribute?" class="section_anchor"></a></h3>
<p>If you want to help the DIYLC project, there are several things you can do: </p>
<ul>
   <li>Help with adding the information to the <a href="https://github.com/bancika/diy-layout-creator/blob/wiki/Manual.md">user manual</a>. </li>
   <li>Extend the component base, read <a href="https://github.com/bancika/diy-layout-creator/blob/wiki/ComponentAPI.md" rel="nofollow">this wiki</a> to learn how to create new components. </li>
   <li>Add a new functionality though plug-ins, read <a href="https://github.com/bancika/diy-layout-creator/blob/wiki/PluginAPI.md" rel="nofollow">this wiki</a> to learn how to create new plug-ins. </li>
   <li>If you are good with design, DIYLC could use a fancy splash screen and/or a new application icon in 16x16, 32x32 and 48x48 formats. </li>
   <li>Consider making a small <a href="http://diy-fever.com/donate" rel="nofollow">PayPal donation</a>. </li>
</ul>
<h3><a name="License_information"></a>License information<a href="#License_information" class="section_anchor"></a></h3>
<p>Source code is released under <a href="https://www.gnu.org/licenses/gpl-3.0.txt">GNU General Public License version 3</a>. Contact the project owner to obtain a license in case you plan to use the source code, or any part of it, in commercial purposes. </p>
<p></p>
<h3><a name="Credits"></a>Credits<a href="#Credits" class="section_anchor"></a></h3>
<ul>
   <li>Big thanks goes to folks from the following sites that helped with testing and ideas: <a href="http://www.diystompboxes.com/smfforum/" rel="nofollow">DIY Stompboxes</a>, <a href="http://ax84.com/bbs" rel="nofollow">AX84</a> and <a href="http://freestompboxes.org" rel="nofollow">Free Stompboxes</a>. </li>
   <li>I'm happy to report that DIYLC 3 is the fastest version ever. <a href="http://www.yourkit.com/java/profiler/index.jsp" rel="nofollow">YourKit Java Profiler</a> helped tremendously with memory and performance profiling and pointed me to places in the code that took longer to run or consumed more memory than I'd like them to. I strongly recommend this tool to anyone developing Java or .NET applications because it helps finding bottlenecks and allocation hot-spots very fast.<br><br>YourKit is kindly supporting open source projects with its full-featured Java Profiler.<br>YourKit, LLC is the creator of innovative and intelligent tools for profiling Java and .NET applications.<br>Take a look at YourKit's leading software products: <a href="http://www.yourkit.com/java/profiler/index.jsp" rel="nofollow">YourKit Java Profiler</a> and <a href="http://www.yourkit.com/.net/profiler/index.jsp" rel="nofollow">YourKit .NET Profiler</a> </li>
</ul>
