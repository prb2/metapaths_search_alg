package Metagraph;

import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Map;

/**
 * Wrapper for nodes in a MetaGraph
 */
public class MetaNode {
    private String id;
    private Map<String, Integer> state;

    public MetaNode(String name, Map<String, Integer> counts) {
        id = name;
        state = counts;
    }

    /**
     * A meta-node is only valid if the sum of the nodes' flow is equal to
     * the required flow.
     * @param requiredFlow The specified flow need for each node in the meta graph
     */
    public Boolean isValid(int requiredFlow) {
        int stateFlow = 0;

        System.out.println("Checking validity of: " + state.toString());
        for (Map.Entry<String, Integer> entry : state.entrySet()) {
            stateFlow += entry.getValue();
        }

        return stateFlow != requiredFlow;
    }

    public Boolean isTarget(String targetID, int targetFlow) {
        if (state.containsKey(targetID)) {
            if (state.get(targetID) == targetFlow) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Integer> getState() {
        return state;
    }

    public void setState(Map<String, Integer> state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
