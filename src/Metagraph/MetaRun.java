package Metagraph;

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
        Graph g = new SingleGraph("Custom1");
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
        fs.readAll("graphs/Custom1/custom1.dot");

        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("ui.label", e.getAttribute("capacity").toString());
        }

//        g.display();
        run(g, "S", "T", 4, g.getId());
    }

    private static void run(Graph g, String start, String target, int desiredFlow, String graphName) {
        MetaGraphCreation mgs = new MetaGraphCreation();
        mgs.constructMetaGraph(g, start, target, desiredFlow);
        MetaGraph mg = mgs.getMeta();

        try {
            writeGraph(mg.getInternal(), graphName + "/MG_" + graphName + ".dot");

        } catch (IOException e) {
            System.out.println(e);
        }

        // Search the MG for paths
        MetaGraphPathSearch search = new MetaGraphPathSearch();
        Iterable<Path> paths = search.findPaths(mg, mg.getStartID(), mg.getTargetID());
        if (paths.iterator().hasNext()) {
            for (Path path : paths) {
                System.out.println("Found path: " + path.toString());
            }
            Graph union = search.unionize(paths);
//            union.display(true);

            try {
                writeGraph(union, graphName + "/Union_" + graphName + ".dot");
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }


    public static void writeGraph(Graph g, String name) throws IOException {
        FileSink fs = new FileSinkDOT();
        fs.writeAll(g, "graphs/" + name);
    }
}
