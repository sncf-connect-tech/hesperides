# Swagger

Nous utilisons la librairie Springfox pour proposer le service Swagger v2 via l'url `/rest/swagger-ui.html`.

Nous avons choisi la version `2.8.0` car la dernière en date (`2.9.2`) ajoute beaucoup d'informations dont nous n'avons pas besoin dans l'interface.

De plus, afin de simplifier au maximum cette interface, nous avons effectué les modifications suivantes :

* Nous utilisons notre propre `swagger-ui.html`, avec un peu de CSS pour cacher certaines zones
* Un peu de personnalisation via `SwaggerConfiguration`
* Chaque controller contient l'annotation `@Api(tags = "x. Something", description = " ")` car :

  * C'est le moyen que nous avons trouver de trier les ressources
  * La description auto-générée (`Something Controller`) n'est pas utile
  
  