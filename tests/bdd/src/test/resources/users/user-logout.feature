Feature: User logout

  Scenario: a user logout from a browser
    Given a known lambda user
    When the user log out
    Then the request is rejected with an unauthorized error
    When the user re-send valid credentials
    Then login is successful
