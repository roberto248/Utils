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
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Roberto Santos Cordeiro
 */
public class TextFilesUtils {

    private final String RUTA;
    private final File ARCHIVO;

    private boolean adjuntar;

    // CONSTRUCTOR
    public TextFilesUtils(String ruta) {
        this.RUTA = ruta;
        this.ARCHIVO = new File(ruta);
        this.adjuntar = true;
    }

    // MÉTODOS =================================================================
    
    /**
     * Elimina el archivo indicado en el constructor.
     *
     * @return true si el archivo ha sido borrado; false en caso contrario.
     */
    public boolean borrar() {
        return ARCHIVO.delete();
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
     * Escribe en el archivo una nueva línea con el texto pasado como parámetro.
     * Si el archivo no existe se creará.
     *
     * @param texto Texto que se escribirá en el archivo.
     * @return true => La escritura se realizó correctamente.<br>
     * false => Ha ocurrido un error. Puede que la escritura no se realizara.
     */
    public boolean escribirLinea(String texto) {
        boolean escrituraOk = true;

        try (BufferedWriter out = new BufferedWriter(new FileWriter(ARCHIVO, adjuntar))) {
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
    public boolean escribirVariasLinea(String[] lineas) {
        boolean escrituraOk = true;

        try (BufferedWriter out = new BufferedWriter(new FileWriter(ARCHIVO, adjuntar))) {

            for (String linea : lineas) {
                out.write(linea);
                out.newLine(); // Salto de línea.
            }

        } catch (IOException e) {
            escrituraOk = false;
        }

        return escrituraOk;
    }

    /**
     * Comprueba si el archivo indicado en el constructor existe.
     *
     * @return true si el archivo existe; false en caso contrario.
     */
    public boolean existe() {
        return ARCHIVO.exists();
    }

    /**
     * Lee el archivo de principio a fin y lo muestra por pantalla.
     *
     */
    public void imprimirTexto() {
        if (existe()) {
            try (BufferedReader in = new BufferedReader(new FileReader(ARCHIVO))) {
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
     * @param posicion Posición en el archivo en el que se incluirá la línea.
     * @param texto Texto que se escribirá en el archivo.
     * @return true si la escritura se realiza correctamente, 
     * false en caso contrario.
     */
    public boolean insertarLineaEnPosicion(int posicion, String texto) {
        boolean resultOK = false;
        ArrayList<String> lineas;
        
        // Si la posicion a insertar es correcta...
        if (posicion > 0 && posicion <= numLineas()) {
            lineas = new ArrayList<>(Arrays.asList(leerLineasTexto()));

            lineas.add(posicion - 1, texto);
            
            resultOK = reescribirArchivo(lineas.toArray(new String[lineas.size()]));
        }

        return resultOK;
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
            try (BufferedReader in = new BufferedReader(new FileReader(ARCHIVO))) {
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
        return ARCHIVO.length();
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
     * @param lineas Array de String con el texto por el que se sustituirá. Cada
     * posición del Array se corresponde con cada línea del archivo.
     * @return true si la reescritura a salido correctamente, false en caso
     * contrario.
     */
    public boolean reescribirArchivo(String[] lineas) {
        borrar();
        return escribirVariasLinea(lineas);
    }

    // GETTERS =================================================================
    public String getRUTA() {
        return RUTA;
    }

    public File getARCHIVO() {
        return ARCHIVO;
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
