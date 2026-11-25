/**
 * Vehicle logic only: no keyboard, JavaFX, track, or lap counting.
 * Coordinates describe the centre; +x is right and +y is down.
 * Heading: 0 degrees = right, 90 = down. Time is measured in seconds.
 */
public abstract class Vehicle {
    /** Top reverse speed is this fraction of the (boosted) top speed. */
    private static final double REVERSE_SPEED_FRACTION = 0.4;
    /** Reversing accelerates at this fraction of normal acceleration. */
    private static final double REVERSE_ACCEL_FRACTION = 0.5;
    /** Longest time step accepted by one update() call, in seconds. */
    private static final double MAX_UPDATE_SECONDS = 0.05;
    /** Coasting slows a vehicle at this fraction of its braking power. */
    private static final double COASTING_FRICTION = 0.25;
    private final String name;
    private final double maxSpeed;
    private final double acceleration;
    private final double handling;
    private final double brakingPower;
    private final double width;
    private final double height;

    private double x;
    private double y;
    private double speed;
    private double headingDegrees;
    private double speedMultiplier = 1.0;
    private boolean steeringEnabled = true;
    private double controlLossSeconds = 0 ;

    protected Vehicle(String name, double maxSpeed, double acceleration,
                      double handling, double brakingPower,
                      double width, double height) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("A vehicle needs a name.");
        }
        requirePositive(maxSpeed, "maxSpeed");
        requirePositive(acceleration, "acceleration");
        requirePositive(brakingPower, "brakingPower");
        requirePositive(width, "width");
        requirePositive(height, "height");
        requirePositive(handling, "handling");
        if (handling > 10) {
            throw new IllegalArgumentException("handling must be at most 10.");
        }
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.handling = handling;
        this.brakingPower = brakingPower;
        this.width = width;
        this.height = height;
    }

      /**
     * Called by the game loop only while the race is running.
     * throttle/braking: 0..1; steering: -1 (left)..1 (right).
     * Out-of-range inputs are clamped and NaN counts as 0, so a bad value
     * from the input code never crashes the game loop.
     * Braking takes priority over throttle. No input means coasting.
     * This version brakes to a stop; it does not reverse.
     * deltaSeconds is clamped to 0..MAX_UPDATE_SECONDS, so one long frame
     * cannot teleport the vehicle through walls.
     */
    public final void update(double deltaSeconds, double throttle,
         double braking, double steering) {
        double dt = clamp(deltaSeconds, 0, MAX_UPDATE_SECONDS);
        double throttleInput = clamp(throttle, 0, 1);
        double brakingInput = clamp(braking, 0, 1);
        double steeringInput = clamp(steering, -1, 1);

        // Small steps keep acceleration and curved movement consistent
        // when frames have different durations.
        double remaining = dt;
        while (remaining > 0) {
            double step = Math.min(remaining, 1.0 / 120.0);
            moveStep(step, throttleInput, brakingInput, steeringInput);
            remaining = Math.max(0, remaining - step);
        }
    }
    private void moveStep(double dt, double throttle, double braking,
                          double steering) {
        double oldSpeed = speed;
                if (braking > 0) {
            if (speed > 0) {
                // Still rolling forward: brake down to a stop.
                speed = Math.max(0, speed - brakingPower * braking * dt);
            } else {
                // Stopped or already backing up: reverse (negative speed).
                speed = Math.max(-getMaxReverseSpeed(), speed
                        - acceleration * REVERSE_ACCEL_FRACTION * braking * dt);
            }
        } else if (throttle > 0) {
            // From a reverse speed this first brings the car back to zero.
            speed = Math.min(getEffectiveMaxSpeed(),
                    speed + acceleration * throttle * dt);
        } else {
            // No input: friction pulls the speed toward zero from either side.
            double drop = brakingPower * COASTING_FRICTION * dt;
            speed = speed > 0 ? Math.max(0, speed - drop)
                              : Math.min(0, speed + drop);
        }

        double averageSpeed = (oldSpeed + speed) / 2.0;
        // Handling 10 gives 180 degrees/second at normal driving speed.
        // Steering fades near a standstill, so the car cannot spin in place.
        double lowSpeedFactor = Math.min(-1,Math.min(1, averageSpeed / (maxSpeed * 0.25)));
        double turn = canSteer()
                ? steering * handling * 18.0 * lowSpeedFactor * dt : 0;
        double middleHeading = Math.toRadians(headingDegrees + turn / 2.0);
        x += Math.cos(middleHeading) * averageSpeed * dt;
        y += Math.sin(middleHeading) * averageSpeed * dt;
        headingDegrees = normalizeAngle(headingDegrees + turn);
        
        controlLossSeconds = Math.max(0, controlLossSeconds - dt);
    }

    /** Each type chooses how much speed it loses on one collision. */
    public abstract void onCollision();

    /** Used by subclasses or track effects; factor 0 stops, 1 keeps speed. */
    public final void reduceSpeed(double factor) {
        requireRange(factor, 0, 1, "factor");
        speed *= factor;
    }

    /**
     * External effect manager owns the timer and combined multiplier.
     * 1.25 increases current speed and its limit by 25%; 1 restores normal.
     * Setting the same multiplier repeatedly does not stack the boost.
     */
    public final void setSpeedMultiplier(double multiplier) {
        requirePositive(multiplier, "multiplier");
        requireFinite(maxSpeed * multiplier, "effectiveMaxSpeed");
        double previousMultiplier = speedMultiplier;
        speedMultiplier = multiplier;
        if (multiplier > previousMultiplier) {
            speed *= multiplier / previousMultiplier;
        }
            speed = Math.max(-getMaxReverseSpeed(),Math.min(speed, getEffectiveMaxSpeed()));
    }

    /** Oil manager disables steering, then restores it when its timer ends. */
    public final void setSteeringEnabled(boolean enabled) {
        steeringEnabled = enabled;
    }

        /**
     * Steering is ignored for this many seconds; the vehicle keeps sliding
     * along its current heading. A longer loss replaces a shorter one, and
     * repeated calls do not add up.
     */
    public final void loseControl(double seconds) {
        requirePositive(seconds, "seconds");
        controlLossSeconds = Math.max(controlLossSeconds, seconds);
    }

    public final void stop() {
        speed = 0;
    }

    /** Track/race code owns bounds and starting positions. */
    public final void setPosition(double x, double y) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        this.x = x;
        this.y = y;
    }

    public final void reset(double startX, double startY, double startHeadingDegrees) {
        requireFinite(startHeadingDegrees, "startHeadingDegrees");
        setPosition(startX, startY);
        headingDegrees = normalizeAngle(startHeadingDegrees);
        speed = 0;
        speedMultiplier = 1;
        steeringEnabled = true;
        controlLossSeconds = 0;
    }

    private static double normalizeAngle(double degrees) {
        return ((degrees % 360) + 360) % 360;
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite.");
        }
    }

    private static void requirePositive(double value, String name) {
        requireFinite(value, name);
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive.");
        }
    }
    /** Keeps value inside min..max; NaN becomes 0. */
    private static double clamp(double value, double min, double max) {
        if (Double.isNaN(value)) {
            return 0;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static void requireRange(double value, double min, double max, String name) {
        requireFinite(value, name);
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
    }

    public final String getName() { return name; }
    public final double getMaxSpeed() { return maxSpeed; }
    public final double getAcceleration() { return acceleration; }
    public final double getHandling() { return handling; }
    public final double getBrakingPower() { return brakingPower; }
    public final double getWidth() { return width; }
    public final double getHeight() { return height; }
    public final double getX() { return x; }
    public final double getY() { return y; }
    public final double getSpeed() { return speed; }
    public final double getHeadingDegrees() { return headingDegrees; }
    public final double getSpeedMultiplier() { return speedMultiplier; }
    public final double getMaxReverseSpeed() { return getEffectiveMaxSpeed() * REVERSE_SPEED_FRACTION; }
    public final boolean isReversing() { return speed < 0; }
    public final double getEffectiveMaxSpeed() { return maxSpeed * speedMultiplier; }
    public final boolean isSteeringEnabled() { return steeringEnabled; }
    public final double getControlLossSeconds() { return controlLossSeconds; }
    public final boolean canSteer() { return steeringEnabled && controlLossSeconds <= 0; }
}
