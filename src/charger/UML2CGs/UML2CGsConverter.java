package charger.UML2CGs;

import charger.obj.Graph;
import org.w3c.dom.Document;

import java.util.ArrayList;

/**
 * Created by bingyang.wei on 7/17/2017.
 */
public abstract class UML2CGsConverter implements Converter{
    private Graph graph;
    private ArrayList<Document> documents;

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public ArrayList<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<Document> documents) {
        this.documents = documents;
    }

    public UML2CGsConverter(Graph graph, ArrayList<Document> documents) {
        this.graph = graph;
        this.documents = documents;
    }
}
