# Clases de Utilidades para Java
Este repositorio contiene clases de utilidades para Java:

1. BinaryFilesUtils: clase de utilidad para trabajar con ficheros binarios. Proporcona metodos para escribir, leer y manipular datos almacenados en un fichero binario.

2. DOMUtils: clase de utilidad para trabajar con documentos XML utilizando la biblioteca DOM. Proporciona métodos para crear documentos XML, agregar y eliminar elementos XML, y leer y escribir datos en nodos XML.

3. JAXBUtils: clase de utilidad para serializar y deserializar objetos Java en XML utilizando la biblioteca JAXB. Proporciona métodos para crear objetos Java a partir de archivos XML y para generar archivos XML a partir de objetos Java.

4. RandomFilesUtils: clase que proporciona métodos para trabajar con archivos de acceso aleatorio, permitiendo la escritura, lectura y eliminación de datos en posiciones específicas del archivo, entre otras cosas.

5. SQLServerUtils: clase de utilidad para interactuar con bases de datos SQL Server. Proporciona métodos para establecer conexiones de base de datos y ejecutar consultas SQL.

6.  TextFilesUtils: clase de utilidad para trabajar con ficheros de texto. Proporciona métodos para escribir, leer, ordenar... ficheros de texto.

## BinaryFilesUtils
La clase BinaryFilesUtils proporciona métodos para leer y escribir datos en un archivo binario. Permite la escritura y lectura de una gran variedad de tipos de datos, incluidos datos primitivos y objetos personalizados serializables. Algunos metodos más relevantes son:

+ ***escribirVariosDatos***: permite pasar un array de Obgect con cualquier tipo de dato dentro y este los escribirá en el archivo de acuerdo al tipo de dato que eran originalmente.
+  ***leerDatoEnPosicion***: leerá y devolverá el dato escrito en la posición N del archivo. La posicion del dato en el archivo se pasa como parámetro.
+  ***reemplazarTodo***: reemplazará todas las ocurrencias de un dato pasadocomo parametro por otro y reescribirá el archivo con los nuevos valores.

## DOMUtils
La clase DOMUtils proporciona una manera fácil de trabajar con documentos XML utilizando la biblioteca DOM. La clase utiliza la interfaz DOM para crear y manipular nodos XML. Algunos de los métodos más importantes de la clase son:

+ ***createDocument***: este método crea un nuevo documento XML con el nodo raíz especificado.
+ ***addElement***: este método agrega un nuevo elemento XML al documento especificado.
+ ***borrarNodo***: este método elimina un elemento XML del documento especificado.
+ ***transformarEnXML***: este método escribe un documento XML en un archivo especificado.

## JAXBUtils
La clase JAXBUtils proporciona una forma fácil de serializar y deserializar objetos Java en XML utilizando la biblioteca JAXB. La clase utiliza anotaciones JAXB para indicar cómo se deben serializar los objetos Java en XML y viceversa. Algunos de los métodos más importantes de la clase son:

+ ***marshal***: este método toma un objeto Java y lo serializa en un archivo XML.
+ ***unmarshal***: este método toma un archivo XML y lo deserializa en un objeto Java.

## RandomFilesUtils
La clase RandomFilesUtils proporciona una serie de métodos para manipular archivos de acceso aleatorio. Esta clase puede realizar operaciones como escribir, leer, eliminar, copiar y reemplazar datos en un archivo del tipo mencionado. La clase también incluye otros métodos para operaciones como copiar el archivo, eliminar todos los datos del documento, obtener el número de registros escritos en el mismo, entre otros. Algunos de los métodos principales de esta clase son:

+ ***escribir***: Este método permite escribir un objeto al final del archivo o en una posición específica del misno entre 1 y n.
+ ***leerEnPosicion***: Este método lee un objeto desde el archivo en la posición especificada.
+ ***eliminarEnPosicion***: Este método elimina el objeto en la posición especificada.
+ ***reemplazarEnPosicion***: Este método reemplaza el objeto en la posición especificada con el nuevo objeto proporcionado.

## SQLServerUtils
La clase SQLServerUtils proporciona una manera fácil de interactuar con bases de datos SQL Server en Java. La clase utiliza el controlador JDBC de Microsoft para establecer una conexión de base de datos y ejecutar consultas SQL. Algunos de los métodos más importantes de la clase son:

+ ***getConnection***: este método establece una conexión con una base de datos SQL Server y devuelve un objeto Connection.
+ ***executeQuery***: este método ejecuta una consulta SQL y devuelve un objeto ResultSet que contiene los resultados de la consulta.
+ ***executeUpdate***: este método ejecuta una consulta SQL que modifica la base de datos, como una consulta de inserción, actualización o eliminación.

## TextFilesUtils
La clase TextFilesUtils proporciona métodos para trabajar con archivos de texto. Incluye funciones para borrar, escribir, comprobar la existencia, imprimir, leer y manipular líneas en archivos de texto. Ofrece capacidades tales como: 

+ ***escribirVariasLinea***: este método escribe múltiples líneas pasadas como parametro en un array de String.
+ ***buscarLineasPorTexto***: este método encuentra líneas de texto que contengan la clave de búsqueda.
+ ***ordenar***: ordena alfabéticamente las líneas de texto siguiendo las normas gramaticales.

Estas clases pueden ser de gran ayuda para los desarrolladores de Java que necesitan interactuar con bases de datos SQL Server, serializar y deserializar objetos Java en XML, y trabajar con documentos XML utilizando la biblioteca DOM.
