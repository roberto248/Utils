package Utils;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Roberto Santos Cordeiro
 */
public class BinaryFilesUtils {

    /**
     * Se usa para verificar el nombre del archivo. Este no puede contener
     * caracteres prohibidos ni estar compuesto por espacios en blanco.
     */
    private final String SIMBOLOS_PROHIBIDOS_REGEX = "^(?!\\s*$)[^\\\\/:?\"<>|]+$";
    private final String SIMBOLOS_PROHIBIDOS_RUTA_REGEX = "^(?!\\s*$)[^:?\"<>|]+$";

    private boolean adjuntar;
    private File archivo;
    private String ruta;

    // CONSTRUCTORES ===========================================================
    public BinaryFilesUtils(String ruta) throws IOException {
        // Comprobando que la ruta es válida.
        if (ruta.matches(SIMBOLOS_PROHIBIDOS_RUTA_REGEX)) {
            throw new IllegalArgumentException("La ruta no es válida.");
        }

        this.archivo = new File(ruta);
        this.ruta = ruta;
        this.adjuntar = true;
    }

    public BinaryFilesUtils(File file) throws IOException {
        this.ruta = file.getAbsolutePath();

        // Comprobando que la ruta es válida.
        if (ruta.matches(SIMBOLOS_PROHIBIDOS_RUTA_REGEX)) {
            throw new IllegalArgumentException("La ruta no es válida.");
        }

        this.archivo = file;
        this.adjuntar = true;
    }

    // MÉTODOS =================================================================
    /**
     * Este método identifica si el archivo existe y fue escrito antes. Si aún
     * no se ha escrito nada se creará un objeto ObjectOutputStream que
     * escribirá una cabecera en el archivo que servirá para leerlo
     * posteriormente. Si ya fue escrito, se usará la clase interna
     * OOSSinCabecera para que no vuelva a escribir nuevas cabeceras y el
     * archivo siga siendo legible.
     *
     * @return ObjectOutputStream con el que se podrá escribir en el archivo.
     */
    private ObjectOutputStream abrirEscritor() {
        ObjectOutputStream out = null;

        try {
            // Si el archivo no existe o existe pero está vacío se crea y 
            // se escribe con cabecera.
            if (!existe() || (existe() && peso() == 0)) {
                out = new ObjectOutputStream(new FileOutputStream(ruta, adjuntar));

            } else { // Si existe y está escrito, se escribe sin cabecera.
                out = new OOSSinCabecera(new FileOutputStream(ruta, adjuntar));
            }

        } catch (IOException e) {
            printException(e);
        }

        return out;
    }

    /**
     * Elimina el archivo indicado en la variable ruta.
     *
     * @return true si el archivo ha sido borrado; false en caso contrario.
     */
    public boolean borrar() {
        return archivo.delete();
    }

    /**
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo binario.
     *
     * @param file
     * @return Devuelve un objeto del tipo BinaryFilesUtils que apunta al nuevo
     * archivo de copia.
     * @throws IOException
     */
    public BinaryFilesUtils copiar(File file) throws IOException {
        Object[] datos = leerTodo();
        BinaryFilesUtils copia = new BinaryFilesUtils(file);
        copia.escribirTodo(datos);

        return copia;
    }

    /**
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo binario.
     *
     * @param path ruta de la copia.
     * @return Devuelve un objeto del tipo BinaryFilesUtils que apunta al nuevo
     * archivo de copia.
     * @throws IOException
     */
    public BinaryFilesUtils copiar(String path) throws IOException {
        File file = new File(path);
        // Si el archivo ya existe se creará un archivo de copia nuevo.
        if (file.exists()) {
            file = new File(genPathCopy(path));
        }

        return copiar(file);
    }

    /**
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo binario.
     *
     * @return Devuelve un objeto del tipo BinaryFilesUtils que apunta al nuevo
     * archivo de copia.
     * @throws IOException
     */
    public BinaryFilesUtils copiar() throws IOException {
        return copiar(genPathCopy(ruta));
    }

