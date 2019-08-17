Feature: Get technos names

  Background:
    Given an authenticated user

  Scenario: get a list of all the technos names
    Given a list of 12 technos with different names
    When I get the technos name
    Then a list of 12 elements is returned

  Scenario: get a list of all the technos name
    Given a list of 12 technos with the same name
    When I get the technos name
    Then a list of 1 element is returned