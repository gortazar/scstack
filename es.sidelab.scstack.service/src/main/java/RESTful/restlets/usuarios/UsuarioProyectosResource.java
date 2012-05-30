/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: UsuarioProyectosResource.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package RESTful.restlets.usuarios;

import RESTful.datos.Proyectos;
import RESTful.restlets.BaseUsuariosResource;
import excepciones.api.ExcepcionLogin;
import excepciones.api.ExcepcionParametros;
import excepciones.gestorLdap.ExcepcionGestorLDAP;
import excepciones.gestorLdap.ExcepcionLDAPNoExisteRegistro;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * <p>Recurso que representa las listas de proyectos de un usuario concreto de
 * la Forja, pudiendo ser la lista CN de proyectos participados o administrados.</p>
 * <p>Sobre este recurso está permitido realizar métodos GET</p>
 * @author Arek Klauza
 */
public class UsuarioProyectosResource extends BaseUsuariosResource {

    public UsuarioProyectosResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
    }



    /**
     * <p>GET</p>
     * <p>Devuelve la lista de proyectos de un usuario concreto, pudiendo ser 
     * proyectos participados (sin parámetros) o proyectos administrados
     * (con parámetro "?administrados").</p>
     * <p>Corresponde con el CUU.3 y CUU.4</p>
     * @param variant Objeto que representa el tipo de petición recibida
     * @return Devuelve una representación de la lista de proyectos
     * @throws ResourceException
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation rep = null;
        Proyectos lista = null;
        String user = this.userReq;
        String pass = this.passReq;

        try {
            // Extrae el uid del usuario cuya lista queremos consultar
            String uid = (String) getRequest().getAttributes().get("uid");

            // Decodifica qué tipo de lista se ha pedido
            Parameter paramNombre = getRequest().getResourceRef().getQueryAsForm().getFirst("administrados", true);
            if (paramNombre != null) {
                lista = this.proxy.getListaProyectosAdministrados(uid, user, pass);
            } else {
                lista = this.proxy.getListaProyectosParticipados(uid, user, pass);
            }

            // Comprobación del tipo de representación pedida y serialización
            if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
                rep = getHtmlRepresentation("/usuarios/usuario.html");
            } else if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
                String stringRep = lista.serializarXml();
                rep = new StringRepresentation(stringRep, MediaType.TEXT_XML);
            } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
                rep = lista.serializarJson();
            } else
                // No es una de las representaciones que puedo ofrecer,
                throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);

        } catch (ExcepcionParametros ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionLogin ex) {
            // this.pedirLogin(ex.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, ex.getMessage());
        } catch (ExcepcionLDAPNoExisteRegistro ex) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND , ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL , ex.getMessage());
        }
        return rep;
    }
}
