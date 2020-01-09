# Build DIY Layout Creator from source

## Requirements

* Java JDK 8 or newer
* Apache (https://maven.apache.org)[Maven]

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

## Run your newly built version of DIYLC

	% mvn -B exec:exec | tee log/run.log
	
or, if you don't want/need to hold on to various debug/error messages printed on the console:

	% mvn exec:exec
	
## Build a complete package for distribution

	% mvn package
	
This builds a fat JAR containing all the required JAR files in the ````./target```` directory.

**To do:** proper application packaging for OSX/Windows/Linux
