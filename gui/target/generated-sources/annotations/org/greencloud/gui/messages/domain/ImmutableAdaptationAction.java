package org.greencloud.gui.messages.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Doubles;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link AdaptationAction}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableAdaptationAction.builder()}.
 */
@Generated(from = "AdaptationAction", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableAdaptationAction implements AdaptationAction {
  private final String name;
  private final String goal;
  private final int runsNo;
  private final ImmutableList<GoalQuality> avgGoalQualities;
  private final double avgDuration;

  private ImmutableAdaptationAction(
      String name,
      String goal,
      int runsNo,
      ImmutableList<GoalQuality> avgGoalQualities,
      double avgDuration) {
    this.name = name;
    this.goal = goal;
    this.runsNo = runsNo;
    this.avgGoalQualities = avgGoalQualities;
    this.avgDuration = avgDuration;
  }

  /**
   * @return The value of the {@code name} attribute
   */
  @JsonProperty("name")
  @Override
  public String getName() {
    return name;
  }

  /**
   * @return The value of the {@code goal} attribute
   */
  @JsonProperty("goal")
  @Override
  public String getGoal() {
    return goal;
  }

  /**
   * @return The value of the {@code runsNo} attribute
   */
  @JsonProperty("runsNo")
  @Override
  public int getRunsNo() {
    return runsNo;
  }

  /**
   * @return The value of the {@code avgGoalQualities} attribute
   */
  @JsonProperty("avgGoalQualities")
  @Override
  public ImmutableList<GoalQuality> getAvgGoalQualities() {
    return avgGoalQualities;
  }

  /**
   * @return The value of the {@code avgDuration} attribute
   */
  @JsonProperty("avgDuration")
  @Override
  public double getAvgDuration() {
    return avgDuration;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link AdaptationAction#getName() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableAdaptationAction withName(String value) {
    String newValue = Objects.requireNonNull(value, "name");
    if (this.name.equals(newValue)) return this;
    return new ImmutableAdaptationAction(newValue, this.goal, this.runsNo, this.avgGoalQualities, this.avgDuration);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link AdaptationAction#getGoal() goal} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for goal
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableAdaptationAction withGoal(String value) {
    String newValue = Objects.requireNonNull(value, "goal");
    if (this.goal.equals(newValue)) return this;
    return new ImmutableAdaptationAction(this.name, newValue, this.runsNo, this.avgGoalQualities, this.avgDuration);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link AdaptationAction#getRunsNo() runsNo} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for runsNo
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableAdaptationAction withRunsNo(int value) {
    if (this.runsNo == value) return this;
    return new ImmutableAdaptationAction(this.name, this.goal, value, this.avgGoalQualities, this.avgDuration);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link AdaptationAction#getAvgGoalQualities() avgGoalQualities}.
   * @param elements The elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableAdaptationAction withAvgGoalQualities(GoalQuality... elements) {
    ImmutableList<GoalQuality> newValue = ImmutableList.copyOf(elements);
    return new ImmutableAdaptationAction(this.name, this.goal, this.runsNo, newValue, this.avgDuration);
  }

  /**
   * Copy the current immutable object with elements that replace the content of {@link AdaptationAction#getAvgGoalQualities() avgGoalQualities}.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param elements An iterable of avgGoalQualities elements to set
   * @return A modified copy of {@code this} object
   */
  public final ImmutableAdaptationAction withAvgGoalQualities(Iterable<? extends GoalQuality> elements) {
    if (this.avgGoalQualities == elements) return this;
    ImmutableList<GoalQuality> newValue = ImmutableList.copyOf(elements);
    return new ImmutableAdaptationAction(this.name, this.goal, this.runsNo, newValue, this.avgDuration);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link AdaptationAction#getAvgDuration() avgDuration} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for avgDuration
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableAdaptationAction withAvgDuration(double value) {
    if (Double.doubleToLongBits(this.avgDuration) == Double.doubleToLongBits(value)) return this;
    return new ImmutableAdaptationAction(this.name, this.goal, this.runsNo, this.avgGoalQualities, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableAdaptationAction} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableAdaptationAction
        && equalTo(0, (ImmutableAdaptationAction) another);
  }

  private boolean equalTo(int synthetic, ImmutableAdaptationAction another) {
    return name.equals(another.name)
        && goal.equals(another.goal)
        && runsNo == another.runsNo
        && avgGoalQualities.equals(another.avgGoalQualities)
        && Double.doubleToLongBits(avgDuration) == Double.doubleToLongBits(another.avgDuration);
  }

  /**
   * Computes a hash code from attributes: {@code name}, {@code goal}, {@code runsNo}, {@code avgGoalQualities}, {@code avgDuration}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + name.hashCode();
    h += (h << 5) + goal.hashCode();
    h += (h << 5) + runsNo;
    h += (h << 5) + avgGoalQualities.hashCode();
    h += (h << 5) + Doubles.hashCode(avgDuration);
    return h;
  }

  /**
   * Prints the immutable value {@code AdaptationAction} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("AdaptationAction")
        .omitNullValues()
        .add("name", name)
        .add("goal", goal)
        .add("runsNo", runsNo)
        .add("avgGoalQualities", avgGoalQualities)
        .add("avgDuration", avgDuration)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "AdaptationAction", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements AdaptationAction {
    @Nullable String name;
    @Nullable String goal;
    int runsNo;
    boolean runsNoIsSet;
    @Nullable List<GoalQuality> avgGoalQualities = ImmutableList.of();
    double avgDuration;
    boolean avgDurationIsSet;
    @JsonProperty("name")
    public void setName(String name) {
      this.name = name;
    }
    @JsonProperty("goal")
    public void setGoal(String goal) {
      this.goal = goal;
    }
    @JsonProperty("runsNo")
    public void setRunsNo(int runsNo) {
      this.runsNo = runsNo;
      this.runsNoIsSet = true;
    }
    @JsonProperty("avgGoalQualities")
    public void setAvgGoalQualities(List<GoalQuality> avgGoalQualities) {
      this.avgGoalQualities = avgGoalQualities;
    }
    @JsonProperty("avgDuration")
    public void setAvgDuration(double avgDuration) {
      this.avgDuration = avgDuration;
      this.avgDurationIsSet = true;
    }
    @Override
    public String getName() { throw new UnsupportedOperationException(); }
    @Override
    public String getGoal() { throw new UnsupportedOperationException(); }
    @Override
    public int getRunsNo() { throw new UnsupportedOperationException(); }
    @Override
    public List<GoalQuality> getAvgGoalQualities() { throw new UnsupportedOperationException(); }
    @Override
    public double getAvgDuration() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableAdaptationAction fromJson(Json json) {
    ImmutableAdaptationAction.Builder builder = ImmutableAdaptationAction.builder();
    if (json.name != null) {
      builder.name(json.name);
    }
    if (json.goal != null) {
      builder.goal(json.goal);
    }
    if (json.runsNoIsSet) {
      builder.runsNo(json.runsNo);
    }
    if (json.avgGoalQualities != null) {
      builder.addAllAvgGoalQualities(json.avgGoalQualities);
    }
    if (json.avgDurationIsSet) {
      builder.avgDuration(json.avgDuration);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link AdaptationAction} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable AdaptationAction instance
   */
  public static ImmutableAdaptationAction copyOf(AdaptationAction instance) {
    if (instance instanceof ImmutableAdaptationAction) {
      return (ImmutableAdaptationAction) instance;
    }
    return ImmutableAdaptationAction.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableAdaptationAction ImmutableAdaptationAction}.
   * <pre>
   * ImmutableAdaptationAction.builder()
   *    .name(String) // required {@link AdaptationAction#getName() name}
   *    .goal(String) // required {@link AdaptationAction#getGoal() goal}
   *    .runsNo(int) // required {@link AdaptationAction#getRunsNo() runsNo}
   *    .addAvgGoalQualities|addAllAvgGoalQualities(org.greencloud.gui.messages.domain.GoalQuality) // {@link AdaptationAction#getAvgGoalQualities() avgGoalQualities} elements
   *    .avgDuration(double) // required {@link AdaptationAction#getAvgDuration() avgDuration}
   *    .build();
   * </pre>
   * @return A new ImmutableAdaptationAction builder
   */
  public static ImmutableAdaptationAction.Builder builder() {
    return new ImmutableAdaptationAction.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableAdaptationAction ImmutableAdaptationAction}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "AdaptationAction", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_NAME = 0x1L;
    private static final long INIT_BIT_GOAL = 0x2L;
    private static final long INIT_BIT_RUNS_NO = 0x4L;
    private static final long INIT_BIT_AVG_DURATION = 0x8L;
    private long initBits = 0xfL;

    private @Nullable String name;
    private @Nullable String goal;
    private int runsNo;
    private ImmutableList.Builder<GoalQuality> avgGoalQualities = ImmutableList.builder();
    private double avgDuration;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code AdaptationAction} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(AdaptationAction instance) {
      Objects.requireNonNull(instance, "instance");
      name(instance.getName());
      goal(instance.getGoal());
      runsNo(instance.getRunsNo());
      addAllAvgGoalQualities(instance.getAvgGoalQualities());
      avgDuration(instance.getAvgDuration());
      return this;
    }

    /**
     * Initializes the value for the {@link AdaptationAction#getName() name} attribute.
     * @param name The value for name 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("name")
    public final Builder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      initBits &= ~INIT_BIT_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link AdaptationAction#getGoal() goal} attribute.
     * @param goal The value for goal 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("goal")
    public final Builder goal(String goal) {
      this.goal = Objects.requireNonNull(goal, "goal");
      initBits &= ~INIT_BIT_GOAL;
      return this;
    }

    /**
     * Initializes the value for the {@link AdaptationAction#getRunsNo() runsNo} attribute.
     * @param runsNo The value for runsNo 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("runsNo")
    public final Builder runsNo(int runsNo) {
      this.runsNo = runsNo;
      initBits &= ~INIT_BIT_RUNS_NO;
      return this;
    }

    /**
     * Adds one element to {@link AdaptationAction#getAvgGoalQualities() avgGoalQualities} list.
     * @param element A avgGoalQualities element
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAvgGoalQualities(GoalQuality element) {
      this.avgGoalQualities.add(element);
      return this;
    }

    /**
     * Adds elements to {@link AdaptationAction#getAvgGoalQualities() avgGoalQualities} list.
     * @param elements An array of avgGoalQualities elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAvgGoalQualities(GoalQuality... elements) {
      this.avgGoalQualities.add(elements);
      return this;
    }


    /**
     * Sets or replaces all elements for {@link AdaptationAction#getAvgGoalQualities() avgGoalQualities} list.
     * @param elements An iterable of avgGoalQualities elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("avgGoalQualities")
    public final Builder avgGoalQualities(Iterable<? extends GoalQuality> elements) {
      this.avgGoalQualities = ImmutableList.builder();
      return addAllAvgGoalQualities(elements);
    }

    /**
     * Adds elements to {@link AdaptationAction#getAvgGoalQualities() avgGoalQualities} list.
     * @param elements An iterable of avgGoalQualities elements
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder addAllAvgGoalQualities(Iterable<? extends GoalQuality> elements) {
      this.avgGoalQualities.addAll(elements);
      return this;
    }

    /**
     * Initializes the value for the {@link AdaptationAction#getAvgDuration() avgDuration} attribute.
     * @param avgDuration The value for avgDuration 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("avgDuration")
    public final Builder avgDuration(double avgDuration) {
      this.avgDuration = avgDuration;
      initBits &= ~INIT_BIT_AVG_DURATION;
      return this;
    }

    /**
     * Builds a new {@link ImmutableAdaptationAction ImmutableAdaptationAction}.
     * @return An immutable instance of AdaptationAction
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableAdaptationAction build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableAdaptationAction(name, goal, runsNo, avgGoalQualities.build(), avgDuration);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_GOAL) != 0) attributes.add("goal");
      if ((initBits & INIT_BIT_RUNS_NO) != 0) attributes.add("runsNo");
      if ((initBits & INIT_BIT_AVG_DURATION) != 0) attributes.add("avgDuration");
      return "Cannot build AdaptationAction, some of required attributes are not set " + attributes;
    }
  }
}
