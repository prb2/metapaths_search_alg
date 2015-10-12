package metagraphs.ModifiedFFA;

import metagraphs.Sandbox.GraphMaker;
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
    static GraphMaker gmaker = new GraphMaker();
    public static void main(String args[]) throws IOException {
//        Graph g1 = gmaker.makeGraph("Random Graph 1", 40, 4, "capacity", 1, 20, false);
//        gmaker.writeGraph(g1, "RG1.dot");

        Graph g = new SingleGraph("RG1");
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
//        fs.readAll("graphs/custom.dot");
//        fs.readAll("graphs/branched.dot");
//        fs.readAll("graphs/simple.dot");
        fs.readAll("graphs/RG1.dot");


        Graph unionG = runMFFA(g, "0", "40", 2.0, g.getNodeCount(), "capacity");

        for (Node n : unionG) {
            System.out.println(n.getId() + " is in paths: " + n.getAttribute("paths"));
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

    public static Graph runMFFA(Graph g, String start, String target, double k, int l, String capacityAttr) {
        ModifiedFordFulkerson mffa = new ModifiedFordFulkerson(k, l);
        mffa.setCapacityAttribute(capacityAttr);
        mffa.init(g, start, target);
        mffa.compute();

        System.out.println("MFFA max flow: " + mffa.getMaximumFlow());
        mffa.printPaths();

        for (Edge e : g.getEdgeSet()) {
            Node u = e.getNode0();
            Node v = e.getNode1();
            e.setAttribute("ui.label", mffa.getFlow(u, v) + " / " +
                    e.getAttribute("capacity").toString());
        }
        return mffa.constructUnionGraph();
    }
}

/*        System.out.println("######### Original FFA");
        FordFulkersonAlgorithm ffa = new FordFulkersonAlgorithm();
        ffa.init(g, "a", "h");
        ffa.setCapacityAttribute("capacity");
        ffa.compute();
        System.out.println("Original max flow: " + ffa.getMaximumFlow());*/
//        g.display();
