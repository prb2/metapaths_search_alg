package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDOT;
import org.graphstream.stream.sync.SourceTime;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Loads a graph from file and applies the algorithm
 */
public class MetaRun {
    public static void main(String args[]) throws IOException {
        // Parse the arguments
        String name = "Medium1";
        String start = "S";
        String target = "T";
        int flow = 1;
        boolean stopOnTarget = false;
        boolean enablePruning = false;

        Scanner in = new Scanner(System.in);
        System.out.println("Enter the input info, otherwise default values will be used.");
        System.out.println("Enter name of input graph: ");
        name = in.next();
        System.out.println("Enter start node ID: ");
        start = in.next();
        System.out.println("Enter target node ID: ");
        target = in.next();
        System.out.println("Enter 1 to enable pruning: ");
        enablePruning = in.nextInt() == 1;

        // Average run time on 10 iterations
        int numIters = 10;
        System.out.println("Enter the number of iterations to run: ");
        numIters = in.nextInt();
        System.out.println("Enter the max flow to try: ");
        flow = in.nextInt();

        for (int i = 1; i <= flow; i++) {
            long totalTime = 0;

            System.out.println("Run Characteristics:");
            System.out.println("\t " + name);
            System.out.println("\t " + start + " --" + i + "--> " + target);
            System.out.println("\t Stop on target: " + stopOnTarget);
            System.out.println("\t Pruning: " + enablePruning);

            for (int iter = 1; iter <= numIters; iter++) {
                // Read in the input graph
                Graph g = new SingleGraph(name);
                FileSourceDOT fs = new FileSourceDOT();
                fs.addSink(g);
                fs.readAll("graphs/" + name + "/" + name + ".dot");

                // Label edges with their capacity for later viewing
                for (Edge e : g.getEdgeSet()) {
                    e.setAttribute("ui.label", e.getAttribute("capacity").toString());
                }

                totalTime += run(g, name, start, target, i, stopOnTarget, enablePruning);
            }
            double avg = totalTime / (numIters + 1);
            System.out.println("Average time for " + numIters + " iterations is: " + avg + "ms\n");
        }
    }

    public static long run(Graph g, String graphName, String start, String target,
                           int desiredFlow,  boolean stopOnTarget, boolean enablePruning) {


        // Create the initial metagraph with the start state
        MetaGraph mg = new MetaGraph(g, graphName, start, target, desiredFlow);

        long startTime = System.nanoTime();
        // Populate the metagraph via neighbor search
        mg.populate(stopOnTarget, enablePruning, true);

        long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
//        System.out.println("\t Metagraph population took: " + elapsed + " milliseconds.");

        try {
            mg.writeToFile(graphName + "/Meta_" + desiredFlow + "_" + graphName);
//            System.out.println("\t Done writing MG to file.");
        } catch (IOException e) {
            System.out.println(e);
        }

//        System.out.println("");
        return elapsed;
    }
}
