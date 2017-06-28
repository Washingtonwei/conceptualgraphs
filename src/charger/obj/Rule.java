package charger.obj;

import java.util.HashMap;

/*
 * By Bingyang Wei
 */
public class Rule {
	private Graph antecedent;
	private Graph consequent;

	//used to keep track of coref linked nodes from antecedent to consequent
	//for now, only concepts are considered
	private HashMap<GNode, GNode> equivlentConcepts = new HashMap<>();

	public Graph getAntecedent() {
		return antecedent;
	}

	public void setAntecedent(Graph antecedent) {
		this.antecedent = antecedent;
	}

	public Graph getConsequent() {
		return consequent;
	}

	public void setConsequent(Graph consequent) {
		this.consequent = consequent;
	}

	public HashMap<GNode, GNode> getEquivlentConcepts() {
		return equivlentConcepts;
	}

	public void setEquivlentConcepts(HashMap<GNode, GNode> equivlentConcepts) {
		this.equivlentConcepts = equivlentConcepts;
	}
}
