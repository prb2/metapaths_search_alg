package metagraphs.ModifiedFFA;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSourceDOT;

import java.io.IOError;
import java.io.IOException;

/**
 * Creates a sample graph to
 */
public class SampleRun {
    public static void main(String args[]) throws IOException {
        Graph g = new SingleGraph("Custom");
        FileSourceDOT fs = new FileSourceDOT();
        fs.addSink(g);
        fs.readAll("graphs/custom.dot");
//        g.display();

        ModifiedFordFulkerson mffa = new ModifiedFordFulkerson();
        mffa.init(g, "S", "T");
        mffa.setCapacityAttribute("capacity");
        mffa.compute();
        System.out.printf("Max flow: " + mffa.getMaximumFlow());

    }
}
