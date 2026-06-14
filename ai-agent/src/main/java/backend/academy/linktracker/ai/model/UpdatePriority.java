package backend.academy.linktracker.ai.model;

public enum UpdatePriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int weight;

    UpdatePriority(int weight) {
        this.weight = weight;
    }

    public static UpdatePriority max(UpdatePriority first, UpdatePriority second) {
        return first.weight >= second.weight ? first : second;
    }
}
