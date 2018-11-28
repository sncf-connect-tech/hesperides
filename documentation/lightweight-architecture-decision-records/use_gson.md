# Pourquoi Gson ?

Nous utilisons Gson pour sérialiser/désérialiser :
- les objets correspondant aux events du legacy
- les inputs de la couche présentation

Contrairement à Jackson, Gson est compatible avec les annotations Lombok
et nous permet de réduire la quantité de code à maintenir.

## Serialize null

Pour être iso-legacy, nous avons décidé de sérialiser les champs dont la valeur est `null`. S'il est nécessaire d'exclure un champ qui est null, il faut créer un Serializer (voir `PropertyOutput.Serializer`).

## Swagger

Swagger ne gère pas nativement le fait d'utiliser Gson plutôt que Jackson. C'est pourquoi on retrouve sur les IO l'annotation @JsonProperty, pour Swagger, en plus de l'annotation @SerializedName, pour Gson.

De plus, Swagger n'aime pas le type primitif boolean donc on utilise désormais Boolean.

Endpoint pour accéder au Swagger : /rest/swagger-ui.html

## Classes abstraites

La sérialisation et désérialisation de classes abstraites n'est pas gérée nativement par Gson. En cas de besoin, il faut créer un Adapter (voir `AbstractValuedPropertyIO.Adapter`).