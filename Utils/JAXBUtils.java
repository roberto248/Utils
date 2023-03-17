package Utils;

import java.io.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Roberto Santos Cordeiro 53610423E
 * @param <T>
 */
public class JAXBUtils<T> {

    private T object;
    private JAXBContext contexto;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;
    private String rutaXSD;

    //CONSTRUCTORES
    public JAXBUtils(T object) {
        this.object = object;
        
        rutaXSD = "";
        marshaller = null;
        unmarshaller = null;

        try {
            contexto = JAXBContext.newInstance(object.getClass());
        } catch (JAXBException e) {
            System.out.println(e.getMessage());
        }
    }

    public JAXBUtils(T object, String rutaXSD) {
        super();
        this.rutaXSD = rutaXSD;
    }

    // MÉTODOS
    /**
     * Prepara el marshaller para generar un XML a partir de objetos de Java.
     */
    public void marshall() {
        try {
            marshaller = contexto.createMarshaller();

            // Para que meta espacios, tabulaciones...
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Para que incluya la validacion con XSD.
            if (!rutaXSD.isEmpty()) {
                marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, this.rutaXSD);
            }

        } catch (JAXBException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Lee un archivo XML y lo mapea a objetos de Java.
     * @param RutaXML Ruta del documento XML que se mapeará.
     * @return 
     */
    public T unmarshall(String RutaXML) {
        try {
            unmarshaller = contexto.createUnmarshaller();
            File file = new File(RutaXML);
            object = (T) unmarshaller.unmarshal(file);

        } catch (JAXBException e) {
            System.out.println(e.getMessage());
        }

        return object;
    }

    /**
     * Muestra por pantalla el XML generado a partir de objetos de Java.
     * @throws JAXBException 
     */
    public void print() throws JAXBException {
        marshaller.marshal(object, System.out);
    }

    /**
     * Genera un XML a partir de objetos de Java y lo muestra a traves del 
     * PrintStream pasado como parámetro. 
     * @param ps PrintStream que servira como salida del XML generado.
     * @throws JAXBException 
     */
    public void print(PrintStream ps) throws JAXBException {
        marshaller.marshal(object, ps);
    }

    /**
     * Tras hacer marshal de un objeto esta funcion lo imprime en un archivo de texto.
     * @param nombreArchivo nombre del archivo de texto que se creará.
     * @throws JAXBException
     * @throws IOException 
     */
    public void export(String nombreArchivo) throws JAXBException, IOException {
        marshaller.marshal(object, new FileWriter(nombreArchivo));
    }

}
