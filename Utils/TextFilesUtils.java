package Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Roberto Santos Cordeiro
 */
public class TextFilesUtils {

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
    public TextFilesUtils(String ruta) {
        // Comprobando que la ruta es válida.
        if (ruta.matches(SIMBOLOS_PROHIBIDOS_RUTA_REGEX)) {
            throw new IllegalArgumentException("La ruta no es válida.");
        }

        this.archivo = new File(ruta);
        this.ruta = ruta;
        this.adjuntar = true;
    }

    public TextFilesUtils(File file) {
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
     * Elimina el archivo indicado en la variable ruta.
     *
     * @return true si el archivo ha sido borrado; false en caso contrario.
     */
    public boolean borrar() {
        return archivo.delete();
    }

    /**
     * Este método busca las lineas del archivo que contengan la cadena pasada
     * como parámetro y devuleve un array con las mismas.
     *
     * @param busqueda La cadena que se buscará.
     * @return String[] con las líneas del texto que contengan la búsqueda.
     */
    public String[] buscarLineasPorTexto(String busqueda) {
        String[] lineas = leerLineasTexto();
        ArrayList<String> ocurrencias = new ArrayList<>();

        for (String texto : lineas) {
            if (texto.contains(busqueda)) {
                ocurrencias.add(texto);
            }
        }

        return ocurrencias.toArray(new String[0]);
    }

    /**
     * Cuenta el número de ocurrencias de una cadena en el texto pasado como
     * parámetro.
     *
     * @param busqueda La cadena de la que se buscarán ocurrencias.
     * @param texto Texto en que le buscarán las ocurrencias.
     * @return Número de ocurrencias.
     */
    public int contarOcurrencias(String busqueda, String texto) {
        int cont = 0;
        int i = texto.indexOf(busqueda);

        while (i != -1) {
            cont++;
            i = texto.indexOf(busqueda, i + busqueda.length());
        }

        return cont;
    }

    /**
     * Cuenta las ocurrencias de una cadena en todo el texto del fichero.
     *
     * @param busqueda La cadena de la que se buscarán ocurrencias.
     * @return Número de ocurrencias.
     */
    public int contarOcurrencias(String busqueda) {
        return contarOcurrencias(busqueda, leerTexto());
    }

    /**
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo de texto.
     *
     * @param file
     * @return Devuelve un objeto del tipo TextFilesUtils que apunta al nuevo
     * archivo de copia.
     */
    public TextFilesUtils copiar(File file) {
        String[] lineas = leerLineasTexto();
        TextFilesUtils copia = new TextFilesUtils(file);
        copia.escribirVariasLineas(lineas);

        return copia;
    }

    /**
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo de texto.
     *
     * @param path ruta de la copia.
     * @return Devuelve un objeto del tipo TextFilesUtils que apunta al nuevo
     * archivo de copia.
     */
    public TextFilesUtils copiar(String path) {
        File file = new File(path);
        // Si el archivo ya existe se creará un archivo de copia nuevo.
        if (file.exists()) {
            file = new File(genPathCopy(path));
        }

        return copiar(file);
    }

    /**
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo de texto.
     *
     * @return Devuelve un objeto del tipo TextFilesUtils que apunta al nuevo
     * archivo de copia.
     */
    public TextFilesUtils copiar() {
        return copiar(genPathCopy(ruta));
    }

    /**
     * Elimina la linea indicada como parámetro del archivo.
     *
     * @param linea número de linea que se eliminara. Debe estar entre 1 y N.
     * @return true si la eliminación fue exitosa, false si no lo fue.
     */
    public boolean eliminarLinea(int linea) {
        ArrayList<String> lineas = new ArrayList<>(Arrays.asList(leerLineasTexto()));

        if (linea > 0 && linea < numLineas()) {
            lineas.remove(linea - 1); // Ajustar a la linea indicada
        }

        return reescribirArchivo(lineas.toArray(new String[0]));
    }

    /**
     * Elimina las lineas indicadas en el Array pasado como parámetro del
     * archivo.
     *
     * @param indexes números de las lineas que se eliminarán. Deben estar entre
     * 1 y N.
     * @return true si la eliminación fue exitosa, false si no lo fue.
     */
    public boolean eliminarVariasLinea(Integer[] indexes) {
        ArrayList<String> lineas = new ArrayList<>(Arrays.asList(leerLineasTexto()));

        // Ordenando el array de indices y revirtiendolo...
        Collections.sort(Arrays.asList(indexes));
        Collections.reverse(Arrays.asList(indexes));

        // eliminando las líneas desde el final del archivo.
        for (int index : indexes) {
            if (index > 0 && index < numLineas()) {
                lineas.remove(index - 1); // Ajustar a la linea indicada
            }

        }

        return reescribirArchivo(lineas.toArray(new String[0]));
    }

    /**
     * Escribe en el archivo una nueva línea con el texto pasado como parámetro.
     * Si el archivo no existe se creará.
     *
     * @param texto Texto que se escribirá en el archivo.
     * @return true => La escritura se realizó correctamente.<br>
     * false => Ha ocurrido un error. Puede que la escritura no se realizara.
     */
    public boolean escribirLinea(String texto) {
        boolean escrituraOk = true;

        try (BufferedWriter out = new BufferedWriter(new FileWriter(archivo, adjuntar))) {
            out.write(texto);
            out.newLine(); // Salto de línea.

        } catch (IOException e) {
            escrituraOk = false;
        }

        return escrituraOk;
    }

    /**
     * Escribe en el archivo varias líneas de texto.
     *
     * @param lineas array de String con las líneas que se escribirán en el
     * archivo.
     * @return true => La escritura se realizó correctamente.<br>
     * false => Ha ocurrido un error. Puede que la escritura no se realizara.
     */
    public boolean escribirVariasLineas(String[] lineas) {
        boolean escrituraOk = true;

        for (String linea : lineas) {
            /* El if hará la escritura de la linea y a la vez comprobará que ha
            salido bien. De no ser así se frenará el bucle y se devolverá un false.*/
            if (!escribirLinea(linea)) {
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
     * Lee el archivo de principio a fin y lo muestra por pantalla.
     *
     */
    public void imprimirTexto() {
        if (existe()) {
            try (BufferedReader in = new BufferedReader(new FileReader(archivo))) {
                String linea = in.readLine();

                while (linea != null) {
                    System.out.println(linea);
                    linea = in.readLine();
                }

            } catch (EOFException e) {
                // No es necesario realizar ninguna acción aqui.
            } catch (IOException e) {
                System.out.println("Error al leer el archivo.");
            }
        } else {
            System.out.println("Error. El archivo no existe.");
        }
    }

    /**
     * Este método inserta una línea de texto en la posicion indicada como
     * párametro. De 1 a n.
     *
     * @param posicion Posición en el archivo en el que se incluirá la línea.
     * @param texto Texto que se escribirá en el archivo.
     * @return true si la escritura se realiza correctamente, false en caso
     * contrario.
     */
    public boolean insertarLineaEnPosicion(int posicion, String texto) {
        ArrayList<String> lineas = new ArrayList<>(Arrays.asList(leerLineasTexto()));

        // Si la posicion a insertar es correcta...
        if (posicion > 0 && posicion <= numLineas()) {
            lineas.add(posicion - 1, texto);
        }

        return reescribirArchivo(lineas.toArray(new String[lineas.size()]));
    }

    /**
     * Devuelve un String con la línea del fichero especificada como parámetro.
     *
     * @param numLinea Número de línea que se quiere obtener. De 1 a n.
     * @return
     */
    public String leerLineaNum(int numLinea) {
        String linea = "";

        // Si es numero de linea es correcto...
        if (numLinea > 0 && numLinea <= numLineas()) {
            String[] lineasTexto = leerLineasTexto();
            linea = lineasTexto[numLinea - 1];

        } else {
            System.out.println("La línea seleccionada debe ser entre 1 y "
                    + numLineas() + ", estos incluidos.");
        }

        return linea;
    }

    /**
     * El método devuelve un array de String cuyas posiciones se corresponden
     * con cada línea del texto.
     *
     * @return String[] Array con las líneas de texto del fichero.
     */
    public String[] leerLineasTexto() {
        return leerTexto().split("\n");
    }

    /**
     * Este método lee el archivo de principio a fin y devuelve un String con el
     * texto del mismo separando las lineas con un salto de linea.
     *
     * @return String con el texto del archivo.
     */
    public String leerTexto() {
        StringBuilder texto = new StringBuilder("");
        String linea;

        // Si el archivo existe se leerá.
        if (existe()) {
            try (BufferedReader in = new BufferedReader(new FileReader(archivo))) {
                linea = in.readLine();

                while (linea != null) {
                    texto.append(linea);
                    /* Se añade el salto de línea para respetar la construcción 
                     original del texto.*/
                    texto.append("\n");
                    linea = in.readLine();
                }

            } catch (EOFException e) {
                // No es necesario realizar ninguna acción aqui.
            } catch (IOException e) {
                System.out.println("Error al leer el archivo.");
            }
        } else {
            System.out.println("Error. El archivo no existe.");
        }

        return texto.toString();
    }

    /**
     * Devuelve el número de líneas que conforman el archivo.
     *
     * @return número de líneas del archivo.
     */
    public int numLineas() {
        return leerLineasTexto().length;
    }

    /**
     * Este método ordena alfabeticamente las líneas del archivo y reescribe el
     * mismo.
     *
     * @return true si todo se completó correctamente y false si ha ocurrido
     * algun error.
     */
    public boolean ordenar() {
        boolean result = true;
        try {
            String[] lineas = leerLineasTexto();

            Arrays.sort(lineas, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    Integer igualdad = null;

                    // Usar una expresión regular para encontrar números en las cadenas.
                    Pattern pattern = Pattern.compile("\\d+");
                    Matcher matcher1;
                    Matcher matcher2;

                    // Separando las lineas a comparar en palabras.
                    String[] palabras1 = o1.split("\\s+");
                    String[] palabras2 = o2.split("\\s+");
                    int minLength = Math.min(palabras1.length, palabras2.length);

                    // Comprobando palabras de ambas frases hasta encontrar un par diferente.
                    for (int i = 0; i < minLength && igualdad == null; i++) {
                        matcher1 = pattern.matcher(palabras1[i]);
                        matcher2 = pattern.matcher(palabras2[i]);

                        // Si las palabras son diferentes...
                        if (!palabras1[i].equalsIgnoreCase(palabras2[i])) {
                            // Si ambas son números...
                            if (matcher1.find() && matcher2.find()) {
                                int num1 = Integer.valueOf(palabras1[i]);
                                int num2 = Integer.valueOf(palabras2[i]);

                                // Se comparan los números.
                                igualdad = Integer.compare(num1, num2);

                            } else {
                                // Si NO son 2 números se comparan normalmente.
                                igualdad = palabras1[i].compareToIgnoreCase(palabras2[i]);
                            }

                            // Si NO hay diferencias hasta el final de la línea más corta...
                        } else if (i == minLength - 1) {
                            if (palabras1.length < palabras2.length) {
                                // Si la primera linea es la mas corta...
                                igualdad = -1;

                            } else if (palabras1.length > palabras2.length) {
                                // Si la segunda linea es la mas corta...
                                igualdad = 1;

                            } else { // Si las 2 líneas son identicas...
                                igualdad = 0;
                            }
                        }
                    }

                    return igualdad;
                }
            });

            // Reescribiendo el archivo con las lineas ordenadas.
            reescribirArchivo(lineas);

        } catch (Exception e) {
            // Si algo va mal durante el proceso se devuelve un false.
            result = false;
        }

        return result;
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
     * Reemplaza la linea indicacda como parámetro, por otra nueva.
     *
     * @param numLinea Número de linea que se desea reemplazar. De 1 a n.
     * @param nuevaLinea Nuevo texto que se escribirá en el archivo.
     * @return true si la operación a salido correctamente, false en caso
     * contrario.
     */
    public boolean reemplazarLinea(int numLinea, String nuevaLinea) {
        boolean resultOK;

        // Si la linea que se va a reemplazar está entre el número de lineas existente...
        if (numLinea > 0 && numLinea <= numLineas()) {
            String[] lineas = leerLineasTexto();
            lineas[numLinea - 1] = nuevaLinea;
            resultOK = reescribirArchivo(lineas);

        } else {
            resultOK = false;
        }

        return resultOK;
    }

    /**
     * Este método elimina el archivo y crea otro con el mismo nombre en la
     * misma ruta. El nuevo archivo contendrá el nuevo texto pasado como
     * párametro.
     *
     * @param lineas Array de String con el texto por el que se sustituirá el
     * original. Cada posición del Array se corresponde con cada línea del
     * archivo.
     * @return true si la reescritura a salido correctamente, false en caso
     * contrario.
     */
    public boolean reescribirArchivo(String[] lineas) {
        borrar();
        return escribirVariasLineas(lineas);
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
            String[] lineas = leerLineasTexto();
            // Esta linea comprueba el tipo de ruta.
            String tipoBarra = (ruta.contains("/")) ? "/" : "\\";
            // Asegurando que el nuevo nombre incluye la extensión.
            nuevoNombre = (nuevoNombre.endsWith(".txt"))
                    ? nuevoNombre : nuevoNombre + ".txt";
            // Construyendo la ruta al archivo con el nuevo nombre.
            String nuevaRuta = ruta.substring(0, ruta.lastIndexOf(tipoBarra) + 1)
                    + nuevoNombre;

            borrar();
            ruta = nuevaRuta;
            archivo = new File(nuevaRuta);
            renombreOk = escribirVariasLineas(lineas);

        } else {
            renombreOk = false;
        }

        return renombreOk;
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
     * En este método se especifica si el texto que se escriba en el archivo se
     * añadirá al ya existente o lo sobreescribirá.
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
