/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ProyectosResource.java
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
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionProyecto;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionRepositorio;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.lib.exceptions.redmine.ExcepcionGestorRedmine;
import es.sidelab.scstack.service.data.ProyectoNuevo;
import es.sidelab.scstack.service.data.Proyectos;
import es.sidelab.scstack.service.restlets.BaseProyectosResource;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * <p>Recurso que representa la lista de Proyectos de la Forja.</p>
 * <p>Sobre este recurso está permitido realizar métodos GET y POST.</p>
 * @author Arek Klauza
 */
public class ProyectosResource extends BaseProyectosResource {

	private static final Logger LOGGER = Logger.getLogger(BaseProyectosResource.class.getName());
	
    public ProyectosResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
    }



    /**
     * <p>GET</p>
     * <p>Devuelve la lista de proyectos que hay en un momento determinado en la
     * Forja.</p>
     * <p>Corresponde con el CUA.6</p>
     * @param variant Objeto que representa el tipo de petición recibida
     * @return Devuelve una representación de los proyectos
     * @throws ResourceException
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation rep = null;
        Proyectos resource = null;
        String user = this.userReq;
        String pass = this.passReq;

        try {
            // Decodifica qué tipo de lista se ha pedido
            resource = this.proxy.getListaProyectos(user, pass);
            
            // Comprobación del tipo de representación pedida y serialización
            if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
                rep = getHtmlRepresentation(getRequest().getResourceRef().getPath() + "/index.html");
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
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND , ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL , ex.getMessage());
        }
        return rep;
    }





    /**
     * <p>POST</p>
     * <p>Crea un nuevo proyecto en la Forja. Teniendo como restricción que el CN
     * del proyecto facilitado en la estructura no esté ya en uso, en cuyo caso
     * lanzará una excepción</p>
     * <p>En el XML del mensaje de request, se pueden obviar los campos opcionales:
     * defaultRepositorio, gidNumber y Repos.
     * <p>Corresponde con el CUS.5</p>
     * @param entity Representación del objeto que se recibe en la petición
     * @throws ResourceException
     */
    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        ProyectoNuevo proyecto = null;
        String user = this.userReq;
        String pass = this.passReq;

        try {
            // Decodificamos la información en función de cómo venga la petición
            proyecto = this.decodificaProyectoNuevo(entity);

            // Guarda el nuevo proyecto a través del proxy que habla con la API
            proxy.postProyecto(proyecto, user, pass);

            // Devuelve en el location header de respuesta la URI del nuevo proyecto
            getResponse().setStatus(Status.SUCCESS_CREATED);
            getResponse().setLocationRef(proyecto.getUri());

        } catch (JSONException ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionParametros ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionProyecto ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionGeneradorFicherosApache ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionConsola ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionRepositorio ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionGestorRedmine ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionLogin ex) {
            // this.pedirLogin(ex.getMessage());
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, ex.getMessage());
        } catch (ExcepcionLDAPNoExisteRegistro ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (IOException ex) {
        	LOGGER.log(Level.SEVERE,"Exception",ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        }
    }
    @Override
    public boolean allowPost() {
        return true;
    }
}
