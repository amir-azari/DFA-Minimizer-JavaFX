package azari.amirhossein.dfa_minimization.models;

import azari.amirhossein.dfa_minimization.MinimizedDFAController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class MinimizationProcess {
    private ArrayList<String> symbols;
    private ArrayList<String> states;
    private String startState;
    private ArrayList<String> finalStates;
    HashMap<String, HashMap<String, String>> graph;
    ArrayList<ArrayList<String>> resultState = new ArrayList<>();
    Button confirmBtn ;
    private Scene mainScene;
    public MinimizationProcess(ArrayList<String> symbols, ArrayList<String> states, String startState, ArrayList<String> finalStates, HashMap<String, HashMap<String, String>> graph , Button confirmBtn) {
        this.symbols = symbols;
        this.states = states;
        this.startState = startState;
        this.finalStates = finalStates;
        this.graph = graph;
        this.confirmBtn = confirmBtn;
    }


    public void start() {

        // Remove all the states that are unreachable from the start state via any
        deleteState();

        int numOfState = states.size();

        //create table
        String[][] table = new String[numOfState][numOfState];

        //Distinct states
        ArrayList<ArrayList<String>> different = new ArrayList<>();
        //Unknown states
        ArrayList<ArrayList<String>> unknown = new ArrayList<>();

        //To check pair statuses
        ArrayList<String> check = new ArrayList<>();
        ArrayList<String> checkRevers;

        //init table
        initTable(table);

        //state-finalState ---> different
        int count = 0;
        for (String fState : finalStates) {
            for (String state : states) {
                //check all state except final state
                if (!finalStates.contains(state)) {
                    different.add(new ArrayList<>());
                    different.get(count).add(fState);
                    different.get(count).add(state);
                    count++;

                }
            }
        }
        //Marking in the table ---> X
        markTable(different, table);


        //Add Unknown pairs of state
        count = 0;
        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < states.size(); j++) {

                if (i > j) {
                    if (!Objects.equals(table[i][j], "X")) {
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
        //check Unknown pair of state
        boolean flag = true;
        int size;
        while (flag) {
            size = different.size();
            for (int i = 0; i < unknown.size(); i++) {
                int k = 0;
                for (String symbol : symbols) {
                    check.clear();
                    String state1 = this.graph.get(unknown.get(i).get(k)).get(symbol);
                    check.add(state1);
                    String state2 = this.graph.get(unknown.get(i).get(k + 1)).get(symbol);
                    check.add(state2);

                    checkRevers = revers(check);


                    if (different.contains(check) || different.contains(checkRevers)) {
                        different.add(unknown.get(i));
                        unknown.remove(unknown.get(i));
                        i--;
                        if (i < 0) {
                            i = 0;
                        }

                    }
                }
            }
            if (different.size() == size) {
                flag = false;

            }
        }
        //Marking in the table ---> X
        markTable(different, table);
        resultState = resultState(table, unknown);
        minimizationDFA(resultState);


    }

    public void deleteState() {
        ArrayList<String> temp = new ArrayList<>();

        temp.add(startState);

        for (int i = 0; i < temp.size(); i++) {
            for (String symbol : symbols) {
                if (!temp.contains((graph.get(temp.get(i)).get(symbol)))) {
                    temp.add(graph.get(temp.get(i)).get(symbol));
                }

            }
        }
        for (int i = 0; i < states.size(); i++) {
            if (!temp.contains(states.get(i))) {
                graph.remove(states.get(i));
                states.remove(i);
                i--;
            }
        }
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
        // Specify the final and start states in minimization DFA
        HashMap<ArrayList<String>, String> stateMapping = new HashMap<>();
        char stateName = 'A';

        for (ArrayList<String> stateGroup : resultStates) {
            stateMapping.put(stateGroup, String.valueOf(stateName));
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
                        break;
                    }
                }
            }
        }
        String start = "";
        Set<String> finales = new HashSet<>();

        for (ArrayList<String> state : resultStates) {
            if (state.contains(startState)) {
                start = stateMapping.get(state);
            }
        }

        for (ArrayList<String> state : resultStates) {
            for (String finalState : finalStates) {
                if (state.contains(finalState)) {
                    finales.add(stateMapping.get(state));
                }
            }
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/azari/amirhossein/dfa_minimization/minimizedDFA.fxml"));
            Parent root = loader.load();

            MinimizedDFAController controller = loader.getController();
            controller.setDFAData(start, finales, minimizedGraph);

            mainScene = confirmBtn.getScene();
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