    /**
     * Elimina la primera ocurrencia del dato pasado como parámetro y reescribe
     * el archivo sin él.
     *
     * @param dato Dato que se buscará para eliminar.
     * @return true si el eliminado ha salido correctamente, false en caso
     * contrario.
     */
    public boolean eliminar(Object dato) {
        ArrayList<Object> datos = new ArrayList<>(Arrays.asList(leerTodo()));
        boolean centinela = false;

        for (int i = 0; i < datos.size() && !centinela; i++) {

            if (datos.get(i).equals(dato)) {
                datos.remove(i);
                centinela = true;
            }
        }

        return reescribirArchivo(datos.toArray());
    }
    
    /**
     * Este método elimina el dato que ocupa la posicion pasada como parámetro.
     * 
     * @param posicion Posición que ocupa el dato que se eliminará. Este debe
     * ser un número entero entre 1 y n.
     * @return true si la eliminación se completó correctamente, false en caso
     * contrario.
     */
    public boolean eliminarEnPosicion(int posicion) {
        boolean borradoOk = false;
        
        // Si la posicion es correcta...
        if(posicion > 0 && posicion <= numDatos()){
            // Se leen los datos, se elimina el indicado y se reescribe el archivo.
            ArrayList<Object> datos = new ArrayList<>(Arrays.asList(leerTodo()));
            datos.remove(posicion - 1);
            borradoOk = reescribirArchivo(datos.toArray());
        }
        
        return borradoOk;
    }

    /**
     * Elimina todas las ocurrencias del dato pasado como parámetro y reescribe
     * el archivo sin ellos.
     *
     * @param dato Dato que se buscará para eliminar.
     * @return true si el eliminado ha salido correctamente, false en caso
     * contrario.
     */
    public boolean eliminarTodo(Object dato) {
        ArrayList<Object> datos = new ArrayList<>(Arrays.asList(leerTodo()));

        for (int i = 0; i < datos.size(); i++) {

            if (datos.get(i).equals(dato)) {
                datos.remove(i);
                i--; // Para compensar la posicion del dato que se eliminó.
            }
        }

        return reescribirArchivo(datos.toArray());
    }

    /**
     * Escribe en el archivo el dato que se le pasa como parámetro.
     *
     * @param dato
     * @return true => La escritura se realizó correctamente.<br>
     * false => Ha ocurrido un error. Puede que la escritura no se realizara.
     */
    public boolean escribir(Object dato) {
        boolean escrituraOk = true;
        Class<?> dataClass = dato.getClass();

        try (ObjectOutputStream out = abrirEscritor()) {

            switch (dataClass.getSimpleName()) {
                case "Boolean":
                    out.writeByte(1);
                    out.writeBoolean((Boolean) dato);
                    break;
                case "Character":
                    out.writeByte(2);
                    out.writeChar((Character) dato);
                    break;
                case "Double":
                    out.writeByte(3);
                    out.writeDouble((Double) dato);
                    break;
                case "Float":
                    out.writeByte(4);
                    out.writeFloat((Float) dato);
                    break;
                case "Integer":
                    out.writeByte(5);
                    out.writeInt((Integer) dato);
                    break;
                case "Long":
                    out.writeByte(6);
                    out.writeLong((Long) dato);
                    break;
                case "Short":
                    out.writeByte(7);
                    out.writeShort((Short) dato);
                    break;
                case "String":
                    out.writeByte(8);
                    out.writeUTF((String) dato);
                    break;
                default:
                    // Si el tipo de dato no se corresponde con ninguno de los anteriores...
                    out.writeByte(100);
                    out.writeObject(dato);
            }

        } catch (IOException e) {
            escrituraOk = false;
        }

        return escrituraOk;
    }

