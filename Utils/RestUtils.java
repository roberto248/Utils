package Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Roberto Santos Cordeiro
 */
public class RestUtils {
    private static Scanner sc = new Scanner(System.in);
    private static String strURL; // URL en la que se harán las operaciones.
    
    // CONSTRUCTORES ===========================================================
    public RestUtils(String strURL) {
        this.strURL = strURL;
    }
    
    
    // MÉTODOS =================================================================
    /**
     * Este método inserta nuevos datos en la base de datos.
     * @param finURL ruta de acceso a la web (slug).
     * @param parametros datos que se insertarán en la base de datos.
     * @return si la consulta trae un id este será devuelto por este método.
     */
    static public int insertar(String finURL, String parametros) {
        URL url = null;
        HttpURLConnection con = null;
        int id = -1;

        try {
            url = new URL(strURL + finURL);
            con = (HttpURLConnection) url.openConnection();

            // pasando los parámetros en el cuerpo de la petición
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            PrintWriter out = new PrintWriter(con.getOutputStream());
            out.print(parametros);
            out.close();
            con.connect();

            if (con.getResponseCode() == 201) {
                // Si la insercion sale bien se recoge el id devuelto desde un json...
                String json = "";
                BufferedReader bufferIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String linea;
                while ((linea = bufferIn.readLine()) != null) {
                    json += linea;
                }
                bufferIn.close();

                // si se devuelve un id...
                if (!json.isEmpty()) {
                    JSONObject object = new JSONObject(json);
                    id = object.getInt("id");
                }

            } else {
                System.out.println("Problemas.Respuesta: (" + con.getResponseCode() + ") " + con.getResponseMessage());
            }
        } catch (IOException ex) {
            System.out.println("Error en la conexión");
        }

        return id;
    }

    /**
     * Este método hace una consulta a la base de datos que devolverá varias
     * filas.
     * @param finURL ruta de acceso a la web (slug).
     * @return JSONArray con la respuesta a la consulta realizada.
     */
    static public JSONArray seleccionMultiple(String finURL) {
        URL url = null;
        HttpURLConnection con = null;
        String json = "";
        JSONArray jSONArray = null;

        try {
            url = new URL(strURL + finURL);
            con = (HttpURLConnection) url.openConnection();
            con.connect();

            if (con.getResponseCode() == 200) {
                BufferedReader bufferIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String linea;
                while ((linea = bufferIn.readLine()) != null) {
                    json += linea;
                }
                bufferIn.close();

                jSONArray = new JSONArray(json);

            } else {
                System.out.println("Problemas.Respuesta: (" + con.getResponseCode() + ") " + con.getResponseMessage());
            }
        } catch (IOException ex) {
            System.out.println("Error en la conexión");
        }
        return jSONArray;
    }

    /**
     * Este método hace una consulta a la base de datos que devolverá un único
     * dato como resultado.
     * @param finURL ruta de acceso a la web (slug).
     * @return JSONObject con el dato resultado de la consulta.
     */
    static public JSONObject seleccionUnica(String finURL) {
        URL url = null;
        HttpURLConnection con = null;
        String json = "";
        JSONObject jSONObject = null;

        try {
            url = new URL(strURL + finURL);
            con = (HttpURLConnection) url.openConnection();
            con.connect();

            if (con.getResponseCode() == 200) {
                BufferedReader bufferIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String linea;
                while ((linea = bufferIn.readLine()) != null) {
                    json += linea;
                }
                bufferIn.close();

                jSONObject = new JSONObject(json);

            } else {
                System.out.println("Problemas.Respuesta: (" + con.getResponseCode() + ") " + con.getResponseMessage());
            }
        } catch (IOException ex) {
            System.out.println("Error en la conexión");
        }

        return jSONObject;
    }

    /**
     * Elimina un registro de la base de datos.
     * @param finURL ruta de acceso a la web (slug).
     */
    static public void eliminar(String finURL) {
        int codCliente = 2;
        URL url = null;
        HttpURLConnection con = null;
        
        try {
            url = new URL(strURL + finURL);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("DELETE");
            con.setDoOutput(true);
            con.connect();
            
            if (con.getResponseCode() == 204) {
                System.out.println("Registro eliminado");
            } else {
                System.out.println("Problemas.Respuesta: (" + con.getResponseCode() + ") " + con.getResponseMessage());
            }
            
        } catch (IOException ex) {
            System.out.println("Error en la conexión");
        }
    }

}
