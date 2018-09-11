Feature: Techno updates

  Regroup all use cases related to the update of technos

  Background:
    Given an authenticated user

  Scenario: release a techno
    Given an existing techno
    When releasing this techno
    Then the techno is successfully released

  Scenario: add a template to an existing techno
    Given an existing techno
    When adding a template to this techno
    Then the template is successfully added to the techno

  Scenario: update an existing template in a techno
    Given an existing techno
    When updating the template in this techno
    Then the template in this techno is updated

  Scenario: delete an existing template in a techno
    Given an existing techno
    When deleting the template in this techno
    Then the template in this techno is successfully deleted


  Scenario: a techno template property cannot have both required and default value annotations
    Given an existing techno
    When trying to create a template in this techno that has a property that is required and with a default value
    Then the creation of the techno template that has a property that is required and with a default value is rejected

#  Scenario: a techno template property cannot have both required and default value annotations
#    Given a template property with required and default value annotations
#    When I create a template in techno with this property
#    Then the creation of the techno template is rejected
