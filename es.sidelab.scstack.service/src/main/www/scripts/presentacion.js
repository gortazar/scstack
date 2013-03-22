/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: presentacion.js
 * Autor: Arek Klauza
 * Fecha: Abril 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */



/**
 * FUNCIONES DE PRESENTACIÓN VISUAL
 */

/**
 * Función encargada de dibujar sobre un panel de formulario HTML los datos de
 * un usuario concreto.
 */
jQuery.printUsuario = function(usuario, tagHtml) {
    $(tagHtml + ' > div > input#uidfijo').val(usuario.uid);
    $(tagHtml + ' > div > input#nombre').val(usuario.nombre);
    $(tagHtml + ' > div > input#apellidos').val(usuario.apellidos);
    $(tagHtml + ' > div > input#email').val(usuario.email);
}


/**
 * Función encargada de dibujar sobre un panel de formulario HTML los datos de
 * un proyecto concreto.
 */
jQuery.printProyecto = function(proyecto, tagHtml) {
    $(tagHtml + ' > div > input#cnfijo').val(proyecto.cn);
    $(tagHtml + ' > div > textarea#descripcion').val(proyecto.descripcion);
    if (proyecto.repositorios.length > 0) {
        var i;
        for (i = 0; i < proyecto.repositorios.length; i++) {
            if (proyecto.repositorios[i].tipo == proyecto.defaultRepositorio)
                $('<option selected>' + proyecto.repositorios[i].tipo + '</option>').appendTo(tagHtml + " > div > select#defaultRepositorio");
            else
                $('<option>' + proyecto.repositorios[i].tipo + '</option>').appendTo(tagHtml + " > div > select#defaultRepositorio");
        }
    } else {
        $('<option>Proyecto sin repositorios</option>').appendTo(tagHtml + " > div > select#defaultRepositorio");
    }    
}



/**
 * Esta función imprime sobre la etiqueta HTML el contenido de la lista
 * @param tagHtml Etiqueta de HTML <ul> donde queremos que se haga append de la lista
 * @param lista Lista que se quiere imprimir en el HTML
 * @param rootUri Comienzo de la URI para los enlaces de la lista
 */
jQuery.printLista = function(lista, tagHtml, rootUri) {
    $.each(lista, function(key, val) {
        if (rootUri == null)
            $('<li><a href="" >' + val + '</a></li>').appendTo(tagHtml);
        else
            $('<li><a href="' + rootUri + "/" + val + '" >' + val + '</a></li>').appendTo(tagHtml);
    })
}



jQuery.printTabla1Columna = function(listaCol, tagHtml) {
    var i;
    for (i = 0; i < listaCol.length; i++) {
        if (i%2 == 0) {
            $('<tr class="alt"><td>' + listaCol[i] + '</td></tr>').appendTo(tagHtml);
        } else {
            $('<tr><td>' + listaCol[i] + '</td></tr>').appendTo(tagHtml);
        }
    }
    /* Expandir fondo de la página acorde al tamaño de la lista */
    if (listaCol.length > 10) {
        var pixels = 500 + (35 * (listaCol.length-10));
        $('#content').css('min-height', pixels + 'px');
    }
}



/**
 * Función encargada de imprimir el contenido de dos lista Javascript en una
 * tabla HTML determinada de la página.
 * @param listaCol1 Array de Javascript para la primera columna (mismo tamaño que listaCol2)
 * @param listaCol2 Array de Javascript para la segunda columna (mismo tamaño que listaCol1)
 * @param tagHtml Etiqueta HTML <table> donde introducir el contenido
 */
jQuery.printTabla2Columnas = function(listaCol1, listaCol2, tagHtml) {
    var i;
    for (i = 0; i < listaCol1.length; i++) {
        if (i%2 == 0) {
            $('<tr class="alt"><td>' + listaCol1[i] + '</td><td>' + listaCol2[i] + '</td></tr>').appendTo(tagHtml);
        } else {
            $('<tr><td>' + listaCol1[i] + '</td><td>' + listaCol2[i] + '</td></tr>').appendTo(tagHtml);
        }
    }
    /* Expandir fondo de la página acorde al tamaño de la lista */
    if (listaCol1.length > 10) {
        var pixels = 500 + (35 * (listaCol1.length-10));
        $('#content').css('min-height', pixels + 'px');
    }
}



