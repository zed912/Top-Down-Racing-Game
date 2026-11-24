public class Motorcycle extends Vehicle {
    public Motorcycle() {
        super("Motorcycle", 230, 140, 7, 115, 38, 16);
    }

    @Override
    public void onCollision() {
        reduceSpeed(0.40); // Lose 60% of current speed.
    }
}
