package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;

public class BoundsCheck extends Predicate {
    private Value value;
    private Value min;
    private Value max;

    public BoundsCheck(Value value, Value min, Value max, Position tokenInfo) {
        super(tokenInfo);
        this.value = value;
        this.min = min;
        this.max = max;
    }

    public Value getValue() {
        return value;
    }

    public Value getMin() {
        return min;
    }

    public Value getMax() {
        return max;
    }
}
