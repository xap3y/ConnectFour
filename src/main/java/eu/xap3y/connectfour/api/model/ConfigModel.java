package eu.xap3y.connectfour.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ConfigModel {
    private int inviteTimeout;
    private boolean tokenFallAnimation;
    private long tokenFallSpeed;
    private boolean inviterStart;
    private boolean winRewardsEnable;
    private List<String> winRewards;
    private boolean hookPapi;
    private boolean hookMiniPlaceholders;
    private boolean hookVault;
    private boolean metrics;
    private boolean updates;
    private boolean doubleEscape;
    private int moveTimeout;
}
