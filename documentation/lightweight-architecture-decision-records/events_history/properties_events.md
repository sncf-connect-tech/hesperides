# Historique des valorisations d'un module déployé

Etape par étape :
* Remettre en place la liste des modifications de propriétés avec le commentaire utilisateur
* Permettre d'afficher les propriétés ajoutées, modifiées et supprimées
* Permettre de déclencher un diff entre 2 valorisations d'un module, depuis la modale

On n'affiche pas les changements de versions d'un module pour les raisons suivantes :
* L'intérêt est limité
* Ça complexifie beaucoup le code
* Il n'y a pas de méthode propre pour connaitre l'historique des version d'un module déployé

## Endpoint

    /applications/{application_name}/platforms/{platform_name}/properties/events?properties_path={properties_path}

`properties_path` est obligatoire.

## Output

L'évènement contenant les valorisations des propriétés est `PlatformModulePropertiesUpdatedEvent`. Il n'est pas nécessaire de modifier cet évènement pour y stocker les différences avec l'état précédent car il contient l'intégralité des valorisations de propriétés d'un module déployé.

Le traitement à exécuter consiste à comparer les évènements n et n-1 afin de déterminer pour chaque évènement de type `PlatformModulePropertiesUpdatedEvent` les données au format suivant :

    [
        {
            timestamp: Long,
            author: String,
            comment: String,
            added_properties: [
                {
                    name: String,
                    value: String
                },...
            ],
            updated_properties: [
                {
                    name: String,
                    old_value: String,
                    new_value: String
                },...
            ],
            removed_properties: [
                {
                    name: String,
                    value: String
                },...
            ],
        }
    ]

Le champ `commment` sera vide pour les propriété globales, qui n'en possèdent pas.

Les champs `added_properties` / `updated_properties` / `removed_properties` seront caclulés en comparant les évènements `PlatformModulePropertiesUpdatedEvent` consécutifs 2 à 2.

Ne pas oublier de tenir compte :
* de l'évènement `RestoreDeletedPlatformEvent` dans le calcul de ces données
* des mot de passes de prod

## Pagination

Un système de pagination doit être mis en place pour éviter de charger l'intégralité des données d'un module déployé, ce qui peut s'avérer coûteux.

2 propositions :

* `?page={}&size={}`
* `?version_id_start={}&version_id_stop={}` : moins standard, mais permettrait des headers HTTP de cache bien plus efficaces.

La première solution a été retenue, comme côté _frontend_ dans la majorité des cas seuls 2 appels à ces APIs seront effectués par affichage de modale.
