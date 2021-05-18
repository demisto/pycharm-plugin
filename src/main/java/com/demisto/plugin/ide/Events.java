package com.demisto.plugin.ide;

import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

/**
 * Created by shacharh on 25/11/18.
 */
public interface Events extends EventListener {
    Topic<Events> DEMISTO_ARGUMENT_CHANGE = Topic.create("Demisto Argument Change", Events.class);
    Topic<Events> DEMISTO_DELETE = Topic.create("Demisto Delete Property", Events.class);

    /**
     * Used to notify event change
     */
    void updateDemistoEvent();

    void deleteDemistoCommand(@NotNull String commandName);

    void deleteDemistoArgument(@NotNull String argumentName, @NotNull String commandName);

    void deleteDemistoParameter(@NotNull String parameterName);

    void deleteDemistoOutput(@NotNull String contextPath, @NotNull String commandName);
}