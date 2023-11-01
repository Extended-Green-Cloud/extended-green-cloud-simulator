package org.greencloud.gui.messages.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.primitives.Doubles;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.time.Instant;
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
 * Immutable implementation of {@link SplitJob}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableSplitJob.builder()}.
 */
@Generated(from = "SplitJob", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableSplitJob implements SplitJob {
  private final String splitJobId;
  private final double power;
  private final Instant start;
  private final Instant end;

  private ImmutableSplitJob(String splitJobId, double power, Instant start, Instant end) {
    this.splitJobId = splitJobId;
    this.power = power;
    this.start = start;
    this.end = end;
  }

  /**
   * @return The value of the {@code splitJobId} attribute
   */
  @JsonProperty("splitJobId")
  @Override
  public String getSplitJobId() {
    return splitJobId;
  }

  /**
   * @return The value of the {@code power} attribute
   */
  @JsonProperty("power")
  @Override
  public double getPower() {
    return power;
  }

  /**
   * @return The value of the {@code start} attribute
   */
  @JsonProperty("start")
  @Override
  public Instant getStart() {
    return start;
  }

  /**
   * @return The value of the {@code end} attribute
   */
  @JsonProperty("end")
  @Override
  public Instant getEnd() {
    return end;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SplitJob#getSplitJobId() splitJobId} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for splitJobId
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSplitJob withSplitJobId(String value) {
    String newValue = Objects.requireNonNull(value, "splitJobId");
    if (this.splitJobId.equals(newValue)) return this;
    return new ImmutableSplitJob(newValue, this.power, this.start, this.end);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SplitJob#getPower() power} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for power
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSplitJob withPower(double value) {
    if (Double.doubleToLongBits(this.power) == Double.doubleToLongBits(value)) return this;
    return new ImmutableSplitJob(this.splitJobId, value, this.start, this.end);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SplitJob#getStart() start} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for start
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSplitJob withStart(Instant value) {
    if (this.start == value) return this;
    Instant newValue = Objects.requireNonNull(value, "start");
    return new ImmutableSplitJob(this.splitJobId, this.power, newValue, this.end);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SplitJob#getEnd() end} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for end
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSplitJob withEnd(Instant value) {
    if (this.end == value) return this;
    Instant newValue = Objects.requireNonNull(value, "end");
    return new ImmutableSplitJob(this.splitJobId, this.power, this.start, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableSplitJob} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableSplitJob
        && equalTo(0, (ImmutableSplitJob) another);
  }

  private boolean equalTo(int synthetic, ImmutableSplitJob another) {
    return splitJobId.equals(another.splitJobId)
        && Double.doubleToLongBits(power) == Double.doubleToLongBits(another.power)
        && start.equals(another.start)
        && end.equals(another.end);
  }

  /**
   * Computes a hash code from attributes: {@code splitJobId}, {@code power}, {@code start}, {@code end}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + splitJobId.hashCode();
    h += (h << 5) + Doubles.hashCode(power);
    h += (h << 5) + start.hashCode();
    h += (h << 5) + end.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code SplitJob} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("SplitJob")
        .omitNullValues()
        .add("splitJobId", splitJobId)
        .add("power", power)
        .add("start", start)
        .add("end", end)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "SplitJob", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements SplitJob {
    @Nullable String splitJobId;
    double power;
    boolean powerIsSet;
    @Nullable Instant start;
    @Nullable Instant end;
    @JsonProperty("splitJobId")
    public void setSplitJobId(String splitJobId) {
      this.splitJobId = splitJobId;
    }
    @JsonProperty("power")
    public void setPower(double power) {
      this.power = power;
      this.powerIsSet = true;
    }
    @JsonProperty("start")
    public void setStart(Instant start) {
      this.start = start;
    }
    @JsonProperty("end")
    public void setEnd(Instant end) {
      this.end = end;
    }
    @Override
    public String getSplitJobId() { throw new UnsupportedOperationException(); }
    @Override
    public double getPower() { throw new UnsupportedOperationException(); }
    @Override
    public Instant getStart() { throw new UnsupportedOperationException(); }
    @Override
    public Instant getEnd() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableSplitJob fromJson(Json json) {
    ImmutableSplitJob.Builder builder = ImmutableSplitJob.builder();
    if (json.splitJobId != null) {
      builder.splitJobId(json.splitJobId);
    }
    if (json.powerIsSet) {
      builder.power(json.power);
    }
    if (json.start != null) {
      builder.start(json.start);
    }
    if (json.end != null) {
      builder.end(json.end);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link SplitJob} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable SplitJob instance
   */
  public static ImmutableSplitJob copyOf(SplitJob instance) {
    if (instance instanceof ImmutableSplitJob) {
      return (ImmutableSplitJob) instance;
    }
    return ImmutableSplitJob.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableSplitJob ImmutableSplitJob}.
   * <pre>
   * ImmutableSplitJob.builder()
   *    .splitJobId(String) // required {@link SplitJob#getSplitJobId() splitJobId}
   *    .power(double) // required {@link SplitJob#getPower() power}
   *    .start(java.time.Instant) // required {@link SplitJob#getStart() start}
   *    .end(java.time.Instant) // required {@link SplitJob#getEnd() end}
   *    .build();
   * </pre>
   * @return A new ImmutableSplitJob builder
   */
  public static ImmutableSplitJob.Builder builder() {
    return new ImmutableSplitJob.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableSplitJob ImmutableSplitJob}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "SplitJob", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_SPLIT_JOB_ID = 0x1L;
    private static final long INIT_BIT_POWER = 0x2L;
    private static final long INIT_BIT_START = 0x4L;
    private static final long INIT_BIT_END = 0x8L;
    private long initBits = 0xfL;

    private @Nullable String splitJobId;
    private double power;
    private @Nullable Instant start;
    private @Nullable Instant end;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code SplitJob} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(SplitJob instance) {
      Objects.requireNonNull(instance, "instance");
      splitJobId(instance.getSplitJobId());
      power(instance.getPower());
      start(instance.getStart());
      end(instance.getEnd());
      return this;
    }

    /**
     * Initializes the value for the {@link SplitJob#getSplitJobId() splitJobId} attribute.
     * @param splitJobId The value for splitJobId 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("splitJobId")
    public final Builder splitJobId(String splitJobId) {
      this.splitJobId = Objects.requireNonNull(splitJobId, "splitJobId");
      initBits &= ~INIT_BIT_SPLIT_JOB_ID;
      return this;
    }

    /**
     * Initializes the value for the {@link SplitJob#getPower() power} attribute.
     * @param power The value for power 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("power")
    public final Builder power(double power) {
      this.power = power;
      initBits &= ~INIT_BIT_POWER;
      return this;
    }

    /**
     * Initializes the value for the {@link SplitJob#getStart() start} attribute.
     * @param start The value for start 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("start")
    public final Builder start(Instant start) {
      this.start = Objects.requireNonNull(start, "start");
      initBits &= ~INIT_BIT_START;
      return this;
    }

    /**
     * Initializes the value for the {@link SplitJob#getEnd() end} attribute.
     * @param end The value for end 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("end")
    public final Builder end(Instant end) {
      this.end = Objects.requireNonNull(end, "end");
      initBits &= ~INIT_BIT_END;
      return this;
    }

    /**
     * Builds a new {@link ImmutableSplitJob ImmutableSplitJob}.
     * @return An immutable instance of SplitJob
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableSplitJob build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableSplitJob(splitJobId, power, start, end);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_SPLIT_JOB_ID) != 0) attributes.add("splitJobId");
      if ((initBits & INIT_BIT_POWER) != 0) attributes.add("power");
      if ((initBits & INIT_BIT_START) != 0) attributes.add("start");
      if ((initBits & INIT_BIT_END) != 0) attributes.add("end");
      return "Cannot build SplitJob, some of required attributes are not set " + attributes;
    }
  }
}
