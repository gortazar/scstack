/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: FactoriaRepositorios.java
 * Autor: Arek Klauza
 * Fecha: Enero 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package es.sidelab.scstack.lib.dataModel.repos;

import es.sidelab.scstack.lib.config.ConfiguracionForja;
import es.sidelab.scstack.lib.exceptions.SCStackException;
import es.sidelab.scstack.lib.exceptions.dataModel.ExcepcionRepositorio;

/**
 *
 * @author Arek Klauza
 */
public class FactoriaRepositorios {

    public enum TipoRepositorio {
        SVN, GIT
    }

    public static Repositorio crearRepositorio(TipoRepositorio tipoRepositorio, boolean esRepoPublico, String rutaRepo) throws SCStackException {
        if (tipoRepositorio == null)
            return null;
        switch (tipoRepositorio) {
            case SVN:   if (rutaRepo == null)
                            return new RepositorioSVN(esRepoPublico, ConfiguracionForja.pathSVNApache);
                        else
                            return new RepositorioSVN(esRepoPublico, rutaRepo);
            case GIT:   if (rutaRepo == null)
                            return new RepositorioGIT(esRepoPublico, ConfiguracionForja.pathGITApache);
                        else
                            return new RepositorioGIT(esRepoPublico, rutaRepo);
            default:    throw new ExcepcionRepositorio("Está intentando crear un repositorio cuyo tipo es incorrecto/inexistente");
        }
    }

    public static Repositorio crearRepositorio(String cadena) throws SCStackException {
        String[] estructura = new String[3];
        estructura = cadena.split("###");
        boolean esPublico = false;
        if (estructura[1].equals("Publico"))
            esPublico = true;
        
        if (estructura[0].equals(RepositorioSVN.tipo))
            return new RepositorioSVN(esPublico, estructura[2]);
        else if (estructura[0].equals(RepositorioGIT.tipo))
            return new RepositorioGIT(esPublico, estructura[2]);
        else
            return null;
    }

    public static TipoRepositorio getTipo(String tipo) {
        if (tipo.equalsIgnoreCase(TipoRepositorio.SVN.toString()))
            return TipoRepositorio.SVN;
        else if (tipo.equalsIgnoreCase(TipoRepositorio.GIT.toString()))
            return TipoRepositorio.GIT;
        else
            return null;
    }

}
