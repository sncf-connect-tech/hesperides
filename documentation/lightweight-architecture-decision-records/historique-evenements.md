# Historique des évènements

## Ressources actuelles

    /events/modules/{name}/{version}/{type}
    /events/platforms/{application_name}/{platform_name}
    /events/{stream}

Ces ressources seront conservées mais dépréciées.

## Besoins

* Visualisation des modifications des valorisations de propriétés
* Visualisation des changements de versions de modules déployés
* Visualisation des ajouts/suppressions de modules déployés
* Visualisation des modifications de versions de platformes
* Visualisation des ajouts/suppressions de templates (dans un module ou une techno)
* Comparer les valorisations d'un module entre 2 versions

3 cas d'utilisations dans des modales _frontend_ :
1. Historique des valorisations d'un module déployé
1. Historique au niveau d'une plateforme
1. Historique d'un module ou d'une techno

### Historique des valorisations d'un module déployé

Etape par étape :
* Remettre en place la liste des modifications de propriétés avec le commentaire utilisateur
* Permettre d'afficher les propriétés ajoutées, modifiées et supprimées
* Permettre de déclencher un diff entre 2 valorisations d'un module, depuis la modale
* Bonus - Afficher les changements de versions du module

#### Endpoints

    /applications/{application_name}/platforms/{platform_name}/properties/events?properties_path={properties_path}

`properties_path` est obligatoire.

Envisagés, plus tard :

    /modules/{name}/{version}/{type}/events
    /modules/{name}/{version}/{type}/templates/{file}/events
    /technos/{name}/{version}/{type}/events
    /technos/{name}/{version}/{type}/templates/{file}/events

#### Output de .../properties/events

L'évènement contenant les valorisations des propriétés est `PlatformModulePropertiesUpdatedEvent`. Il n'est pas nécessaire de modifier cet évènement pour y stocker les différences avec l'état précédent car il contient l'intégralité des valorisations de propriétés d'un module déployé.

Le traitement à exécuter consiste à comparer les évènements n et n-1 afin de déterminer pour chaque évènement de type `PlatformModulePropertiesUpdatedEvent` les données au format suivant :

    {
        author: "",
        comment: "",
        added_properties: ["property-1", "property-2", ...],
        updated_properties: [
            {
                name: "",
                old_value: "",
                new_value: ""
            },
        ],
        removed_properties: []
    }

Le champ `commment` sera vide pour les propriété globales, qui n'en possèdend pas.

Les champs `added_properties` / `updated_properties` / `removed_properties`
seront caclulés en comparant les évenements `PlatformModulePropertiesUpdatedEvent` consécutifs 2 à 2.

Ne pas oublier de tenir compte de l'évènement `RestoreDeletedPlatformEvent` dans le calcul de ces données.

#### Output de .../events

TODO

#### Pagination

Un système de pagination doit être mis en place pour éviter de charger l'intégralité des données d'un module déployé,
ce qui peut s'avérer coûteux.

2 propositions :

* `?offset={}&size={}`
* `?version_id_start={}&version_id_stop={}` : moins standard, mais permettrait des headers HTTP de cache bien plus efficaces.

La première solution a été retenue, comme côté _frontend_ dans la majorité des cas seuls 2 appels à ces APIs
seront effectués par affichage de modale.

### Historique au niveau d'une plateforme

L'idée est d'afficher le détails des modifications d'une platforme. Il y a 4 types d'évènements à détailler :
* Changements de version de la plateforme
* Changements de versions de modules déployés
* Ajout de modules déployés
* Suppression de modules déployés

Ces modifications peuvent être effectuées à chaque évènement. On se base donc sur le timestamp des évènements et on détaille ce qui s'y est passsé.

Les évènements contenant les données permettant d'extraire ces informations sont `PlatformCreatedEvent` et `PlatformUpdatedEvent`.

#### Endpoint

    /applications/{application_name}/platforms/{platform_name}/events

#### Structure

    [
        {
            timestamp: Long,
            events: [
                {
                    name: "platform_version_updated", 
                    old_version: String,
                    new_version: String
                },
                {
                    name: "deployed_module_version_updated",
                    old_version: String,
                    new_version: String
                },
                {
                    name: "deployed_module_added",
                    path: String
                },
                {
                    name: "deployed_module_removed",
                    path: String
                }
            ]
        }
    ]

### Historique d'un module ou d'une techno

* Templates ajoutés, modifiés ou supprimés
* Diff des modifications apportés aux templates (ligne par ligne comme le diff Git)
