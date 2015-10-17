package ModifiedFFA;

import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Container for a path that contains the path nodes and the flow.
 */
public class GraphSegment {
    private ArrayList<String> pathNodes;
    private double pathFlow;
    public GraphSegment(LinkedList<Node> path, double flow) {
        pathFlow = flow;

        pathNodes = new ArrayList<String>();
        for (Node n : path) {
            pathNodes.add(n.getId());
        }
    }

    public ArrayList<String> getPathNodes() {
        return pathNodes;
    }

    public double getPathFlow() {
        return pathFlow;
    }

    @Override
    public String toString() {
        return "GraphSegment{" +
                "pathNodes=" + pathNodes +
                ", pathFlow=" + pathFlow +
                '}';
    }
}
