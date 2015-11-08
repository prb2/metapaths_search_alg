package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDOT;

import java.io.IOException;

/**
 * Loads a graph from file and applies the algorithm
 */
public class MetaRun {
    public static void main(String args[]) throws IOException {
        Graph g = new SingleGraph("Custom");
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
        fs.readAll("graphs/custom.dot");

        Graph g1 = new SingleGraph("Simple");
        FileSourceDOT fs1 = new FileSourceDOT();
        fs1.addSink(g1);
        fs1.readAll("graphs/simple.dot");

        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("ui.label", e.getAttribute("capacity").toString());
        }
        for (Edge e : g1.getEdgeSet()) {
            e.setAttribute("ui.label", e.getAttribute("capacity").toString());
        }

//        g.display();

        run(g, "S", "T", 5);
//        run(g1, "a", "h", 4);
    }

    private static void run(Graph g, String start, String target, int desiredFlow) {
        MetaGraphSearch mgs = new MetaGraphSearch();
        mgs.constructMetaGraph(g, start, target, desiredFlow);
    }
}
