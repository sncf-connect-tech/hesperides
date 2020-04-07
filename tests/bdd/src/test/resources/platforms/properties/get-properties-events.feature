Feature: Get properties events

  Background:
    Given an authenticated user

  Scenario: get properties events
    Given an existing module with this template content
      """
      {{ added-property }}
      {{ modified-property }}
      {{ removed-property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name              | value |
      | added-property    | val   |
      | modified-property | val-1 |
      | removed-property  | val   |
    And the platform has these valued properties
      | name              | value |
      | added-property    | val   |
      | modified-property | val-2 |
      | another-property  | val   |
    When I get the properties events
    Then the properties event at index 0 has these added properties
      | name             | value |
      | another-property | val   |
    And the properties event at index 0 has these updated properties
      | name              | old_value | new_value |
      | modified-property | val-1     | val-2     |
    And the properties event at index 0 has these removed properties
      | name             | value |
      | removed-property | val   |
    And the properties event at index 1 has these added properties
      | name              | value |
      | removed-property  | val   |
      | modified-property | val-1 |
      | added-property    | val   |

  Scenario: get properties events with hidden passwords
    Given an existing module with this template content
      """
      {{ password-property | @password }}
      """
    And an existing prod platform with this module
    And the platform has these valued properties
      | name              | value    |
      | password-property | SECRET-1 |
    And the platform has these valued properties
      | name              | value    |
      | password-property | SECRET-2 |
    And an authenticated lambda user
    When I get the properties events
    Then the properties event at index 0 has these updated properties
      | name              | old_value | new_value |
      | password-property | ********  | ********  |
    And the properties event at index 1 has these added properties
      | name              | value    |
      | password-property | ******** |

  Scenario: get properties events with displayed prod passwords
    Given an existing module with this template content
      """
      {{ password-property | @password }}
      """
    And an existing prod platform with this module
    And the platform has these valued properties
      | name              | value    |
      | password-property | SECRET-1 |
    And the platform has these valued properties
      | name              | value    |
      | password-property | SECRET-2 |
    When I get the properties events
    Then the properties event at index 0 has these updated properties
      | name              | old_value | new_value |
      | password-property | SECRET-1  | SECRET-2  |
    And the properties event at index 1 has these added properties
      | name              | value    |
      | password-property | SECRET-1 |

  Scenario: get global properties events
    Given an existing platform
    And the platform has these global properties
      | name              | value |
      | added-property    | val   |
      | modified-property | val-1 |
      | removed-property  | val   |
    And the platform has these global properties
      | name              | value |
      | added-property    | val   |
      | modified-property | val-2 |
      | another-property  | val   |
    When I get the global properties events
    Then the properties event at index 0 has these added properties
      | name             | value |
      | another-property | val   |
    And the properties event at index 0 has these updated properties
      | name              | old_value | new_value |
      | modified-property | val-1     | val-2     |
    And the properties event at index 0 has these removed properties
      | name             | value |
      | removed-property | val   |
    And the properties event at index 1 has these added properties
      | name              | value |
      | removed-property  | val   |
      | modified-property | val-1 |
      | added-property    | val   |

  Scenario: get global properties events with page and size
    Given an existing platform
    And the platform has these global properties
      | name     | value |
      | property | val-1 |
    And the platform has these global properties
      | name     | value |
      | property | val-2 |
    And the platform has these global properties
      | name     | value |
      | property | val-3 |
    And the platform has these global properties
      | name     | value |
      | property | val-4 |
    When I get the global properties events with page 1 and size 3
    Then the properties event at index 0 has these updated properties
      | name     | old_value | new_value |
      | property | val-3     | val-4     |
    And the properties event at index 1 has these updated properties
      | name     | old_value | new_value |
      | property | val-2     | val-3     |
    And the properties event at index 2 has these updated properties
      | name     | old_value | new_value |
      | property | val-1     | val-2     |
    When I get the global properties events with page 2 and size 3
    Then the properties event at index 0 has these added properties
      | name     | value |
      | property | val-1 |

  Scenario: get properties events with page and size
    Given an existing module with this template content
      """
      {{ property }}
      """
    And an existing platform with this module
    And the platform has these valued properties
      | name     | value |
      | property | val-1 |
    And the platform has these valued properties
      | name     | value |
      | property | val-2 |
    And the platform has these valued properties
      | name     | value |
      | property | val-3 |
    And the platform has these valued properties
      | name     | value |
      | property | val-4 |
    When I get the properties events with page 1 and size 3
    Then the properties event at index 0 has these updated properties
      | name     | old_value | new_value |
      | property | val-3     | val-4     |
    And the properties event at index 1 has these updated properties
      | name     | old_value | new_value |
      | property | val-2     | val-3     |
    And the properties event at index 2 has these updated properties
      | name     | old_value | new_value |
      | property | val-1     | val-2     |
    When I get the properties events with page 2 and size 3
    Then the properties event at index 0 has these added properties
      | name     | value |
      | property | val-1 |
