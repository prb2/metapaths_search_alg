import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;

import java.io.IOException;
import java.util.Random;

/**
 * Collection of tools and helper functions for using graphs.
 */
public class ToolKit {
    public static void main(String args[]) throws IOException {
        writeGraph(getRandGraph("Random1k", false, true, 1000, 2, 10));
    }

    public static SingleGraph getRandGraph(String name, Boolean allowRemove,
                                           Boolean directed, int numNodes,
                                           int avgDeg, int maxEdgeCapacity) {
        Random rand = new Random();
        SingleGraph g = new SingleGraph(name);
        RandomGenerator gen = new RandomGenerator(avgDeg, allowRemove, directed);
        gen.addSink(g);
        gen.begin();
        for (int i = 0; i < numNodes; i++) {
            gen.nextEvents();
        }
        gen.end();
        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("capacity", rand.nextInt(maxEdgeCapacity-1) + 1);
            e.setAttribute("label", e.getAttribute("capacity").toString());
            System.out.println(e.getSourceNode().toString());
        }

        return g;
    }

    public static void writeGraph(Graph g) throws IOException {
        FileSink fs = new FileSinkDOT();
        String filename = "graphs/" + g.getId() + "/" + g.getId() + ".dot";
        fs.writeAll(g, filename);
    }
}
