# Historique au niveau d'une plateforme

L'idée est d'afficher le détails des modifications d'une platforme. Il y a 4 types d'évènements à détailler :
* Changements de version de la plateforme
* Mise à jour de modules déployés (changement de version, release, etc.)
* Ajout de modules déployés
* Suppression de modules déployés

Ces modifications peuvent être effectuées à chaque évènement. On se base donc sur le timestamp des évènements et on détaille ce qui s'y est passsé.

Les évènements contenant les données permettant d'extraire ces informations sont `PlatformCreatedEvent` et `PlatformUpdatedEvent`.

## Endpoint

    /applications/{application_name}/platforms/{platform_name}/events

## Structure

    [
        {
            timestamp: Long,
            author: String,
            changes: [
                {
                    event_name: "platform_created",
                },
                {
                    event_name: "platform_version_updated",
                    old_version: String,
                    new_version: String
                },
                {
                    event_name: "deployed_module_updated",
                    old_properties_path: String,
                    new_properties_path: String
                },
                {
                    event_name: "deployed_module_added",
                    properties_path: String,
                },
                {
                    event_name: "deployed_module_removed",
                    properties_path: String,
                }
            ]
        }
    ]

Chaque évènement peut contenir un ou plusieurs changements.
