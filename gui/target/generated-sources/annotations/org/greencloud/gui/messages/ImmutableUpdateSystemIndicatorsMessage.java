package org.greencloud.gui.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.Doubles;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link UpdateSystemIndicatorsMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableUpdateSystemIndicatorsMessage.builder()}.
 */
@Generated(from = "UpdateSystemIndicatorsMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableUpdateSystemIndicatorsMessage
    implements UpdateSystemIndicatorsMessage {
  private final double systemIndicator;
  private final ImmutableMap<Integer, Double> data;

  private ImmutableUpdateSystemIndicatorsMessage(
      double systemIndicator,
      ImmutableMap<Integer, Double> data) {
    this.systemIndicator = systemIndicator;
    this.data = data;
  }

  /**
   * @return quality indicator of the entire system
   */
  @JsonProperty("systemIndicator")
  @Override
  public double getSystemIndicator() {
    return systemIndicator;
  }

  /**
   * @return map of goal identifiers and the corresponding current qualities
   */
  @JsonProperty("data")
  @Override
  public ImmutableMap<Integer, Double> getData() {
    return data;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateSystemIndicatorsMessage#getSystemIndicator() systemIndicator} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for systemIndicator
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateSystemIndicatorsMessage withSystemIndicator(double value) {
    if (Double.doubleToLongBits(this.systemIndicator) == Double.doubleToLongBits(value)) return this;
    return new ImmutableUpdateSystemIndicatorsMessage(value, this.data);
  }

  /**
   * Copy the current immutable object by replacing the {@link UpdateSystemIndicatorsMessage#getData() data} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the data map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableUpdateSystemIndicatorsMessage withData(Map<Integer, ? extends Double> entries) {
    if (this.data == entries) return this;
    ImmutableMap<Integer, Double> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableUpdateSystemIndicatorsMessage(this.systemIndicator, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableUpdateSystemIndicatorsMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableUpdateSystemIndicatorsMessage
        && equalTo(0, (ImmutableUpdateSystemIndicatorsMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableUpdateSystemIndicatorsMessage another) {
    return Double.doubleToLongBits(systemIndicator) == Double.doubleToLongBits(another.systemIndicator)
        && data.equals(another.data);
  }

  /**
   * Computes a hash code from attributes: {@code systemIndicator}, {@code data}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Doubles.hashCode(systemIndicator);
    h += (h << 5) + data.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code UpdateSystemIndicatorsMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("UpdateSystemIndicatorsMessage")
        .omitNullValues()
        .add("systemIndicator", systemIndicator)
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "UpdateSystemIndicatorsMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements UpdateSystemIndicatorsMessage {
    double systemIndicator;
    boolean systemIndicatorIsSet;
    @Nullable Map<Integer, Double> data = ImmutableMap.of();
    @JsonProperty("systemIndicator")
    public void setSystemIndicator(double systemIndicator) {
      this.systemIndicator = systemIndicator;
      this.systemIndicatorIsSet = true;
    }
    @JsonProperty("data")
    public void setData(Map<Integer, Double> data) {
      this.data = data;
    }
    @Override
    public double getSystemIndicator() { throw new UnsupportedOperationException(); }
    @Override
    public Map<Integer, Double> getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableUpdateSystemIndicatorsMessage fromJson(Json json) {
    ImmutableUpdateSystemIndicatorsMessage.Builder builder = ImmutableUpdateSystemIndicatorsMessage.builder();
    if (json.systemIndicatorIsSet) {
      builder.systemIndicator(json.systemIndicator);
    }
    if (json.data != null) {
      builder.putAllData(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link UpdateSystemIndicatorsMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable UpdateSystemIndicatorsMessage instance
   */
  public static ImmutableUpdateSystemIndicatorsMessage copyOf(UpdateSystemIndicatorsMessage instance) {
    if (instance instanceof ImmutableUpdateSystemIndicatorsMessage) {
      return (ImmutableUpdateSystemIndicatorsMessage) instance;
    }
    return ImmutableUpdateSystemIndicatorsMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableUpdateSystemIndicatorsMessage ImmutableUpdateSystemIndicatorsMessage}.
   * <pre>
   * ImmutableUpdateSystemIndicatorsMessage.builder()
   *    .systemIndicator(double) // required {@link UpdateSystemIndicatorsMessage#getSystemIndicator() systemIndicator}
   *    .putData|putAllData(int =&gt; double) // {@link UpdateSystemIndicatorsMessage#getData() data} mappings
   *    .build();
   * </pre>
   * @return A new ImmutableUpdateSystemIndicatorsMessage builder
   */
  public static ImmutableUpdateSystemIndicatorsMessage.Builder builder() {
    return new ImmutableUpdateSystemIndicatorsMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableUpdateSystemIndicatorsMessage ImmutableUpdateSystemIndicatorsMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "UpdateSystemIndicatorsMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_SYSTEM_INDICATOR = 0x1L;
    private long initBits = 0x1L;

    private double systemIndicator;
    private ImmutableMap.Builder<Integer, Double> data = ImmutableMap.builder();

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code UpdateSystemIndicatorsMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(UpdateSystemIndicatorsMessage instance) {
      Objects.requireNonNull(instance, "instance");
      systemIndicator(instance.getSystemIndicator());
      putAllData(instance.getData());
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateSystemIndicatorsMessage#getSystemIndicator() systemIndicator} attribute.
     * @param systemIndicator The value for systemIndicator 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("systemIndicator")
    public final Builder systemIndicator(double systemIndicator) {
      this.systemIndicator = systemIndicator;
      initBits &= ~INIT_BIT_SYSTEM_INDICATOR;
      return this;
    }

    /**
     * Put one entry to the {@link UpdateSystemIndicatorsMessage#getData() data} map.
     * @param key The key in the data map
     * @param value The associated value in the data map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putData(int key, double value) {
      this.data.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link UpdateSystemIndicatorsMessage#getData() data} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putData(Map.Entry<Integer, ? extends Double> entry) {
      this.data.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link UpdateSystemIndicatorsMessage#getData() data} map. Nulls are not permitted
     * @param entries The entries that will be added to the data map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(Map<Integer, ? extends Double> entries) {
      this.data = ImmutableMap.builder();
      return putAllData(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link UpdateSystemIndicatorsMessage#getData() data} map. Nulls are not permitted
     * @param entries The entries that will be added to the data map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllData(Map<Integer, ? extends Double> entries) {
      this.data.putAll(entries);
      return this;
    }

    /**
     * Builds a new {@link ImmutableUpdateSystemIndicatorsMessage ImmutableUpdateSystemIndicatorsMessage}.
     * @return An immutable instance of UpdateSystemIndicatorsMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableUpdateSystemIndicatorsMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableUpdateSystemIndicatorsMessage(systemIndicator, data.build());
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_SYSTEM_INDICATOR) != 0) attributes.add("systemIndicator");
      return "Cannot build UpdateSystemIndicatorsMessage, some of required attributes are not set " + attributes;
    }
  }
}
