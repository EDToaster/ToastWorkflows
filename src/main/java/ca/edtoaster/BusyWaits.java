package ca.edtoaster;

import ca.edtoaster.data.Artifact;
import ca.edtoaster.data.ExecutionStep;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BusyWaits {

    public static void main(String[] args) {
        ExecutionStep target = generateRandomBusyWaits(0, "target", 3, 0.75d);
        target.execute();
    }

    public static long randomWait() {
        return (long) (Math.random() * 0);
    }

    public static ExecutionStep generateRandomBusyWaits(int depth, String id, int maxDepth, double probability) {
        System.out.println(String.format("Generate id %s", id));
        boolean doSubtasks = Math.random() < probability;
        if (depth > maxDepth || !doSubtasks) return new BusyStep(id, Collections.emptyList(), randomWait());
        else {
            // generate subtasks
            int num = (int) (Math.random() * 4) + 1;
            Collection<ExecutionStep> thisTask = IntStream.range(0, num).mapToObj((i) -> generateRandomBusyWaits(depth + 1, id + "-" + i, maxDepth, probability)).collect(Collectors.toList());
            return new BusyStep(id, thisTask, randomWait());
        }

    }


    public static class BusyStep extends ExecutionStep {

        private final long wait;
        public BusyStep(String name, Collection<ExecutionStep> parents, long wait) {
            super(name, parents);
            this.wait = wait;
        }

        @Override
        public Optional<Artifact> doStep(Map<String, Artifact> artifacts) {
            System.out.println(String.format("Step %s will wait for %d ms", this.name, this.wait));

            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(String.format("Step %s finished", this.name));
            return Optional.empty();
        }
    }
}
