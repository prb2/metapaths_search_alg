package metagraphs;

import org.graphstream.algorithm.Prim;
import org.graphstream.algorithm.flow.FordFulkersonAlgorithm;
import org.graphstream.algorithm.generator.FullGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.Random;

/**
 * Creates a weighted, directed graph and applies the algorithm.
 */
public class Run {


    /**
     *
     * Initializes the graph and call the alg.
     * @param args None needed
     */
    public static void main(String args[]) {
        Random rand = new Random();
        // The graph input to the algorithm
        Graph g = new SingleGraph("Demo Graph 1");

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
        g.addEdge("Sa", "S", "a", true);
        g.getEdge("Sa").addAttribute("capacity", 5);
        g.addEdge("Sb", "S", "b", true);
        g.getEdge("Sb").addAttribute("capacity", 3);
        g.addEdge("Sc", "S", "c", true);
        g.getEdge("Sc").addAttribute("capacity", 2);
        g.addEdge("cd", "c", "d", true);
        g.getEdge("cd").addAttribute("capacity", 2);
        g.addEdge("de", "d", "e", true);
        g.getEdge("de").addAttribute("capacity", 3);
        g.addEdge("eT", "e", "T", true);
        g.getEdge("eT").addAttribute("capacity", 2);
        g.addEdge("bf", "b", "f", true);
        g.getEdge("bf").addAttribute("capacity", 1);
        g.addEdge("fe", "f", "e", true);
        g.getEdge("fe").addAttribute("capacity", 2);
        g.addEdge("ah", "a", "h", true);
        g.getEdge("ah").addAttribute("capacity", 3);
        g.addEdge("aT", "a", "T", true);
        g.getEdge("aT").addAttribute("capacity", 1);
        g.addEdge("hg", "h", "g", true);
        g.getEdge("hg").addAttribute("capacity", 3);
        g.addEdge("gT", "g", "T", true);
        g.getEdge("gT").addAttribute("capacity", 3);

        for (Node n : g) {
            n.addAttribute("ui.label", n.getId());
        }
        for (Edge e : g.getEachEdge()) {
            e.addAttribute("ui.label", (Object)e.getAttribute("capacity"));
        }

//        metagraphs.Algorithm alg = new metagraphs.Algorithm(g);
//        System.out.println("Processing graph...");
//        alg.process("S", "T", 5, 10);
//        System.out.println("Found a solution: " + alg.getPath());

        Graph g2 = new DefaultGraph("G2");
        Generator gen = new RandomGenerator(3, false, true);
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

//        g2.display();


        Prim prim = new Prim();
        prim.init(g2);
        prim.compute();

        // Ford-Fulkerson metagraphs.Algorithm
        FordFulkersonAlgorithm ffa = new FordFulkersonAlgorithm();
        ffa.init(g2, "1", "8");
        ffa.setCapacityAttribute("capacity");
//        ffa.setAllCapacities(10);
        ffa.compute();
        System.out.println("The max flow for this graph is: " + ffa.getMaximumFlow());

    }

    public void bipartite() {

    }
}
