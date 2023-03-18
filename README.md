# Clases de Utilidades para Java
Este repositorio contiene tres clases de utilidades para Java:

1. SQLServerUtils: clase de utilidad para interactuar con bases de datos SQL Server. Proporciona métodos para establecer conexiones de base de datos y ejecutar consultas SQL.

2. JAXBUtils: clase de utilidad para serializar y deserializar objetos Java en XML utilizando la biblioteca JAXB. Proporciona métodos para crear objetos Java a partir de archivos XML y para generar archivos XML a partir de objetos Java.

3. DOMUtils: clase de utilidad para trabajar con documentos XML utilizando la biblioteca DOM. Proporciona métodos para crear documentos XML, agregar y eliminar elementos XML, y leer y escribir datos en nodos XML.

## SQLServerUtils
La clase SQLServerUtils proporciona una manera fácil de interactuar con bases de datos SQL Server en Java. La clase utiliza el controlador JDBC de Microsoft para establecer una conexión de base de datos y ejecutar consultas SQL. Algunos de los métodos más importantes de la clase son:

+ ***getConnection***: este método establece una conexión con una base de datos SQL Server y devuelve un objeto Connection.
+ ***executeQuery***: este método ejecuta una consulta SQL y devuelve un objeto ResultSet que contiene los resultados de la consulta.
+ ***executeUpdate***: este método ejecuta una consulta SQL que modifica la base de datos, como una consulta de inserción, actualización o eliminación.

## JAXBUtils
La clase JAXBUtils proporciona una forma fácil de serializar y deserializar objetos Java en XML utilizando la biblioteca JAXB. La clase utiliza anotaciones JAXB para indicar cómo se deben serializar los objetos Java en XML y viceversa. Algunos de los métodos más importantes de la clase son:

+ ***marshal***: este método toma un objeto Java y lo serializa en un archivo XML.
+ ***unmarshal***: este método toma un archivo XML y lo deserializa en un objeto Java.

## DOMUtils
La clase DOMUtils proporciona una manera fácil de trabajar con documentos XML utilizando la biblioteca DOM. La clase utiliza la interfaz DOM para crear y manipular nodos XML. Algunos de los métodos más importantes de la clase son:

+ ***createDocument***: este método crea un nuevo documento XML con el nodo raíz especificado.
+ ***addElement***: este método agrega un nuevo elemento XML al documento especificado.
+ ***borrarNodo***: este método elimina un elemento XML del documento especificado.
+ ***transformarEnXML***: este método escribe un documento XML en un archivo especificado.

Estas tres clases pueden ser de gran ayuda para los desarrolladores de Java que necesitan interactuar con bases de datos SQL Server, serializar y deserializar objetos Java en XML, y trabajar con documentos XML utilizando la biblioteca DOM.
