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
  "ad_groups": ["GG_XX", "GG_YY"],
  "flat_ad_groups": ["GG_XX", "GG_YY", "GG_ZZ"],
  "authorities": ["AAA_PROD_USER", "BBB_PROD_USER"]
}
```

**Error**: `401` si les _credentials_ de l'utilisateur sont invalides.

### GET /applications/$APP
Ajout d'un champ `ad_groups` dans la réponse + cette ressource ne doit pas nécessiter d'autentification.

**Output**: 
```
{
    "name": "AAA",
    "ad_groups": ["GG_XX", "GG_YY", "GG_ZZ"],
    "platforms: [ ... ]
}
```

### GET /applications/$APP?with_passwords_count=true
Ajout de ce _query parameter_.

**Output**:
```
{
    "name": "AAA",
    "ad_groups": ["GG_XX", "GG_YY", "GG_ZZ"],
    "platforms: [{
        "name": "PRD1",
        "production": true,
        "passwordsCount": 4
    }, ... ]
}
```

### POST/PUT /applications/$APP
Doit permettre l'inclusion du champ `ad_groups` lors de la création / mise à jour.

En termes de règles "métier", lors d'un `PUT`, le champ `ad_groups` ne peut être modifié que s'il est vide
OU que l'utilisateur effectuant l'appel appartient a l'un de ces groupes. Sinon : `401`.
