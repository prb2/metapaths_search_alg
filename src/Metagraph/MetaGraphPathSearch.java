package Metagraph;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;

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

    public Graph unionize(Iterable<Path> paths) {
        SingleGraph union = new SingleGraph("Union");
        for (Path path : paths) {
            for (Node n : path.getEachNode()) {
                try {
                    union.addNode(n.getId());
                    union.getNode(n.getId()).setAttribute("ui.label", n.getId());
                } catch (IdAlreadyInUseException e) {}
            }
            for (Edge e : path.getEachEdge()) {
                try {
                    union.addEdge(e.getId(), e.getNode0().getId(), e.getNode1().getId(), e.isDirected());
                } catch (IdAlreadyInUseException i) {}
            }
        }
        return union;
    }
}

