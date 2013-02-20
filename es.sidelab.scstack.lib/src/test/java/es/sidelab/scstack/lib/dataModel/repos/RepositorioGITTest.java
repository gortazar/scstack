package es.sidelab.scstack.lib.dataModel.repos;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: GerritManagerTest.java
 * Autor: -
 * Fecha: -
 * Revisión: -
 * Versión: -
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

/**
 * Esta clase de pruebas es la encargada de probar el correcto funcionamiento de
 * los distintos métodos ofrecidos por la API de la Forja.
 * 
 * @author Arek Klauza
 */
public class RepositorioGITTest {

	private Logger log;

	/* PARÁMETROS DE CONFIGURACIÓN */
	private String uidSuperAdmin = "sadmin";
	private String passSuperAdmin = "sadmin";

	private RepositorioGIT repositorioGIT;

	@Before
	public void setUp() throws Exception {

	    repositorioGIT = new RepositorioGIT(true, null);
	}

	/**
	 * Test para Crear repositorios Git.
	 * 
	 * @throws Exception
	 */
	@Test
	public void tesCrearRepositorio() throws Exception {
	    
	    
	}
	
}