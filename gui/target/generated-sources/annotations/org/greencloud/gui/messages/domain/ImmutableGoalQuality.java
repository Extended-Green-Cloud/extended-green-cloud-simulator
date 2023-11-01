package org.greencloud.gui.messages.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
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
 * Immutable implementation of {@link GoalQuality}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableGoalQuality.builder()}.
 */
@Generated(from = "GoalQuality", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableGoalQuality implements GoalQuality {
  private final String name;
  private final double avgQuality;

  private ImmutableGoalQuality(String name, double avgQuality) {
    this.name = name;
    this.avgQuality = avgQuality;
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
   * @return The value of the {@code avgQuality} attribute
   */
  @JsonProperty("avgQuality")
  @Override
  public double getAvgQuality() {
    return avgQuality;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GoalQuality#getName() name} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for name
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGoalQuality withName(String value) {
    String newValue = Objects.requireNonNull(value, "name");
    if (this.name.equals(newValue)) return this;
    return new ImmutableGoalQuality(newValue, this.avgQuality);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link GoalQuality#getAvgQuality() avgQuality} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for avgQuality
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableGoalQuality withAvgQuality(double value) {
    if (Double.doubleToLongBits(this.avgQuality) == Double.doubleToLongBits(value)) return this;
    return new ImmutableGoalQuality(this.name, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableGoalQuality} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableGoalQuality
        && equalTo(0, (ImmutableGoalQuality) another);
  }

  private boolean equalTo(int synthetic, ImmutableGoalQuality another) {
    return name.equals(another.name)
        && Double.doubleToLongBits(avgQuality) == Double.doubleToLongBits(another.avgQuality);
  }

  /**
   * Computes a hash code from attributes: {@code name}, {@code avgQuality}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + name.hashCode();
    h += (h << 5) + Doubles.hashCode(avgQuality);
    return h;
  }

  /**
   * Prints the immutable value {@code GoalQuality} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("GoalQuality")
        .omitNullValues()
        .add("name", name)
        .add("avgQuality", avgQuality)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "GoalQuality", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements GoalQuality {
    @Nullable String name;
    double avgQuality;
    boolean avgQualityIsSet;
    @JsonProperty("name")
    public void setName(String name) {
      this.name = name;
    }
    @JsonProperty("avgQuality")
    public void setAvgQuality(double avgQuality) {
      this.avgQuality = avgQuality;
      this.avgQualityIsSet = true;
    }
    @Override
    public String getName() { throw new UnsupportedOperationException(); }
    @Override
    public double getAvgQuality() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableGoalQuality fromJson(Json json) {
    ImmutableGoalQuality.Builder builder = ImmutableGoalQuality.builder();
    if (json.name != null) {
      builder.name(json.name);
    }
    if (json.avgQualityIsSet) {
      builder.avgQuality(json.avgQuality);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link GoalQuality} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable GoalQuality instance
   */
  public static ImmutableGoalQuality copyOf(GoalQuality instance) {
    if (instance instanceof ImmutableGoalQuality) {
      return (ImmutableGoalQuality) instance;
    }
    return ImmutableGoalQuality.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableGoalQuality ImmutableGoalQuality}.
   * <pre>
   * ImmutableGoalQuality.builder()
   *    .name(String) // required {@link GoalQuality#getName() name}
   *    .avgQuality(double) // required {@link GoalQuality#getAvgQuality() avgQuality}
   *    .build();
   * </pre>
   * @return A new ImmutableGoalQuality builder
   */
  public static ImmutableGoalQuality.Builder builder() {
    return new ImmutableGoalQuality.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableGoalQuality ImmutableGoalQuality}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "GoalQuality", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_NAME = 0x1L;
    private static final long INIT_BIT_AVG_QUALITY = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String name;
    private double avgQuality;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code GoalQuality} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(GoalQuality instance) {
      Objects.requireNonNull(instance, "instance");
      name(instance.getName());
      avgQuality(instance.getAvgQuality());
      return this;
    }

    /**
     * Initializes the value for the {@link GoalQuality#getName() name} attribute.
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
     * Initializes the value for the {@link GoalQuality#getAvgQuality() avgQuality} attribute.
     * @param avgQuality The value for avgQuality 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("avgQuality")
    public final Builder avgQuality(double avgQuality) {
      this.avgQuality = avgQuality;
      initBits &= ~INIT_BIT_AVG_QUALITY;
      return this;
    }

    /**
     * Builds a new {@link ImmutableGoalQuality ImmutableGoalQuality}.
     * @return An immutable instance of GoalQuality
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableGoalQuality build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableGoalQuality(name, avgQuality);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_NAME) != 0) attributes.add("name");
      if ((initBits & INIT_BIT_AVG_QUALITY) != 0) attributes.add("avgQuality");
      return "Cannot build GoalQuality, some of required attributes are not set " + attributes;
    }
  }
}
