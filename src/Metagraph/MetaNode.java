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
    protected Boolean isValid(int requiredFlow) {
        int stateFlow = 0;

        for (Map.Entry<String, Integer> entry : state.entrySet()) {
            stateFlow += entry.getValue();
        }

        return stateFlow != requiredFlow;
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
