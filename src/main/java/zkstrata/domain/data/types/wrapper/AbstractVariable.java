package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;

public abstract class AbstractVariable extends AbstractTraceable implements Variable {
    private Value value;

    public AbstractVariable(Value value, Position.Absolute position) {
        super(position);
        this.value = value;
    }

    @Override
    public Position.Absolute getPosition() {
        return (Position.Absolute) super.getPosition();
    }

    @Override
    public Class<?> getType() {
        return value.getType();
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return value.equals(((Variable) obj).getValue());
    }
}
