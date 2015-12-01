package Metagraph;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Path;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Searchs a metagraph for valid complete flow paths.
 */
public class MetaGraphPathSearch {
    public Iterable<Path> findPaths(MetaGraph mg, String start, String target) {
        Graph g = mg.getInternal();
        System.out.println("Starting at: " + start);
        System.out.println("Ending at: " + target);

        for (Edge e : g.getEdgeSet()) {
            e.setAttribute("length", 1);
        }

        Dijkstra dj = new Dijkstra(Dijkstra.Element.EDGE, "result", "length");
        dj.init(g);
        dj.setSource(g.getNode(start));
        dj.compute();

        Iterable<Path> paths = dj.getAllPaths(g.getNode(target));
        return paths;
    }
}
