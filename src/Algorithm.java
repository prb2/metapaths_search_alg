import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.xml.internal.bind.v2.TODO;
import org.graphstream.graph.*;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Implementation of the experimental path finding algorithm
 */
public class Algorithm {
    /**
     * Either a partial or complete solution, null until
     * process(...) is called.
     */
    private ArrayList<Map<String, Integer>> solution = new ArrayList<Map<String, Integer>>();

    /**
     * Searches the graph, attempting to move all k balls from
     * start to goal within l steps
     *
     * @param g The input weighted, directed graph
     * @param start The start node
     * @param goal The goal node
     * @param k The number of "balls" to move from start to goal
     * @param l The maximum path length
     * @return Whether or not a full path was found
     */
    public Boolean process(Graph g, String start, String goal, int k, int l) {
        HashMap<String, Integer> startState = new HashMap<>();
        // All of the balls are at the start node
        startState.put(start, k);
        // The start state is the beginning of the path
        solution.add(startState);

        // TODO: Should a deque be used instead (more flexibility)?
        // Holds the set of valid states that can be further explored
        Stack<Map<String, Integer>> availableStates = new Stack<Map<String, Integer>>();
        // We'll start exploring from the start state
        availableStates.add(startState);

        while (availableStates.size() > 0) {
            // Get the current state
            Map<String, Integer> current = availableStates.pop();
            System.out.println("Current state is: " + current);

            // Check if current state is the goal, all balls are at the goal node
            if (current.containsKey(goal) && current.get(goal) == k) {
                return true;
            }

            System.out.println("Previously available states: " + availableStates);
            // Generate and stack all valid states one step away from current state
            generateNbrStates(current, availableStates);
            System.out.println("With newly added states: " + availableStates);
        }
        return false;
    }

    private void generateNbrStates(Map<String, Integer> current, Stack<Map<String, Integer>> states) {
//        ArrayList<Node> bucket = new ArrayList<Node>();
//        Iterator<Node> nbrs = current.getNeighborNodeIterator();
//        while (nbrs.hasNext()) {
//            bucket.add(nbrs.next());
//        }
//        System.out.println(bucket);
        Map<String, Integer> temp = new HashMap<String, Integer>();
        states.push(temp);

    }

    private boolean isValidState() {

    }

    /**
     * Will return the solution path in the following format:
     * [{start:5}, {node1:2, node2:3}, ... ,{goal:5}]
     * @return A list of mappings, where each mapping represents a
     * "step" and consists of nodes with their ball counts
     */
    public ArrayList<Map<String, Integer>> getPath() {
        if (solution != null) {
            return solution;
        } else {
            System.out.println("No solution currently exists. Try running process again.");
            return null;
        }
    }
}
