Feature: Get properties with details

  Background:
    Given an authenticated user

  Scenario: get properties details of platform
    Given an existing module
    And an existing platform with this module
    When I get the properties with details of this platforms
    Then the properties with theirs details are successfully retrieved

  Scenario: get properties details of platform with module and techno properties
    Given an existing techno with properties
    And an existing module with properties and this techno
    And an existing platform with this module and valued properties
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

  Scenario: get properties details of platform with valued properties and iterables properties
    Given an existing module with this template content
    """
     {{ simple-property }}
     {{ set-default-property | @default 42 }}
     {{ unset-default-property | @default 42 }}
     {{#iterable_property}}{{ some-property | a comment}}{{/iterable_property}}
     """
    And an existing platform with this module
    And the platform has these iterable properties
      | iterable          | bloc   | name          | value      |
      | iterable_property | bloc-1 | some-property | iterable_1 |
    When I get the platform properties with details for this module
    Then the properties with details and its contain are successfully retrieved
    And the properties details match these values
      | name                   | storedValue | finalValue | defaultValue | transformations |
      | simple-property        |             |            |              |                 |
      | set-default-property   |             |            | 42           |                 |
      | unset-default-property |             |            | 42           |                 |
      | some-property          | iterable_1  | iterable_1 |              |                 |

  Scenario: get properties details of platform with valued properties that reference global property
    Given an existing module with this template content
    """
      {{ simple-property }}
      {{ set-default-property | @default 31 }}
      {{ unset-default-property | @default 17 }}
      {{ some-property | a comment}}
     """
    And an existing platform with this module
    And the platform has these valued properties
      | name                   | value        |
      | some-property          | global_value |
      | unset-default-property | 19           |
    When I get the platform properties with details for this module
    Then the properties with details and its contain are successfully retrieved
    And the properties details match these values
      | name                   | storedValue  | finalValue   | defaultValue | transformations |
      | simple-property        |              |              |              |                 |
      | set-default-property   |              |              | 31           |                 |
      | unset-default-property | 19           | 19           | 17           |                 |
      | some-property          | global_value | global_value |              |                 |

  Scenario: get properties details of global property referenced another global property
    Given an existing module with this template content
    """
     {{ simple-property }}
     {{ set-default-property | @default 11 }}
     {{ unset-default-property | @default 23 }}
     {{ another-global-property | @default 17 }}
     """
    And an existing platform with this module
    And the platform has these valued properties
      | name                    | value               |
      | global-property         | global_value        |
      | unset-default-property  | 19                  |
      | another-global-property | {{global-property}} |
    When I get the platform properties with details for this module
    Then the properties with details and its contain are successfully retrieved
    And the properties details match these values
      | name                    | storedValue         | finalValue   | defaultValue | transformations |
      | simple-property         |                     |              |              |                 |
      | set-default-property    |                     |              | 11           |                 |
      | unset-default-property  | 19                  | 19           | 23           |                 |
      | another-global-property | {{global-property}} | global_value | 17           |                 |

  Scenario: get properties details with global properties used in instance property values
    Given an existing module with this template content
      """
      {{ module-property }}
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
      | name              | value                 |
      | instance-property | {{ global-property }} |
    When I get the platform properties with details for this module
    Then the properties with details and its contain are successfully retrieved
    And the properties details match these values
      | name            | storedValue             | finalValue | defaultValue | transformations |
      | module-property |                         |            |              |                 |
      | some-property   | {{ instance-property }} |            |              |                 |

  Scenario: get properties details of module valued by another valued property
    Given an existing module with this template content
      """
      {{ some-property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name            | value                 |
      | some-property   | {{ some-property-a }} |
      | some-property-a | {{ some-property-b }} |
      | some-property-b | property-value        |
    When I get the platform properties with details for this module
    Then the properties with details and its contain are successfully retrieved
    And the properties details match these values
      | name          | storedValue           | finalValue     | defaultValue | transformations |
      | some-property | {{ some-property-a }} | property-value |              |                 |
