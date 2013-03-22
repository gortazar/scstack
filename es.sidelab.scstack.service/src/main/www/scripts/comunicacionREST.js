/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * Fichero: comunicacionREST.js
 * Autor: Arek Klauza
 * Fecha: Abril 2011
 * Revisión: -
 * Versión: 1.0
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */




/**
 * Función que recupera una lista del servicio REST, devolviéndola como paso por
 * referencia de la lista de entrada.
 * @param uriRest URI del servicio REST donde se sirve la información
 * @param keyJson nombre del campo del objeto JSON que contiene la lista
 * @param lista Lista donde se guardará la lista recuperada de REST
 */
jQuery.getLista = function(uriRest, keyJson, lista) {
    $.getJSON(uriRest, function(data) {
        $.each(data, function(key, val) {
            if (key == keyJson) {
                $.each(val, function(key, val) {
                    lista.push(val);
                })
            }
        });
    });
}


jQuery.getUsuario = function(uid) {
    $.getJSON("/usuarios/" + uid, function(data) {
        usuario = data;
    })
}


jQuery.getProyecto = function(cn) {
    $.getJSON("/proyectos/" + cn, function(data) {
        proyecto = data;
    })
}


jQuery.getServicios = function() {
    $.getJSON("/servicios", function(data) {
        servicios = data;
    })
}






/**
 *  HANDLER DE EVENTOS
 */
