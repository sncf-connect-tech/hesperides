Feature: Get properties with details

  Background:
    Given an authenticated user

  Scenario: get the properties details of a platform without properties
    Given an existing module
    And an existing platform with this module
    When I get the detailed properties of this module
    Then the properties details are successfully retrieved and they are empty

  Scenario: get the properties details of a platform with valued properties
    Given an existing module with this template content
      """
      {{ simple-property }}
      {{ another-property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name             | value            |
      | simple-property  | simple-value     |
      | another-property | {{stored-value}} |
      | stored-value     | final-value      |
    When I get the detailed properties of this module
    Then the properties details match these values
      | name             | storedValue      | finalValue   | defaultValue | transformations               |
      | stored-value     | final-value      | final-value  |              |                               |
      | another-property | {{stored-value}} | final-value  |              | PROPERTY_SUBSTITUTION_LEVEL_1 |
      | simple-property  | simple-value     | simple-value |              |                               |

  Scenario: get the properties details of a platform with default properties
    Given an existing module with this template content
      """
      {{ default-property | @default 42 }}
      {{ overridden-default-property | @default 42 }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name                        | value |
      | overridden-default-property | 12    |
    When I get the detailed properties of this module
    Then the properties details match these values
      | name                        | storedValue | finalValue | defaultValue | transformations |
      | overridden-default-property | 12          | 12         | 42           |                 |
      | default-property            |             | 42         | 42           |                 |

  Scenario: get the properties details of a platform with valued properties referencing global properties
    Given an existing module with this template content
    """
      {{ global-property }}
      {{ ref-global-property }}
     """
    And an existing platform with this module
    And the platform has these valued properties
      | name                | value                       |
      | ref-global-property | {{another-global-property}} |
    And the platform has these global properties
      | name                    | value                |
      | global-property         | global-value         |
      | another-global-property | another-global-value |
    When I get the detailed properties of this module
    Then the properties details match these values
      | name                | storedValue                 | finalValue           | defaultValue | transformations               |
      | ref-global-property | {{another-global-property}} | another-global-value |              | PROPERTY_SUBSTITUTION_LEVEL_1 |
      | global-property     |                             | global-value         |              | OVERRIDDEN_BY_GLOBAL          |

  Scenario: get the properties details of a platform with a global property referencing another global property
    Given an existing platform
    And the platform has these global properties
      | name                    | value                       |
      | global-property         | {{another-global-property}} |
      | another-global-property | global-value                |
    When I get the detailed properties of this platform
    Then the properties details match these values
      | name                    | storedValue                 | finalValue   | defaultValue | transformations                                     |
      | global-property         | {{another-global-property}} | global-value |              | OVERRIDDEN_BY_GLOBAL, PROPERTY_SUBSTITUTION_LEVEL_1 |
      | another-global-property | global-value                | global-value |              | OVERRIDDEN_BY_GLOBAL                                |

  Scenario: get the properties details of a platform with iterable valued properties
    Given an existing module with this template content
    """
     {{#iterable_property}}{{ some-property | a comment}}{{/iterable_property}}
     """
    And an existing platform with this module
    And the platform has these iterable properties
      | iterable          | bloc   | name          | value      |
      | iterable_property | bloc-1 | some-property | iterable_1 |
    When I get the detailed properties of this module
    Then the properties details are successfully retrieved and they are empty