    /**
     * Este método escribe en el archivo binario una serie de datos pasados como
     * parámetro en un array de Object.
     * @param datos array de objetos que se escribiran en el archivo.
     * @return true si la escritura de todos los datos se ha realizado correctamente,
     * false en caso contrario.
     */
    public boolean escribirTodo(Object[] datos) {
        boolean escrituraOk = true;

        for (Object dato : datos) {
            /* El if hará la escritura del objeto y a la vez comprobará que ha
            salido bien. De no ser así se frenará el bucle y se devolverá un false.*/
            if (!escribir(dato)) {
                escrituraOk = false;
                break;
            }
        }

        return escrituraOk;
    }

    /**
     * Comprueba si el archivo indicado en la constante RUTA existe.
     *
     * @return true si el archivo existe; false en caso contrario.
     */
    public boolean existe() {
        return archivo.exists();
    }

    /**
     * Este método genera un nuevo nombre para un archivo copiado construye una
     * nueva ruta que apunta al mismo. La sintaxis del nuevo nombre será:
     * "nombre - copia (n).dat" siendo n el número de copias que ya existen.
     *
     * @param path
     * @return
     */
    private String genPathCopy(String path) {
        String nuevaRuta;
        int i = 1; // Indica el número de copia repetida con el mismo nombre.
        do {

            // Esta linea comprueba el tipo de ruta.
            String tipoBarra = (path.contains("/")) ? "/" : "\\";
            // Construyendo el nuevo nombre de la copia.
            String nombre = getNombre().substring(0, getNombre().lastIndexOf("."))
                    + " - copia" + ((i == 1) ? "" : " (" + i + ")")
                    + getNombre().substring(getNombre().lastIndexOf("."));
            // Construyendo la ruta al archivo con el nuevo nombre.
            nuevaRuta = path.substring(0, path.lastIndexOf(tipoBarra) + 1)
                    + nombre;
            i++;
            // Aumentará el número de la copia mientras existan otras con número mas bajo.
        } while (new File(nuevaRuta).exists());

        return nuevaRuta;
    }

    /**
     * En base al byte pasado como parámetro, el método sabrá que tipo de dato
     * se leerá a continuación, recogerá ese dato y lo devolverá.
     *
     * @param etiqueta Byte que se usa para determinar que tipo de dato se leerá
     * a continuación.
     * @param in ObjectInputStream que leerá los datos.
     * @return Object con el dato que se leyó.
     */
    private Object leerDato(byte etiqueta, ObjectInputStream in) {
        Object obj = null;

        try {
            switch (etiqueta) {
                case 1: // Boolean
                    obj = in.readBoolean();
                    break;
                case 2: // Character
                    obj = in.readChar();
                    break;
                case 3: // Double
                    obj = in.readDouble();
                    break;
                case 4: // Float
                    obj = in.readFloat();
                    break;
                case 5: // Integer
                    obj = in.readInt();
                    break;
                case 6: // Long
                    obj = in.readLong();
                    break;
                case 7: // Short
                    obj = in.readShort();
                    break;
                case 8: // String
                    obj = in.readUTF();
                    break;
                default: // Cualquier otro
                    obj = in.readObject();
            }

        } catch (EOFException e) {
            // No es necesario realizar ninguna acción aqui.
        } catch (IOException ex) {
            System.out.println("Error al leer el archivo.");

        } catch (ClassNotFoundException ex) {
            System.out.println("Clase no encontrada.");

        } catch (Exception e) {
            printException(e);
        }

        return obj;
    }

