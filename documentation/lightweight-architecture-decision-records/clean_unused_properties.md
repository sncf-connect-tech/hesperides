# Nettoyage des propriétés inutilisées

Lorsqu'une propriété est valorisée mais ne correspond à aucune propriété du modèle (déclarée dans un template), on considère qu'elle est inutilisée.

Aujourd'hui le front permet de supprimer ces propriétés au niveau d'un module déployé. Nous souhaitons porter cette fonctionnalité dans le backend et permettre de nettoyer ces propriétés aussi pour l'ensemble des modules déployés d'une plateforme.

Voici le endpoint proposé :

    DELETE /applications/{application_name}/platforms/{platform_name}/properties/clean_unused_properties?properties_path={properties_path}
    
Le paramètre de requête `properties_path` est facultatif.
