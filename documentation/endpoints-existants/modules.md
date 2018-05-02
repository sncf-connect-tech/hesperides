# Liste des endpoints de modules et leurs templates

## Codes erreurs

* 404 si non trouvé
* 500 si erreur serveur
* 409 si déjà existant ou s'il y a conflit (modifications parallèles)

Que se passe-t-il si on crée ou si on met à jour un module qui a été supprimé ?

Séparer modules et templates ou working copy et release

## Modules

### POST /modules

Crée/Duplique un module.

#### Input

Paramètres de requêtes facultatifs :

* from_module_name
* from_module_version
* from_is_working_copy

Si ces pramètres sont fournis, cela crée une copie profonde incluant les technos et les templates du module dupliqué.

Dans le cas d'une duplication, on ne tient pas compte de la liste de technos de l'input.

    {
        "name": "",
        "version: "",
        "working_copy: false,
        "version_id: 0,
        "technos": [
            {
                "version": "",
                "name": "",
                "working_copy: false
            }
        ]
    }
    
#### Output

Code HTTP 201

En cas de création : même structure et contenu que l'input mais avec le version_id initialisé.

En cas de duplication : même structure que l'input mais contenu relatif aux données récupérées et un version_id à 1.

### PUT /modules

Met à jour de module => permet d'ajouter/supprimer une ou plusieurs technos.

Le version_id est vérifié.

#### Input

    {
        "name": "",
        "version: "",
        "working_copy: false,
        "version_id: 0,
        "technos": [
            {
                "version": "",
                "name": "",
                "working_copy: false
            }
        ]
    }
    
#### Ouput

Code HTTP 200 + Même structure et contenu que l'input mais avec le version_id mis à jour.

### GET /modules

Récupère la liste des différents noms de modules.

#### Input

Rien

#### Ouput

Code HTTP 200 + Tableau de strings.

### GET /modules/{module_name}

Récupère la liste des versions différentes d'un module.

#### Input

Rien

#### Ouput

Code HTTP 200 + Tableau de strings.

### GET /modules/{module_name}/{module_version}

Récupère la liste des types de versions pour une version

#### Input

Rien

#### Output

Code HTTP 200 + Tableau de strings pouvant contenir "workingcopy" et "release"

### GET /modules/{module_name}/{module_version}/{module_type}

Récupère les infos d'un module à partir de son nom, sa version et son type

#### Input

Rien

#### Output

Code HTTP 200

    {
        "name": "",
        "version: "",
        "working_copy: false,
        "version_id: 0,
        "technos": [
            {
                "version": "",
                "name": "",
                "working_copy: false
            }
        ]
    }

### DELETE /modules/{module_name}/{module_version}/{module_type}

Supprime un module.

*Quels impacts sur les plateformes qui utilisent ce module ?*

#### Input

Rien

#### Output

Code HTTP 200

### POST /modules/create_release

Crée la release d'un module à partir d'une working copy.
On reprend le nom, les technos et les templates du module.
La version de release est facultative. Si elle n'est pas définie, on reprend la version de la working copy.
On peut créer plusieurs release d'un même module working copy, du moment que la verion de release change.

#### Input

Paramètres de requêtes :

* module_name
* module_version
* release_version (facultatif)

#### Output

Code HTTP 200

    {
        "name": "",
        "version: "",
        "working_copy: false,
        "version_id: 0,
        "technos": [
            {
                "version": "",
                "name": "",
                "working_copy: false
            }
        ]
    }

L'attribut `working_copy` est à false et le version_id est à 1.

### POST /modules/perform_search

Recherche un module à partir de son nom et de sa version

#### Input

Paramètre de requête : terms

#### Output

Code HTTP 200

Retourne une liste de modules

    [
        {
            "name": "",
            "version: "",
            "working_copy: false,
            "version_id: 0,
            "technos": [
                {
                    "version": "",
                    "name": "",
                    "working_copy: false
                }
            ]
        }
    ]
    
## Templates

### GET /modules/{module_name}/{module_version}/{module_type}/model

Récupère les propriétés valorisables d'un module.

#### Input

Rien

#### Output

Code HTTP 200

    {
        "key_value_properties": [
            {
                "name": "",
                "comment": "",
                "required": false,
                "defaultValue": "",
                "pattern": "",
                "password: false"
            }
        ],
        "iterable_properties": [
            TODO
        ]
    }
    
### GET /modules/{module_name}/{module_version}/{module_type}/templates

Récupère la liste des templates d'un module.

#### Input

Rien

#### Output

Code HTTP 200 + Un tableau json :

    [
        {
            "name": "",
            "namespace": "",
            "filename": "",
            "location": ""
        }
    ]
    
Namespace est de la forme :

    modules#{module name}#{module version}#{WORKINGCOPY|RELEASE}
    
### GET /modules/{module_name}/{module_version}/{module_type}/templates/{template_name}

Récupère le contenu d'un template.

#### Input

Rien

#### Output

Code HTTP 200

    {
        "namespace": "",
        "name": "",
        "filename": "",
        "location": "",
        "content": "",
        "version_id": 0,
        "rights": {
            "user": {
                "read": null,
                "write": null,
                "execute": null
            },
            "group": {
                "read": null,
                "write": null,
                "execute": null
            },
            "other": {
                "read": null,
                "write": null,
                "execute": null
            }
        }
    }
    
"other" est null dans le legacy.

### POST /modules/{module_name}/{module_version}/workingcopy/templates

Ajoute un template à un module working copy existant. Si le module n'existe pas, on ne le crée pas.

#### Input 

    {
        "namespace": "",
        "name": "",
        "filename": "",
        "location": "",
        "content": "",
        "version_id": 0,
        "rights": {
            "user": {
                "read": null,
                "write": null,
                "execute": null
            },
            "group": {
                "read": null,
                "write": null,
                "execute": null
            },
            "other": {
                "read": null,
                "write": null,
                "execute": null
            }
        }
    }
    
Le namespace n'est pas obligatoire. Le nom ne doit pas être utilisé par un autre template du module.
    
#### Output

Code HTTP 201 + Même contenu que l'input avec le namespace complété et le version_id initialisé.

### PUT /modules/{module_name}/{module_version}/workingcopy/templates

Met à jour le template d'un module working copy.

#### Input

Même chose que pour la création. Le namespace n'est pas utilisé. Le nom du template et le version_id sont contrôlés.

Si le nom n'existe pas : 404. Si le version_id est différent de celui du module existant : 409 (conflit).

#### Output

Code HTTP 200 + Même contenu que l'input mais avec le namespace complété et le version_id mis à jour.

### DELETE /modules/{module_name}/{module_version}/workingcopy/templates/{template_name}

Supprime le template d'un module à partir de la clé du module et du nom du template.

#### Input

Rien

On vérifie que le template existe, sinon 404.

#### Output

Code HTTP 204.
