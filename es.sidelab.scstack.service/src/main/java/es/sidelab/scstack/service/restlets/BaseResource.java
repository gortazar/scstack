/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: BaseResource.java
 * Autor: Arek Klauza
 * Fecha: Marzo 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.service.restlets;

import es.sidelab.scstack.service.ServicioREST;
import es.sidelab.scstack.service.data.Proxy;

import java.io.BufferedReader;
import java.io.FileReader;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

/**
 * <p>Clase padre de la cual heredan todos los recursos del servicio REST.</p>
 * <p>Se utiliza para métodos y atributos comunes a todos los recursos, como es
 * el caso del Proxy que maneja la API de la Forja y la petición de login().</p>
 * @author Arek Klauza
 */
public class BaseResource extends Resource {

    protected Proxy proxy;
    protected String userReq;
    protected String passReq;

    public BaseResource(Context context, Request request, Response response) throws ResourceException {
        super(context, request, response);
        
        this.proxy = Proxy.getInstance();

        // Variantes de representación soportadas
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));

        // Sistema de captura de Login de la petición del cliente
        ChallengeResponse chalReq = getRequest().getChallengeResponse();
        if (chalReq != null && !chalReq.getIdentifier().isEmpty()) {
            userReq = getRequest().getChallengeResponse().getIdentifier();
            passReq = new String(getRequest().getChallengeResponse().getSecret());
            return;
        }
        // La petición no contiene credenciales o no está autenticado, hay que pedirlos al cliente
        this.pedirLogin();
    }





    /**
     * <p>Solicita al navegador o cliente que envíe su login y contraseña mediante el mecanismo
     * de autenticación básico de HTTP.</p>
     * @param msg String con el mensaje que se debe mandar en la respuesta de error
     */
    protected final void pedirLogin() {
        pedirLogin(null);
    }

    protected final void pedirLogin(String msg) {
        ChallengeRequest req = new ChallengeRequest(ChallengeScheme.HTTP_BASIC, "API SidelabCode");
        getResponse().setChallengeRequest(req);
        if (msg == null || msg.isEmpty()) {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, msg);
        }
    }



    /**
     * <p>Devuelve el fichero HTML que representa el recurso.
     * @param ruta Ruta relativa al rootHTML donde se encuentra el HTML a servir
     * @return Representación del HTML del recurso pedido
     * @throws ResourceException Cuando se produce algún tipo de erro durante la
     * apertura y lectura del HTML para ser servido por el servidor.
     */
    protected final StringRepresentation getHtmlRepresentation(String ruta) throws ResourceException {
        StringBuilder html = new StringBuilder();
        try {
        	// an explanation for this 'substring(7)':
        	//  it appears that rootHTML looks like this: file://xxxxa-pathxxxx
        	//  so it only wants the path, without the file://
        	// at least, this is what I've managed to find out.
            FileReader fr = new FileReader(ServicioREST.rootHTML.substring(7) + ruta);
            BufferedReader buf = new BufferedReader(fr);
            String linea;
            while ((linea = buf.readLine()) != null) {
                html.append(linea);
            }
            buf.close();
            fr.close();
        } catch (Exception ex) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
        }
        return new StringRepresentation(html, MediaType.TEXT_HTML);
    }
}
