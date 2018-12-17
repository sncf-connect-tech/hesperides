Feature: Get file

  Background:
    Given an authenticated user

  Scenario: get file
    Given an existing module with a template with properties
    And an existing platform with this module and valued properties
    When I get the module template file
    Then the file is successfully retrieved

  Scenario: get file with iterable-ception properties

  Scenario: get file with valued properties
    Given an existing module with this template content
      """
      {{ simple-property }}
      {{ another-property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name             | value        |
      | simple-property  | first-value  |
      | another-property | second-value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      first-value
      second-value
      """

  Scenario: get file with default values
    Given an existing module with this template content
      """
      {{ default-property | @default default-value }}
      {{ default-with-value | @default default-value }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name               | value       |
      | default-with-value | other-value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      default-value
      other-value
      """

  Scenario: get file with iterable and default values
    Given an existing module with this template content
      """
      {{#a}}
        {{ simple-property }}
        {{ default-property | @default default-value }}
        {{ default-with-value | @default default-value }}
      {{/a}}
      """
    And an existing platform with this module
    And the platform has these iterable properties
      | iterable | bloc   | name               | value     |
      | a        | bloc-1 | simple-property    | value-1   |
      | a        | bloc-2 | simple-property    | value-2   |
      | a        | bloc-1 | default-with-value | default-1 |
      | a        | bloc-2 | default-with-value | default-2 |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
        value-1
        default-value
        default-1
        value-2
        default-value
        default-2

      """

  Scenario: get file with global properties
    Given an existing module with this template content
      """
      {{ global-property }}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      global-value
      """

  Scenario: get file with instance properties
    Given an existing module with this template content
      """
      {{ some-property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name          | value                   |
      | some-property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value          |
      | instance-property | instance-value |
    When I get the instance template file
    Then the file is successfully retrieved and contains
      """
      instance-value
      """

  Scenario: get file with global properties used in property values
    Given an existing module with this template content
      """
      {{ some-property }}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    And the platform has these valued properties
      | name          | value                 |
      | some-property | {{ global-property }} |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
      global-value
      """

  Scenario: get file with global properties used in iterable properties
    Given an existing module with this template content
      """
      {{#a}}
        {{ global-property }}
        {{ will-be-replaced-by-global-value }}
      {{/a}}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    And the platform has these iterable properties
      | iterable | bloc   | name                             | value                 |
      | a        | bloc-1 | will-be-replaced-by-global-value | {{ global-property }} |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
        global-value
        global-value

      """

  Scenario: get file with global properties used in instance property values
    Given an existing module with this template content
      """
      {{ some-property }}
      """
    And an existing platform with this module
    And the platform has these global properties
      | name            | value        |
      | global-property | global-value |
    And the platform has these valued properties
      | name          | value                   |
      | some-property | {{ instance-property }} |
    And the platform has these instance properties
      | name              | value        |
      | instance-property | global-value |
    When I get the module template file
    Then the file is successfully retrieved and contains
      """
        global-value
      """

    # technos, iterable-ception