public class BMWCar extends Vehicle {
    public BMWCar() {
        // Temporary drawing dimensions: length along x, thickness along y.
        super("BMW", 260, 100, 9, 130, 48, 26);
    }

    @Override
    public void onCollision() {
        reduceSpeed(0.60); // Lose 40% of current speed.
    }
}
