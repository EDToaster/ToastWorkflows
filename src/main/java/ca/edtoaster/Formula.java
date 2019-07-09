package ca.edtoaster;

import ca.edtoaster.data.Artifact;
import ca.edtoaster.data.ExecutionStep;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formula {

    public static void main(String[] args) throws URISyntaxException, IOException {
        String path = "/formula.list";
        URL url = Formula.class.getResource(path);

        File file = new File(url.toURI());
        BufferedReader reader = new BufferedReader(new FileReader(file));

        Map<String, ExecutionStep> stepMap = new HashMap<>();
        ExecutionStep target = null;
        String line;
        while ((line = reader.readLine()) != null) {
            Optional<ExecutionStep> step = generateStep(stepMap, line);
            if (step.isPresent()) {
                ExecutionStep executionStep = step.get();
                target = executionStep;
            }
        }

        if (target != null) {
            Optional<Artifact> artifact = target.execute();
            artifact.ifPresent(value -> System.out.println(value.toString()));
        }
    }

    public static Pattern evalPattern = Pattern.compile("^([\\w]+?)\\s*?([+\\-*/^])\\s*?([\\w]+?)$");
    public static Optional<ExecutionStep> generateStep(Map<String, ExecutionStep> stepMap, String formula) {
        String[] split = formula.split("=");

        if (split.length != 2) return Optional.empty();

        String target = split[0].trim();
        String eval = split[1].trim();

        if (eval.equals("in")) {
            ExecutionStep step = new InputStep(target);
            stepMap.put(target, step);
            return Optional.of(step);
        } else {
            Matcher matcher = evalPattern.matcher(eval);
            if (matcher.find()) {
                String a = matcher.group(1);
                char op = matcher.group(2).charAt(0);
                String b = matcher.group(3);

                List<ExecutionStep> steps = new ArrayList<>();

                try {
                    ExecutionStep stepA = stepMap.get(a);
                    ExecutionStep stepB = stepMap.get(b);

                    steps.add(stepA);
                    steps.add(stepB);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return Optional.empty();
                }

                ExecutionStep step = new BiFormulaStep(target, steps, a, b, op);
                stepMap.put(target, step);

                return Optional.of(step);
            } else {
                return Optional.empty();
            }
        }
    }

    public static class BiFormulaStep extends ExecutionStep {

        public String a, b;
        public char op;

        public BiFormulaStep(String name, Collection<ExecutionStep> parents, String a, String b, char op) {
            super(name, parents);
            this.a = a;
            this.b = b;
            this.op = op;
        }

        @Override
        public Optional<Artifact> doStep(Map<String, Artifact> artifacts) {
            Artifact artifactA = artifacts.getOrDefault(a, null);
            Artifact artifactB = artifacts.getOrDefault(b, null);

            boolean missing = false;
            if (artifactA == null) {
                System.err.println("Parent steps have not finished for task " + name + ": a = " + a);
                missing = true;
            } else if (artifactB == null) {
                System.err.println("Parent steps have not finished for task " + name + ": b = " + b);
                missing = true;
            }

            if (missing) {
                return Optional.empty();
            }

            double a = Double.parseDouble(artifactA.payload);
            double b = Double.parseDouble(artifactB.payload);

            double answer;
            switch(op) {
                case '+': answer = a + b; break;
                case '-': answer = a - b; break;
                case '*': answer = a * b; break;
                case '/': answer = a / b; break;
                case '^': answer = Math.pow(a, b); break;
                default: answer = 0; break;
            }

            String answerString = Double.toString(answer);
            Artifact artifact = new Artifact(name, answerString);
            return Optional.of(artifact);
        }
    }


    public static class InputStep extends ExecutionStep {

        public static final Scanner scanner = new Scanner(System.in);

        public InputStep(String name) {
            super(name, Collections.emptyList());
        }

        @Override
        public Optional<Artifact> doStep(Map<String, Artifact> artifacts) {

            double in;
            synchronized (scanner) {
                System.out.print(name + " = ");
                in = scanner.nextDouble();
            }
            String inString = Double.toString(in);
            Artifact artifact = new Artifact(name, inString);
            return Optional.of(artifact);
        }

    }
}
