Feature: Save properties

  Background:
    Given an authenticated user

  #issue-504
  Scenario: saving properties with @required and/or @pattern annotations`
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

  Scenario: save a valid required property and an invalid pattern property
    Given an existing module with this template content
      """
      {{ required-property | @required }}
      {{ pattern-property | @pattern "[a-z]*" }}
      """
    And an existing platform with this module
    When I try to save these properties
      | name              | value   |
      | required-property | a-value |
      | pattern-property  | aValue  |
    Then the request is rejected with a bad request error

  Scenario: save an invalid required property and a valid pattern property
    Given an existing module with this template content
      """
      {{ required-property | @required }}
      {{ pattern-property | @pattern "[a-z]*" }}
      """
    And an existing platform with this module
    When I try to save these properties
      | name              | value  |
      | required-property |        |
      | pattern-property  | avalue |
    Then the request is rejected with a bad request error

  Scenario: saving a property without a value should return a bad request error
    Given an existing module with this template content
      """
      {{ property }}
      """
    And an existing platform with this module
    When I try to save these properties
      | name     |
      | property |
    Then the request is rejected with a bad request error

  Scenario: saving an iterable property without a value should return a bad request error
    Given an existing module with this template content
      """
      {{#a}}
      {{ property }}
      {{/a}}
      """
    And an existing platform with this module
    When I try to save these iterable properties
      | iterable | bloc   | name     |
      | a        | bloc-1 | property |
    Then the request is rejected with a bad request error

  Scenario: saving a required property without value when it's also defined as a= global property should not return an error
    Given an existing module with this template content
      """
      {{ property }}
      {{ global-property | @required }}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    When I save these properties
      | name     | value |
      | property | value |
    Then the properties are successfully saved

  Scenario: save properties of 2 modules of the same platform simultaneously
    Given an existing module named "toto"
    And an existing module named "tata"
    And an existing platform with those modules
    When I update the properties of those modules one after the other using the same platform version_id
    Then the properties are successfully saved for those modules
    And the platform version_id is incremented twice

  Scenario: save properties of a module and global properties simultaneously
    Given an existing module
    And an existing platform with this module
    When I update the module properties and then the platform global properties
    Then the properties are successfully saved
    And the platform version_id is incremented twice

  Scenario: reject a platform update that has had a property update
    Given an existing module
    And an existing platform with this module
    When I try to update the module properties and then the platform using the same platform version_id
    Then the platform update is rejected with a conflict error

  Scenario: reject updating properties of the same module concurrently
    Given an existing module
    And an existing platform with this module
    When I try to update the properties of this module twice with the same deployed module version_id
    Then the properties update is rejected with a conflict error

  Scenario: an update of a platform after an update of properties should not impact the properties version_id

  Scenario: fail trying update global properties simultaneously
