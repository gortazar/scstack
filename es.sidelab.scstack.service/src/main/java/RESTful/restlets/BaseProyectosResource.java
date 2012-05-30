/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: BaseProyectosResource.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package RESTful.restlets;

import RESTful.datos.Proyecto;
import RESTful.datos.ProyectoNuevo;
import RESTful.datos.Repositorio;
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
public class BaseProyectosResource extends BaseResource {

    public BaseProyectosResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
    }




    /**
     * <p>A partir de la representación capturada por el Framework, extrae la
     * información relativa a un nuevo proyecto contenida en el mensaje HTTP recibido.</p>
     * @param rep Objeto que representa el mensaje recibido en la petición
     * @return Proyecto creado acorde al mensaje recibido
     * @throws ResourceException Cuando la sintaxis del mensaje recibido no es
     * la correcta.
     * @throws IOException Cuando no se ha podido serializarXml el mensaje.
     */
    protected final ProyectoNuevo decodificaProyectoNuevo(Representation rep) throws ResourceException, IOException, JSONException {
        ProyectoNuevo proyecto;

        if (rep.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
            Form form = new Form(rep);
            proyecto = new ProyectoNuevo(
                    form.getFirstValue("cn"),
                    form.getFirstValue("descripcion"),
                    form.getFirstValue("primerAdmin"),
                    form.getFirstValue("tipoRepo"),
                    form.getFirstValue("esRepoPublico"),
                    form.getFirstValue("rutaRepo"));

        } else if (rep.getMediaType().equals(MediaType.TEXT_XML)) {
            proyecto = (ProyectoNuevo) ProyectoNuevo.deserializarXml(rep.getText());
        } else if (rep.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            JSONObject objeto = new JSONObject(rep.getText());
            proyecto = new ProyectoNuevo(
                    objeto.getString("cn"),
                    objeto.getString("descripcion"),
                    objeto.getString("primerAdmin"),
                    objeto.getString("tipoRepo"),
                    objeto.getString("esRepoPublico"),
                    objeto.getString("rutaRepo"));
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
        }
        proyecto.setUri("proyectos/" + proyecto.getCn());
        return proyecto;
    }



    /**
     * <p>A partir de la representación capturada por el Framework, extrae la
     * información relativa a un proyecto contenida en el mensaje HTTP recibido.</p>
     * @param rep Objeto que representa el mensaje recibido en la petición
     * @return Proyecto creado acorde al mensaje recibido
     * @throws ResourceException Cuando la sintaxis del mensaje recibido no es
     * la correcta.
     * @throws IOException Cuando no se ha podido serializarXml el mensaje.
     */
    protected final Proyecto decodificaProyecto(Representation rep) throws ResourceException, IOException, JSONException {
        Proyecto proyecto;

        if (rep.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
            Form form = new Form(rep);
            proyecto = new Proyecto(
                    form.getFirstValue("cn"),
                    form.getFirstValue("descripcion"),
                    form.getFirstValue("defaultRepositorio"));

        } else if (rep.getMediaType().equals(MediaType.TEXT_XML)) {
            proyecto = (Proyecto) Proyecto.deserializarXml(rep.getText());
        } else if (rep.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            JSONObject objeto = new JSONObject(rep.getText());
            proyecto = new Proyecto(
                    objeto.getString("cn"),
                    objeto.getString("descripcion"),
                    objeto.getString("defaultRepositorio"));
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
        }
        proyecto.setUri("proyectos/" + proyecto.getCn());
        return proyecto;
    }



    /**
     * <p>A partir de la representación capturada por el Framework, extrae la
     * información relativa a un repositorio contenida en el mensaje HTTP recibido.</p>
     * @param rep Objeto que representa el mensaje recibido en la petición
     * @return Repositorio creado acorde al mensaje recibido
     * @throws ResourceException Cuando la sintaxis del mensaje recibido no es
     * la correcta.
     * @throws IOException Cuando no se ha podido serializarXml el mensaje.
     */
    protected final Repositorio decodificaRepositorio(Representation rep) throws ResourceException, IOException, JSONException {
        Repositorio repo;

        if (rep.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
            Form form = new Form(rep);
            boolean esPublico = false;
            if (form.getFirstValue("esPublico") != null && form.getFirstValue("esPublico").equalsIgnoreCase("true"))
                esPublico = true;
            repo = new Repositorio(
                    form.getFirstValue("tipo"),
                    esPublico,
                    form.getFirstValue("ruta"));

        } else if (rep.getMediaType().equals(MediaType.TEXT_XML)) {
            repo = (Repositorio) Repositorio.deserializarXml(rep.getText());
        } else if (rep.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            JSONObject objeto = new JSONObject(rep.getText());
            boolean esPublico = false;
            if (objeto.getString("esPublico").equalsIgnoreCase("true"))
                esPublico = true;
            repo = new Repositorio(
                    objeto.getString("tipo"),
                    esPublico,
                    objeto.getString("ruta"));

        } else {
            throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
        }
        repo.setUri("repos/" + repo.getTipo());
        return repo;
    }

}
