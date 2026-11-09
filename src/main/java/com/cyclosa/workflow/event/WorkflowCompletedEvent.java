package com.cyclosa.workflow.event;

import com.cyclosa.workflow.enums.RequestType;
import com.cyclosa.workflow.enums.StepDecision;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public class WorkflowCompletedEvent extends ApplicationEvent {

    private final UUID requestId;
    private final RequestType requestType;
    private final UUID referenceId;
    private final StepDecision finalDecision;
    private final String comment;

    public WorkflowCompletedEvent(Object source, UUID requestId, RequestType requestType, UUID referenceId, StepDecision finalDecision, String comment) {
        super(source);
        this.requestId = requestId;
        this.requestType = requestType;
        this.referenceId = referenceId;
        this.finalDecision = finalDecision;
        this.comment = comment;
    }
}

