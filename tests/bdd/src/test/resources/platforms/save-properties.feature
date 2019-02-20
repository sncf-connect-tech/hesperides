Feature: Save properties

  Background:
    Given an authenticated user

  #issue-504
  Scenario: save a required property and a property that has to match a pattern
    Given an existing module with this template content
      """
      {{ required-property | @required }}
      {{ pattern-property | @pattern "[a-z]*" }}
      {{ unfilled-pattern-property | @pattern "[a-z]*" }}
      {{ required-pattern-property | @required @pattern "[a-z]*" }}
      """
    And an existing platform with this module
    When I save these properties
      | name                      | value   |
      | required-property         | a-value |
      | pattern-property          | avalue  |
      | unfilled-pattern-property |         |
      | required-pattern-property | avalue  |
    Then the properties are successfully saved

  #issue-504
  Scenario: save a property that does not match a pattern
    Given an existing module with this template content
      """
      {{ a-property | @pattern "[a-z]*" }}
      """
    And an existing platform with this module
    When I try to save these properties
      | name       | value  |
      | a-property | aValue |
    Then the request is rejected with a bad request error

  #issue-447
  Scenario: save a required property without value
    Given an existing module with this template content
      """
      {{ a-property | @required }}
      """
    And an existing platform with this module
    When I try to save these properties
      | name       | value |
      | a-property |       |
    Then the request is rejected with a bad request error
