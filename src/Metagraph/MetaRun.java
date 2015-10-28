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
        Graph g = new SingleGraph("Input Graph");
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
        fs.readAll("graphs/simple.dot");

        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("ui.label", e.getAttribute("capacity").toString());
//            System.out.println(e.isDirected());
        }

//        g.display();

        run(g, "a", "h", 4);
    }

    private static void run(Graph g, String start, String target, int desiredFlow) {
        MetaGraphSearch mgs = new MetaGraphSearch();
        mgs.constructMetaGraph(g, start, target, desiredFlow);
    }
}
