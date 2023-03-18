package Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Roberto Santos Cordeiro
 */
public class DOMUtils {

    // Para validar usando XSD
    private final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

    private Document doc;
    private Element raiz;

    /**
     * Para cargar un documento XML en un arbol DOM
     * @param rutaXML La ruta al archivo XML que se quiere cargar.
     * @param valDTD true si el XML se valida con DTD, false si no.
     * @param valXSD true si el XML se valida con XSD, false si no.
     * @param espacioDeNombres true si el XML tiene espacio de nombres, false si no.
     * @param ignorarComentarios true para que no se tengan en cuenta los comentarios del XML.
     */
    public DOMUtils(String rutaXML, boolean valDTD, boolean valXSD, boolean espacioDeNombres, boolean ignorarComentarios) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(valDTD); // Valida con DTD?
            dbf.setNamespaceAware(espacioDeNombres); // Tiene soporte para espacio de nombres?
            dbf.setIgnoringComments(ignorarComentarios); // Ignora los comentarios?
            dbf.setIgnoringElementContentWhitespace(true); // Ignora los espacios en blanco.

            // Si el XML se valida con XSD...
            if (valXSD) {
                dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            }

            DocumentBuilder constructor = dbf.newDocumentBuilder();
            constructor.setErrorHandler(new SimpleErrorHandler()); // Control de errores.

            // Si el archivo especificado no tiene la extensión, se añade.
            if (!rutaXML.endsWith(".xml")) {
                rutaXML += ".xml";
            }

            doc = constructor.parse(new File(rutaXML)); // Creando el documento.
            raiz = doc.getDocumentElement(); // Sacando el elemento raiz del documento.

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Para crear en memoria un nuevo arbol DOM vacio.
     * @param nombreRaiz 
     */
    public DOMUtils(String nombreRaiz) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            DOMImplementation implementacion = db.getDOMImplementation();
            doc = implementacion.createDocument(null, nombreRaiz, null);

            // estableciendo caracteristicas al documento xml
            doc.setXmlStandalone(false);
            doc.setXmlVersion("1.1");
            
