package Utils;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author Roberto Santos Cordeiro
 */
public class RandomFilesUtils {

    /**
     * Se usa para verificar el nombre del archivo. Este no puede contener
     * caracteres prohibidos ni estar compuesto por espacios en blanco.
     */
    private final String SIMBOLOS_PROHIBIDOS_REGEX = "^(?!\\s*$)[^\\\\/:?\"<>|]+$";
    private final String SIMBOLOS_PROHIBIDOS_RUTA_REGEX = "^(?!\\s*$)[^:?\"<>|]+$";

    /**
     * Tamaño máximo en Bytes que puede ocupar un registro.
     */
    final int TAMANHO_REGISTROS;
    final int TAMANHO_REGISTROS_DEFAULT = 128;

    private String ruta;
    private File archivo;

    // CONSTRUCTORES ===========================================================
    public RandomFilesUtils(int tamanhoRegistros, String ruta) throws IOException {
        // Comprobando que la ruta es válida.
        if (ruta.matches(SIMBOLOS_PROHIBIDOS_RUTA_REGEX)) {
            throw new IllegalArgumentException("La ruta no es válida.");
        }

        this.ruta = ruta;
        this.archivo = new File(ruta);

        // Si el tamaño de los registros es correcto y no existe el archivo...
        if (tamanhoRegistros > 0 && !existe()) {
            // se establece el tamaño de los registros y se guarda en el archivo.
            this.TAMANHO_REGISTROS = tamanhoRegistros;
            guardarTamanhoRegistros(tamanhoRegistros);

        } else if (tamanhoRegistros <= 0 && !existe()) {
            // Si el tamaño de los registros es menor que 0 y NO existe el archivo...
            System.out.println("El parámetro \"tamanhoRegistros\" debe ser "
                    + "mayor que 0. Se le ha asignado por defecto el valor 128.");
            // se establece el tamaño por defecto y se guarda en el archivo.
            this.TAMANHO_REGISTROS = TAMANHO_REGISTROS_DEFAULT;
            guardarTamanhoRegistros(TAMANHO_REGISTROS_DEFAULT);

        } else {
            // Si el archivo ya existe se lee de él el tamaño de los registros.
            this.TAMANHO_REGISTROS = leerTamanhoRegistros();

        }

    }