$(function()
{
    /**
     * Función que realiza la comprobación de login de usuario y redirige a la
     * página correspondiente. Hay que hacerla a bajo nivel porque no se vale del
     * ajaxSetup() de la inicialización.
     */
    $("#login_button").click(function() {
        $.ajax({
            async:false,
            cache:false,
            dataType: 'xml',
            type: 'GET',
            url: "/login",
            username: $('#username').val(),
            password: $('#password').val(),
            success:  function(data, status, response){
                $.cookie("rol", response.responseText);
                $.cookie("uid", $('#username').val());
                $.cookie("pass", $('#password').val());
                if (response.responseText == "superadmin")
                    window.location = "/usuarios/";
                else
                    window.location = "/usuarios/" + $.cookie("uid");
            },
            error:function(response){
                $.printDialog("Error", response.responseText);
            }
        });
        return false;
    });


    /**
     * Función encargada de crear un nuevo usuario
     */
    $("input#submitUsuario").click(function() {
        if ($(this).hasClass('btn')) {
            $.ajax({
                async:true,
                type: 'POST',
                url: "/usuarios",
                data: $("#crearUsuario").serialize(),
                beforeSend: function(html) {
                    $.printWaitDialog("Creando usuario... un momento por favor");
                },
                success: function(response){
                    $.printDialog("Confirmaci&oacute;n", "<h3>Usuario creado satisfactoriamente</h3>", "/usuarios");
                },
                error:function(response){
                    $.printDialog("Error", response.responseText);
                }
            });
        }
        return false;
    });


    /**
     * Elimina al usuario en cuya página nos encontremos, mostrando primero un
     * diálogo de confirmación
     */
    $('a#eliminarUsuario').click(function() {
        /* Mostar un dialog de confirmación */
        var msg = "&iquest;Seguro que quieres eliminar a <b>" + uid + "</b>?";
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
                    $.ajax({
                        async:true,
                        type: 'DELETE',
                        url: "/usuarios/" + uid,
                        beforeSend: function(html) {
                            $.printWaitDialog("Eliminando usuario... un momento por favor");
                        },
                        success: function(response){
                            $.printDialog("Confirmaci&oacute;n", "<h3>El usuario ha sido eliminado satisfactoriamente</h3>", "/usuarios");
                        },
                        error:function(response){
                            $.printDialog("Error", response.responseText);
                        }
                    });
                },
                Cancelar: function() {
                    $( this ).dialog( "close" );
                    return false;
                }
            },
            width: "500px",
            modal: true
        });
        return false;
    });



    /**
     * Función encargada de editar los datos personales de un usuario
     */
    $('input#submitEditarUsuario').click(function() {
        if ($(this).hasClass('btn')) {
            $('input#uidfijo').removeAttr("disabled");

            /* Cuando el editor es administrador o usuario, debe proveer la contraseña actual,
             * cuando es superadmin no es necesario. Tampoco cuando no se vaya a editar la pass */
            var userUid = uid;
            var passActual = passActual = $('input#passActual').val();
            if ($.cookie("rol") == "superadmin" || !$('input#checkModificarPass').is(':checked')) {
                userUid = $.cookie("uid");
                passActual = $.cookie("pass");
            }

            $.ajax({
                async:true,
                type: 'PUT',
                url: "/usuarios/" + uid,
                data: $("#formEditarUsuario").serialize(),
                username: userUid,
                password: passActual,
                beforeSend: function(html) {                    
                    $.printWaitDialog("Editando usuario... un momento por favor");
                },
                success: function(response) {
                    /* Si es el propio usuario quien ha cambiado la contraseña deberá hacer login de nuevo */
                    if ($('input#checkModificarPass').is(':checked') && uid == $.cookie("uid")) {
                        $.printDialog("Confirmaci&oacute;n", "<h3>Usuario modificado satisfactoriamente</h3><p>Como se ha cambiado la contrase&ntilde;a, deber&aacute;s volver a hacer login con la nueva contrase&ntilde;a</p>", "/logout");
                    }
                    else {
                        $.ajaxSetup({
                            async:false,
                            cache:false,
                            username: $.cookie("uid"),
                            password: $.cookie("pass")
                        });
                        $.printDialog("Confirmaci&oacute;n", "<h3>Usuario modificado satisfactoriamente</h3>", "");
                    }
                },
                error:function(response){
                    $.printDialog("Error", response.responseText);
                }
            });
        }
        return false;
    });



    /**
     * Función encargada de bloquear a un usuario de SidelabCode
     */
    $('a#bloquearUsuario').click(function() {
        /* Mostar un dialog de confirmación */
        var msg = "&iquest;Seguro que quieres bloquear el acceso a SidelabCode del usuario <b>" + uid + "</b>?";
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
                    $.ajax({
                        async:true,
                        type: 'DELETE',
                        url: "/usuarios/" + uid + "/activado",
                        beforeSend: function(html) {
                            $.printWaitDialog("Bloqueando usuario... un momento por favor");
                        },
                        success: function(response){
                            $.printDialog("Confirmaci&oacute;n", "<h3>El usuario ha sido bloqueado indefinidamente</h3>", "");
                        },
                        error:function(response){
                            $.printDialog("Error", response.responseText);
                        }
                    });
                },
                Cancelar: function() {
                    $( this ).dialog("close");
                    return false;
                }
            },
            width: "500px",
            modal: true
        });
        return false;
    });



    /**
     * Función que indica mediante un dialog cómo desbloquear un usuario bloqueado
     * mediante el formulario de edición de usuarios.
     */
    $('a#desbloquearUsuario').click(function() {
        $.printDialog("Informaci&oacute;n", "<p>Para desbloquear al usuario, utiliza el panel de edici&oacute;n, introduce una nueva contrase&ntilde;a para el usuario y  pulsa el bot&oacute;n 'Guardar cambios'</p> ");
        $.printUsuario(usuario, "#formEditarUsuario");
        $('input#nombre, input#apellidos, input#email, input#pass, input#pass2, input#checkModificarPass').removeAttr("disabled");
        $('input#nombre, input#apellidos, input#email').addClass("correctbox");
        $('input#submitEditarUsuario').addClass("btnalt").removeClass("btn");
        $('input#checkModificarPass').attr("checked", true);
        $('#formEditarUsuario > div.oculto').slideDown("slow");
        return false;
    });



    /**
     * Función encargada de crear un nuevo proyecto
     */
    $("#crearProyecto input#submitProyecto").click(function() {
        if ($(this).hasClass('btn')) {
            $.ajax({
                async:true,
                type: 'POST',
                url: "/proyectos",
                data: $("#crearProyecto").serialize(),
                beforeSend: function(html) {
                    $.printWaitDialog("Creando proyecto... un momento por favor");
                },
                success: function(response){
                    $.printDialog("Confirmaci&oacute;n", "<h3>Proyecto creado satisfactoriamente</h3>", "/proyectos");
                },
                error:function(response){
                    $.printDialog("Error", response.responseText);
                }
            });
        }
        return false;
    });



    /**
     * Función encargada de editar los datos de un proyecto determinado
     */
    $('form#editarProyecto input#submitEditarProyecto').click(function() {
        if ($(this).hasClass('btn')) {
            $('input#cnfijo').removeAttr("disabled");
            $.ajax({
                async:true,
                type: 'PUT',
                url: "/proyectos/" + cn,
                data: $("form#editarProyecto").serialize(),
                beforeSend: function(html) {
                    $.printWaitDialog("Editando proyecto... un momento por favor");
                },
                success: function(response) {
                    $.printDialog("Confirmaci&oacute;n", "<h3>Proyecto modificado satisfactoriamente</h3>", "");
                },
                error:function(response){
                    $.printDialog("Error", response.responseText);
                }
            });
        }
        return false;
    });



    /**
     * Elimina el proyecto en cuya página nos encontremos, mostrando primero un
     * diálogo de confirmación
     */
    $('a#eliminarProyecto').click(function() {
        /* Mostar un dialog de confirmación */
        var msg = "&iquest;Seguro que quieres eliminar el proyecto <b>" + cn + "</b>?";
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
                    $.ajax({
                        async:true,
                        type: 'DELETE',
                        url: "/proyectos/" + cn,
                        beforeSend: function(html) {
                            $.printWaitDialog("Eliminando proyecto... un momento por favor");
                        },
                        success: function(response){
                            $.printDialog("Confirmaci&oacute;n", "<h3>El proyecto ha sido eliminado satisfactoriamente</h3>", "/proyectos");
                        },
                        error:function(response){
                            $.printDialog("Error", response.responseText);
                        }
                    });
                },
                Cancelar: function() {
                    $( this ).dialog( "close" );
                    return false;
                }
            },
            width: "500px",
            modal: true
        });
        return false;
    });



    /**
     * Crea un nuevo repositorio en el proyecto actual, mostrando para ello un
     * formulario en un dialog.
     */
    $('a#crearRepositorio').click(function() {
        /* Mostar un dialog de confirmación */
        $("form#crearRepo").dialog({
            title: "Datos necesarios",
            show: "slide",
            hide: "fadeOut",
            buttons: {
                Crear: function() {
                    $( this ).dialog( "close" );
                    $.ajax({
                        async:true,
                        type: 'PUT',
                        url: "/proyectos/" + cn + "/repos/" + $('form#crearRepo select#tipoRepositorio').val(),
                        data: $("form#crearRepo").serialize(),
                        beforeSend: function(html) {
                            $.printWaitDialog("Creando repositorio... un momento por favor");
                        },
                        success: function(response){
                            $.printDialog("Confirmaci&oacute;n", "<h3>El repositorio ha sido creado satisfactoriamente</h3>", "");
                        },
                        error:function(response){
                            $.printDialog("Error", response.responseText);
                        }
                    });
                },
                Cancelar: function() {
                    $( this ).dialog( "close" );
                    return false;
                }
            },
            width: "500px",
            modal: true
        });
        return false;
    });



    /**
     * Elimina el repositorio del proyecto en cuya página nos encontremos,
     * mostrando primero un diálogo de confirmación
     */
    $('a#eliminarRepositorio').click(function() {
        if ($('form#eliminarRepo select#tipo').val() == null)
            $.printDialog("Informaci&oacute;n", "<h3>No hay repositorios</h3><p>Este proyecto no tiene repositorios para poder ser eliminados</p>");
        else {
            /* Mostar un dialog de confirmación */
            $("form#eliminarRepo").dialog({
                title: "Confirmaci&oacute;n necesaria",
                show: "slide",
                hide: "fadeOut",
                buttons: {
                    Eliminar: function() {
                        $( this ).dialog( "close" );
                        $.ajax({
                            async:true,
                            type: 'DELETE',
                            url: "/proyectos/" + cn + "/repos/" + $('form#eliminarRepo select#tipo').val(),
                            beforeSend: function(html) {
                                $.printWaitDialog("Eliminando repositorio... un momento por favor");
                            },
                            success: function(response){
                                $.printDialog("Confirmaci&oacute;n", "<h3>El repositorio ha sido eliminado satisfactoriamente</h3>", "");
                            },
                            error:function(response){
                                $.printDialog("Error", response.responseText);
                            }
                        });
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
        return false;
    });



    /**
     * Añadir un administrador nuevo a un proyecto determinado.
     */
    $('a#addAdmin').click(function() {
        /* Mostar un dialog de confirmación */
        $("form#addAdmin").dialog({
            title: "Seleccione usuario",
            show: "slide",
            hide: "fadeOut",
            buttons: {
                Agregar: function() {
                    $( this ).dialog( "close" );
                    $.ajax({
                        async:true,
                        type: 'PUT',
                        url: "/proyectos/" + cn + "/admins/" + $('form#addAdmin select#uid').val(),
                        data: $("form#addAdmin").serialize(),
                        beforeSend: function(html) {
                            $.printWaitDialog("Añadiendo administrador... un momento por favor");
                        },
                        success: function(response){
                            $.printDialog("Confirmaci&oacute;n", "<h3>Se ha añadido al administrador satisfactoriamente al proyecto</h3>", "");
                        },
                        error:function(response){
                            $.printDialog("Error", response.responseText);
                        }
                    });
                },
                Cancelar: function() {
                    $( this ).dialog( "close" );
                    return false;
                }
            },
            width: "500px",
            modal: true
        });
        return false;
    });


    /**
     * Añadir un nuevo miembro a un proyecto determinado.
     */
    $('a#addMiembro').click(function() {
        /* Mostar un dialog de confirmación */
        $("form#addMiembro").dialog({
            title: "Seleccione usuario",
            show: "slide",
            hide: "fadeOut",
            buttons: {
                Agregar: function() {
                    $( this ).dialog( "close" );
                    $.ajax({
                        async:true,
                        type: 'PUT',
                        url: "/proyectos/" + cn + "/miembros/" + $('form#addMiembro select#uid').val(),
                        data: $("form#addMiembro").serialize(),
                        beforeSend: function(html) {
                            $.printWaitDialog("Añadiendo miembro... un momento por favor");
                        },
                        success: function(response){
                            $.printDialog("Confirmaci&oacute;n", "<h3>Se ha añadido al miembro satisfactoriamente al proyecto</h3>", "");
                        },
                        error:function(response){
                            $.printDialog("Error", response.responseText);
                        }
                    });
                },
                Cancelar: function() {
                    $( this ).dialog( "close" );
                    return false;
                }
            },
            width: "500px",
            modal: true
        });
        return false;
    });



    /**
     * Borra a un administrador de un proyecto determinado.
     */
    $('a#delAdmin').click(function() {
        /* Mostar un dialog de confirmación */
        $("form#delAdmin").dialog({
            title: "Seleccione usuario",
            show: "slide",
            hide: "fadeOut",
            buttons: {
                Borrar: function() {
                    $( this ).dialog( "close" );
                    $.ajax({
                        async:true,
                        type: 'DELETE',
                        url: "/proyectos/" + cn + "/admins/" + $('form#delAdmin select#uid').val(),
                        beforeSend: function(html) {
                            $.printWaitDialog("Borrando administrador... un momento por favor");
                        },
                        success: function(response){
                            $.printDialog("Confirmaci&oacute;n", "<h3>Se ha borrado al administrador satisfactoriamente del proyecto</h3>", "");
                        },
                        error:function(response){
                            $.printDialog("Error", response.responseText);
                        }
                    });
                },
                Cancelar: function() {
                    $( this ).dialog( "close" );
                    return false;
                }
            },
            width: "500px",
            modal: true
        });
        return false;
    });


    /**
     * Borra a un administrador de un proyecto determinado.
     */
    $('a#delMiembro').click(function() {
        /* Mostar un dialog de confirmación */
        $("form#delMiembro").dialog({
            title: "Seleccione usuario",
            show: "slide",
            hide: "fadeOut",
            buttons: {
                Borrar: function() {
                    $( this ).dialog( "close" );
                    $.ajax({
                        async:true,
                        type: 'DELETE',
                        url: "/proyectos/" + cn + "/miembros/" + $('form#delMiembro select#uid').val(),
                        beforeSend: function(html) {
                            $.printWaitDialog("Borrando miembro... un momento por favor");
                        },
                        success: function(response){
                            $.printDialog("Confirmaci&oacute;n", "<h3>Se ha borrado al miembro satisfactoriamente del proyecto</h3>", "");
                        },
                        error:function(response){
                            $.printDialog("Error", response.responseText);
                        }
                    });
                },
                Cancelar: function() {
                    $( this ).dialog( "close" );
                    return false;
                }
            },
            width: "500px",
            modal: true
        });
        return false;
    });
    
});