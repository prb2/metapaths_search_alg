package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;
import org.graphstream.stream.file.FileSourceDOT;

import java.io.IOException;

/**
 * Loads a graph from file and applies the algorithm
 */
public class MetaRun {
    public static void main(String args[]) throws IOException {
        Graph g = new SingleGraph("Custom2");
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
        fs.readAll("graphs/Custom2/custom2.dot");

        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("ui.label", e.getAttribute("capacity").toString());
        }

//        g.display();
        run(g, "S", "T", 5);
    }

    private static void run(Graph g, String start, String target, int desiredFlow) {
        MetaGraphSearch mgs = new MetaGraphSearch();
        mgs.constructMetaGraph(g, start, target, desiredFlow);
        MetaGraph mg = mgs.getMeta();

        try {
            writeGraph(mg.getInternal(), "Custom2/MG_Custom2TargetOnly.dot");
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public static void writeGraph(Graph g, String name) throws IOException {
        FileSink fs = new FileSinkDOT();
        fs.writeAll(g, "graphs/" + name);
    }
}
