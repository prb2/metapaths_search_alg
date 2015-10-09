package metagraphs.ModifiedFFA;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDOT;

import java.io.IOError;
import java.io.IOException;
import java.util.Random;

/**
 * Creates a sample graph to
 */
public class SampleRun {
    public static void main(String args[]) throws IOException {
        Random rand = new Random();
        Graph g = new SingleGraph("Custom");
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
//        fs.readAll("graphs/custom.dot");
        fs.readAll("graphs/branched.dot");

        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("capacity", rand.nextInt(10)+3);
        }


        ModifiedFordFulkerson mffa = new ModifiedFordFulkerson();
        mffa.init(g, "C00031", "C00078");
        mffa.setCapacityAttribute("capacity");
        mffa.compute();
        for (Edge e : g.getEdgeSet()) {
            Node u = e.getNode0();
            Node v = e.getNode1();
            e.setAttribute("ui.label", mffa.getFlow(u, v) + " / " +
                    e.getAttribute("capacity").toString());
        }
        System.out.printf("Max flow: " + mffa.getMaximumFlow());
//        g.display();

    }
}
