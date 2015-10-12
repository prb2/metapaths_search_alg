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
        fs.readAll("graphs/custom.dot");
//        fs.readAll("graphs/branched.dot");
//        fs.readAll("graphs/simple.dot");

//        for (Edge e : g.getEdgeSet()) {
//            e.setAttribute("capacity", rand.nextInt(10)+3);
//        }


        System.out.println("######### Modified FFA");
        ModifiedFordFulkerson mffa = new ModifiedFordFulkerson(5.0, g.getNodeCount());
        mffa.init(g, "S", "T");
//        mffa.init(g, "a", "h");
        mffa.setCapacityAttribute("capacity");
        mffa.compute();
        for (Edge e : g.getEdgeSet()) {
            Node u = e.getNode0();
            Node v = e.getNode1();
            e.setAttribute("ui.label", mffa.getFlow(u, v) + " / " +
                    e.getAttribute("capacity").toString());
        }
        System.out.println("Modified max flow: " + mffa.getMaximumFlow());

        mffa.printPaths();
        Graph unionG = mffa.constructUnionGraph();

        for (Node n : unionG) {
            System.out.println(n.getId() + " is in paths: " + n.getAttribute("paths"));
        }

/*        System.out.println("######### Original FFA");
        FordFulkersonAlgorithm ffa = new FordFulkersonAlgorithm();
        ffa.init(g, "a", "h");
        ffa.setCapacityAttribute("capacity");
        ffa.compute();
        System.out.println("Original max flow: " + ffa.getMaximumFlow());*/

        for (Node n : g) {
            n.setAttribute("ui.label", n.getId());
        }
        for (Node n : unionG) {
            String label = "";
            label += n.getId() + " is in paths: ";
            label += n.getAttribute("paths");
            n.setAttribute("ui.label", label);
        }
        g.display();
        unionG.display();
    }
}
