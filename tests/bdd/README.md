# Tests fonctionnels avec Cucumber Java

Les tests fonctionnels, complétés par quelques tests unitaires, représentent l'essentiel des tests automatisés sur Hespérides.

Afin d'alléger le code qu'il y a derrière les `feature`, c'est-à-dire la glue, nous avons été amenés à rendre *implicites* certains aspects du code.

L'inconvénient est que cela nécessite d'en avoir connaissance pour mieux s'y retrouver. L'intérêt est que cela améliore la maintenance et la lisibilité du code.

## CustomRestTemplate

La classe `CustomRestTemplate` contient la configuration du `RestTemplate` et surcharge toutes ses méthodes d'appel à l'API afin d'aider au debuggage en cas d'erreur.

## TestContext.setResponse(...)

La classe `TestContext` est utilisée pour partager un état entre les étapes des scénarios. Elle contient notamment le résultat des appels à l'API.

Ce résultat est stocké dans le contexte juste après l'appel à l'API dans la méthode `CustomRestTemplate.wrapForEntity` :

    // Appel API
    ...
    testContext.setResponseEntity(responseEntity);
    ...

## Try to

Nous partons du principe que lors que l'étape d'un scénario contient l'expression `...try to...`, l'appel à l'API retournera une erreur. Cela implique que nous attendons un corps de type `String`.

Lorsqu'une méthode attend un paramètre nommé `tryTo` mais qu'il n'est pas fourni par la définition de l'étape du scénario, la convention est de fournir la chaîne de caractère `"should-fail"`.

## TechnoHistory, ModuleHistory et PlatformHistory

Les classes `TechnoHistory` et `ModuleHistory` contiennent la liste des `Builder` créés pour chaque nouvelle entité.

La classe `PlatformHistory` aussi mais elle va un peu plus loin. Pour répondre aux cas d'utilisation permettant de récupérer les données d'une plateforme à un instant T, nous sauvegardons les `Builder` de chaque plateforme *à chaque `POST/PUT` envoyé à l'API*, et nous associons ce builder à un timestamp.

## Factorisation

La gestion des `version_id` et l'historisation des `Builder` se trouve dans les méthodes `CreatePlatforms.createPlatform`, `SaveProperties.saveGlobalProperties`, `CreateModule.createModule`, etc. 

Il suffit d'utiliser l'injection de dépendance pour accéder à ces méthodes.

Exemple :

    @Autowired
    private SaveProperties saveProperties;
    ...
    saveProperties.saveValuedProperties();
