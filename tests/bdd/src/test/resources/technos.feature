Feature: technos related features

  Regroup all use cases related to technos

  Background:
    Given an authenticated user

  Scenario: create a new techno (it's the same as adding a template to a techno that does not exist)
    Given a techno to create
    When creating a new techno
    Then the techno is successfully created

  Scenario: delete a techno
    Given an existing techno
    When deleting this techno
    Then the techno is successfully deleted

  Scenario: release a techno
    Given an existing techno
    When releasing this techno
    Then the techno is successfully released

  Scenario: delete a released techno
    Given an existing techno
    When releasing this techno
    And deleting this techno
    Then the techno is successfully deleted

  Scenario: copy a techno
    Given an existing techno
    When creating a copy of this techno
    Then the techno is successfully and completely duplicated

  Scenario: retrieve all templates
    Given an existing techno
    And multiple templates in this techno
    When retrieving the templates of this techno
    Then I get a list of all the templates of this techno

  Scenario: retrieve a template
    Given an existing techno
    When retrieving the template of this techno
    Then I get the detail of the template of this techno

  Scenario: add a template to an existing techno
    Given an existing techno
    When adding a template to this techno
    Then the template is successfully added to the techno

  Scenario: update an existing template in a techno
    Given an existing techno
    When updating the template in this techno
    Then the template in this techno is updated

  Scenario: search for an existing techno
    Given a list of 12 technos
    When searching for a specific techno
    Then the techno is found

  Scenario: search for existing technos
    Given a list of 12 technos
    When searching for some of those technos
    Then the number of techno results is 10

  Scenario: search for a techno that does not exist
    Given a list of 12 technos
    When searching for a techno that does not exist
    Then the number of techno results is 0

  Scenario: get model properties
    Given an existing techno
    And templates with properties in this techno
    When retrieving the model of this tehno
    Then I get the properties model of this techno