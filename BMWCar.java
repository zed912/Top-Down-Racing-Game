public class BMWCar extends Vehicle {
    public BMWCar() {
        // Temporary drawing dimensions: length along x, thickness along y.
        super("BMW", 260, 160, 6, 130, 48, 26);
    }

        @Override
    public void onCollision() {
        reduceSpeed(0.60);  // Lose 40% of current speed.
        loseControl(0.5);   // Cannot steer for half a second.
    }
}
