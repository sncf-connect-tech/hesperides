# Access control

Dans le but de permettre un contrôle fin des accès en lecture & écriture aux données,
nous intégrons dans Hesperides un mécanisme d'[ACLs](https://fr.wikipedia.org/wiki/Access_Control_List).

<!-- Pour mettre à jour ce sommaire: 
    markdown-toc --indent "    " -i access_control.md
-->

<!-- toc -->

- [Besoin fonctionnel](#besoin-fonctionnel)
- [Design](#design)
- [Ressources REST](#ressources-rest)
    * [GET /users/auth](#get-usersauth)
    * [GET /applications/$APP](#get-applicationsapp)
    * [GET /applications/$APP?with_passwords_count=true](#get-applicationsappwith_passwords_counttrue)
    * [PUT /applications/$APP](#put-applicationsapp)
- [Détails notables d'implémentation](#details-notables-dimplementation)
    * [Algorithme de collecte des _authorities_](#algorithme-de-collecte-des-_authorities_)
    * [Caches](#caches)

<!-- tocstop -->

## Besoin fonctionnel

- Associer des droits de lecture & édition par plateforme, et y lier les restrictions d'accès aux **mots de passe** sur les plateformes de **production**
- S'intégrer avec un annuaire _Active Directory_ et à des normes de nommage de groupes pré-existantes
(incluant des cas de sous-groupes, avec transmissions de droits correspondants)
- Déléguer la gestion des droits aux utilisateurs, pour les rendre autonomes


## Design

Globalement, l'ajout de ces fonctionnalités entraîne quelques modifications structurelles :
- À chaque plateforme est associé un "privilège" Spring Boot (`authority`) : `${APP}_PROD_USER`
- Une nouvelle collection dans la base de données fait le lien entre `${APP}_PROD_USER` et groupes _Active Directory_
- De nouvelles ressources dans le _endpoint_ `/users`, détaillées ci-dessous
- À chaque appel aux ressources `/platforms`, `/files` & `/users` ces privilèges sont consultés pour déterminer les opérations autorisées,
et si les mots de passes doivent être obfusqués

![](ACLs-pseudo-UML.png)


## Ressources REST

### GET /users/auth

Ajout de 3 champs dans la réponse.

**Input** : le _login_ de l'utilisateur (pour l'instant via la `Basic Auth`)

**Output** nominal : la liste exhaustive des groupes ActiveDirectory auxquels l'utilisateur appartient, et la liste des applications où il a des droits de prod
```
{
  "username": "...",
  "prodUser": true,
  "techUser": false,
  "authorities": {
    "roles": ["GLOBAL_IS_PROD", "ABC_PROD_USER", "DEF_PROD_USER"],
    "groups": ["GG_XX", "GG_YY"]
  }
}
```

Ici les `authorities` sont issues de la gestion de rôles de [Spring Boot](https://www.baeldung.com/role-and-privilege-for-spring-security-registration).
Ils sont déclinés en 3 types :
- Les rôles globaux à toutes les applications : `prodUser` (`GLOBAL_IS_PROD`) et `techUser` (`GLOBAL_IS_TECH`)
- Les nouveaux privilèges : correspondent à la liste des applications sur lesquelles l'utilisateur a les droits de prod (`${APP}_PROD_USER`)
- Les groupes Active Directory auxquels l'utilisateur appartient : ils apparaissent à titre indicatif et incluent les groupes parents ainsi que leurs ancêtres

**Error** : `401` si les _credentials_ de l'utilisateur sont invalides.

### GET /applications/$APP

Ajout d'un champ `authorities` dans la réponse.

**Output** : 
```
{
    "name": "ABC",
    "directory_groups": {
        "ABC_PROD_USER": ["GG_XX", "GG_ZZ"]
    },
    "platforms: [ ... ]
}
```

Ici, le paramètre `directory_groups` représentent les groupes AD qui donnent les droits de prod sur les plateformes de l'application.

### GET /applications/$APP?with_passwords_count=true

Ajout de ce _query parameter_.

**Besoin** : pouvoir identifier les plateformes nommées "PRDx" contenant des mots de passes,
mais non catégorisées comme "production".

**Output** :
```
{
    "name": "ABC",
    "directory_groups": {
        "ABC_PROD_USER": ["GG_XX", "GG_ZZ"]
    },
    "platforms: [{
        "name": "PRD1",
        "production": true,
        "passwordsCount": 4
    }, ... ]
}
```

### PUT /applications/$APP/authorities

Permet de mettre à jour la propriété `directory_groups` d'une application.

**Droits de modification des `directory_groups`** : ce champ peut être modifié uniquement si l'utilisateur effectuant la modification :
- a les droits "prod" globaux
- appartient a l'un des groupes dans `directory_groups`.

Dans le cas contraire, une `403` est retournée.

**Input** :
```
{
    "directory_groups": {
        "ABC_PROD_USER": ["GG_XX", "GG_ZZ"]
    }
}
```


## Détails notables d'implémentation

### Algorithme de collecte des _authorities_

**Contrainte de performances** : Historiquement, Hesperides effectuait un appel à l'ActiveDirectory par connexion d'utilisateur et par rôle (`prodUser` / `techUser`),
via une requête `(memberOf:$oid:=$groupDN)`.

Avec désormais potentiellement plusieurs groupes ActiveDirectory associés à chaque application,
pour plus d'une centaine d'applications, cela signifierait - en moyenne et en ordre de grandeur - une centaine de requête `memberOf` par connexion d'utilisateur.

Cette approche n'étant donc plus envisageable en terme de charge générée sur l'ActiveDirectory,
pour minimiser ce nombre d'appels Hesperides va désormais collecter la liste de tous les groupes parents de l'utilisateur,
transitivement et avec un cache.

Voici donc le nouvel algorithme de collecte des `directory_groups` par connexion d'utilisateur :
1. un appel systématique à l'ActiveDirectory pour récupérer la liste des groupes directement parents, via une requête `memberOf`.
2. on résoud la liste exhaustive des groupes parents en bénéficiant d'un cache, générant entre 0 et `N` appels
(avec `N` la profondeur maximum de l'arbre des groupes parents, et donc très peu élevé).
3. on compare cette liste exhaustive de groupes parents avec ceux configurés pour correspondre aux rôles `GLOBAL_IS_PROD` / `GLOBAL_IS_TECH`
afin de déterminer si l'utilisateur les possède.
4. on requête la base de données d'Hesperides pour obtenir la liste des privilèges `${APP}_PROD_USER` associés aux groupes de l'utilisateur.
Une seule requête est nécessaire.


### Caches

Deux caches sont mis en place au niveau de la gestions des `directory_groups` :

- par utilisateur, ses `directory_groups` sont mémorisées dans un cache avec TTL de 5 minutes et comme clef son _login_
- globalement, l'arbre des dépendances entre groupes ActiveDirectory étant mis en cache, avec pour chaque groupe un TTL de 1 heure

Malgré ces caches, comme Hesperides est _stateless_ et qu'aucune information relative à l'utilisateur n'est stockée en base de données,
l'_ActiveDirectory_ est toujours consulté au moins une fois par connexion d'utilisateur.


## Tests

Comme les tests BDD validant ce fonctionnement dépendent de l'utilisation d'un serveur ActiveDirectory,
nous avons tagué ces tests en `@require-real-ad`, et ils ne sont pas exécutés avec les autres tests Cucumber par défaut.

Pour lancer ces tests, il vous faudra définir les variables d'environnement suivantes :

    LDAP_URL
    LDAP_DOMAIN
    LDAP_USER_SEARCH_BASE
    LDAP_ROLE_SEARCH_BASE
    LDAP_PROD_GROUP_DN
    LDAP_TECH_GROUP_DN

Ainsi que les paramètres suivants :

    -Dauth.lambdaUserName=...
    -Dauth.lambdaUserPassword=...
    -Dauth.lambdaUserParentGroupDN=...
    -Dauth.prodUserName=...
    -Dauth.prodUserPassword=...
