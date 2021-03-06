package infomgag.monteCarloDealEvaluator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class Node {
	private int visits;
	private float reward;
	private Gamestate state;
	private Map<Gamestate, Node> children;
	
	//private TreeMap<TreeSet<String>, Node> children2; // The set of strings represents the orders.
	
	private Node parent;
	
	public Node(Node parent, Gamestate state){
		this.state = state;
		this.parent = parent;
		this.visits = 1;
		this.reward = 0.0f;
		this.children = new TreeMap<Gamestate, Node>();
	}
	
	public void addChild(Gamestate childstate){
		Node child = new Node(this, childstate);
		this.children.put(childstate, child);
	}
	
	public void update(float iteration){
		this.reward += iteration;
		this.visits +=1;
	}
	
	public String toString(){
		return "Node:" + this.state.hashCode() + " Parent:" + 
				this.parent.state.hashCode() + " Visits:" + 
				this.visits + " Reward:" + this.reward + 
				" Children:" + children.size();
	}
}
