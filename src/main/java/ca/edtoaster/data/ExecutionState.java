package ca.edtoaster.data;

import java.util.Optional;

public class ExecutionState {

    public ExecutionStateEnum state;
    private Artifact outputArtifact;

    public ExecutionState() {
        this.state = ExecutionStateEnum.PENDING;
        outputArtifact = null;
    }

    public Optional<Artifact> getOutputArtifact() {
        return Optional.ofNullable(outputArtifact);
    }

    public void setOutputArtifact(Artifact artifact) {
        this.outputArtifact = artifact;
    }

    public boolean isPending() {
        return state == ExecutionStateEnum.PENDING;
    }

    public boolean isInProgress() {
        return state == ExecutionStateEnum.IN_PROGRESS;
    }

    public boolean isSuccess() {
        return state == ExecutionStateEnum.SUCCESS;
    }

    public boolean isFailure() {
        return state == ExecutionStateEnum.FAILURE;
    }

}
