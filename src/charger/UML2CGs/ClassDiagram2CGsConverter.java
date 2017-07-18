package charger.UML2CGs;

import charger.obj.Arrow;
import charger.obj.Concept;
import charger.obj.Graph;
import charger.obj.Relation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bingyang.wei on 7/17/2017.
 */
public class ClassDiagram2CGsConverter extends UML2CGsConverter{

    private HashMap<String, String> classStore = new HashMap<>(10);

    public ClassDiagram2CGsConverter(Graph graph, ArrayList<Document> documents) {
        super(graph, documents);
    }

    public void init(Document doc){
        NodeList listOfClasses = doc.getElementsByTagName("UML:Class");

        for(int i=0; i<listOfClasses.getLength() ; i++) {
            Node classNode = listOfClasses.item(i);
            if (classNode.hasChildNodes()) {
                Element classElement = (Element) classNode;
                classStore.put(classElement.getAttribute("xmi.id"), classElement.getAttribute("name"));
            }
        }
    }

    @Override
    public void convert() {

        for (Document doc: getDocuments()) {
            init(doc);
            NodeList listOfClasses = doc.getElementsByTagName("UML:Class");

            int totalClasses = listOfClasses.getLength();
            System.out.println("Total number of classes : " + totalClasses);

            for(int i=0; i<listOfClasses.getLength() ; i++){
                Node classNode = listOfClasses.item(i);
                if(classNode.hasChildNodes()){
                    Element classElement = (Element)classNode;
                    Concept classConcept = new Concept();
                    classConcept.setTextLabel(classElement.getAttribute("name") + " @forall");
                    getGraph().insertObject(classConcept);
                    System.out.println("UML Class: " + classElement.getAttribute("name"));

                    NodeList attributeList = classElement.getElementsByTagName("UML:Attribute");
                    for(int k=0; k<attributeList.getLength() ; k++) {
                        Element attributeElement = (Element) attributeList.item(k);
                        String attributeName = attributeElement.getAttribute("name");

                        //create a concept node for attribute
                        Concept attributeConcept = new Concept();
                        attributeConcept.setTextLabel(attributeName);
                        //create a concept node for relation Attribute
                        Relation attributeRelation = new Relation();
                        attributeRelation.setTextLabel("attribute");
                        //create two arrows to connect them together
                        Arrow a1 = new Arrow(classConcept, attributeRelation);
                        Arrow a2 = new Arrow( attributeRelation, attributeConcept);
                        //add everything into the graph
                        getGraph().insertObject(attributeConcept);
                        getGraph().insertObject(attributeRelation);
                        getGraph().insertObject(a1);
                        getGraph().insertObject(a2);

                        System.out.println("   Attr Name : " + attributeName);
                    }

                    NodeList operationList = classElement.getElementsByTagName("UML:Operation");
                    for(int j=0; j<operationList.getLength() ; j++) {
                        Element operationElement = (Element) operationList.item(j);
                        String operationName = operationElement.getAttribute("name");

                        //create a concept node for attribute
                        Concept operationConcept = new Concept();
                        operationConcept.setTextLabel(operationName);
                        //create a concept node for relation Attribute
                        Relation operationRelation = new Relation();
                        operationRelation.setTextLabel("operation");
                        //create two arrows to connect them together
                        Arrow a1 = new Arrow(classConcept, operationRelation);
                        Arrow a2 = new Arrow( operationRelation, operationConcept);
                        //add everything into the graph
                        getGraph().insertObject(operationConcept);
                        getGraph().insertObject(operationRelation);
                        getGraph().insertObject(a1);
                        getGraph().insertObject(a2);

                        System.out.println("   Oper Name : " + operationName);
                    }

                    NodeList associationList = doc.getElementsByTagName("UML:Association");
                    for(int k=0; k<associationList.getLength() ; k++) {
                        Element associationElement = (Element) associationList.item(k);
                        NodeList listOfAssociatedClasses = associationElement.getElementsByTagName("UML:Class");
                        Element participatedClass1 = (Element)listOfAssociatedClasses.item(0);
                        Element participatedClass2 = (Element)listOfAssociatedClasses.item(1);
                        if(classElement.getAttribute("xmi.id").equals(participatedClass1.getAttribute("xmi.idref"))){
                            //create a concept node for attribute
                            Concept associatedConcept = new Concept();
                            associatedConcept.setTextLabel(classStore.get(participatedClass2.getAttribute("xmi.idref")));
                            //create a concept node for relation Attribute
                            Relation associationRelation = new Relation();
                            associationRelation.setTextLabel("association");
                            //create two arrows to connect them together
                            Arrow a1 = new Arrow(classConcept, associationRelation);
                            Arrow a2 = new Arrow( associationRelation, associatedConcept);
                            //add everything into the graph
                            getGraph().insertObject(associatedConcept);
                            getGraph().insertObject(associationRelation);
                            getGraph().insertObject(a1);
                            getGraph().insertObject(a2);

                        }else if(classElement.getAttribute("xmi.id").equals(participatedClass2.getAttribute("xmi.idref"))){
                            //create a concept node for attribute
                            Concept associatedConcept = new Concept();
                            associatedConcept.setTextLabel(classStore.get(participatedClass1.getAttribute("xmi.idref")));
                            //create a concept node for relation Attribute
                            Relation associationRelation = new Relation();
                            associationRelation.setTextLabel("association");
                            //create two arrows to connect them together
                            Arrow a1 = new Arrow(classConcept, associationRelation);
                            Arrow a2 = new Arrow( associationRelation, associatedConcept);
                            //add everything into the graph
                            getGraph().insertObject(associatedConcept);
                            getGraph().insertObject(associationRelation);
                            getGraph().insertObject(a1);
                            getGraph().insertObject(a2);

                        }else{
                            continue;
                        }
                    }

                }//end of if clause
            }//end of for loop with s var
        }
        classStore.clear();
    }
}
