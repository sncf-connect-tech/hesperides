package org.hesperides.presentation.controllers;

import lombok.Value;

@Value
public class RightsInput {
    FileRights user;
    FileRights group;
    FileRights other;

    @Value
    public static class FileRights {
        Boolean read;
        Boolean write;
        Boolean execute;
    }
}
