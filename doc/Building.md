# Build DIY Layout Creator from source

## Requirements

* Java JDK 8 or newer
* Apache [Maven](https://maven.apache.org)

Note: Eclipse is not required to build DIYLC. Eclipse setup is not covered here.

## Get the source

All Java source files required to build DIYLC are now under the
_diy-layout-creator_ project. All required JAR files are fetched by
Maven automatically.

### Just get the source and get on with it

    % git clone https://github.com/rockola/diy-layout-creator.git

### Get your own personal copy to work on

Fork (https://github.com/rockola/diy-layout-creator) on GitHub,
````git clone```` your personal copy, and continue as
below. Instructions on using GitHub are beyond the scope of this
document.

You will need to go this route if you want to send pull requests.

## Compile the source

	% cd diy-layout-creator
	% mkdir log
	% mvn -B compile 2>&1 | tee log/compile.log
	
Sit back and wait for Maven to fetch a number of JARs required to build DIYLC.

### zsh function for compiling
````
mc () {
        mvn -B compile 2>&1 | tee log/compile.txt
}
````
## Run your newly built version of DIYLC

	% mvn -B exec:exec | tee log/run.log
	
or, if you don't want/need to hold on to various debug/error messages printed on the console:

	% mvn exec:exec
### zsh function for running
````
mr () {
        mvn -B exec:exec 2>&1 | tee log/run-$(date +'%Y%m%d-%H%M%S').txt
}
````
## Build a complete package for distribution

	% mvn package
	
This builds a fat JAR containing all the required JAR files in the ````./target```` directory.

**To do:** proper application packaging for OSX/Windows/Linux

## Participate in development

First, get your own personal copy of the source tree (see above).

[SpotBugs](https://spotbugs.github.io) can be used to look for bugs
and otherwise suspicious code. See the 
[SpotBugs website](https://spotbugs.github.io) for further info.

The following Maven goals are available:

### mvn spotbugs:spotbugs

Creates a SpotBugs analysis file (see below). The project needs to
have been compiled first (SpotBugs analyzes the bytecode).

### mvn spotbugs:check

As above, except fails if any bugs are found.

### mvn spotbugs:gui

Launches the SpotBugs GUI and shows the analysis result.

### mvn spotbugs:help

Displays a help message for the SpotBugs Maven plugin.
