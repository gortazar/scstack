/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: ProyectoRepositorioResource.java
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
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionRepositorio;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionGestorLDAP;
import es.sidelab.scstack.lib.exceptions.ldap.ExcepcionLDAPNoExisteRegistro;
import es.sidelab.scstack.service.data.Proyecto;
import es.sidelab.scstack.service.data.Repositorio;
import es.sidelab.scstack.service.restlets.BaseProyectosResource;

import java.io.IOException;
import org.json.JSONException;
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
 * <p>Recurso que representa el repositorio de un proyecto de la Forja.</p>
 * <p>Sobre este recurso está permitido realizar métodos GET, POST y DELETE.</p>
 * @author Arek Klauza
 */
public class ProyectoRepositorioResource extends BaseProyectosResource {

    public ProyectoRepositorioResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
    }





    /**
     * <p>GET</p>
     * <p>Consulta los datos (entre ellos los repositorios) de un proyecto
     * determinado de la Forja.</p>
     * <p>Corresponde con el CUU.7</p>
     * @param variant Objeto que representa el tipo de petición recibida
     * @return Devuelve una representación del proyecto.
     * @throws ResourceException
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException  {
        Representation rep = null;
        Proyecto resource = null;
        String user = this.userReq;
        String pass = this.passReq;

       try {
            // Recupera el nombre del proyecto a consultar
            String cn = (String) getRequest().getAttributes().get("cn");
            resource = proxy.getProyecto(cn, user, pass);

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
     * <p>Crea un nuevo repositorio en el proyecto {cn} de la URI.</p>
     * <p>Corresponde con el CUA.5</p>
     * @param entity Representación del objeto que se recibe en la petición
     * @throws ResourceException
     */
    @Override
    public void storeRepresentation(Representation entity) throws ResourceException {
        Repositorio repo = null;
        String user = this.userReq;
        String pass = this.passReq;

        try {
            // Decodificamos la información en función de cómo venga la petición
            repo = this.decodificaRepositorio(entity);
            
            // Si el tipo de repositorio de la URI no coincide con el de la petición se aborta
            if (!((String) getRequest().getAttributes().get("tipo")).equals(repo.getTipo())) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "El tipo de repositorio suministrado en la estructura de datos no coincide con el de la URI");
            }

            // Recupera el nombre del proyecto
            String cn = (String) getRequest().getAttributes().get("cn");

            // Guarda el nuevo proyecto a través del proxy que habla con la API
            proxy.postRepositorio(repo, cn, user, pass);

            // Devuelve en el location header de respuesta la URI del nuevo proyecto
            getResponse().setStatus(Status.SUCCESS_CREATED);
            getResponse().setLocationRef(repo.getUri());

        } catch (JSONException ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionParametros ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
        } catch (ExcepcionGeneradorFicherosApache ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionConsola ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionRepositorio ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (ExcepcionLogin ex) {
            // this.pedirLogin(ex.getMessage());
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, ex.getMessage());
        } catch (ExcepcionLDAPNoExisteRegistro ex) {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, ex.getMessage());
        } catch (ExcepcionGestorLDAP ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        } catch (IOException ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        }
    }
    @Override
    public boolean allowPut() {
        return true;
    }






    /**
     * <p>DELETE</p>
     * <p>Elimina definitivamente un repositorio concreto de un proyecto de la Forja.</p>
     * <p>Corresponde con el CUA.10</p>
     * @throws ResourceException
     */
    @Override
    public void removeRepresentations()throws ResourceException {
        String user = this.userReq;
        String pass = this.passReq;

        try {
            // Si no se suministra el CN del proyecto no se puede realizar la acción
            String cn = (String)getRequest().getAttributes().get("cn");
            if (cn == null || cn.isEmpty()) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Para poder realizar esta acción se debe suministrar un CN de proyecto en la URI de petición");
            }
            String tipoRepo = (String)getRequest().getAttributes().get("tipo");

            proxy.deleteRepositorio(tipoRepo, cn, user, pass);

            // Notifica que la solicitud ha sido realizada. No necesita enviarse ningún contenido.
            getResponse().setStatus(Status.SUCCESS_NO_CONTENT);

        } catch (ExcepcionParametros ex) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, ex.getMessage());
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
