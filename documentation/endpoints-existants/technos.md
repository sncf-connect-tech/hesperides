# Liste des endpoints de technos

### POST /templates/packages

Crée une techno à partir d'une autre.

#### Input

    {
        "name": "",
        "version": "",
        "working_copy": false
    }

Paramètes de requête :

* from_package_name
* from_package_version
* from_is_working_copy

#### Output

Code HTTP 201 + Même contenu que l'input

### DELETE /templates/packages/{package_name}/{package_version}/{package_type}

Supprime une techno à partir de sa clé.

#### Input

Rien

#### Output

Code HTTP 200

### GET /templates/packages/{package_name}/{package_version}/{package_type}/model

Récupère les propriétés valorisables d'une techno.

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
            {
                "name": "",
                "comment": "",
                "required": false,
                "defaultValue": "",
                "pattern": "",
                "password: false"
                "fields": [
                    {
                        "name": "",
                        "comment": "",
                        "required": false,
                        "defaultValue": "",
                        "pattern": "",
                        "password: false"
                    }
                ]
            }
        ]
    }

### GET /templates/packages/{package_name}/{package_version}/{package_type}/templates

Récupère la liste des templates d'une techno.

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

    packages#{techno name}#{techno version}#{WORKINGCOPY|RELEASE}
    
### GET /templates/packages/{package_name}/{package_version}/{package_type}/templates/{template_name}

Récupère le contenu d'un template d'une techno.

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
    
"other" est toujours null dans le legacy.

### POST /templates/packages/{package_name}/{package_version}/workingcopy/templates

Ajoute un template à une techno. Si elle n'existe pas, on la crée.

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
    
Le namespace n'est pas obligatoire. Le nom ne doit pas être utilisé par un autre template de la techno.
    
#### Output

Code HTTP 201 + Même contenu que l'input avec le namespace complété et le version_id initialisé.

### PUT /templates/packages/{package_name}/{package_version}/workingcopy/templates

Met à jour le template d'une techno en working copy.

#### Input

Même chose que pour la création. Le namespace n'est pas utilisé. Le nom du template et le version_id sont contrôlés.

Si le nom n'existe pas : 404. Si le version_id est différent de celui du module existant : 409 (conflit).

#### Output

Code HTTP 200 + Même contenu que l'input mais avec le namespace complété et le version_id mis à jour.

### DELETE /templates/packages/{package_name}/{package_version}/workingcopy/templates/{template_name}

Supprime le template d'une techno à partir de la clé de la techno et du nom du template.

#### Input

Rien

On vérifie que le template existe, sinon 404.

#### Output

Code HTTP 200.

### POST /templates/packages/create_release

Crée la release d'une techno à partir d'une working copy.
On reprend le nom et les templates de la techno existante.
On ne peut pas créer plusieurs releases d'une même techno => 409.

#### Input

Paramètres de requêtes :

* package_name
* packge_version

#### Output

Code HTTP 201

    {
        "name": "",
        "version: "",
        "working_copy: false
    }

L'attribut `working_copy` est à false.

### POST /templates/packages/perform_search

Recherche une techno à partir de son nom et de sa version

#### Input

Paramètre de requête : terms

#### Output

Code HTTP 200 + Un tableau de clés de technos :

    [
        {
            "name": "",
            "version: "",
            "working_copy: false
        }
    ]