            raiz = doc.getDocumentElement();
            
        } catch (ParserConfigurationException ex) {
            System.out.println(ex.getMessage());
        }

    }

    // METODOS
    /**
     * Añade un nuevo atributo al elemento pasado como parámetro.
     * @param element Elemento al que se añadirá el atributo.
     * @param nombreAttr Nombre del atributo.
     * @param valorAtrr Valor del atributo.
     */
    public void addAtributo(Element element, String nombreAttr, String valorAtrr) {
        element.setAttribute(nombreAttr, valorAtrr);
    }

    /**
     * Añade un nuevo elemento después del elemento pasado como parámetro.
     * @param dadElement Elemento que estará antes del que se añadirá.
     * @param nameNewElement Nombre del muevo elemento.
     * @param valorElement Valor del nuevo elemento. 
     * Puede estar vacio si se quiere añadir nuevos elementos dentro del que se añadirá.
     */
    public void addElementoPosterior(Element dadElement, String nameNewElement, String valorElement) {
        Element nuevoElemento = doc.createElement(nameNewElement);

        // Si el nuevo elemento va a tener un valor se añade
        if (!valorElement.isEmpty()) {
            Text valor = doc.createTextNode(valorElement);
            nuevoElemento.appendChild(valor);
        }

        // Añadiendo el nuevo elemento al elemento padre.
        dadElement.appendChild(nuevoElemento);
    }

    /**
     * Añade un nuevo elemento antes del elemento pasado como parámetro.
     * @param dadElement Elemento que estará después del que se añadirá.
     * @param nameNewElement Nombre del muevo elemento.
     * @param valorElement Valor del nuevo elemento. 
     * Puede estar vacio si se quiere añadir nuevos elementos dentro del que se añadirá.
     */
    public void addElementoAnterior(Element dadElement, String nameNewElement, String valorElement) {
        Element primerHijo = (Element) dadElement.getFirstChild();
        Element nuevoElement = doc.createElement(nameNewElement);

        // Si el nuevo elemento va a tener un valor se añade
        if (!valorElement.isEmpty()) {
            Text valor = doc.createTextNode(valorElement);
            nuevoElement.appendChild(valor);
        }

        dadElement.insertBefore(nuevoElement, primerHijo);
    }

    /**
     * Elimina el elemento pasado como parámetro del arbol DOM.
     * @param elemento Elemento que se eliminará del arbol DOM
     */
    public void borrarNodo(Element elemento) {
        raiz.removeChild(elemento);
    }

    /**
     * Crea una copia del elemento pasado como parámetro y lo coloca al final 
     * de su elemento padre (si se pasa el parámetro) o al final del raiz. 
     * @param ElementoAClonar Elemento del que se hará la copia.
     * @param dadElement Elemento padre dentro del cual se añadirá la copia.
     */
    public void clonarNodo(Element ElementoAClonar, Element dadElement) {
        Element clon = (Element) ElementoAClonar.cloneNode(true);

        // Si se especifica un elemento padre el clon se añade al final del mismo.
        if (dadElement != null) {
            dadElement.appendChild(clon);

        } else {
            // Si no se especifica un elemento padre se añade al final del raiz.
            raiz.appendChild(clon);
        }
    }

    /**
     * Reemplaza un elemento con otro nuevo.
     * @param oldElement Antiguo elemento que se reemplazará.
     * @param newElement Nuevo elemento que tomará el lugar del antiguo.
     */
    public void reemplazarElemento(Element oldElement, Element newElement) {
        raiz.replaceChild(newElement, oldElement);
    }

    /**
     * Genera un documento XML a partir del arbol DOM cargado en memoria.
     * @param nombreArchivo Nombre del archivo XML que se creará.
     * @throws TransformerConfigurationException
     * @throws IOException
     * @throws TransformerException 
     */
    public void transformarEnXML(String nombreArchivo) throws TransformerConfigurationException, IOException, TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        // Sangrado cada 3 espacios.
        transformerFactory.setAttribute("indent-number", 3);

        Transformer trans = transformerFactory.newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

        DOMSource domSource = new DOMSource(doc);

        // Si el nombre del archivo especificado no tiene la extensión, se añade.
        if (!nombreArchivo.endsWith(".xml")) {
            nombreArchivo += ".xml";
        }

        // Escribiendo el XML
        FileWriter write = new FileWriter(nombreArchivo);
        StreamResult sr = new StreamResult(write);

        // Por si ocurre un error irrecuperable
        trans.transform(domSource, sr);

    }
    
    /**
     * Imprime por pantalla la información de un elemento y todos sus hijos buscándolo por su id.
     * @param id Id del elemento que se quiera mostrar.
     */
    public void verInfoById(String id){
        Element element = doc.getElementById(id);
        visualizarTodosNodos(element);
    }

    /**
     * Imprime por pantalla la información de un nodo y todos sus hijos.
     * @param nodo Nodo del cual se quiere mostrar toda la información
     */
    public void visualizarTodosNodos(Node nodo) {
        // Visualizar atributos.
        NamedNodeMap att = nodo.getAttributes();
        if (att != null) {
            for (int i = 0; i < att.getLength(); i++) {
                System.out.println(att.item(i).getNodeName() + ": " + att.item(i).getNodeValue());
            }
        }

        // Visualizar info
        if (nodo.getNodeValue() != null) {
            System.out.println(nodo.getParentNode().getNodeName()
                    + ": " + nodo.getNodeValue());
        }

        // Visualizar hijos
        NodeList hijos = nodo.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            visualizarTodosNodos(hijos.item(i));
        }
    }

    /**
     * Imprime por pantalla la información de una lista de nodos y sus respectivos hijos.
     * @param lista Lista de los nodos cuyos datos se van a mostrar.
     */
    public void visualizarTodosNodos(NodeList lista) {
        for (int i = 0; i < lista.getLength(); i++) {
            visualizarTodosNodos(lista.item(i));
            System.out.println("");
        }
    }

    // GETTERS
    public Element getRaiz() {
        return raiz;
    }

    public Document getDoc() {
        return doc;
    }
}

/**
 * Clase para control de errores
 * @author Roberto Santos Cordeiro
 */
class SimpleErrorHandler  implements ErrorHandler{

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
