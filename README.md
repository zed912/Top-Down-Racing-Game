# Racing Game — Vehicle Module

Java vehicle logic for team member 1, independent of JavaFX.

## Files

- `Vehicle.java`: abstract base class for movement, steering, braking, and effects.
- `BMWCar.java`, `Motorcycle.java`, `Truck.java`: vehicle stats and collision reactions.

## Vehicle Stats

Speed is in px/s, acceleration and braking in px/s², and dimensions in pixels.

| Vehicle | Max Speed | Acceleration | Handling | Braking | Collision Speed Loss | Size |
|---|---:|---:|---:|---:|---:|---|
| BMW | 260 | 100 | 9 | 130 | 40% | 48 × 26 |
| Motorcycle | 230 | 140 | 7 | 115 | 60% | 38 × 16 |
| Truck | 120 | 70 | 5 | 60 | 15% | 48 × 48 |

## Usage

```java
Vehicle car = new BMWCar();
car.reset(100, 200, 0);
car.update(0.016, 1, 0, 1); // Seconds, throttle, braking, steering
```

- Throttle and braking: `0..1`. Steering: `-1` left, `0` straight, `1` right.
- Position is the vehicle centre. X increases right; Y increases down.
- Heading is in degrees: `0` right, `90` down. Handling controls turning speed.
- Braking takes priority and stops at zero. No input keeps the current speed.


