package Metagraph;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.graphstream.graph.Node;

import javax.swing.text.StyledEditorKit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for nodes in a MetaGraph
 */
public class MetaNode {
    private String id;
    private HashMap<String, Double> state;

    public MetaNode(String name, HashMap<String, Double> counts) {
        id = name;
        state = counts;
    }

    /**
     * A meta-node is only valid if the sum of the nodes' flow is equal to
     * the required flow.
     * @param requiredFlow The specified flow need for each node in the meta graph
     */
    public Boolean isValid(double requiredFlow) {
        double stateFlow = 0;

//        System.out.println("Checking validity of: " + state.toString());
        for (Map.Entry<String, Double> entry : state.entrySet()) {
            stateFlow += entry.getValue();
        }

//        System.out.println("State flow: " + stateFlow + " Required flow: " + requiredFlow);
        return stateFlow == requiredFlow;
    }

    public Boolean isTarget(String targetID, double targetFlow) {
//        System.out.println("Called isTarget with: " + targetID + " and " + targetFlow);
        if (state.containsKey(targetID)) {
            if (state.get(targetID) == targetFlow) {
                return true;
            }
        }
        return false;
    }

    public Boolean isSameAs(MetaNode other) {
        return toString().equals(other.toString());
    }

    public HashMap<String, Double> getState() {
        return state;
    }

    public void setState(HashMap<String, Double> state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String toString() {
        return getState().toString();
    }
}
