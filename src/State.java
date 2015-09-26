import java.util.HashMap;

/**
 * A state is defined as a mapping of nodes to the
 * number of balls held at the node.
 */
public class State {
    /**
     * A state is represented as a map
     */
    private HashMap<String, Integer> state = null;

    /**
     * Constructor to create a State with predefined values
     * @param predefinedState A hashmap containing the state information
     */
    public State(HashMap<String, Integer> predefinedState) {
        state = predefinedState;
    }

    /**
     * Constructor to create an empty State
     */
    public State() {
        state = new HashMap<String, Integer>();
    }

    /**
     * A state is valid if the sum of the balls at all
     * the nodes in the state equal k
     * @param k The total number of balls there should be
     * @return Whether the state adheres to the valid definition
     */
    public boolean isValid(int k) {
        int sum = 0;

        for (int value : state.values()) {
            sum += value;
        }

        return sum == k;
    }
}
