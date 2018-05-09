Créer une working copy de techno à partir de rien
	POST /templates/packages
	Nouveaux endpoints :
    POST /technos
    GET /technos/{name}/{version}/workingcopy


Créer une release depuis une working copy
	POST /templates/packages/create_release
	(nouveau endpoint) GET /templates/packages/{package_name}/{package_version}/release

Créer une working copy de techno depuis une release
	POST /templates/packages

Rechercher une techno
	POST /templates/packages/perform_search

Gérer un template dans une techno en working copy
	POST /templates/packages/{package_name}/{package_version}/workingcopy/templates
	PUT /templates/packages/{package_name}/{package_version}/workingcopy/templates
	GET /templates/packages/{package_name}/{package_version}/workingcopy/templates
	DEL /templates/packages/{package_name}/{package_version}/workingcopy/templates/{template_name}
	GET /templates/packages/{package_name}/{package_version}/workingcopy/templates/{template_name}
	=> Properties

Récupérer les properties d'un template (via le model)
	GET /templates/packages/{package_name}/{package_version}/workingcopy/model
	GET /templates/packages/{package_name}/{package_version}/release/model
	=> Gérer le model à partir du template (analyse du template pour en sortir le model)

Afficher les templates d'une release
    GET /templates/packages/{package_name}/{package_version}/release/templates/{template_name}
    GET /templates/packages/{package_name}/{package_version}/release/templates

Supprimer une techno releasée ou working copy
	DEL /templates/packages/{package_name}/{package_version}/release
	DEL /templates/packages/{package_name}/{package_version}/workingcopy
	=> Vérifier qu'une techno releasée n'est pas utilisée dans un module