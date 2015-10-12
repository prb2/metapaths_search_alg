package metagraphs.Sandbox;

import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;

import java.io.IOException;
import java.util.Random;

public class GraphMaker {
    public Graph makeGraph(String name, int numNodes, int avgDeg, String weightAttr, int weightLower, int weightUpper, boolean directed) {
        Random rand = new Random();
        Graph g = new SingleGraph(name);
        Generator gen = new RandomGenerator(avgDeg, false, directed);
        gen.addSink(g);

        gen.begin();
        for (int i = 0; i < numNodes; i++) {
            // Add numNodes new nodes to the graph
            gen.nextEvents();
        }
        gen.end();

        for (Node n : g) {
            n.setAttribute("ui.label", n.getId());
        }
        for (Edge e : g.getEdgeSet()) {
            // Assign random edge weights within given range
            e.setAttribute(weightAttr, rand.nextInt(weightUpper)+weightLower);
            e.setAttribute("ui.label", weightAttr + ": " + e.getAttribute(weightAttr));
        }

        return g;

    }

    public void writeGraph(Graph g, String name) throws IOException {
        FileSink fs = new FileSinkDOT();
        fs.writeAll(g, name);
    }
}
