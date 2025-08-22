package core.player;

import UI.CustomMethods;

public record Award(int id, String description) {

    public static Award createAward(String description) {
        return new Award(CustomMethods.getNewAwardIdentifier(), description);
    }

}
