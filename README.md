# Metabolic Pathway Search

This is the development repository for an algorithm for finding biologically
meaningful pathways in metabolic networks. The goal is to find pathways in a
graph of metabolic reactions that conserve a given number of carbon atoms from
start to goal. Pathways which conserve a high percentage of atoms from the
start to goal compounds will be biologically relevant. So, by tracking atoms,
we are more likely to find metabolic pathways of biological importance. The
movement of atoms through the pathway's reactions is modeled as the movement of
flow through a directed graph. Applications for this work include metabolic
engineering, as well as more general flow movement problems.

##### This project has moved to the [Kavraki Lab Repository](https://github.com/KavrakiLab) and will continue to be developed there.

## Overview

### Generalized Algorithm
Given a weighted directed graph, a start and target node, and an amount of flow as input, the algorithm
generates a metagraph. Each metanode in the metagraph represents a valid distribution
of the given flow across nodes in the input graph. Metanodes share an edge in the
metagraph if the flow in the source metanode can be moved to the nodes in the target
metanode in one step. The initial metanode is simply the state in which all flow
resides at the specified start node.

From this point, the metagraph can be populated by finding neighbors of the start metanode
and the neighbors of any newly found metanodes.

Once the metagraph has been fully populated, standard graph search algorithms can
used to find a path from the start metanode to the target metanode.

### Metabolic Pathways Application
The same algorithm will be used to find metabolic pathways. The input graph represents
a metabolic network of compounds and reactions and the flow represents the number
of atoms to conserve.

## Usage

After compiling the project, there are two ways to generate a metagraph:

1. Run the `Controller.java` file and use the GUI

2. Run the `MetaRun.java` file with the following arguments:

    `java MetaRun input_file start_node target_node desired_flow stop_on_target enable_pruning`

* input_file - filepath to the input graph, must be in the `.dot` format
* start_node - ID of the start node in the input graph
* target_node - ID of the target node in the input graph
* desired_flow - integer, amount of flow to move
* stop_on_target - "true" will stop as soon as the target metanode is found,
  otherwise will continue searching until exhausted
* enable_pruning - "true" will cause terminal nodes to be pruned (recommended)

### Dependencies
This project relies on the GraphStream library ([Website](http://graphstream-project.org), [GitHub](https://github.com/graphstream)) for Java and is used under the LGPL license.
