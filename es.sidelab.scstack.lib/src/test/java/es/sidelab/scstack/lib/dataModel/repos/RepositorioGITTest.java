package es.sidelab.scstack.lib.dataModel.repos;

import org.junit.Before;
import org.junit.Test;

import es.sidelab.scstack.lib.api.API_Abierta;
import es.sidelab.scstack.lib.config.ConfiguracionForja;

import java.util.logging.Logger;

import junit.framework.Assert;

/**
 * Test class for RepositorioGit methods.
 */
public class RepositorioGITTest {

    private Logger log;

    private RepositorioGIT repositorioGIT;

    @Before
    public void setUp() throws Exception {

        repositorioGIT = new RepositorioGIT(true,
                ConfiguracionForja.pathGITApache);
    }

    /**
     * Test para Crear repositorios Git.
     * 
     * @throws Exception
     */
    @Test
    public void tesCrearRepositorio() throws Exception {

        API_Abierta apiAbierta = new API_Abierta(
                "src/test/resources/scstack.conf");
        String cnProyecto;
        String uidAdminProyecto;

        /*
         * Test 1 - create repository
         */
        cnProyecto = "javaconsole";
        uidAdminProyecto = "test";

        try {
            repositorioGIT.crearRepositorio(cnProyecto, uidAdminProyecto,
                    apiAbierta);
            Assert.assertTrue("[Success] - Project '" + cnProyecto
                    + "' created and configurated.", true);
        } catch (Exception e) {
            Assert.assertFalse("[Error] - error creating the project:\t"
                    + cnProyecto + "\n" + e.getStackTrace(), false);
        }
    }

}