/**
 * Función encargada de imprimir la tabla de repositorios de un proyecto determinado.
 * @param listaRepos Array de Javascript con la lista de objetos repositorio
 * @param tagHtml Etiqueta HTML <table> donde introducir el contenido
 */
jQuery.printTablaRepositorios = function(listaRepos, tagHtml) {
    var i;
    for (i = 0; i < listaRepos.length; i++) {
        var tipoRepo = listaRepos[i].tipo;
        var enlace = servicios.baseHost;
        if (tipoRepo == "GIT")
            enlace += servicios.pathGIT + "/" + cn;
        else
            enlace += servicios.pathSVN + "/" + cn;

        var tr = "<tr>";
        if (i%2 == 0)
            tr = '<tr class="alt">';

        var enlacePublico = "No";
        if (listaRepos[i].esPublico == true)
            enlacePublico = '<a href="' + servicios.baseHost + servicios.pathReposPublicos + '/' + cn + '" target="_blank">URL</a>';
        $(tr + '<td>' + tipoRepo + '</td><td><a href="'+ enlace +'" target="_blank">'+enlace +'</a></td><td>' + listaRepos[i].ruta + '</td><td>' + enlacePublico + '</td></tr>').appendTo(tagHtml);
        
    }
}



/**
 * Función encargada de imprimir y reutilizar el cuadro de diálogo de la interfaz
 * @param titulo Título de la ventana
 * @param msg Mensaje a mostrar en la ventana de diálogo.
 */
jQuery.printDialog = function(titulo, msg) {
    $.printDialog(titulo, msg, null);
}



/**
 * Función encargada de imprimir y reutilizar el cuadro de diálogo de la interfaz
 * y de redirigir a otra página tras aceptar el dialog.
 * @param titulo Título de la ventana
 * @param msg Mensaje a mostrar en la ventana de diálogo.
 * @param redireccion URL a la que queremos redirigir tras aceptar el dialog
 */
jQuery.printDialog = function(titulo, msg, redireccion) {
    $('.ui-dialog').remove();
    $('#dialog').remove();
    $('body').append('<div id="dialog">' + msg + '</div>');
    $("#dialog").dialog({
        buttons: [{
            text: "Ok",
            click: function() {
                $(this).dialog("close");
                if (redireccion != null)
                    window.location = redireccion;
            }
        }],
        show: "slide",
        hide: "fadeOut",
        title: titulo,
        width: "500px",
        modal: true
    });
}



/**
 * Función encargada de dibujar un dialog de espera con un gif animado y un mensaje
 * personalizado.
 * @param msg Mensaje a mostrar en el dialog
 */
jQuery.printWaitDialog = function(msg) {
    $('.ui-dialog').remove();
    $('#dialog').remove();
    $('body').append('<div id="dialog"><p>&nbsp;</p><h3 class="center"><img src="/images/loading2.gif" /><p class="center">' + msg + '</p></div>');
    $("#dialog").dialog({
        title: "Procesando",
        show: "slide",
        hide: "fadeOut",
        width: "500px",
        modal: true
    });
}



/**
 * Dibuja un cuadro de diálogo de confirmación con 2 botones, uno para confirmar
 * y otro para cancelar la selección.
 * @param msg Mensaje a mostrar en el dialog
 */
jQuery.printConfirmDialog = function(msg) {
    $('.ui-dialog').remove();
    $('#dialog').remove();
    $('body').append('<div id="dialog">' + msg + '</div>');
    $("#dialog").dialog({
        title: "Confirmaci&oacute;n necesaria",
        show: "slide",
        hide: "fadeOut",
        buttons: {
            Aceptar: function() {
                $( this ).dialog( "close" );
                return true;
            },
            Cancelar: function() {
                $( this ).dialog( "close" );
                return false;
            }
        },
        width: "500px",
        modal: true
    });
}



jQuery.printListaSelect = function(lista, tagHtml) {
    var i;
    for (i = 0; i < lista.length; i++) {
        $('<option>' + lista[i] + '</option>').appendTo(tagHtml);
    }
}


