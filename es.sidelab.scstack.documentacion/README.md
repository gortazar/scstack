# Proceso de Desarrollo

Documentación del Proceso de Desarrollo de SidelabCode Stack.

## Requerimientos

* Maven 3.0.x

## Estructura

```shell
es.sidelab.scstack.documentacion
├── documentation
│   ├── 000-index.textile
│   ├── 001-git-introduccion.textile
│   ├── 002-proceso-git.textile
│   ├── 003-eclipse.textile
│   ├── 004-jenkins.textile
│   ├── images\
│   └── index.txt
├── build-doc.xml
├── pom.xml
├── styles
│   └── main.css
└── README.md
```

El ficheo `index.txt` es el encargado de gestionar el orden de la concatenación 
del fichero `index.textile` que se genera para crear un documento `html` de una
sola página.

```shell
000-index.textile
001-git-introduccion.textile
002-proceso-git.textile
003-eclipse.textile
004-jenkins.textile
```

## Generar documentación

Ejecutar el comando **Maven** para la fase `generate-sources` que está asociada
 al target de **Ant** *generate-index-html*: 
```shell
$ mvn clean generate-sources
```
El resultado por defecto es un documento continuo `html` y documentos dividivos por secciones del mismo contenido.

* El documento continuo se puede ver en el directorio @target/documentation/index.html@.
* Los ficheros `html` separados por temas se encuentran en el directorio `documentation` del target junto a los ficheros `textile`:

```shell
target/documentation/
├── 000-index.html
├── 000-index.textile
├── 001-git-introduccion.html
├── 001-git-introduccion.textile
├── 002-proceso-git.html
├── 002-proceso-git.textile
├── 003-eclipse.html
├── 003-eclipse.textile
├── 004-jenkins.html
├── 004-jenkins.textile
├── images/
```

### Otros formatos

Se han definido distintos @target@ de ant:

* Generación de documentación HTML por página: `generate-html`
* Generación de documentación formato Eclipse: `generate-eclipse-help`

Para invocarlos es necesario cambiar el target en la definición del plugin 
`ant-run` de maven en el pom.xml:
```xml
<target name="generate-index-html" />
```
