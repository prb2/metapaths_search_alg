package metagraphs.Sandbox;



import org.graphstream.algorithm.flow.FordFulkersonAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

public class MaxFlow {

    //example from Coreman, 3rd edition, page 733
    public static SingleGraph bipartite_graph(){

        SingleGraph bipartite_graph = new SingleGraph("bipartite");
        bipartite_graph.addNode("1").setAttribute("ui.label", "1");


        bipartite_graph.addNode("2").setAttribute("ui.label", "2");
        bipartite_graph.addNode("3").setAttribute("ui.label", "3");
        bipartite_graph.addNode("4").setAttribute("ui.label", "4");
        bipartite_graph.addNode("5").setAttribute("ui.label", "5");
        bipartite_graph.addNode("6").setAttribute("ui.label", "6");
        bipartite_graph.addNode("7").setAttribute("ui.label", "7");
        bipartite_graph.addNode("8").setAttribute("ui.label", "8");
        bipartite_graph.addNode("9").setAttribute("ui.label", "9");

        bipartite_graph.addNode("source").setAttribute("ui.label",
                "s");
        bipartite_graph.addEdge("s-1", "source", "1", false);
        bipartite_graph.addEdge("s-2", "source", "2", false);
        bipartite_graph.addEdge("s-3", "source", "3", false);
        bipartite_graph.addEdge("s-4", "source", "4", false);
        bipartite_graph.addEdge("s-5", "source", "5", false);

        bipartite_graph.addNode("sink").setAttribute("ui.label", "t");
        bipartite_graph.addEdge("6-t", "6", "sink", false);
        bipartite_graph.addEdge("7-t", "7", "sink", false);
        bipartite_graph.addEdge("8-t", "8", "sink", false);
        bipartite_graph.addEdge("9-t", "9", "sink", false);

        bipartite_graph.addEdge("1-6", "1", "6", false);
        bipartite_graph.addEdge("2-6", "2", "6", false);
        bipartite_graph.addEdge("2-8", "2", "8", false);
        bipartite_graph.addEdge("3-7", "3", "7", false);
        bipartite_graph.addEdge("3-8", "3", "8", false);
        bipartite_graph.addEdge("3-9", "3", "9", false);
        bipartite_graph.addEdge("4-8", "4", "8", false);
        bipartite_graph.addEdge("5-8", "5", "8", false);

        bipartite_graph.display();
        return bipartite_graph;
    }

    public static double getMaxFlow(Graph bipartite_graph){

        FordFulkersonAlgorithm flow = new FordFulkersonAlgorithm();
        flow.init(bipartite_graph, "source", "sink");
        flow.setAllCapacities(1.0);
        flow.compute();

        System.out.println(flow.getMaximumFlow());
//        for(Edge e: bipartite_graph.getEachEdge()) {
//            System.out.println(e.getNode0().getId() + " " +
//                    e.getNode1().getId() + " - flow=" + flow.getFlow(e.getNode0(), e.getNode1()));
//        }
        System.out.println("Max flow = " + flow.getMaximumFlow());


        return flow.getMaximumFlow();
    }

    public static void main(String[] s){

        SingleGraph bipartite_graph = bipartite_graph();


        getMaxFlow(bipartite_graph);

    }
}

