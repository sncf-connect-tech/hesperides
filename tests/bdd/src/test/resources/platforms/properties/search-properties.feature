Feature: Search properties

  Background:
    Given an authenticated user

  Scenario: trying to search properties without a name or a value should fail
    When I try to search for properties without a name or a value
    Then the request is rejected with a bad request error

  Scenario: search a property by exact name and/or value
    Given an existing module
    And an existing platform with this module
    And the platform has these valued properties
      | name                    | value        |
      | simple-property         | simple-value |
      | another-property        | simple-value |
      | another-simple-property | other-value  |
    When I search for properties by name "another-property"
    Then the list of properties found is
      | propertyName     | propertyValue |
      | another-property | simple-value  |
    When I search for properties by value "simple-value"
    Then the list of properties found is
      | propertyName     | propertyValue |
      | simple-property  | simple-value  |
      | another-property | simple-value  |
    When I search for properties by name "simple-property" and value "simple-value"
    Then the list of properties found is
      | propertyName    | propertyValue |
      | simple-property | simple-value  |

  Scenario: search a production password by name and/or value as a production user
    Given a template with the following content
      """
      {{password|@password}}
      """
    And an existing module with this template
    And an existing prod platform with this module
    And the platform has these valued properties
      | name     | value  |
      | password | SECRET |
    And an authenticated prod user
    When I search for properties by name "password"
    Then the list of properties found is
      | propertyName | propertyValue |
      | password     | SECRET        |
    When I search for properties by value "SECRET"
    Then the list of properties found is
      | propertyName | propertyValue |
      | password     | SECRET        |
    When I search for properties by name "password" and value "SECRET"
    Then the list of properties found is
      | propertyName | propertyValue |
      | password     | SECRET        |

  Scenario: search a production password by name and/or value as a lambda user
    Given a template with the following content
      """
      {{password|@password}}
      """
    And an existing module with this template
    And an existing prod platform with this module
    And the platform has these valued properties
      | name     | value  |
      | password | SECRET |
    And an authenticated lambda user
    When I search for properties by name "password"
    Then the list of properties found is
      | propertyName | propertyValue |
      | password     | ******        |
    When I search for properties by value "SECRET"
    Then the list of properties found is empty
    When I search for properties by name "password" and value "SECRET"
    Then the list of properties found is empty

  Scenario: search a non-production password by name and/or value as a lambda user
    Given a template with the following content
      """
      {{password|@password}}
      """
    And an existing module with this template
    And an existing platform with this module
    And the platform has these valued properties
      | name     | value  |
      | password | SECRET |
    And an authenticated lambda user
    When I search for properties by name "password"
    Then the list of properties found is
      | propertyName | propertyValue |
      | password     | SECRET        |
    When I search for properties by value "SECRET"
    Then the list of properties found is
      | propertyName | propertyValue |
      | password     | SECRET        |
    When I search for properties by name "password" and value "SECRET"
    Then the list of properties found is
      | propertyName | propertyValue |
      | password     | SECRET        |

  Scenario: search a property by name in archived module
    Given an existing module
    And an existing platform with this module
    And the platform has these valued properties
      | name       | value   |
      | property-a | value-1 |
    And a copy of this module in version "2.0"
    And I update this platform, upgrading its module version to "2.0"
    And the platform has these valued properties
      | name       | value   |
      | property-b | value-2 |
    When I search for properties by name "property-a"
    Then the list of properties found is empty
    When I search for properties by name "property-b"
    Then the list of properties found is
      | propertyName | propertyValue |
      | property-b   | value-2       |
    When I search for properties by value "value-1"
    Then the list of properties found is empty
    When I search for properties by value "value-2"
    Then the list of properties found is
      | propertyName | propertyValue |
      | property-b   | value-2       |
    When I search for properties by name "property-a" and value "value-1"
    Then the list of properties found is empty
    When I search for properties by name "property-b" and value "value-2"
    Then the list of properties found is
      | propertyName | propertyValue |
      | property-b   | value-2       |

  Scenario: search a property in multiple applications
    Given an existing module
    And an existing platform in application "A" with this module
    And the platform has these valued properties
      | name     | value |
      | property | value |
    And an existing platform in application "B" with this module
    And the platform has these valued properties
      | name     | value |
      | property | value |
    When I search for properties by name "property"
    Then the list of properties found is
      | propertyName | propertyValue | applicationName |
      | property     | value         | A               |
      | property     | value         | B               |

  #issue-872
  Scenario: trying to search properties with less than 3 characters should fail
    When I try to search for properties by name "ab"
    Then the request is rejected with a bad request error
    When I try to search for properties by value "ab"
    Then the request is rejected with a bad request error
    When I try to search for properties by name "ab" and value "de"
    Then the request is rejected with a bad request error
    When I try to search for properties by name "abc" and value "de"
    Then the request is successful
    When I try to search for properties by name "ab" and value "cde"
    Then the request is successful

  #issue-872
  Scenario: search a property by partial name and/or value
    Given an existing module
    And an existing platform with this module
    And the platform has these valued properties
      | name                    | value        |
      | simple-property         | simple-value |
      | another-property        | simple-value |
      | another-simple-property | other-value  |
    When I search for properties by name "simple-"
    Then the list of properties found is
      | propertyName            | propertyValue |
      | simple-property         | simple-value  |
      | another-simple-property | other-value   |
    When I search for properties by value "simple-"
    Then the list of properties found is
      | propertyName     | propertyValue |
      | simple-property  | simple-value  |
      | another-property | simple-value  |
    When I search for properties by name "simple-" and value "simple-"
    Then the list of properties found is
      | propertyName    | propertyValue |
      | simple-property | simple-value  |

    #todo essayer de trouver un test de cas marginal + mettre à jour le LADR
