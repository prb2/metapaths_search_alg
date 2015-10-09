package metagraphs.Sandbox;

import org.graphstream.algorithm.AStar;
import org.graphstream.algorithm.flow.FordFulkersonAlgorithm;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;

import java.io.IOException;
import java.util.Random;

/**
 * Creates a weighted, directed graph and applies the algorithm.
 */
public class Run {

    public static SingleGraph getGraph() {
        SingleGraph g = new SingleGraph("Demo Graph 1");

        // The start node
        g.addNode("S");
        // The target node
        g.addNode("T");
        // Internal nodes
        g.addNode("a");
        g.addNode("b");
        g.addNode("c");
        g.addNode("d");
        g.addNode("e");
        g.addNode("f");
        g.addNode("g");
        g.addNode("h");

        // Edge(ID, start node, end node, directed?)
        g.addEdge("Sa", "S", "a", false);
        g.getEdge("Sa").addAttribute("capacity", 5);
        g.addEdge("Sb", "S", "b", false);
        g.getEdge("Sb").addAttribute("capacity", 3);
        g.addEdge("Sc", "S", "c", false);
        g.getEdge("Sc").addAttribute("capacity", 2);
        g.addEdge("cd", "c", "d", false);
        g.getEdge("cd").addAttribute("capacity", 2);
        g.addEdge("de", "d", "e", false);
        g.getEdge("de").addAttribute("capacity", 3);
        g.addEdge("eT", "e", "T", false);
        g.getEdge("eT").addAttribute("capacity", 2);
        g.addEdge("bf", "b", "f", false);
        g.getEdge("bf").addAttribute("capacity", 1);
        g.addEdge("fe", "f", "e", false);
        g.getEdge("fe").addAttribute("capacity", 2);
        g.addEdge("ah", "a", "h", false);
        g.getEdge("ah").addAttribute("capacity", 3);
        g.addEdge("aT", "a", "T", false);
        g.getEdge("aT").addAttribute("capacity", 1);
        g.addEdge("hg", "h", "g", false);
        g.getEdge("hg").addAttribute("capacity", 3);
        g.addEdge("gT", "g", "T", false);
        g.getEdge("gT").addAttribute("capacity", 3);

        for (Node n : g) {
            n.addAttribute("ui.label", n.getId());
        }

        return g;
    }

    public static SingleGraph getRandGraph(Boolean directed) {
        Random rand = new Random();
        SingleGraph g2 = new SingleGraph("G2");
        Generator gen = new RandomGenerator(3, false, directed);
        gen.addSink(g2);
        gen.begin();
        for (int i = 0; i < 12; i++) {
            gen.nextEvents();
        }
        gen.end();
        for (Edge e : g2.getEdgeSet()) {
            e.setAttribute("capacity", rand.nextInt(10));
            System.out.println(e.getSourceNode().toString());
//            System.out.println(e.getAttribute("capacity").toString());
        }

        return g2;
    }

    public static void writeGraph(Graph g, String name) throws IOException {
        FileSink fs = new FileSinkDOT();
        fs.writeAll(g, name);
    }

    public static void applyFFA(SingleGraph g, String start, String target) {
        // Ford-Fulkerson metagraphs.Playground.Algorithm
        FordFulkersonAlgorithm ffa = new FordFulkersonAlgorithm();
        ffa.setCapacityAttribute("capacity");
        ffa.init(g, start, target);
//        ffa.setAllCapacities(10);
        ffa.compute();

        for (Edge e : g.getEdgeSet()) {
            String n0 = e.getNode0().getId();
            String n1 = e.getNode1().getId();
            ffa.setAllCapacities(10.0);
            double flow = ffa.getFlow(n0, n1);
            System.out.println("Flow between " + n0 + " and " + n1 + " is: " + flow);
            e.addAttribute("flow", flow);

            // Add ledge labels to the UI
            String edgeLabel = e.getAttribute("flow") + " / " + e.getAttribute("capacity");
            e.addAttribute("ui.label", edgeLabel);
        }
        g.display();

        System.out.println("Max flow: " + ffa.getMaximumFlow());

        AStar astar = new AStar();
        astar.init(g);
        astar.compute("S", "T");
//        System.out.println(astar.);
    }

    /**
     *
     * Initializes the graph and call the alg.
     * @param args None needed
     */
    public static void main(String args[]) throws IOException {
        // The graph input to the algorithm
        // Custom graph
        SingleGraph g = getGraph();

        writeGraph(g, "graphs/custom.dot");

        // get random graph
//        SingleGraph g = getRandGraph(false);

//        applyFFA(g, "S", "T");

    }
}
