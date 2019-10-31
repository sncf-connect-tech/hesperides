Feature: Get properties with details

  Background:
    Given an authenticated user

  Scenario: get properties details of platform
    Given an existing module
    And an existing platform with this module
    When I get the properties with details of this platforms
    Then the properties with theirs details are successfully retrieved


  Scenario: get properties details of platform with valued properties
    Given an existing module with this template content
      """
      {{ simple-property }}
      {{ set-default-property | @default 42 }}
      {{ unset-default-property | @default 42 }}
      """
    And an existing platform with this module
    When I get the platform properties with details for this module
    Then the properties with details and its contain are successfully retrieved
    And the properties details match these values
      | name                   | storedValue | finalValue | defaultValue | transformations |
      | simple-property        |             |            |              |                 |
      | set-default-property   |             |            | 42           |                 |
      | unset-default-property |             |            | 42           |                 |

