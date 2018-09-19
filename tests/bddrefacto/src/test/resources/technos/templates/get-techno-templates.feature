Feature: Get techno templates

  Background:
    Given an authenticated user

  Scenario: get the list of templates of an existing techno
    Given an existing techno
    And multiple templates in this techno
    When I get the list of templates of this techno
    Then a list of all the templates of the techno is returned

  Scenario: get the list of templates of a released techno
    Given an existing techno
    And multiple templates in this techno
    And I release this techno
    When I get the list of templates of this techno
    Then a list of all the templates of the techno is returned

  Scenario: get a template of a techno
    Given an existing techno
    And a template in this techno
    When I get this template in this techno
    Then the techno template is successfully returned

  Scenario: get a template that doesn't exist in a a techno
    Given an existing techno
    And a template that doesn't exist in this techno
    When I try to get this template in this techno
    Then the techno template is not found

  Scenario: get the list of templates of a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to get the list of templates of this techno
    Then the templates techno is not found

  Scenario: get a template of a techno that doesn't exist
    Given a techno that doesn't exist
    When I try to get this template in this techno
    Then the template techno is not found