    public RandomFilesUtils(File archivo) throws IOException {
        this.ruta = archivo.getAbsolutePath();

        // Comprobando que la ruta es válida.
        if (ruta.matches(SIMBOLOS_PROHIBIDOS_RUTA_REGEX)) {
            throw new IllegalArgumentException("La ruta no es válida.");
        }

        this.archivo = archivo;
        // Si el archivo existe y contiene datos...
        if (existe() && peso() > 0) {
            // se lee el tamaño de los registros de este.
            this.TAMANHO_REGISTROS = leerTamanhoRegistros();
        } else {
            // Si no existe o está vacío se establece el tamaño por defecto.
            this.TAMANHO_REGISTROS = TAMANHO_REGISTROS_DEFAULT;
            guardarTamanhoRegistros(TAMANHO_REGISTROS_DEFAULT);
        }
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
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo de acceso aleatorio.
     *
     * @param file
     * @return Devuelve un objeto del tipo RandomAccessFile que apunta al nuevo
     * archivo de copia.
     * @throws IOException
     */
    public RandomFilesUtils copiar(File file) throws IOException {
        Object[] datos = leerTodo();
        RandomFilesUtils copia = new RandomFilesUtils(getTAMANHO_REGISTROS(), file.getAbsolutePath());
        copia.escribirTodo(datos);

        return copia;
    }

    /**
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo de acceso aleatorio.
     *
     * @param path ruta de la copia.
     * @return Devuelve un objeto del tipo RandomAccessFile que apunta al nuevo
     * archivo de copia.
     * @throws IOException
     */
    public RandomFilesUtils copiar(String path) throws IOException {
        File file = new File(path);
        // Si el archivo ya existe se creará un archivo de copia nuevo.
        if (file.exists()) {
            file = new File(genPathCopy(path));
        }

        return copiar(file);
    }

    /**
     * El método realiza una copia de todos los datos del archivo y los escribe
     * en un nuevo archivo de acceso aleatorio.
     *
     * @return Devuelve un objeto del tipo RandomAccessFile que apunta al nuevo
     * archivo de copia.
     * @throws IOException
     */
    public RandomFilesUtils copiar() throws IOException {
        return copiar(genPathCopy(ruta));
    }

    /**
     * Este método elimina el dato que ocupa la posicion pasada como parámetro.
     *
     * @param posicion Posición que ocupa el dato que se eliminará. Este debe
     * ser un número entero entre 1 y n.
     * @return true si la eliminación se completó correctamente, false en caso
     * contrario.
     * @throws IOException
     */
    public boolean eliminarEnPosicion(int posicion) throws IOException {
        boolean borradoOk = false;

        if (posicion > 0 && hayRegistro(posicion)) {
            try (RandomAccessFile out = new RandomAccessFile(ruta, "rw")) {
                out.seek(posicion * TAMANHO_REGISTROS);
                out.write(0);
            }
        }

        return borradoOk;
    }

    /**
     * Este método reescribe el archivo sin posiciones vacias.
     *
     * @throws IOException
     */
    public void eliminarPosicionesVacias() throws IOException {
        Object[] datos = leerTodo();
        reescribirArchivo(datos);
    }

    /**
     * Elimina la primera ocurrencia del dato pasado como parámetro.
     * 
     * @param obj Dato que se buscará para eliminar.
     * @return true si el eliminado ha salido correctamente, false en caso
     * contrario.
     * @throws IOException 
     */
    public boolean eliminarPrimero(Object obj) throws IOException {
        int i = 0;
        boolean centinela = false;
        boolean borradoOk = false;

        do {
            i++;
            if (hayRegistro(i)) {
                // Si el objeto en turno es igual al buscado...
                if (leerEnPosicion(i).equals(obj)) {
                    // se para el bucle.
                    centinela = true;
                    borradoOk = eliminarEnPosicion(i);
                }
            }
            
        } while (!centinela && i <= numRegistros());
        // Mientras no encuentre el dato y no llegue al final del documento...
        
        return borradoOk;
    }

    /**
     * Este método elimina todos los datos guardados en el archivo, pero no el
     * fichero.
     *
     * @throws IOException
     */
    public void eliminarTodo() throws IOException {

        for (int i = 1; i <= numRegistros(); i++) {
            eliminarEnPosicion(i);
        }
    }
    
    /**
     * Elimina todas las ocurrencias del dato pasado como parámetro.
     * 
     * @param obj Dato que se buscará para eliminar.
     * @throws IOException 
     */
    public void eliminarTodo(Object obj) throws IOException {
        for (int i = 1; i <= numRegistros(); i++) {
            if (hayRegistro(i)) {
                // Si el objeto en turno es igual al buscado...
                if (leerEnPosicion(i).equals(obj)) {
                    eliminarEnPosicion(i);
                }
            }
        }
        
    }

    /**
     * Escribe al final del archivo el dato que se le pasa como parámetro.
     *
     * @param dato
     * @return true => La escritura se realizó correctamente.<br>
     * false => Ha ocurrido un error. Puede que la escritura no se realizara.
     * @throws java.io.IOException
     */
    public boolean escribirAlFinal(Object dato) throws IOException {
        // sumando +1, el cursor se posicionará depués del último registro no antes que él.
        return escribir(dato, numRegistros() + 1);
    }

    /**
     * Este método escribirá el objeto pasado como parámetro en el fichero.
     *
     * @param dato objeto que se escribirá.
     * @param out escritor que se usará para guardar el dato.
     * @return true si la escritura se realizó correctamente, false en caso
     * contrario.
     */
    private boolean escribirDato(Object dato, RandomAccessFile out) {
        boolean escrituraOk = true;

        try {
            switch (dato.getClass().getSimpleName()) {
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
                    writeObject(dato, out);
            }

        } catch (IOException ex) {
            System.out.println("Error al escribir el archivo.");
            escrituraOk = false;

        } catch (Exception e) {
            printException(e);
            escrituraOk = false;
        }

        return escrituraOk;
    }

    /**
     * Escribe el dato en la posición pasada como párametro.
     *
     * @param dato Objeto que se escribirá en el archivo.
     * @param posicion Posición en que se escribirá el dato.
     * @return
     */
    public boolean escribir(Object dato, int posicion) {
        boolean escrituraOk = false;
        if (posicion > 0) {

            try {
                if (hayRegistro(posicion)) {
                    throw new PosicionOcupadaException("La posición " + posicion
                            + " ya está ocupada.");
                }

                try (RandomAccessFile out = new RandomAccessFile(ruta, "rw")) {

                    // Posiciona el cursor en el último registro.
                    out.seek(posicion * TAMANHO_REGISTROS);

                    // Si el tamaño del dato es inferior a TAMANHO_REGISTROS se escribe.
                    if (esTamanhoCorrecto(dato)) {
                        escribirDato(dato, out);
                        escrituraOk = true;
                    }
                }

            } catch (IOException ex) {
                System.out.println("Error al escribir el archivo.");

            } catch (PosicionOcupadaException ex) {
                System.out.println(ex.getMessage());
            }
        } else {
            System.out.println("Error! La posición debe ser mayor que cero");
        }

        return escrituraOk;
    }

    /**
     * Este método escribe el dato pasado como parámetro en la primera posición
     * vacía que haya en el documento.
     *
     * @param dato Dato que se escribirá
     * @return true si la escritura se realizó correctamente, false en caso
     * contrario.
     * @throws IOException
     */
    public boolean escribir(Object dato) throws IOException {
        int posicion = 1;
        boolean centinela = false;

        // Mientras no se encuentre la primera posición vacía o se llegue al final del archivo...
        while (!centinela && posicion <= numRegistros()) {
            if (!hayRegistro(posicion)) {
                centinela = true; // Parando el bucle
            } else {
                posicion++;
            }
        }

        return escribir(dato, posicion);
    }

    /**
     * Este método escribe en el archivo binario una serie de datos pasados como
     * parámetro en un array de Object.
     *
     * @param datos array de objetos que se escribiran en el archivo.
     * @return true si la escritura de todos los datos se ha realizado
     * correctamente, false en caso contrario.
     * @throws java.io.IOException
     */
    public boolean escribirTodo(Object[] datos) throws IOException {
        boolean escrituraOk = true;

        for (Object dato : datos) {
            /* El if hará la escritura del objeto y a la vez comprobará que ha
            salido bien. De no ser así se frenará el bucle y se devolverá un false.*/
            if (!escribirAlFinal(dato)) {
                escrituraOk = false;
                break;
            }
        }

        return escrituraOk;
    }

    /**
     * Este método comprueba que el tamaño del dato que se quiere escribir es
     * adecuado acorde al tamaño máximo aceptable.
     *
     * @param dato Objeto para calcular su tamaño.
     * @return true si el tamaño del dato a escribir es adecuado para este
     * archivo. False en caso contrario.
     */
    public boolean esTamanhoCorrecto(Object dato) {
        boolean tamanhoOK;

        switch (dato.getClass().getSimpleName()) {
            case "Boolean":
                tamanhoOK = Byte.BYTES < TAMANHO_REGISTROS;
                break;
            case "Character":
                tamanhoOK = Character.BYTES < TAMANHO_REGISTROS;
                break;
            case "Double":
                tamanhoOK = Double.BYTES < TAMANHO_REGISTROS;
                break;
            case "Float":
                tamanhoOK = Float.BYTES < TAMANHO_REGISTROS;
                break;
            case "Integer":
                tamanhoOK = Integer.BYTES < TAMANHO_REGISTROS;
                break;
            case "Long":
                tamanhoOK = Long.BYTES < TAMANHO_REGISTROS;
                break;
            case "Short":
                tamanhoOK = Short.BYTES < TAMANHO_REGISTROS;
                break;
            case "String":
                tamanhoOK = String.valueOf(dato).length() + 2 < TAMANHO_REGISTROS;
                break;
            default:
                /* 
                Si el tipo de dato no se corresponde con ninguno de los anteriores
                se calcula el tamaño convirtiendo el dato en un array de bytes
                y obteniendo el tamaño de este.
                NOTA BENE: Este método puede no ser del todo preciso.
                 */
                try (ByteArrayOutputStream bs = new ByteArrayOutputStream();
                        ObjectOutputStream os = new ObjectOutputStream(bs)) {

                    os.writeObject(dato);
                    os.flush();

                    tamanhoOK = bs.toByteArray().length < TAMANHO_REGISTROS;

                } catch (IOException ex) {
                    tamanhoOK = false;
                }

        }

        return tamanhoOK;
    }

    /**
     * Comprueba si el archivo indicado en la constante RUTA existe.
     *
     * @return true si el archivo existe; false en caso contrario.
     */
    public boolean existe() {
        return new File(ruta).exists();
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
     * Este método se usa para guardar el tamaño de los registros en la posición
     * 0 del documento, de forma que cuando el documento ya exista, se pueda
     * recuperar esta información para operar con el archivo correctamente.
     *
     * @param tamanhoRegistros tamaño máximo que ocuparan los registros.
     * @throws IOException
     */
    private void guardarTamanhoRegistros(int tamanhoRegistros) throws IOException {
        try (RandomAccessFile out = new RandomAccessFile(ruta, "rw")) {
            out.seek(0);
            out.writeInt(tamanhoRegistros);
        }
    }

    /**
     * Este método comprueba si en la posición pasada como parámetro hay un dato
     * o no.
     *
     * @param posicion posición en la que se hará la comprobación,
     * @return true si esa posición esta ocupada con algún dato, false si esta
     * vacia.
     * @throws java.io.IOException
     */
    public boolean hayRegistro(int posicion) throws IOException {
        int etiqueta = 0;

        // Si el documento existe y la posición es correcta...
        if (existe() && posicion > 0) {
            try (RandomAccessFile out = new RandomAccessFile(ruta, "r")) {
                out.seek(posicion * TAMANHO_REGISTROS);
                etiqueta = out.readByte();
            } catch (EOFException e) {
                // No es necesario realizar ninguna acción aqui.
            }

        }

        return etiqueta != 0;
    }

    /**
     * Este método lee el dato que está en la posición indicada como parámetro.
     * Si la posición es incorrecta o está vacía se devolverá un null.
     *
     * @param posicion Posicion en la que se leerá el dato.
     * @return Object con el dato escrito en el archivo o NULL sí no hay nada en
     * dicha posición o si el parametro "posicion" es incorrecto.
     * @throws IOException
     */
    public Object leerEnPosicion(int posicion) throws IOException {
        Object obj = null;

        // Si la posición es correcta y hay datos en ella...
        if (posicion > 0 && hayRegistro(posicion)) {
            try (RandomAccessFile in = new RandomAccessFile(ruta, "r")) {
                // se posiciona el cursor y se lee el dato.
                in.seek(posicion * TAMANHO_REGISTROS);
                obj = leerDato(in.readByte(), in);
            }
        }

        return obj;
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
    private Object leerDato(byte etiqueta, RandomAccessFile in) {
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
                    obj = readObject(in.getFilePointer());
            }

        } catch (EOFException e) {
            // No es necesario realizar ninguna acción aqui.
        } catch (IOException ex) {
            System.out.println("Error al leer el archivo.");

        } catch (Exception e) {
            printException(e);
        }

        return obj;
    }

    /**
     * Este método se usa para leer el tamaño de los registros de un archivo que
     * ya existía al momento de crear un objeto de esta clase. De esta forma se
     * podrá operar con los datos de dicho archivo de forma satisfactoria.
     *
     * @return tamaño maximo de los registros del archivo.
     * @throws IOException
     */
    private int leerTamanhoRegistros() throws IOException {
        int tamanhoRegistros;

        try (RandomAccessFile in = new RandomAccessFile(ruta, "r")) {
            in.seek(0);
            int tamanho = in.readInt();
            // Si por algún error el tamaño es cero o negativo se usará el tamaño por defecto.
            tamanhoRegistros = tamanho > 0 ? tamanho : TAMANHO_REGISTROS_DEFAULT;
        }

        return tamanhoRegistros;
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
            try (RandomAccessFile in = new RandomAccessFile(ruta, "r")) {
                int i = TAMANHO_REGISTROS;

                // Lee mientras no llegue al final del documento.
                while (i < peso()) {
                    if (hayRegistro((i / TAMANHO_REGISTROS))) {
                        in.seek(i);
                        /* Lee el byte etiqueta, lee el siguiente dato del archivo 
                    y lo añade al array */
                        dataList.add(leerDato(in.readByte(), in));
                    }

                    i += TAMANHO_REGISTROS;
                }

            } catch (IOException ex) {
                System.out.println("Error al leer el archivo. " + ex.getMessage());
            } catch (Exception e) {
                printException(e);
            }
        }

        return dataList.toArray(new Object[0]);
    }

