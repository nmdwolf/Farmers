package core.player;

import UI.CustomMethods;

import java.util.function.BooleanSupplier;

public record Award(int id, String description, BooleanSupplier supplier) {

    public static Award createAward(String description, BooleanSupplier supplier) {
        return new Award(CustomMethods.getNewAwardIdentifier(), description, supplier);
    }

    public static Award createFreeAward(String description) {
        return new Award(CustomMethods.getNewAwardIdentifier(), description, () -> true);
    }
}
