package azari.amirhossein.dfa_minimization.models;

import azari.amirhossein.dfa_minimization.MinimizedDFAController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class MinimizationProcess {
    private final ArrayList<String> symbols;
    private final ArrayList<String> states;
    private final String startState;
    private final ArrayList<String> finalStates;
    HashMap<String, HashMap<String, String>> graph;
    ArrayList<ArrayList<String>> resultState = new ArrayList<>();
    Button confirmBtn;

    public MinimizationProcess(ArrayList<String> symbols, ArrayList<String> states, String startState, ArrayList<String> finalStates, HashMap<String, HashMap<String, String>> graph, Button confirmBtn) {
        this.symbols = symbols;
        this.states = states;
        this.startState = startState;
        this.finalStates = finalStates;
        this.graph = graph;
        this.confirmBtn = confirmBtn;
    }

    public void start() {
        try {
            // Validate DFA before starting the process
            validateDFA();

            // Remove unreachable states
            deleteState();

            int numOfState = states.size();
            String[][] table = new String[numOfState][numOfState];
            ArrayList<ArrayList<String>> different = new ArrayList<>();
            ArrayList<ArrayList<String>> unknown = new ArrayList<>();
            ArrayList<String> check = new ArrayList<>();
            ArrayList<String> checkRevers;

            initTable(table);

            // Find initial distinct states (final and non-final states)
            findInitialDistinctStates(different);
            markTable(different, table);

            // Find unknown pairs
            findUnknownPairs(unknown, table);

            // Process unknown pairs
            processUnknownPairs(different, unknown);
            markTable(different, table);

            resultState = resultState(table, unknown);
            minimizationDFA(resultState);

        } catch (IllegalStateException e) {
            showError("DFA Validation Error", e.getMessage());
        } catch (Exception e) {
            showError("Unexpected Error", "An error occurred while processing DFA: " + e.getMessage());
        }
    }

    private void validateDFA() throws IllegalStateException {
        List<String> warnings = new ArrayList<>();
        
        // Check for states existence
        if (states == null || states.isEmpty()) {
            throw new IllegalStateException("Error: No states defined.\nSuggestion: Add at least one state to your DFA.");
        }

        // Check for symbols existence
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalStateException("Error: No input symbols defined.\nSuggestion: Add at least one input symbol to your DFA.");
        }

        // Check for epsilon transitions (empty string transitions)
        if (symbols.contains("ε") || symbols.contains("")) {
            warnings.add("Warning: Epsilon transitions are not allowed in DFAs.\nSuggestion: Remove epsilon transitions as they are only valid in NFAs.");
        }

        // Validate start state
        if (startState == null || startState.isEmpty()) {
            throw new IllegalStateException("Error: No start state defined.\nSuggestion: Select one state as the start state.");
        }
        if (!states.contains(startState)) {
            throw new IllegalStateException("Error: Start state '" + startState + "' does not exist in the states list.\nSuggestion: Choose a valid state as the start state.");
        }

        // Validate final states
        if (finalStates == null || finalStates.isEmpty()) {
            throw new IllegalStateException("Error: No final states defined.\nSuggestion: Select at least one state as a final state.");
        }
        
        for (String finalState : finalStates) {
            if (!states.contains(finalState)) {
                throw new IllegalStateException("Error: Final state '" + finalState + "' does not exist in the states list.\nSuggestion: Remove or correct the invalid final state.");
            }
        }

        // Check if start state is also a final state (not an error, but might be unintended)
        if (finalStates.contains(startState)) {
            warnings.add("Note: Start state is also a final state. This means empty string will be accepted.");
        }

        // Check for potential dead states
        checkForDeadStates(warnings);

        // Validate transition table
        validateTransitionTable(warnings);

        // Show warnings if any exist
        if (!warnings.isEmpty()) {
            showWarnings(warnings);
        }
    }

    private void validateTransitionTable(List<String> warnings) throws IllegalStateException {
        if (graph == null) {
            throw new IllegalStateException("Error: Transition table is not initialized.\nSuggestion: Create a transition table for your DFA.");
        }

        // Check for missing transitions
        for (String state : states) {
            if (!graph.containsKey(state)) {
                throw new IllegalStateException("Error: No transitions defined for state '" + state + "'.\nSuggestion: Define transitions for all states.");
            }

            HashMap<String, String> transitions = graph.get(state);
            if (transitions == null) {
                throw new IllegalStateException("Error: Transitions for state '" + state + "' are null.\nSuggestion: Initialize transitions for state '" + state + "'.");
            }

            // Check for missing or invalid transitions
            for (String symbol : symbols) {
                if (!transitions.containsKey(symbol)) {
                    throw new IllegalStateException("Error: Missing transition for symbol '" + symbol + "' in state '" + state + "'.\nSuggestion: Define a transition for every symbol in each state.");
                }
                
                String targetState = transitions.get(symbol);
                if (targetState == null || targetState.isEmpty()) {
                    throw new IllegalStateException("Error: Empty transition for symbol '" + symbol + "' in state '" + state + "'.\nSuggestion: Specify a valid target state.");
                }
                
                if (!states.contains(targetState)) {
                    throw new IllegalStateException("Error: Invalid target state '" + targetState + "' for transition from state '" + state + "' with symbol '" + symbol + "'.\nSuggestion: Use only existing states as transition targets.");
                }
            }

            // Check for non-determinism (multiple transitions with same symbol)
            Set<String> usedSymbols = new HashSet<>();
            for (Map.Entry<String, String> transition : transitions.entrySet()) {
                if (!usedSymbols.add(transition.getKey())) {
                    throw new IllegalStateException("Error: Multiple transitions found for symbol '" + transition.getKey() + "' in state '" + state + "'.\nSuggestion: DFAs must have exactly one transition for each symbol in each state.");
                }
            }
        }
    }

    private void checkForDeadStates(List<String> warnings) {
        for (String state : states) {
            if (!finalStates.contains(state)) {
                boolean canReachFinal = canReachFinalState(state, new HashSet<>());
                if (!canReachFinal) {
                    warnings.add("Warning: State '" + state + "' cannot reach any final state.\nSuggestion: This might be a dead state. Consider adding a path to a final state or removing this state.");
                }
            }
        }
    }

    private boolean canReachFinalState(String currentState, Set<String> visited) {
        if (finalStates.contains(currentState)) {
            return true;
        }
        
        if (visited.contains(currentState)) {
            return false;
        }
        
        visited.add(currentState);
        
        HashMap<String, String> transitions = graph.get(currentState);
        if (transitions != null) {
            for (String targetState : transitions.values()) {
                if (canReachFinalState(targetState, visited)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private void showWarnings(List<String> warnings) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("DFA Validation Warnings");
        alert.setHeaderText("The following issues were found:");
        
        StringBuilder content = new StringBuilder();
        for (String warning : warnings) {
            content.append("• ").append(warning).append("\n\n");
        }
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void findInitialDistinctStates(ArrayList<ArrayList<String>> different) {
        int count = 0;
        for (String fState : finalStates) {
            for (String state : states) {
                if (!finalStates.contains(state)) {
                    different.add(new ArrayList<>());
                    different.get(count).add(fState);
                    different.get(count).add(state);
                    count++;
                }
            }
        }
    }

    private void findUnknownPairs(ArrayList<ArrayList<String>> unknown, String[][] table) {
        int count = 0;
        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < states.size(); j++) {
                if (i > j && !Objects.equals(table[i][j], "X")) {
                    String state1 = table[i][i];
                    String state2 = table[j][j];
                    unknown.add(new ArrayList<>());
                    unknown.get(count).add(state1);
                    unknown.get(count).add(state2);
                    count++;
                }
            }
        }
    }

    private void processUnknownPairs(ArrayList<ArrayList<String>> different, ArrayList<ArrayList<String>> unknown) {
        boolean flag = true;
        while (flag) {
            int size = different.size();
            for (int i = 0; i < unknown.size(); i++) {
                if (checkPairForDifference(different, unknown.get(i))) {
                    different.add(unknown.get(i));
                    unknown.remove(i);
                    i = Math.max(-1, i - 1);
                }
            }
            flag = different.size() != size;
        }
    }

    private boolean checkPairForDifference(ArrayList<ArrayList<String>> different, ArrayList<String> pair) {
        ArrayList<String> check = new ArrayList<>();
        for (String symbol : symbols) {
            check.clear();
            String state1 = graph.get(pair.get(0)).get(symbol);
            String state2 = graph.get(pair.get(1)).get(symbol);
            check.add(state1);
            check.add(state2);
            ArrayList<String> checkRevers = revers(check);
            if (different.contains(check) || different.contains(checkRevers)) {
                return true;
            }
        }
        return false;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void deleteState() {
        ArrayList<String> temp = new ArrayList<>();
        System.out.println("\n=== Starting State Removal Process ===");
        System.out.println("Initial states: " + states);
        System.out.println("Start state: " + startState);
        System.out.println("Final state: " + finalStates);

        temp.add(startState);

        for (int i = 0; i < temp.size(); i++) {
            for (String symbol : symbols) {
                String nextState = graph.get(temp.get(i)).get(symbol);
                if (!temp.contains(nextState)) {
                    temp.add(nextState);
                    System.out.println("Added reachable state: " + nextState + " through symbol: " + symbol + " from state: " + temp.get(i));
                }
            }
        }
        
        System.out.println("\nReachable states: " + temp);
        
        for (int i = 0; i < states.size(); i++) {
            if (!temp.contains(states.get(i))) {
                System.out.println("Removing unreachable state: " + states.get(i));
                graph.remove(states.get(i));
                states.remove(i);
                i--;
            }
        }
        System.out.println("Final states after removal: " + states);
        System.out.println("=== State Removal Process Complete ===\n");
    }


    public void initTable(String[][] table) {
        System.out.println(graph);
        int count = 0;
        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < states.size(); j++) {
                if (i == j) {
                    table[i][j] = states.get(count);
                    count++;
                    break;
                }
                if (i < j) {
                    table[i][j] = null;
                }
                if (i > j) {
                    table[i][j] = "O";
                }
            }
        }
    }

    //mark distinct state in table ---> X
    public void markTable(ArrayList<ArrayList<String>> different, String[][] table) {

        ArrayList<String> temp = new ArrayList<>();
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table.length; j++) {
                if (i > j) {
                    if (!Objects.equals(table[i][j], "X")) {
                        String state1 = table[i][i];
                        temp.add(state1);
                        String state2 = table[j][j];
                        temp.add(state2);
                        if (different.contains(temp)) {
                            table[i][j] = "X";

                        }
                        temp.clear();

                    } else {
                        break;
                    }
                }
            }
        }
    }

    //result minimization state of DFA
    public ArrayList<ArrayList<String>> resultState(String[][] table, ArrayList<ArrayList<String>> unknown) {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < states.size(); j++) {
                if (i == states.size() - 1) {
                    if (Objects.equals(table[i][j], "O")) {
                        break;
                    }
                    if (j == states.size() - 2 && Objects.equals(table[i][j], "X")) {
                        result.add(new ArrayList<>());
                        result.get(index).add(states.get(i));
                        index++;
                        break;

                    }

                } else {
                    if (Objects.equals(table[j][i], "O")) {
                        break;
                    }

                    if (j == states.size() - 1 && Objects.equals(table[j][i], "X")) {
                        result.add(new ArrayList<>());
                        result.get(index).add(states.get(i));
                        index++;

                    }
                }
            }
        }
        result.addAll(0, unknown);

        for (int i = 0; i < result.size(); i++) {
            for (int k = 0; k < result.size(); k++) {
                for (int j = 0; j < result.get(i).size(); j++) {

                    if (i == k) {
                        break;
                    } else {
                        if (result.get(k).contains(result.get(i).get(j))) {
                            result.get(i).remove(result.get(i).get(j));
                            for (int l = 0; l < result.get(k).size(); l++) {
                                if (!result.get(i).contains(result.get(k).get(l))) {
                                    result.get(i).add(result.get(k).get(l));
                                }

                            }
                            result.remove(result.get(k));
                            k = 0;
                        }
                    }
                }
            }
        }
        return result;
    }

    //minimization DFA
    public void minimizationDFA(ArrayList<ArrayList<String>> resultStates) {

        System.out.println("\n=== Starting DFA Minimization Process ===");
        System.out.println("Result states before mapping: " + resultStates);
        
        // Specify the final and start states in minimization DFA
        HashMap<ArrayList<String>, String> stateMapping = new HashMap<>();
        char stateName = 'A';

        for (ArrayList<String> stateGroup : resultStates) {
            stateMapping.put(stateGroup, String.valueOf(stateName));
            System.out.println("Mapping state group " + stateGroup + " to " + stateName);
            stateName++;
        }

        //print minimization DFA
        HashMap<String, HashMap<String, String>> minimizedGraph = new HashMap<>();
        System.out.println("\nTransitions for minimized DFA:");

        for (int i = 0; i < resultStates.size(); i++) {
            for (String symbol : symbols) {
                for (ArrayList<String> targetState : resultStates) {
                    if (targetState.contains(graph.get(resultStates.get(i).get(0)).get(symbol))) {
                        String currentState = stateMapping.get(resultStates.get(i));
                        String nextState = stateMapping.get(targetState);
                        minimizedGraph.putIfAbsent(currentState, new HashMap<>());
                        minimizedGraph.get(currentState).put(symbol, nextState);
                        System.out.println("Added transition: " + currentState + " --" + symbol + "--> " + nextState);
                        break;
                    }
                }
            }
        }

        String start = "";
        Set<String> finales = new HashSet<>();

        System.out.println("\n=== Processing States ===");
        System.out.println("Original final states: " + finalStates);
        System.out.println("Result states to process: " + resultStates);

        // Process start state
        for (ArrayList<String> state : resultStates) {
            if (state.contains(startState)) {
                start = stateMapping.get(state);
                System.out.println("Selected start state: " + start + " (mapped from " + state + ")");
            }
        }

        // Process final states
        for (ArrayList<String> state : resultStates) {
            System.out.println("\nChecking state group: " + state);
            for (String finalState : finalStates) {
                System.out.println("Checking if contains final state: " + finalState);
                if (state.contains(finalState)) {
                    String mappedState = stateMapping.get(state);
                    finales.add(mappedState);
                    System.out.println("Added final state: " + mappedState + " (mapped from group containing " + finalState + ")");
                }
            }
        }

        if (finales.isEmpty()) {
            System.out.println("WARNING: No final states were mapped in the minimized DFA!");
            showError("DFA Minimization Warning", "No final states were found in the minimized DFA. This may indicate an error in the minimization process.");
            return;
        }

        System.out.println("\nFinal states mapping complete");
        System.out.println("Final states in minimized DFA: " + finales);
        System.out.println("=== Final States Processing Complete ===\n");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/azari/amirhossein/dfa_minimization/minimizedDFA.fxml"));
            Parent root = loader.load();

            MinimizedDFAController controller = loader.getController();
            
            // Add debug print before setting DFA data
            System.out.println("\n=== Setting DFA Data in Controller ===");
            System.out.println("Start state: " + start);
            System.out.println("Final states being set: " + finales);
            System.out.println("Minimized graph: " + minimizedGraph);
            
            controller.setDFAData(start, finales, minimizedGraph);

            Scene mainScene = confirmBtn.getScene();
            controller.setMainScene(mainScene);

            Stage stage = (Stage) confirmBtn.getScene().getWindow();
            Scene newScene = new Scene(root);
            stage.setScene(newScene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private ArrayList<String> revers(ArrayList<String> checkRevers) {
        ArrayList<String> arrayReversed = new ArrayList<>();
        for (int i = checkRevers.size() - 1; i >= 0; i--) {
            arrayReversed.add(checkRevers.get(i));
        }
        return arrayReversed;

    }

}


