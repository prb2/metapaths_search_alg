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
        System.out.println("Args: " + args[0]);
        String input = args[0];
        String start = args[1];
        String target = args[2];
        int flow = Integer.parseInt(args[3]);

        Graph g = new SingleGraph(input);
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
        fs.readAll("graphs/" + input + "/" + input + ".dot");

        // Label edges with their capacity for later viewing
        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("ui.label", e.getAttribute("capacity").toString());
        }

//        run(g, start, target, flow, g.getId(), true);
        MetaGraph mg = new MetaGraph(g, input, start, target, flow);
        mg.populate(false, false);
        mg.writeToFile("newwww1");
    }

//    public static void run(Graph g, String start, String target, int desiredFlow, String graphName, boolean pruning) {
//        MetaGraphCreation mgs = new MetaGraphCreation();
//        mgs.constructMetaGraph(g, start, target, desiredFlow, pruning);
//        MetaGraph mg = mgs.getMeta();
//
//        try {
//            writeGraph(mg.getInternal(), graphName + "/MG_" + graphName + "_Pruned.dot");
//            System.out.println("Done writing MG to file.");
//        } catch (IOException e) {
//            System.out.println(e);
//        }
//
//        /*
//        // Search the MG for paths
//        MetaGraphPathSearch search = new MetaGraphPathSearch();
//        Iterable<Path> paths = search.findPaths(mg, mg.getStartID(), mg.getTargetID());
//        if (paths.iterator().hasNext()) {
//            for (Path path : paths) {
//                System.out.println("Found path: " + path.toString());
//            }
//            Graph union = search.unionize(paths);
////            union.display(true);
//
//            try {
//                writeGraph(union, graphName + "/Union_" + graphName + "_Pruned.dot");
//            } catch (IOException e) {
//                System.out.println(e);
//            }
//        }
//        */
//    }

//    public static void writeGraph(Graph g, String name) throws IOException {
//        FileSink fs = new FileSinkDOT();
//        fs.writeAll(g, "graphs/" + name);
//    }

}