    /**
     * Este método devuelve el dato en la posicion que pasada como parámetro.
     * La posición debe estar entre 1 y N.
     * @param posicion Posición en el archivo del dato que se quiere leer.
     * @return Dato leido en la posicion especificada.
     */
    public Object leerEnPosicion(int posicion) {
        Object obj = null;
        
        // Si el archivo existe y la posición es correcta...
        if (existe() && posicion > 0 && posicion <= numDatos()) {
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(archivo))) {
         
                for (int i = 1; i <= posicion; i++) {
                    // Si es la posición indicada se guarda el dato requerido.
                    if (i == posicion) {
                        obj = leerDato(in.readByte(), in);
                        
                    } else {
                        // Si NO es la posicion adecuada se lee el dato para pasar al siguiente.
                        leerDato(in.readByte(), in);
                    }
                }
            }catch (EOFException e) {
                // No es necesario realizar ninguna acción aqui.
            } catch (IOException ex) {
                System.out.println("Error al leer el archivo.");
            } catch (Exception e) {
                printException(e);
            }
        }

        return obj;
    }
    
    /**
     * Este método lee el archivo de principio a fin y devuelve un Object[] con
     * el contenido del mismo.
     *
     * @return Object[] Array de objetos con todos los datos guardados en el
     * archivo.
     */
    public Object[] leerTodo() {
        ArrayList<Object> dataList = new ArrayList<>();

        if (existe()) {
            try (ObjectInputStream in = new ObjectInputStream(
                    new FileInputStream(archivo))) {

                while (true) {
                    /* Lee el byte etiqueta, lee el siguiente dato del archivo 
                    y lo añade al array */
                    dataList.add(leerDato(in.readByte(), in));
                }

            } catch (EOFException e) {
                // No es necesario realizar ninguna acción aqui.
            } catch (IOException ex) {
                System.out.println("Error al leer el archivo.");
            } catch (Exception e) {
                printException(e);
            }
        }

        return dataList.toArray(new Object[0]);
    }

    /**
     * Indica la cantidad de datos que están escritos en el archivo.
     * @return 
     */
    public int numDatos(){
        return leerTodo().length;
    }

    /**
     * Calcula el tamaño del fichero.
     *
     * @return Peso del fichero en bytes.
     */
    public long peso() {
        return archivo.length();
    }
    
    /**
     * Imprime por pantalla la clase y el mensaje de la excepción pasada como
     * parámetro con un fondo de color rojo y letras en blanco para que resalte
     * más en consola.
     *
     * @param e Exception que se imprimirá por pantalla.
     */
    private void printException(Exception e) {
        System.out.println("\u001B[41m\u001B[37m " + e.getClass()
                + " \n " + e.getMessage() + " \u001B[0m\n");
    }

    /**
     * Reemplaza la primera ocurrencia del dato especificado con
     * un dato nuevo pasado como parámetro.
     *
     * @param objAntiguo dato que se buscará para reemplazar.
     * @param objNuevo nuevo dato por el que reemplazará el antiguo.
     * @return true si se consigue reemplazar correctamente, false en caso
     * contrario.
     */
    public boolean reemplazar(Object objAntiguo, Object objNuevo) {
        Object[] datos = leerTodo();
        boolean centinela = false;

        for (int i = 0; i < datos.length && !centinela; i++) {

            if (datos[i].equals(objAntiguo)) {
                datos[i] = objNuevo;
                centinela = true;
            }
        }

        return reescribirArchivo(datos);
    }

    /**
     * Reemplaza todas las ocurrencias que encuentre del dato especificado con
     * un dato nuevo pasado como parámetro.
     *
     * @param objAntiguo dato que se buscará para reemplazar.
     * @param objNuevo nuevo dato por el que reemplazará el antiguo.
     * @return true si se consigue reemplazar correctamente, false en caso
     * contrario.
     */
    public boolean reemplazarTodo(Object objAntiguo, Object objNuevo) {
        Object[] datos = leerTodo();

        for (int i = 0; i < datos.length; i++) {

            if (datos[i].equals(objAntiguo)) {
                datos[i] = objNuevo;
            }
        }
        return reescribirArchivo(datos);
    }

    /**
     * Este método cambia el nombre del archivo por uno nuevo pasado como
     * parámetro.
     *
     * @param nuevoNombre El nombre nuevo que se le dará al archivo.
     * @return true si se pudo cambiar el nombre, false si no se pudo.
     */
    public boolean renombrar(String nuevoNombre) {
        boolean renombreOk;

        // Si el nuevo nombre NO contiene símbolos prohibidos...
        if (valNombreArchivo(nuevoNombre)) {
            Object[] datos = leerTodo();
            // Esta linea comprueba el tipo de ruta.
            String tipoBarra = (ruta.contains("/")) ? "/" : "\\";
            // Asegurando que el nuevo nombre incluye la extensión.
            nuevoNombre = (nuevoNombre.endsWith(".dat"))
                    ? nuevoNombre : nuevoNombre + ".dat";
            // Construyendo la ruta al archivo con el nuevo nombre.
            String nuevaRuta = ruta.substring(0, ruta.lastIndexOf(tipoBarra) + 1)
                    + nuevoNombre;

            borrar();
            ruta = nuevaRuta;
            archivo = new File(nuevaRuta);
            renombreOk = escribirTodo(datos);
        } else {
            renombreOk = false;
        }

        return renombreOk;
    }

    /**
     * Este método elimina el archivo y crea otro con el mismo nombre en la
     * misma ruta. El nuevo archivo contendrá los nuevos datos pasados como
     * párametro en un Array de Object.
     *
     * @param objetos Array de Object con los datos por los que se sustituirán
     * los originales.
     * @return true si la reescritura a salido correctamente, false en caso
     * contrario.
     */
    public boolean reescribirArchivo(Object[] objetos) {
        borrar();
        return escribirTodo(objetos);
    }

    /**
     * Comprueba que el nombre de archivo pasado como parámetro es válido. Esto
     * incluye que no contenga caracteres prohibidos, y que no este compuesto
     * exclusivamete por espacios en blanco.
     *
     * @param nombre Nombre que se comprobará.
     * @return true si el nombre es valido, false en caso contrario.
     */
    private boolean valNombreArchivo(String nombre) {
        boolean nombreOk;
        if (nombre.matches(SIMBOLOS_PROHIBIDOS_REGEX)) {
            nombreOk = true;
        } else {
            System.out.println("Los nombres de archivo no pueden contener "
                    + "ninguno de los siguientes caracteres: "
                    + "\n\t\\ / : * ? \" < > |");
            nombreOk = false;
        }
        return nombreOk;
    }

    // GETTERS =================================================================
    public String getRuta() {
        return ruta;
    }

    public File getArchivo() {
        return archivo;
    }

    public String getNombre() {
        // Esta linea comprueba el tipo de ruta.
        String tipoBarra = (ruta.contains("/")) ? "/" : "\\";
        return ruta.substring(ruta.lastIndexOf(tipoBarra) + 1);
    }

    // SETTERS =================================================================
    /**
     * En este método se especifica si los datos que se escriban en el archivo
     * se añadirán a los ya existentes o se sobreescribirán.
     *
     * @param adjuntar <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; true => El texto se añadirá al que ya
     * existe.<br>
     * &nbsp;&nbsp;&nbsp;&nbsp; false => Se borrará el contenido del archivo
     * antes de incluir el nuevo texto.
     */
    public void setAdjuntar(boolean adjuntar) {
        this.adjuntar = adjuntar;
    }
}

/*============================================================================*/
/**
 * Esta clase extiende de ObjectOutputStream y sobreescribe el método
 * writeStreamHeader() para que no se escriba la cabecera a la hora de
 * introducir nuevos datos en el archivo.
 *
 * @author Roberto Santos Cordeiro
 */
class OOSSinCabecera extends ObjectOutputStream {

    // CONSTRUCTORES
    public OOSSinCabecera(OutputStream out) throws IOException {
        super(out);
    }

    public OOSSinCabecera() throws IOException, SecurityException {
    }

    // MÉTODOS
    /**
     * Restablece el estado del flujo de salida, lo que incluye eliminar
     * cualquier cabecera almacenada en el flujo. Esto evitará problemas de
     * compatibilidad al mezclar tipos de datos primitivos y complejos en el
     * archivo.
     *
     * @throws IOException
     */
    @Override
    protected void writeStreamHeader() throws IOException {
        reset();
    }

}
