# Access control

Dans le but de permettre un contrôle fin des accès en lecture & écriture aux données,
nous intégrons dans Hesperides un mécanisme d'[ACLS](https://fr.wikipedia.org/wiki/Access_Control_List).

## Besoin fonctionnel
- associer des droits de lecture & édition par plateforme, et y lier les restrictions d'accès aux **mots de passe** sur les plateformes de **production**
- s'intégrer avec un annuaire _Active Directory_ et à des normes de nommage de groupes pré-existantes
(incluant des cas de sous-groupes, avec transmissions de droits correspondants)
- déléguer la gestion des droits aux utilisateurs, pour les rendre autonomes

## Design

Globalement, l'ajout de ces fonctionnalités entraine quelques ajouts structurels :
- à chaque plateforme est associé un "privilège" Spring Boot (`authority`) : `$APP_PROD_USER`
- une nouvelle collection dans la base de données fait le lien entre `$APP_PROD_USER` et groupes _Active Directory_
- de nouvelles ressources dans le _endpoint_ `/users`, détaillées ci-dessous
- à chaque appel aux ressources `/platforms`, `/files` & `/users` ces privilèges sont consultés pour déterminer les opérations autorisées,
et si les mots de passes doivent être obfusqués

![](ACLs-pseudo-UML.png)

## Ressources REST modifiées

### GET /users/auth
Ajout de 3 champs dans la réponse.

**Input**: le _login_ de l'utilisateur (pour l'instant via la `Basic Auth`)

**Output** nominal: la liste exhaustive des groupes AD auxquels l'utilisateur appartient, et la liste des applications où il a des droits de prod
```
{
  "username": "...",
  "prodUser": false,
  "techUser": false,
  "prod_groups": ["GG_XX", "GG_YY", "GG_ZZ"]
}
```

Ici `prod_groups` inclus tous les groupes AD contenant l'utilisateur, directement ou transitivement.

**Error**: `401` si les _credentials_ de l'utilisateur sont invalides.

**Note**: ces informations ne sont pas stockées côté Hesperides, l'_ActiveDirectory_ est consulté à chaque appel.
Cependant un cache avec TTL de 5min est configuré pour limiter la charge sur ce serveur.

### GET /applications/$APP
Ajout d'un champ `prod_groups` dans la réponse.

**Output**: 
```
{
    "name": "AAA",
    "prod_groups": ["GG_XX", "GG_YY", "GG_ZZ"],
    "platforms: [ ... ]
}
```

### GET /applications/$APP?with_passwords_count=true
Ajout de ce _query parameter_.

**Besoin** : pouvoir identifier les plateformes nommées "PRDx" contenant des mots de passes,
mais non classifiées comme "production".

**Output**:
```
{
    "name": "AAA",
    "prod_groups": ["GG_XX", "GG_YY", "GG_ZZ"],
    "platforms: [{
        "name": "PRD1",
        "production": true,
        "passwordsCount": 4
    }, ... ]
}
```

### PUT /applications/$APP
Permet de mettre à jour `prod_groups`.

**Droits de modification des `prod_groups`**: ce champ ne peut être modifié que lorsqu'il est initialement vide
OU que l'utilisateur effectuant la modification appartient a l'un des `prod_groups`. Dans le cas contraire, une `401` est retournée.
