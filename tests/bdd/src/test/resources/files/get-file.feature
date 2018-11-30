Feature: Get file

  Background:
    Given an authenticated user

  Scenario: get file
    Given an existing module with a template with properties
    And an existing platform with this module and valued properties
    When I get the module template file
    Then the file is successfully retrieved

  Scenario: get file with iterable properties
#    Given an existing module with iterable properties

    #Propriétés globales, propriétés d'instances, technos, propriété globale ayant le même nom qu'une propriété d'instance ou inversement
  # Propriétés itérables