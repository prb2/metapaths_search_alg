import org.graphstream.graph.Graph;

import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

/**
 * Implementation of the experimental path finding algorithm
 */
public class Algorithm {
    /**
     * Either a partial or complete solution, null until
     * process(...) is called.
     */
    private ArrayList<State> solution = new ArrayList<State>();

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
        State startState = new State();
        // All of the balls are at the start node
        startState.addNode(start, k);
        // The start state is the beginning of the path
        solution.add(startState);

        // TODO: Should a deque be used instead (more flexibility)?
        // Holds the set of valid states that can be further explored
        Stack<State> availableStates = new Stack<State>();
        // We'll start exploring from the start state
        availableStates.add(startState);

        while (availableStates.size() > 0) {
            // Get the current state
            State current = availableStates.pop();
            System.out.println("Current state is: " + current);

            // Check if current state is the goal, all balls are at the goal node
            if (current.isGoal(goal, k)) {
                solution.add(current);
                return true;
            }

            System.out.println("Previously available states: " + availableStates);
            // Generate and stack all valid states one step away from current state
            generateNbrStates(current, availableStates);
            System.out.println("With newly added states: " + availableStates);
        }
        return false;
    }

    /**
     * Generates all the valid states that are one step away from the current state
     * @param current The current state
     * @param states The stack of states that will be explored
     */
    private void generateNbrStates(State current, Stack<State> states) {
        // TODO: If goal state is generated, add to sol and return?

    }

    /**
     * Will return the solution path in the following format:
     * [{start:5}, {node1:2, node2:3}, ... ,{goal:5}]
     * @return A list of mappings, where each mapping represents a
     * "step" and consists of nodes with their ball counts
     */
    public ArrayList<State> getPath() {
        if (solution != null) {
            return solution;
        } else {
            System.out.println("No solution currently exists. Try running process again.");
            return null;
        }
    }
}
