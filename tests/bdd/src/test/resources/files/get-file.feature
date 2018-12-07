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
    And an existing platform with these valued properties
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
    And an existing platform with these valued properties
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
    And an existing platform with these iterable valued properties
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

    #Propriétés globales, propriétés d'instances, technos, propriété globale ayant le même nom qu'une propriété d'instance ou inversement
  # Propriétés itérables
  # @required, pattern, default, etc.