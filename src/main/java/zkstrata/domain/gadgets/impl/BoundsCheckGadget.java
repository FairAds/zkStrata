package zkstrata.domain.gadgets.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import zkstrata.analysis.Contradiction;
import zkstrata.analysis.Implication;
import zkstrata.codegen.TargetFormat;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Nullable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.gadgets.AbstractGadget;
import zkstrata.domain.gadgets.AstElement;
import zkstrata.domain.gadgets.Gadget;
import zkstrata.domain.gadgets.Type;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.optimizer.Substitution;
import zkstrata.parser.ast.predicates.BoundsCheck;
import zkstrata.utils.SemanticsUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@AstElement(BoundsCheck.class)
public class BoundsCheckGadget extends AbstractGadget<BoundsCheckGadget> {
    private static final Logger LOGGER = LogManager.getLogger(BoundsCheckGadget.class);
    private static final BigInteger MIN = BigInteger.valueOf(0);
    private static final BigInteger MAX = new BigInteger("ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

    @Type({BigInteger.class})
    private WitnessVariable value;

    @Type({Nullable.class, BigInteger.class})
    private InstanceVariable min;

    @Type({Nullable.class, BigInteger.class})
    private InstanceVariable max;

    public BoundsCheckGadget() {

    }

    public BoundsCheckGadget(WitnessVariable value, InstanceVariable min, InstanceVariable max) {
        this.value = value;
        this.min = min;
        this.max = max;

        this.performChecks();
    }

    @Implication(premise = {EqualityGadget.class, BoundsCheckGadget.class})
    public static Optional<Gadget> implyBounds(EqualityGadget eq, BoundsCheckGadget bc) {
        if (eq.getLeft().equals(bc.getValue()) && eq.getRight() instanceof WitnessVariable)
            return Optional.of(new BoundsCheckGadget((WitnessVariable) eq.getRight(), bc.getMin(), bc.getMax()));

        if (eq.getRight().equals(bc.getValue()) && eq.getLeft() instanceof WitnessVariable)
            return Optional.of(new BoundsCheckGadget((WitnessVariable) eq.getLeft(), bc.getMin(), bc.getMax()));

        return Optional.empty();
    }

    @Contradiction(propositions = {BoundsCheckGadget.class, BoundsCheckGadget.class})
    public static void checkContradiction1(BoundsCheckGadget bc1, BoundsCheckGadget bc2) {
        if (bc1.getValue().equals(bc2.getValue())) {
            if (bc1.getMinValue().compareTo(bc2.getMaxValue()) > 0)
                throw new CompileTimeException("Contradiction.", Set.of(bc1.getMin(), bc2.getMax()));

            if (bc1.getMaxValue().compareTo(bc2.getMinValue()) < 0)
                throw new CompileTimeException("Contradiction.", Set.of(bc1.getMax(), bc2.getMin()));
        }
    }

    @Contradiction(propositions = {BoundsCheckGadget.class})
    public static void checkContradiction2(BoundsCheckGadget bc) {
        if (bc.getMinValue().compareTo(bc.getMaxValue()) > 0)
            throw new CompileTimeException("Contradiction.", Set.of(bc.getMin(), bc.getMax()));
    }

    /*
    // TODO: type checking for gadget variables is tedious
    @Contradiction(propositions = {EqualityGadget.class, BoundsCheckGadget.class})
    public static void checkContradiction(EqualityGadget eq, BoundsCheckGadget bc) {
        if (eq.getLeft().equals(bc.getValue()) && eq.getRightHand() instanceof InstanceVariable
            && (eq.getRightHand().getType() == BigInteger.class &&
                ((BigInteger) eq.getRightHand().getValue()).compareTo())
            throw new CompileTimeException("Contradiction.", Set.of(eq.getLeft(), eq.getRightHand()));
    }
    */

    @Substitution(target = {BoundsCheckGadget.class, BoundsCheckGadget.class})
    public static List<Gadget> replaceEquality2(BoundsCheckGadget bc1, BoundsCheckGadget bc2) {
        if (bc1.value.equals(bc2.value) &&
                (bc1.getMaxValue().subtract(bc2.getMinValue()).equals(BigInteger.valueOf(0))
                        || bc2.getMaxValue().subtract(bc1.getMinValue()).equals(BigInteger.valueOf(0)))) {
            // TODO: try out, maybe add statements information
            LOGGER.info("Removed equality predicate of two instance variables.");
            return List.of(new EqualityGadget(bc1.value, bc1.getMin()));
        }

        return List.of(bc1, bc2);
    }

    @Substitution(target = {BoundsCheckGadget.class})
    public static Set<Gadget> replaceEquality1(BoundsCheckGadget bc) {
        if (bc.getMaxValue().subtract(bc.getMinValue()).equals(BigInteger.valueOf(0))) {
            LOGGER.info("Replaced bounds predicate with max = min by equality predicate.");
            return Set.of(new EqualityGadget(bc.getValue(), bc.getMin()));
        }

        return Set.of(bc);
    }

    public BigInteger getMinValue() {
        return (BigInteger) min.getValue().getValue();
    }

    public BigInteger getMaxValue() {
        return (BigInteger) max.getValue().getValue();
    }

    public WitnessVariable getValue() {
        return value;
    }

    public InstanceVariable getMin() {
        return min;
    }

    public InstanceVariable getMax() {
        return max;
    }

    // TODO: optimizer: check min == 1 --> replace by equals 0

    @Override
    public boolean isEqualTo(BoundsCheckGadget other) {
        return value.equals(other.value) && min.equals(other.min) && max.equals(other.max);
    }

    @Override
    public void performChecks() {
        if (this.min == null)
            this.min = InstanceVariable.of(MIN);

        if (this.max == null)
            this.max = InstanceVariable.of(MAX);

        if (SemanticsUtils.testMaxBitSize(getMinValue(), 256))
            throw new CompileTimeException("The lower bound cannot be longer than 32 bytes.", this.min);

        if (SemanticsUtils.testMaxBitSize(getMaxValue(), 256))
            throw new CompileTimeException("The upper bound cannot be longer than 32 bytes.", this.max);

        if (getMinValue().compareTo(MIN) < 0 || getMinValue().compareTo(MAX) > 0)
            throw new CompileTimeException(String.format("The lower bound must be in the range between %s and %s.", MIN, MAX), this.max);

        if (getMaxValue().compareTo(MIN) < 0 || getMinValue().compareTo(MAX) > 0)
            throw new CompileTimeException(String.format("The upper bound must be in the range between %s and %s.", MIN, MAX), this.max);
    }

    @Override
    public TargetFormat toTargetFormat() {
        Map<String, Variable> args = Map.ofEntries(
                Map.entry("value", value),
                Map.entry("min", min),
                Map.entry("max", max)
        );
        return new TargetFormat("BOUND %(value) %(min) %(max)", args);
    }
}
