/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ProyectoAdminsResource.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service.restlets.projects;

import es.sidelab.scstack.lib.exceptions.apache.ExcepcionConsola;
import es.sidelab.scstack.lib.exceptions.apache.ExcepcionGeneradorFicherosApache;
import es.sidelab.scstack.lib.exceptions.api.ExcepcionLogin;
import es.sidelab.scstack.lib.exceptions.api.ExcepcionParametros;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPAdministradorUnico;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionGestorRedmine;
import es.sidelab.scstack.service.data.Usuarios;
import es.sidelab.scstack.service.restlets.BaseProyectosResource;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;


/**
 * <p>Recurso que representa a los administradores de un determinado proyecto.</p>
 * <p>Sobre este recurso está permitido realizar métodos GET, PUT y DELETE</p>
 * @author Arek Klauza
 */
public class ProyectoAdminsResource extends BaseProyectosResource {

    public ProyectoAdminsResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
    }





    /**
     * <p>GET</p>
     * <p>Consulta la lista de administradores de un determinado proyecto.</p>
     * <p>Corresponde con el CUU.6</p>
     * @param variant Objeto que representa el tipo de petición recibida
     * @return Devuelve una representación de la lista de usuarios
     * @throws ResourceException
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException  {
        Representation rep = null;
        Usuarios resource = null;
        String user = this.userReq;
        String pass = this.passReq;

       try {
            // Recupera el cn del proyecto a consultar
            String cn = (String) getRequest().getAttributes().get("cn");
            resource = proxy.getAdminsProyecto(cn, user, pass);

            // Comprobación del tipo de representación pedida y serialización
            if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
                rep = getHtmlRepresentation("/proyectos/index.html");
            } else if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
                String stringRep = resource.serializarXml();
                rep = new StringRepresentation(stringRep, MediaType.TEXT_XML);
            } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
                rep = resource.serializarJson();
            } else
                // No es una de las representaciones que puedo ofrecer,
                throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);

        } catch (ExcepcionParametros ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionLogin ex) {
            // this.pedirLogin(ex.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, ex.getMessage());
        } catch (ExcepcionLDAPNoExisteRegistro ex) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        }
        return rep;
    }







    /**
     * <p>PUT</p>
     * <p>Añade un usuario a la lista de administradores de un proyecto. Si el UID no
     * existe devuelve un 404.</p>
     * <p>Corresponde con el CUA.3</p>
     * @param entity Representación del objeto que se recibe en la petición
     * @throws ResourceException
     */
    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        String user = this.userReq;
        String pass = this.passReq;

        try {
            // Si no se suministra el UID del usuario no se puede realizar la acción
            String uid = (String) getRequest().getAttributes().get("uid");
            if (uid == null || uid.isEmpty()) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Para poder realizar esta acción se debe suministrar un UID en la URI de petición");
            }
            String cn = (String) getRequest().getAttributes().get("cn");

            // Añade al usuario como nuevo administrador del proyecto a través del proxy que habla con la API
            proxy.putAdminProyecto(uid, cn, user, pass);

            // Devuelve en el location header de respuesta la URI del proyecto
            getResponse().setStatus(Status.SUCCESS_OK);
            getResponse().setLocationRef("/proyectos/" + cn);

        } catch (ExcepcionGestorRedmine ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionParametros ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionLogin ex) {
            // this.pedirLogin(ex.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, ex.getMessage());
        } catch (ExcepcionLDAPNoExisteRegistro ex) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        }
    }
    @Override
    public boolean allowPut() {
        return true;
    }






    /**
     * <p>DELETE</p>
     * <p>Elimina un administrador de un proyecto concreto de la Forja.</p>
     * <p>Corresponde con el CUA.9</p>
     * @throws ResourceException
     */
    @Override
    public void removeRepresentations()throws ResourceException {
        String user = this.userReq;
        String pass = this.passReq;

        try {
            // Si no se suministra el UID del usuario no se puede realizar la acción
            String uid = (String) getRequest().getAttributes().get("uid");
            if (uid == null || uid.isEmpty()) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Para poder realizar esta acción se debe suministrar un UID en la URI de petición");
            }
            String cn = (String) getRequest().getAttributes().get("cn");

            // Borra al miembro a través del Proxy de la API
            proxy.deleteAdminProyecto(uid, cn, user, pass);

            // Notifica que la solicitud ha sido realizada. No necesita enviarse ningún contenido.
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);

        } catch (ExcepcionGestorRedmine ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionParametros ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionLDAPAdministradorUnico ex) {
            throw new ResourceException(Status.CLIENT_ERROR_FAILED_DEPENDENCY, ex.getMessage());
        } catch (ExcepcionLogin ex) {
            // this.pedirLogin(ex.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, ex.getMessage());
        } catch (ExcepcionLDAPNoExisteRegistro ex) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, ex.getMessage());
        } catch (ExcepcionConsola ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionGeneradorFicherosApache ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        }
        
    }
    @Override
    public boolean allowDelete() {
        return true;
    }
}