jQuery.printListaSelectRepos = function(lista, tagHtml) {
    var i;
    for (i = 0; i < lista.length; i++) {
        $('<option>' + lista[i].tipo + '</option>').appendTo(tagHtml);
    }
}



/**
 * Slider del menú de la izquierda de mis usuarios y mis proyectos.
 */
$(document).ready(function() {
    $("#nav > li > a.collapsed + ul").slideToggle("medium");
    $("#nav > li > a").click(function() {
        $(this).toggleClass("expanded").toggleClass("collapsed").find("+ ul").slideToggle("medium");
        return false;
    });
    $(".toolboxdrop").click( function() {
        if ($("#openCloseIdentifier").is(":hidden")) {
            $("#slider").animate({
                marginTop: "-150px"
            }, 500 );
            $("#openCloseIdentifier").show();
        } else {
            $("#slider").animate({
                marginTop: "0px"
            }, 500 );
            $("#openCloseIdentifier").hide();
        }
        return false;
    });
});








/**
 *  HANDLER DE EVENTOS
 */
$(function() {

    /**
    * Imprime un cuadro de diálogo con la dirección de los superadmins de SidelabCode
    */
    $('a#contacto').click(function() {
        $.printDialog("Informaci&oacute;n de contacto",
            "<h3>Superadmin de SidelabCode</h3><p>Para poder contactar con los superadmins de SidelabCode, manda un eMail a <a href='mailto:sidelabcode@sidelab.es'>sidelabcode@sidelab.es</a></p>");
        return false;
    });



    /***************************** Gestión del formulario de alta de usuarios */

    $('div.inputboxes > input#uid').blur(function() {
        var val = $(this).val();
        if (val == "" || val == null) {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        } else if (val.match('[_a-z0-9]+') != val) {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        } else {
            var match = false;
            $.each(listaUsuarios, function(key, value) {
                if (val.toLowerCase() == value.toLowerCase()) {
                    $(this).addClass('errorbox');
                    match = true;
                }
            });
            if (match == false) {
                $(this).removeClass('errorbox');
                $(this).addClass('correctbox');
            } else {
                $(this).addClass('errorbox');
                $(this).removeClass('correctbox');
            }
        }
    });
    $('div.inputboxes > input#nombre, div.inputboxes > input#apellidos').blur(function() {
        var val = $(this).val();
        if (val == "" || val == null) {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        } else {
            $(this).removeClass('errorbox');
            $(this).addClass('correctbox');
        }
    });
    $('div.inputboxes > input#pass, div.inputboxes > input#passActual').blur(function() {
        var val = $(this).val();
        if (val.length < 5 || val == "" || val == null) {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        } else {
            $(this).removeClass('errorbox');
            $(this).addClass('correctbox');
        }
    });
    $('div.inputboxes > input#email').blur(function() {
        var val = $(this).val();
        if (val == "" || val == null) {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        } else if (val.match('^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,4}$') != val) {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        } else {
            var match = false;
            $.each(listaUsuariosEmails, function(key, value) {
                if (val.toLowerCase() == value.toLowerCase()) {
                    $(this).addClass('errorbox');
                    match = true;
                }
            });
            if (match == false) {
                $(this).removeClass('errorbox');
                $(this).addClass('correctbox');
            } else {
                $(this).addClass('errorbox');
                $(this).removeClass('correctbox');
            }
        }
    });
    $('div.inputboxes > input#pass2').blur(function() {
        var pass2 = $(this).val();
        var pass = $('input#pass').val();

        if (pass == pass2 && pass != "" && pass != null) {
            $(this).removeClass('errorbox');
            $(this).addClass('correctbox');
        } else {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        }
    });
    $('input#uid, input#nombre, input#apellidos, input#pass, input#pass2, input#email').blur(function() {
        if ($('input#uid').hasClass('correctbox') && $('input#nombre').hasClass('correctbox') && $('input#apellidos').hasClass('correctbox')
            && $('input#pass').hasClass('correctbox') && $('input#pass2').hasClass('correctbox') && $('input#email').hasClass('correctbox')) {
            $('input#submitUsuario').addClass('btn');
            $('input#submitUsuario').removeClass('btnalt');
        } else {
            $('input#submitUsuario').removeClass('btn');
            $('input#submitUsuario').addClass('btnalt');
        }
    });
    $('#borrarCamposUsuario').click(function() {
        $('input#uid, input#nombre, input#apellidos, input#pass, input#pass2, input#email').val("");
        $('input#uid, input#nombre, input#apellidos, input#pass, input#pass2, input#email').removeClass("errorbox");
        $('input#uid, input#nombre, input#apellidos, input#pass, input#pass2, input#email').removeClass("correctbox");
        $('input#submitUsuario, input#submitEditarUsuario').removeClass('btn');
        $('input#submitUsuario, input#submitEditarUsuario').addClass('btnalt');
    });




    /************************** Gestión del formulario de edición de usuarios */
    $('a#editarUsuario, #reestablecerCamposUsuario').click(function(){
        $.printUsuario(usuario, "#formEditarUsuario");
        if ($.cookie("rol") == "superadmin")
            $('input#nombre, input#apellidos, input#email, input#pass, input#pass2, input#checkModificarPass').removeAttr("disabled");
        else
            $('input#nombre, input#apellidos, input#email, input#pass, input#pass2, input#passActual, input#checkModificarPass').removeAttr("disabled");
        $('input#nombre, input#apellidos, input#email').addClass("correctbox");
        $('input#submitEditarUsuario').addClass("btn").removeClass("btnalt");
        $('input#checkModificarPass').attr("checked", false);
        $('#formEditarUsuario > div.oculto').slideUp("slow");
        return false;
    });
    $('input#checkModificarPass').click(function() {
        if ($(this).is(':checked')) {
            $('#formEditarUsuario > div.oculto').slideDown("slow");
            $('#content').animate({
                height: "550px"
            }, "slow", function() {});
            $('input#submitEditarUsuario').addClass("btnalt").removeClass("btn");
        } else {
            $('#formEditarUsuario > div.oculto').slideUp("slow");
            $('#content').animate({
                height: "500px"
            }, "slow", function() {});
            $('input#pass, input#pass2').val("");
            $('input#pass, input#pass2').removeClass("errorbox").removeClass("correctbox");
        }
    });
    $('input#nombre, input#apellidos, input#email, input#pass, input#pass2, input#passActual').blur(function() {
        if ($('input#nombre').hasClass('correctbox') && $('input#apellidos').hasClass('correctbox') && $('input#email').hasClass('correctbox')) {
            if ($('input#checkModificarPass').is(':checked')) {
                if (!$('input#pass').hasClass('correctbox') || !$('input#pass2').hasClass('correctbox') || !$('input#passActual').hasClass('correctbox')) {
                    $('input#submitEditarUsuario').addClass("btnalt").removeClass("btn");
                    return;
                }
            }
            $('input#submitEditarUsuario').addClass("btn").removeClass("btnalt");
        } else {
            $('input#submitEditarUsuario').addClass("btnalt").removeClass("btn");
        }
    });
    $('a#expandirAdmin').click(function() {
        $('ul#proyectosAdmin').slideToggle("slow");
        return false;
    });
    $('a#expandirParticip').click(function() {
        $('ul#proyectosParticip').slideToggle("slow");
        return false;
    });



    /**************************** Gestión del formulario de alta de proyectos */
    $('form#crearProyecto input#checkRepositorio').click(function() {
        if ($(this).is(":checked")) {
            $('form#crearProyecto > div.oculto > input, form#crearProyecto > div.oculto > select').removeAttr("disabled");
            $('form#crearProyecto #rutaRepo').attr("disabled", true);
            $('#crearProyecto > div.oculto').slideDown("slow");
            $('#content').animate({
                height: "600px"
            }, "slow", function() {});
        } else {
            $('form#crearProyecto #tipoRepositorio, form#crearProyecto #esRepoPublico, form#crearProyecto #rutaRepo').attr("disabled", true);
            $('form#crearProyecto > div.oculto').slideUp("slow");
            $('#content').animate({
                height: "550px"
            }, "slow", function() {});
            $('form#crearProyecto #rutaRepo').val("");
        }
    });
    $('form#crearProyecto #borrarCamposProyecto').click(function() {
        $('form#crearProyecto #cn, form#crearProyecto #descripcion, form#crearProyecto #rutaRepo').val("");
        $('form#crearProyecto #cn, form#crearProyecto #rutaRepo, form#crearProyecto #descripcion').removeClass("correctbox");
        $('form#crearProyecto #cn, form#crearProyecto #rutaRepo, form#crearProyecto #descripcion').removeClass("errorbox");
        $('input#submitProyecto').removeClass('btn').addClass('btnalt');
        return false;
    });
    $('form#crearProyecto input#cn').blur(function() {
        var val = $(this).val();
        if (val == "" || val == null) {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        } else if (val.match('[a-zA-Z0-9]+') != val) {
            $(this).addClass('errorbox');
            $(this).removeClass('correctbox');
        } else {
            var match = false;
            $.each(listaProyectos, function(key, value) {
                if (val.toLowerCase() == value.toLowerCase()) {
                    $(this).addClass('errorbox');
                    match = true;
                }
            });
            if (match == false) {
                $(this).removeClass('errorbox');
                $(this).addClass('correctbox');
            } else {
                $(this).addClass('errorbox');
                $(this).removeClass('correctbox');
            }
        }
    });
    $('textarea#descripcion').keypress(function() {
        if ($(this).val().length > 200)
            $(this).val($(this).val().substr(0, 200));
    });
    $('form#crearProyecto textarea#descripcion, form#editarProyecto textarea#descripcion').blur(function() {
        if ($(this).val().length > 200)
            $(this).val($(this).val().substr(0, 200));
    });
    $('form#crearProyecto #descripcion').blur(function() {
        if ($(this).val().length > 0) {
            $(this).removeClass('errorbox').addClass('correctbox');
        } else {
            $(this).addClass('errorbox').removeClass('correctbox');
        }
    });
    $('form#crearProyecto #cn, form#crearProyecto #descripcion, #crearProyecto input#checkRepositorio').blur(function() {
        if ($('form#crearProyecto #cn').hasClass('correctbox') && $('form#crearProyecto #descripcion').hasClass('correctbox')) {
            $('input#submitProyecto').addClass('btn').removeClass('btnalt');
        } else {
            $('input#submitProyecto').removeClass('btn').addClass('btnalt');
        }
    });
    $('form#crearProyecto #checkRutaDefault, form#crearRepo #checkRutaDefault').click(function() {
        if ($(this).is(":checked")) {
            $('form#crearProyecto #rutaRepo, form#crearRepo #rutaRepo').val("");
            $('form#crearProyecto #rutaRepo, form#crearRepo #rutaRepo').attr("disabled", true);
        } else {
            $('form#crearProyecto #rutaRepo, form#crearRepo #rutaRepo').val("");
            $('form#crearProyecto #rutaRepo, form#crearRepo #rutaRepo').attr("disabled", false);
        }
    });





    /************************* Gestión del formulario de edición de proyectos */
    $('form#editarProyecto input#borrarCamposProyecto').click(function() {
        $('form#editarProyecto #descripcion').val("");
        $('form#editarProyecto #descripcion').removeClass("correctbox");
        $('form#editarProyecto #descripcion').removeClass("errorbox");
        $('input#submitEditarProyecto').removeClass('btn').addClass('btnalt');
        return false;
    });
    $('a#editarProyecto, form#editarProyecto input#reestablecerCamposProyecto').click(function() {
        $('form#editarProyecto select option').remove();
        $.printProyecto(proyecto, "form#editarProyecto");
        $('form#editarProyecto textarea#descripcion, form#editarProyecto select').removeAttr("disabled");
        $('form#editarProyecto textarea#descripcion, form#editarProyecto select').addClass("correctbox");
        $('input#submitEditarProyecto').addClass("btn").removeClass("btnalt");
        return false;
    });

    $('form#editarProyecto textarea#descripcion').blur(function() {
        if ($(this).val().length > 0) {
            $(this).addClass("correctbox");
            $(this).removeClass("errorbox");
            $('input#submitEditarProyecto').addClass("btn").removeClass("btnalt");
        } else {
            $(this).addClass("errorbox");
            $(this).removeClass("correctbox");
            $('input#submitEditarProyecto').addClass("btnalt").removeClass("btn");
        }
    });

});
