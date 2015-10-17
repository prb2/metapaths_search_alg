package Sandbox;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A state is defined as a mapping of nodes to the
 * number of balls held at the node.
 */
public class State {
    /**
     * A state is represented as a map
     */
    private HashMap<String, Integer> state = null;

    private int count;

    /**
     * Constructor to create a metagraphs.Playground.State with predefined values
     * @param predefinedState A hashmap containing the state information
     */
    public State(HashMap<String, Integer> predefinedState) {
        state = predefinedState;
        count = this.calculateCount();
    }

    /**
     * Constructor to create an empty metagraphs.Playground.State
     */
    public State() {
        state = new HashMap<String, Integer>();
        count = 0;
    }

    /**
     * Adds a node to be included in the state
     * @param node The id of the node
     * @param numBalls The number of balls at the node
     */
    public void addNode(String node, int numBalls) {
        count += numBalls;
        state.put(node, numBalls);
    }

    /**
     * An iterator over the nodes in this state
     * @return An Iterator over nodes in this state
     */
    public Iterator<String> getNodeIterator() {
        return state.keySet().iterator();
    }

    public int getCount() {
        return count;
    }

    /**
     * Counts the number of balls in this state
     * @return The sum of all the balls at all nodes in this state
     */
    public int calculateCount() {
        int sum = 0;

        for (int value : state.values()) {
            sum += value;
        }

        return sum;
    }

    /**
     * A state is valid if its nodes collectively contain k balls
     * @param k The desired number of balls
     * @return Whether the state contains exactly k balls
     */
    public boolean isValid(int k) {
        return count == k;
    }

    /**
     * A state is the goal if it only has the goal node and contains k balls
     * @param goal The id of the goal node
     * @param k The desired number of balls
     * @return Whether the state is the goal state
     */
    public boolean isGoal(String goal, int k) {
        // Checks that there is only one node
        if (state.keySet().size() == 1 && state.containsKey(goal)) {
            // Checks that the goal node has all k balls
            if (state.get(goal) == k) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return state.toString();
    }
}
