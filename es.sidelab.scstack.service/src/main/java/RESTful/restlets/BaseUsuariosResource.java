/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: BaseUsuariosResource.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package RESTful.restlets;

import RESTful.datos.Usuario;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;


/**
 * <p>Clase padre de usuarios de la cual heredan todos los recursos relacionados
 * con la gestión de usuarios de la Forja.</p>
 * <p>Se utiliza para métodos comunes a todos los recursos de usuarios, como es
 * el caso de decodificaUsuario(...).</p>
 * @author Arek Klauza
 */
public class BaseUsuariosResource extends BaseResource {

    public BaseUsuariosResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
    }





    /**
     * <p>A partir de la representación capturada por el Framework, extrae la
     * información relativa a un usuario contenida en el mensaje HTTP recibido.</p>
     * @param rep Objeto que representa el mensaje recibido en la petición
     * @return Usuario creado acorde al mensaje recibido
     * @throws ResourceException Cuando la sintaxis del mensaje recibido no es
     * la correcta.
     * @throws IOException Cuando no se ha podido serializarXml el mensaje.
     */
    protected final Usuario decodificaUsuario(Representation rep) throws ResourceException, IOException, JSONException {
        Usuario usuario;

        if (rep.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
            Form form = new Form(rep);
            usuario = new Usuario(
                    form.getFirstValue("uid"),
                    form.getFirstValue("nombre"),
                    form.getFirstValue("apellidos"),
                    form.getFirstValue("email"),
                    form.getFirstValue("pass"));
        } else if (rep.getMediaType().equals(MediaType.TEXT_XML)) {
            usuario = (Usuario) Usuario.deserializarXml(rep.getText());
        } else if (rep.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            JSONObject objeto = new JSONObject(rep.getText());
            usuario = new Usuario(
                    objeto.getString("uid"),
                    objeto.getString("nombre"),
                    objeto.getString("apellidos"),
                    objeto.getString("email"),
                    objeto.getString("pass"));
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
        }
        usuario.setUri("usuarios/" + usuario.getUid());
        return usuario;
    }
}
