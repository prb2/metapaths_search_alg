package Metagraph;

//import GUI.GUI;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSourceDOT;

import java.io.IOException;

/**
 * Loads a graph from file and applies the algorithm
 */
public class MetaRun {
    public static void main(String args[]) throws IOException {
        // Parse the arguments
        String input = args[0];
        String start = args[1];
        String target = args[2];
        int flow = Integer.parseInt(args[3]);
        boolean stopOnTarget = Boolean.getBoolean(args[4]);
        boolean enablePruning = Boolean.getBoolean(args[5]);

        // Read in the input graph
        Graph g = new SingleGraph(input);
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
        fs.readAll("graphs/" + input + "/" + input + ".dot");

        // Label edges with their capacity for later viewing
        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("ui.label", e.getAttribute("capacity").toString());
        }

        run(g, input, start, target, flow, stopOnTarget, enablePruning);
    }

    public static void run(Graph g, String graphName, String start, String target,
                           int desiredFlow,  boolean stopOnTarget, boolean enablePruning) {

        System.out.println("Run Characteristics:");
        System.out.println("\t " + graphName);
        System.out.println("\t " + start + " --" + desiredFlow + "--> " + target);
        System.out.println("\t Stop on target: " + stopOnTarget + " Pruning: " + enablePruning);

        // Create the initial metagraph with the start state
        MetaGraph mg = new MetaGraph(g, graphName, start, target, desiredFlow);

        // Populate the metagraph via neighbor search
        mg.populate(stopOnTarget, enablePruning, true);

        try {
            mg.writeToFile(graphName + "/Meta_" + graphName);
            System.out.println("\t Done writing MG to file.");
        } catch (IOException e) {
            System.out.println(e);
        }

        System.out.println("");
    }
}
