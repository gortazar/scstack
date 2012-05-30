/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: logica.js
 * Autor: Arek Klauza
 * Fecha: Abril 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


/* VARS GLOBALES */

/* Globales de SidelabCode */
var listaUsuarios = [];
var listaProyectos = [];

/* Del usuario logueado */
var listaMisUsuarios = [];
var listaMisProyectos = [];





/**
 * Función encargada de inicializar todos los elementos y componentes comunes a
 * todas las páginas web de SidelabCode
 */
jQuery.inicializacionComun = function() {
    /* Las funciones AJAX deben ser síncronas para no intentar pintar las listas
     * antes de recibirlas */
    $.ajaxSetup({
        async:false,
        cache:false,
        username: $.cookie("uid"),
        password: $.cookie("pass")
    });

    /* Recuperar todas las listas necesarias para los menús y searchbox */
    $.getLista("/usuarios", "listaUsuarios", listaUsuarios);
    $.getLista("/proyectos", "listaProyectos", listaProyectos);
    if ($.cookie("rol") == "superadmin") {
        listaMisProyectos = listaProyectos;
        listaMisUsuarios = listaUsuarios;
    } else if ($.cookie("rol") == "admin") {
        $.getLista("/usuarios/" + $.cookie("uid") + "/proyectos?administrados", "listaProyectos", listaMisProyectos);
        $.getLista("/usuarios/" + $.cookie("uid") + "/usuarios", "listaUsuarios", listaMisUsuarios);
    } else {
        $.getLista("/usuarios/" + $.cookie("uid") + "/proyectos", "listaProyectos", listaMisProyectos);
        listaMisUsuarios = new Array($.cookie("uid"));
    }

    /* Imprime el menú de la derecha con las listas "MIS" en el HTML */
    $.printLista(listaMisUsuarios, "ul#listaUsuarios", "/usuarios");
    $.printLista(listaMisProyectos, "ul#listaProyectos", "/proyectos");

    /* Funciones para la lista de sugerencias del searchbox */
    var miListaUsuariosProyectos = [];
    $.merge(miListaUsuariosProyectos, listaMisUsuarios);
    $.merge(miListaUsuariosProyectos, listaMisProyectos);
    $("input.searchinput").autocomplete({
        source: miListaUsuariosProyectos
    });

    /* Otras cosas a inicializar */
    $('p#welcome').append("Bienvenido " + $.cookie("uid"));
    $('a.miPanel').attr('href', '/usuarios/' + $.cookie('uid'));
}







/**
 *  HANDLER DE EVENTOS
 */
$(function()
{

    /**
     * Esta función comprueba el contenido de la caja de búsqueda y, en función de
     * qué se haya introducido, allí redirige.
     */
    $("#boton_busqueda").click(function() {
        var clave = $('#caja_busqueda').val();
        if (clave == "" || clave == null)
            return false;
        var encontrado = false;

        $.each(listaMisUsuarios, function(key, val) {
            if (val == clave) {
                window.location = "/usuarios/" + clave;
                encontrado = true;
            }
        });
        $.each(listaMisProyectos, function(key, val) {
            if (val == clave) {
                window.location = "/proyectos/" + clave;
                encontrado = true;
            }
        });
        if (encontrado == false)
            $.printDialog("Error", "<h3>" + clave + " es inaccesible</h3>" +
                "<p>El usuario o proyecto: <b>" + clave + "</b> no existe en SidelabCode o no tienes permisos para su gesti&oacute;n</p>");
        return false;
    });

});