    /**
     * Este método cuenta y devuelve el número de registros escritos en el
     * archivo. TAMBIÉN se cuentan los registros que están vacíos. NO se tiene
     * en cuenta el int con el tamaño de los registros escrito al principio del
     * archivo.
     *
     * @return número de registros en el archivo.
     * @throws IOException
     */
    public int numRegistros() throws IOException {
        return (int) (peso() / TAMANHO_REGISTROS);
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
     * Este método lista todas las posiciones vacías del documento.
     *
     * @return Array de Integer con las posiciones vacias del documento.
     * @throws IOException
     */
    public Integer[] posicionesVacias() throws IOException {
        ArrayList<Integer> posiciones = new ArrayList<>();

        for (int i = 1; i <= numRegistros(); i++) {
            if (!hayRegistro(i)) {
                posiciones.add(i);
            }
        }
        return posiciones.toArray(new Integer[0]);
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
     * Este método se usa para leer un dato de tipo Object escrito en el
     * documento.
     *
     * @param pointer posición en el documento donde se encuentra el objeto a
     * leer.
     * @return dato de tipo Object leido.
     */
    private Object readObject(long pointer) {
        Object obj = null;
        try {
            /* Se usa un RandomAccessFile diferente para que el try catch con recursos
            no cierre el flujo original después de leer el objeto*/
            // IMPORTANTE! NO se puede usar el try catch con recursos.
            RandomAccessFile ran = new RandomAccessFile(ruta, "r");

            // Posicionando el putero en el mismo lugar del RandomAccessFile original.
            ran.seek(pointer);

            ObjectInputStream in = new ObjectInputStream(new FileInputStream(ran.getFD()));

            obj = in.readObject();

            // cerrando los flujos de lectura abiertos en este método.
            in.close();
            ran.close();

        } catch (EOFException e) {
            // No es necesario realizar ninguna acción aqui.
        } catch (IOException ex) {
            System.out.println("Error al leer el archivo. " + ex.getMessage());
        } catch (ClassNotFoundException e) {
            printException(e);
        }

        return obj;
    }

    /**
     * Este método sustituye los datos guardados en una posición pasada como
     * parámetro por un nuevo dato.
     *
     * @param posicion Posición en la que se encuentra el dato a sustituir.
     * @param dato Nuevo dato que se escribirá en la posición indicada.
     * @return true si la operación se completó correctamente, false en caso
     * contrario.
     * @throws IOException
     */
    public boolean reemplazarEnPosicion(int posicion, Object dato) throws IOException {
        boolean reescrituraOk = false;

        if (posicion > 0) {
            eliminarEnPosicion(posicion);
            reescrituraOk = escribir(dato, posicion);
        }

        return reescrituraOk;
    }

    /**
     * Reemplaza la primera ocurrencia del dato especificado con
     * un dato nuevo pasado como parámetro.
     * 
     * @param original dato que se buscará para reemplazar.
     * @param nuevo nuevo dato por el que reemplazará el antiguo.
     * @return true si se consigue reemplazar correctamente, false en caso
     * contrario.
     * @throws IOException 
     */
    public boolean reemplazarPrimero(Object original, Object nuevo) throws IOException {
        int i = 0;
        boolean centinela = false;
        boolean borradoOk = false;

        do {
            i++;
            if (hayRegistro(i)) {
                // Si el objeto en turno es igual al buscado...
                if (leerEnPosicion(i).equals(original)) {
                    // se para el bucle.
                    centinela = true;
                    borradoOk = reemplazarEnPosicion(i, nuevo);
                }
            }
            
        } while (!centinela && i <= numRegistros());
        // Mientras no encuentre el dato y no llegue al final del documento...
        
        return borradoOk;
    }
    
    /**
     * Reemplaza todas las ocurrencias que encuentre del dato especificado con
     * un dato nuevo pasado como parámetro.
     * 
     * @param original dato que se buscará para reemplazar.
     * @param nuevo nuevo dato por el que reemplazará el antiguo.
     * @throws IOException 
     */
    public void reemplazarTodo(Object original, Object nuevo) throws IOException{
        
        for (int i = 0; i <= numRegistros(); i++) {
            if (hayRegistro(i)) {
                // Si el objeto en turno es igual al buscado...
                if (leerEnPosicion(i).equals(original)) {
                    reemplazarEnPosicion(i, nuevo);
                }
            }
        }
        
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
     * @throws java.io.IOException
     */
    public boolean reescribirArchivo(Object[] objetos) throws IOException {
        borrar();
        return escribirTodo(objetos);
    }

    /**
     * Este método cambia el nombre del archivo por uno nuevo pasado como
     * parámetro.
     *
     * @param nuevoNombre El nombre nuevo que se le dará al archivo.
     * @return true si se pudo cambiar el nombre, false si no se pudo.
     * @throws java.io.IOException
     */
    public boolean renombrar(String nuevoNombre) throws IOException {
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

    /**
     * Este método se usa para escribir un dato de tipo Object en el archivo.
     *
     * @param obj objeto a escribir.
     * @param ran escritor de acceso aleatorio que se usará para registrar el
     * dato en el fichero.
     * @return true si la escritura se realizó correctamente, false en caso
     * contrario.
     */
    private boolean writeObject(Object obj, RandomAccessFile ran) {
        boolean escrituraOk = true;

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(ran.getFD()))) {

            out.writeObject(obj);

        } catch (IOException e) {
            printException(e);
            escrituraOk = false;
        }

        return escrituraOk;
    }

    // GETTERS =================================================================
    public String getNombre() {
        // Esta linea comprueba el tipo de ruta.
        String tipoBarra = (ruta.contains("/")) ? "/" : "\\";
        return ruta.substring(ruta.lastIndexOf(tipoBarra) + 1);
    }

    public String getRuta() {
        return ruta;
    }

    public int getTAMANHO_REGISTROS() {
        return TAMANHO_REGISTROS;
    }

    // EXCEPCION PERSONALIZADA =================================================
    public static class PosicionOcupadaException extends Exception {

        public PosicionOcupadaException(String mensaje) {
            super(mensaje);
        }
    }

}
