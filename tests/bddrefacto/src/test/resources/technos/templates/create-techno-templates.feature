Feature: Create techno templates

  Background:
    Given an authenticated user

  Scenario: add a template to an existing techno
    Given an existing techno
    And a template to create
    When I add this template to the techno
    Then the template is successfully added to the techno

  Scenario: add a template to a released techno (the endpoint doesn't exist)
#    Given a released techno
#    And a template to create
#    When I try to add this template to the techno
#    Then the template is rejected with a conflict error

  Scenario: add a template to a techno that doesn't exist (it the same as creating a new techno)
#    Given a techno that doesn't exist
#    And a template to create
#    When I try to add this template to the techno
#    Then the template is rejected with a not found error

  Scenario: add a template that already exists
    Given an existing techno
    And a template to create with the same name as the existing one
    When I try to add this template to the techno
    Then the template is rejected with a conflict error

#  Scenario: a techno template property cannot have both required and default value annotations
#    Given an existing techno
#    When trying to create a template in this techno that has a property that is required and with a default value
#    Then the creation of the techno template that has a property that is required and with a default value is rejected

#  Scenario: a techno template property cannot have both required and default value annotations
#    Given a template property with required and default value annotations
#    When I create a template in techno with this property
#    Then the creation of the techno template is rejected
