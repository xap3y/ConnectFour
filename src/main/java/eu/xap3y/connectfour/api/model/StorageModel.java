package eu.xap3y.connectfour.api.model;

import java.util.ArrayList;
import java.util.List;

public class StorageModel {

    private List<PlayerStatModel> players = new ArrayList<>();

    public StorageModel() {
    }

    public StorageModel(List<PlayerStatModel> players) {
        this.players = players;
    }

    public List<PlayerStatModel> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerStatModel> players) {
        this.players = players;
    }
}