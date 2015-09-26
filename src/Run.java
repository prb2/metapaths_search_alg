import org.graphstream.graph.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Creates a weighted, directed graph and applies the algorithm.
 */
public class Run {

    /**
     * Initializes the graph and call the alg.
     * @param args None needed
     */
    public static void main(String args[]) {
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

        Algorithm alg = new Algorithm();
        alg.process(g, "S", "T", 5, 10);

//        g.display();
    }
}
