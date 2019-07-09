package ca.edtoaster.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutionStep {
    private final Collection<ExecutionStep> parents;
    public final String name;
    private final ExecutionState state;
    private final ExecutorCompletionService<Optional<Artifact>> executorCompletionService;

    public ExecutionStep(String name, Collection<ExecutionStep> parents) {
        this.parents = parents;
        this.name = name;
        this.state = new ExecutionState();
        this.executorCompletionService = new ExecutorCompletionService<>(Executors.newWorkStealingPool());
    }

    public synchronized final Optional<Artifact> execute() {
        if (!state.isPending()) {
            return state.getOutputArtifact();
        } else {
            for (ExecutionStep step : this.parents) {
                executorCompletionService.submit(step::execute);
            }

            int numFinished = 0;
            boolean error = false;

            Map<String, Artifact> artifacts = new HashMap<>();

            while (numFinished < parents.size()) {
                try {
                    Future<Optional<Artifact>> parentFuture = executorCompletionService.take();
                    Optional<Artifact> parentArtifact = parentFuture.get();

                    if (parentArtifact.isPresent()) {
                        Artifact artifact = parentArtifact.get();
                        artifacts.put(artifact.name, artifact);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    error = true;
                    e.printStackTrace();
                } finally {
                    numFinished ++;
                }
            }

            if (error) {
                System.err.println("Error on subtasks of task " + this.name);
                return Optional.empty();
            }

            this.state.state = ExecutionStateEnum.IN_PROGRESS;
            Optional<Artifact> ret = doStep(artifacts);
            state.setOutputArtifact(ret.orElse(null));
            this.state.state = ExecutionStateEnum.SUCCESS;
            return ret;
        }
    }

    public Optional<Artifact> doStep(Map<String, Artifact> artifacts) {
        return Optional.empty();
    }
}
