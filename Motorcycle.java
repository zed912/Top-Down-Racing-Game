public class Motorcycle extends Vehicle {
    public Motorcycle() {
        super("Motorcycle", 200, 110, 9, 115, 38, 16);
    }

    @Override
    public void onCollision() {
        reduceSpeed(0.40); // Lose 60% of current speed.
    }
}
