Feature: Get technos templates

  Regroup all use cases related to the retrieval of templates in technos

  Background:
    Given an authenticated user

  Scenario: retrieve all templates
    Given an existing techno
    And multiple templates in this techno
    When retrieving the templates of this techno
    Then I get a list of all the templates of this techno

  Scenario: retrieve a template
    Given an existing techno
    When retrieving the template of this techno
    Then I get the detail of the template of this techno
