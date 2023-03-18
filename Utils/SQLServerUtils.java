package Utils;

import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 *
 * @author Roberto Santos Cordeiro
 */
public class SQLServerUtils {

    private final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private final String URL;
    private final String USER = "sa";
    private final String PASSWORD = "abc123.";

    private Connection conn = null;

    // CONSTRUCTORES
    public SQLServerUtils(String nombreBD) throws SQLException {
        
        this.URL = "jdbc:sqlserver://localhost;database=" + nombreBD 
                + ";TrustServerCertificate=True;user=" + USER 
                + ";password=" + PASSWORD + ";";

        try {
            Class.forName(DRIVER);
            this.conn = DriverManager.getConnection(URL);
            
        } catch (SQLException ex) {
            System.out.println("Error al conectarse a la BD: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("No se ha encontrado la clase del Driver, " + ex.getMessage());
        }

    }
    
    // MÉTODOS
    /**
     * Ejecuta un procedimiento almacenado
     * @param nameProc Nombre del procedimiento almacenado que se quiere ejecutar.
     * @param paramsIN Array de Object con los parámetros de entrada del procedimiento.
     * @return ResultSet con los datos de salida del procedimiento.
     * @throws SQLException 
     */
    public Object callProcedure(String nameProc, Object[] paramsIN) throws SQLException{
        DatabaseMetaData metaData = conn.getMetaData();
        // Construyendo la query para llamar al procedimiento.
        /*
        * NOTA BENE: Es importante añadir el catálogo como parametro para el
        * método "getProcedureColumns" pues de no hacerlo crea un problema de
        * rendimiento que hace que la ejecución de dicho método se demore mucho.
        */
        String callString = generarCall(nameProc, 
                metaData.getProcedureColumns(conn.getCatalog(), null , nameProc, null));
        
        // Sacando los tipos de parametro (IN, OUT, INOUT)
        ResultSet rs = metaData.getProcedureColumns(conn.getCatalog(), null , nameProc, null);
        
        // Preparando la llamada.
        CallableStatement call = conn.prepareCall(callString);
        
        int i = 0; // Indice para acceder al array de parametros.
        ArrayList<Object> datosSalida = new ArrayList<>();
        
        while (rs.next()) { // Para cada parámetro del procedimiento...
            /*
            COLUMN_TYPE VALUES:
                0: UNKNOWN
                1: IN
                2: INOUT
                3: RESULT
                4: OUT
                5: RETURN
            */
            switch(rs.getShort("COLUMN_TYPE")){
                case 1: // IN
                    call.setObject(rs.getString("COLUMN_NAME"), paramsIN[i]);
                    i++;
                    break;
                case 2: // INOUT
                case 4: // OUT
                    String nombreParam = rs.getString("COLUMN_NAME");
                    
                    call.registerOutParameter(
                            nombreParam, // Nombre del parametro.
                            Integer.valueOf(rs.getString("DATA_TYPE")) // Tipo de dato.
                    );
                    
                    // Se guarda el nombre del parámetro para poder llamarlo más tarde. 
                    datosSalida.add(nombreParam);
                    i++;
                    break;
                default:
            }
        }
        
        Object resultado;
        if (call.execute()){ // Si el resultado es un ResultSet...
            resultado = call.getResultSet(); // Se extrae el ResultSet.
            
        } else { // Si el resultado NO es un ResultSet...
            // Añadiendo los valores devueltos en un ArrayList.
            for (int j = 0; j < datosSalida.size(); j++) {
                datosSalida.set(j, call.getObject((String) datosSalida.get(j)));
            }
            
            return datosSalida;
        }
        
        return resultado;
    }
    
    /**
     * Ejecuta por lote una seri de consultas pasadas como parametro.
     * Solo se admiten querys que devuelven un valor numérico como:
     * INSERT INTO, UPDATE, DELETE, CREATE TABLE, DROP TABLE e ALTER TABLE
     * 
     * @param querys Lista de consultas para ejecutar por lote.
     * @return Array con los resultados devueltos por cada query del lote.
     * @throws SQLException 
     */
    public int[] ejecutarLote(String[] querys) throws SQLException{
        int[] resultados = null;
        try {
            conn.setAutoCommit(false);
            Statement stmt = getStatement();
            
            // Añadiendo al bach cada una de las querys.
            for (String query : querys) {
                stmt.addBatch(query);
            }
            
            // Ejecutando el batch
            resultados = stmt.executeBatch();
            
            conn.commit();
            
        } catch (BatchUpdateException ex) {
            System.out.println("ERROR! Alguna de las querys del lote no es valida.");
            System.out.println(ex.getMessage());
            
        }finally{
            conn.setAutoCommit(true);
        }
        
        return resultados;
    }
    
    /**
     * Genera un String con la informacion necesaria para llamar a un 
     * procedimiento almacenado con sus parámetros 
     * @param nameProc Nombre del procedimiento a llamar.
     * @param rs ResultSet del cual se sacará el numero de parámetros necesarios.
     * @return String
     * @throws SQLException 
     */
    private String generarCall(String nameProc, ResultSet rs) throws SQLException {
        String call = "{call " + nameProc + "(";
        
        // Sacando el numero de parametroos del procedimiento.
        int numParams = lengthResultSet(rs);
        
        // Añadiendo los parametros indicados separados por comas.
        for (int i = 1; i < numParams; i++) {
            call += "?,";
        }
        
        // Quitando la ultima coma y cerrando la call.
        return call.substring(0, call.lastIndexOf(',')) + ")}";
    }
    
    /**
     * Devuelve el tamaño de un ResultSet pasado como parametro.
     * @param rs ResultSet del cual queremos saber el tamaño.
     * @return int con el tamaño del ResultSet.
     * @throws SQLException 
     */
    private int lengthResultSet(ResultSet rs) throws SQLException{
        int count = 0;
        
        // Contando los registros de un ResultSet.
        while(rs.next()){
            count++;
        }
        
        return count;
    }
    
    /**
     * Ejecuta una sentencia de tipo SELECT y devuelve un ResultSet 
     * cuyo cursor se puede mover en cualquier direccion además de ser 
     * actualizable.
     * @param query Consulta que se ejecutará.
     * @param args Array de Object con los parámetros de la consulta
     * @return ResultSet con los resultados de la consulta.
     * @throws SQLException 
     */
    public ResultSet executeSelect(String query, Object[] args) throws SQLException{
        PreparedStatement sentencia = conn.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
        
        if(args != null){
            for (int i = 0; i < args.length; i++) {
                sentencia.setObject(i + 1, args[i]);
            }
        }
        
        return sentencia.executeQuery();
    }

    /**
     * Ejecuta una serie de consultas usando transacciones de forma NO automática
     * @param querys Array con las sentencias que se ejecutarán.
     * @param params Array de Arrays de Object con los parámetros de cada sentencia.
     * @throws SQLException 
     */
    public void executeTransaction(String[] querys, Object[][] params) throws SQLException {
        PreparedStatement sentencia = null;
        try {
            conn.setAutoCommit(false);

            // Preparando cada una de las sentencias.
            for (int i = 0; i < querys.length; i++) {
                sentencia = conn.prepareStatement(querys[i]);

                // Si la sentencia en turno tiene parametros, se añaden.
                if (params != null && params[i] != null) {
                    for (int j = 0; j < params[i].length; j++) {
                        sentencia.setObject(j + 1, params[i][j]);
                    }
                }
                
                // Ejecutando la sentencia en turno.
                sentencia.executeUpdate();
            }

            conn.commit();
            System.out.println("Transacción realizada con éxito");
        } catch (SQLException ex) {
            // Si algo va mal antes de terminar la transacción...
            System.out.println("No se ha podido llevar a cabo la transacción:");
            System.out.println(ex.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                System.out.println("No se pudo hacer rollback.");
            }

        } finally {
            if (sentencia != null) {
                sentencia.close();
            }
            conn.setAutoCommit(true);
        }
    }
    
    /**
     * Ejecuta una sentencia de tipo: INSERT INTO, UPDATE, DELETE, 
     * CREATE TABLE, DROP TABLE.
     * @param query La sentencia que se ejecutará.
     * @param args Array de Object con los parámetros de esa sentencia.
     * @return Se devuelve el número de filas afectadas.
     * @throws SQLException 
     */
    public int executeUpdate(String query, Object[] args) throws SQLException {
        
        PreparedStatement sentencia = conn.prepareStatement(query);
        
        if(args != null){
            for (int i = 0; i < args.length; i++) {
                sentencia.setObject(i + 1, args[i]);
            }
        }
        
        return sentencia.executeUpdate();
    }
    
    /**
     * Ejecuta una sentencia de tipo: INSERT INTO, UPDATE, DELETE, 
     * CREATE TABLE, DROP TABLE devolviendo el ultimo id generado.
     * @param query La sentencia que se ejecutará.
     * @param args Array de Object con los parámetros de esa sentencia.
     * @return Se devuelve el último id generado o 0 si no hay id que devolver.
     * @throws SQLException 
     */
    public int executeUpdateReturnLastId(String query, Object[] args) throws SQLException{
        PreparedStatement sentencia = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        
        // Añadiendo los parametros a la query.
        if(args != null){
            for (int i = 0; i < args.length; i++) {
                sentencia.setObject(i + 1, args[i]);
            }
        }
        
        // Ejecutando la query.
        sentencia.executeUpdate();
        
        // Recuperando la clave.
        ResultSet rs = sentencia.getGeneratedKeys();

        int clave = 0;
        if (rs.next()) {
            clave = rs.getInt(1);
        }
        
        return clave;
    }
    
    /**
     * Comprueba que los datos pasados como parametro existen en la tabla indicada 
     * de la base de datos.
     * @param nombreTabla Tabla en la que se hará la consulta.
     * @param nombresColumnas Columnas en las que se guardan los datos a comprobar.
     * @param datos Los datos que se quiere verificar si están en la BD.
     * @return true si los datos existen false si no.
     */
    public boolean isInDB(String nombreTabla, String[] nombresColumnas, Object[] datos) throws SQLException{
        String query = "select " + nombresColumnas[0] 
                + " from " + nombreTabla + " where ";
        
        // Añadiendo los datos al where.
        for (int i = 0; i < nombresColumnas.length; i++) {
            query += nombresColumnas[i] + " = " 
                    + ((datos[i].getClass().getSimpleName().equals("String")) 
                    ? "'" + datos[i] + "'" : datos[i]) + " and ";
        }
        query += "0 = 0"; // Esto anula el último "and" del bucle.


        // Realizando la consulta
        ResultSet rs = executeSelect(query, null);
        
        // Si existe un primer rerultado ya se comprueba su existencia.
        return rs.first();
    }

    /**
     * Cierra la conexión actual con la base de datos.
     */
    public void close() {
        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.println("No se pudo cerrar la conexión.");
        }
    }
    
    // GETTER
    public Connection getConnection() {
        return conn;
    }
    
    public Statement getStatement() throws SQLException {
        return conn.createStatement();
    }

}
