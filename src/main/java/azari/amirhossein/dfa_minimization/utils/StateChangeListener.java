package azari.amirhossein.dfa_minimization.utils;

import azari.amirhossein.dfa_minimization.models.State;

public interface StateChangeListener {
    void onStateChanged(State state);
}