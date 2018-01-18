Feature: Get modules

  Scenario: Retrieve existing modules
    Given There is at least one existing module
    When a user retrieves the module's list
    Then he should get the modules' list

  Scenario: Retrieve empty list of modules
    Given There is no modules
    When a user retrieves the module's list
    Then he should get an empty list

