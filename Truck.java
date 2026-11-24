public class Truck extends Vehicle {
    public Truck() {
        super("Truck", 120, 70, 5, 60, 48, 48);
    }

    @Override
    public void onCollision() {
        reduceSpeed(0.85); // Lose 15% of current speed.
    }
}
