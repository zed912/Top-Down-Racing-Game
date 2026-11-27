public class Truck extends Vehicle {
    public Truck() {
        super("Truck", 120, 50, 4, 60, 56, 32);
    }

    @Override
    public void onCollision() {
        reduceSpeed(0.85); // Lose 15% of current speed.
    }
    
    @Override
    public String getSpecialBehavior() {
        return "Loses only 15% speed on collision";
}
}
