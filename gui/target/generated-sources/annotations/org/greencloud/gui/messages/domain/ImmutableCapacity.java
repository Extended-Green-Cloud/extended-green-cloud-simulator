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
 * Immutable implementation of {@link Capacity}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableCapacity.builder()}.
 */
@Generated(from = "Capacity", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableCapacity implements Capacity {
  private final double powerInUse;
  private final double maximumCapacity;

  private ImmutableCapacity(double powerInUse, double maximumCapacity) {
    this.powerInUse = powerInUse;
    this.maximumCapacity = maximumCapacity;
  }

  /**
   * @return The value of the {@code powerInUse} attribute
   */
  @JsonProperty("powerInUse")
  @Override
  public double getPowerInUse() {
    return powerInUse;
  }

  /**
   * @return The value of the {@code maximumCapacity} attribute
   */
  @JsonProperty("maximumCapacity")
  @Override
  public double getMaximumCapacity() {
    return maximumCapacity;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Capacity#getPowerInUse() powerInUse} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for powerInUse
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableCapacity withPowerInUse(double value) {
    if (Double.doubleToLongBits(this.powerInUse) == Double.doubleToLongBits(value)) return this;
    return new ImmutableCapacity(value, this.maximumCapacity);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link Capacity#getMaximumCapacity() maximumCapacity} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for maximumCapacity
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableCapacity withMaximumCapacity(double value) {
    if (Double.doubleToLongBits(this.maximumCapacity) == Double.doubleToLongBits(value)) return this;
    return new ImmutableCapacity(this.powerInUse, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableCapacity} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableCapacity
        && equalTo(0, (ImmutableCapacity) another);
  }

  private boolean equalTo(int synthetic, ImmutableCapacity another) {
    return Double.doubleToLongBits(powerInUse) == Double.doubleToLongBits(another.powerInUse)
        && Double.doubleToLongBits(maximumCapacity) == Double.doubleToLongBits(another.maximumCapacity);
  }

  /**
   * Computes a hash code from attributes: {@code powerInUse}, {@code maximumCapacity}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Doubles.hashCode(powerInUse);
    h += (h << 5) + Doubles.hashCode(maximumCapacity);
    return h;
  }

  /**
   * Prints the immutable value {@code Capacity} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("Capacity")
        .omitNullValues()
        .add("powerInUse", powerInUse)
        .add("maximumCapacity", maximumCapacity)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "Capacity", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements Capacity {
    double powerInUse;
    boolean powerInUseIsSet;
    double maximumCapacity;
    boolean maximumCapacityIsSet;
    @JsonProperty("powerInUse")
    public void setPowerInUse(double powerInUse) {
      this.powerInUse = powerInUse;
      this.powerInUseIsSet = true;
    }
    @JsonProperty("maximumCapacity")
    public void setMaximumCapacity(double maximumCapacity) {
      this.maximumCapacity = maximumCapacity;
      this.maximumCapacityIsSet = true;
    }
    @Override
    public double getPowerInUse() { throw new UnsupportedOperationException(); }
    @Override
    public double getMaximumCapacity() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableCapacity fromJson(Json json) {
    ImmutableCapacity.Builder builder = ImmutableCapacity.builder();
    if (json.powerInUseIsSet) {
      builder.powerInUse(json.powerInUse);
    }
    if (json.maximumCapacityIsSet) {
      builder.maximumCapacity(json.maximumCapacity);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link Capacity} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable Capacity instance
   */
  public static ImmutableCapacity copyOf(Capacity instance) {
    if (instance instanceof ImmutableCapacity) {
      return (ImmutableCapacity) instance;
    }
    return ImmutableCapacity.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableCapacity ImmutableCapacity}.
   * <pre>
   * ImmutableCapacity.builder()
   *    .powerInUse(double) // required {@link Capacity#getPowerInUse() powerInUse}
   *    .maximumCapacity(double) // required {@link Capacity#getMaximumCapacity() maximumCapacity}
   *    .build();
   * </pre>
   * @return A new ImmutableCapacity builder
   */
  public static ImmutableCapacity.Builder builder() {
    return new ImmutableCapacity.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableCapacity ImmutableCapacity}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "Capacity", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_POWER_IN_USE = 0x1L;
    private static final long INIT_BIT_MAXIMUM_CAPACITY = 0x2L;
    private long initBits = 0x3L;

    private double powerInUse;
    private double maximumCapacity;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code Capacity} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(Capacity instance) {
      Objects.requireNonNull(instance, "instance");
      powerInUse(instance.getPowerInUse());
      maximumCapacity(instance.getMaximumCapacity());
      return this;
    }

    /**
     * Initializes the value for the {@link Capacity#getPowerInUse() powerInUse} attribute.
     * @param powerInUse The value for powerInUse 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("powerInUse")
    public final Builder powerInUse(double powerInUse) {
      this.powerInUse = powerInUse;
      initBits &= ~INIT_BIT_POWER_IN_USE;
      return this;
    }

    /**
     * Initializes the value for the {@link Capacity#getMaximumCapacity() maximumCapacity} attribute.
     * @param maximumCapacity The value for maximumCapacity 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("maximumCapacity")
    public final Builder maximumCapacity(double maximumCapacity) {
      this.maximumCapacity = maximumCapacity;
      initBits &= ~INIT_BIT_MAXIMUM_CAPACITY;
      return this;
    }

    /**
     * Builds a new {@link ImmutableCapacity ImmutableCapacity}.
     * @return An immutable instance of Capacity
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableCapacity build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableCapacity(powerInUse, maximumCapacity);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_POWER_IN_USE) != 0) attributes.add("powerInUse");
      if ((initBits & INIT_BIT_MAXIMUM_CAPACITY) != 0) attributes.add("maximumCapacity");
      return "Cannot build Capacity, some of required attributes are not set " + attributes;
    }
  }
}
