/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.sidelab.scstack.service.restlets;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import es.sidelab.scstack.service.data.Servicios;

/**
 * <p>
 * @author Arek
 */
public class ServiciosResource extends BaseResource {

    public ServiciosResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
    }

    /**
     * <p>GET</p>
     * <p>Devuelve un String con el rol del usuario que hace el Login.</p>
     * @param variant Objeto que representa el tipo de petición recibida
     * @return Devuelve una representación String del rol de usuario.
     * @throws ResourceException
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation rep = null;
        Servicios resource = null;
        String user = this.userReq;
        String pass = this.passReq;

        resource = new Servicios();

        // Comprobación del tipo de representación pedida y serialización
        if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            rep = getHtmlRepresentation("/index.html");
        } else if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            String stringRep = resource.serializarXml();
            rep = new StringRepresentation(stringRep, MediaType.TEXT_XML);
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            rep = resource.serializarJson();
        } else // No es una de las representaciones que puedo ofrecer,
        {
            throw new ResourceException(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
        }
        return rep;
    }
}
