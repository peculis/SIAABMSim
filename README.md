### Software Intensive Acquisition Agent-Based Modelling and Simulation (SIAABMSim)

SIAABMSim was developed as part of a PhD Thesis by Dr Ricardo Peculis and used for testing hypotheses.

================
### Instructions to compile and execute SIAABMSim

SIAABMSim was developed with Repast 3.1 in Eclipse SDK 3.4.2. Repast 3.1 is available from http://repast.sourceforge.net/repast_3

Eclipse is available from http://www.eclipse.org/downloads/

SIAABMSimV8 was developed in compliance with Java Version 1.5 and compiled
with Java Version 1.6.0_24 from Sun Microsystems, Inc.

To compile and run the software the following External JARs are required:
* repast.jar (included with Repast 3.1, inside installed location root directory)
* colt.jar (included with Repast 3.1, inside `lib` sub-directory)
* plot.jar (from **QN Plot 1.6.1** available at [here](https://sourceforge.net/projects/qn-plot/)).

To compile the software in Eclipse, follow these steps:

1. Create a new Java Project, by going to `File -> New -> Java Project`. Enter a project name and click `Next >`
2. In the next page, click on the Libraries tab and include the above `JAR` files by clicking on `Add External JARs`. Click Finish to create the project.
3. Right-click on the newly created project and select `Import...`. Select `File system...` and then click `Next>`. Enter the directory cloned/downloaded from this repository and import only the `src` and `Data` folders into the Project workspace.


To execute SIAABMSim in Eclipse choose to run the main file, (SIAABMSimModel.java in the SIAABMSimModel package) as a Java program. The program will use the default configuration files (SimSettings.txt, Probabilities.txt and Organisations.txt) located in the Input folder.

Note, the Data folder is placed at the same level as the src folder under the Project workspace. The Data folder contains two folders (Input and Output).

The simulations configuration files are placed in the Input folder (see the structure of the Input folder) and the results from the Simulation are written in the Output folder.

For a complete overview of SIAABMSim please see [here](https://github.com/peculis/SIAABMSim/blob/master/docs/SIAABMSimSimulationEnvironment.pdf).

For the complete PhD Thesis please see  [here](https://github.com/peculis/SIAABMSim/blob/master/docs/RPeculis-PhDThesis-UniSA-Final.pdf). For all configuration files used in the simulation to test the hypotheses formulated by the thesis see [here](https://github.com/peculis/SIAABMSim/tree/master/docs/Simulation%20-%20Configuration%20Files).

