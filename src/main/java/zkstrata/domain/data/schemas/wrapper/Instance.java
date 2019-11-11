package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.exceptions.Position;

/**
 * Wrapper class to mark a {@link Schema} as instance wide (to all participants, usually publicly) known.
 */
public class Instance extends AbstractStructuredData<InstanceVariable> {
    public Instance(String alias, Schema schema, ValueAccessor accessor, ValueAccessor metaData) {
        super(alias, schema, accessor, metaData);

        if (accessor == null) {
            String msg = String.format("Missing instance data for `%s`.", alias);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public boolean isWitness() {
        return false;
    }

    @Override
    public InstanceVariable getVariable(Selector selector, Position.Absolute position) {
        Literal value = (Literal) resolve(getSchema(), selector);
        return new InstanceVariable(value, new Reference(value.getType(), getAlias(), selector), position);
    }
}
