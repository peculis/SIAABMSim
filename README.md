Software Intensive Acquisition Agent-Based Modelling and Simulation
================
### Instructions to compile and execute SIAABMSimV8

SIAABMSim was developed with Repast 3.1 in Eclipse SDK 3.4.2. Repast 3.1 is available from http://repast.sourceforge.net/repast_3

Eclipse SDK is available from http://www.eclipse.org/platform

SIAABMSimV8 was developed in compliance with Java Version 1.5 and compiled
with Java Version 1.6.0_24 from Sun Microsystems, Inc.

To compile the software the following External JARs are required:
- repast.jar (included with Repast 3.1)
- colt.jar (included with Repast 3.1 and available from http://acs.lbl.gov/software/colt/)
- plot.jar (QN Plot 1.6.1 available from multiple sites)

To compile the software in Eclipse, create a Java Project with the External JARs and copy the src and Data folders into the Project workspace.

The Data folder is placed at the same level as the src folder under the Project workspace. The Data folder contains two folders (Input and Output).

The simulations configuration files are placed in the Input folder (see the structure of the Input folder) and the results from the Simulation are written in the Output folder.

To execute SIAABMSim in Eclipse choose to run the main file, (SIAABMSimModel.java in the SIAABMSimModel package) as a Java program. The program will use the default configuration files (SimSettings.txt, Probabilities.txt and Organisations.txt) located in the Input folder.

For a complete overview of SIAABMSim please click [here](https://github.com/peculis/SIAABMSim/blob/master/docs/SIAABMSimSimulationEnvironment.pdf).
