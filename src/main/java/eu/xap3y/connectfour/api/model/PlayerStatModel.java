package eu.xap3y.connectfour.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatModel {

    private String uuid;
    private String name;
    private int gamesPlayed;
    private int wins;
    private int losses;
    private int draws;

}
