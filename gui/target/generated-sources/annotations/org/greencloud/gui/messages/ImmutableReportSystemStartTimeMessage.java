package org.greencloud.gui.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.primitives.Longs;
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
 * Immutable implementation of {@link ReportSystemStartTimeMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableReportSystemStartTimeMessage.builder()}.
 */
@Generated(from = "ReportSystemStartTimeMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableReportSystemStartTimeMessage
    implements ReportSystemStartTimeMessage {
  private final long time;
  private final long secondsPerHour;

  private ImmutableReportSystemStartTimeMessage(long time, long secondsPerHour) {
    this.time = time;
    this.secondsPerHour = secondsPerHour;
  }

  /**
   * @return unix time instant representing system start time
   */
  @JsonProperty("time")
  @Override
  public long getTime() {
    return time;
  }

  /**
   * @return number of seconds that corresponds in simulation time to 1 hour
   */
  @JsonProperty("secondsPerHour")
  @Override
  public long getSecondsPerHour() {
    return secondsPerHour;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ReportSystemStartTimeMessage#getTime() time} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for time
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableReportSystemStartTimeMessage withTime(long value) {
    if (this.time == value) return this;
    return new ImmutableReportSystemStartTimeMessage(value, this.secondsPerHour);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ReportSystemStartTimeMessage#getSecondsPerHour() secondsPerHour} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for secondsPerHour
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableReportSystemStartTimeMessage withSecondsPerHour(long value) {
    if (this.secondsPerHour == value) return this;
    return new ImmutableReportSystemStartTimeMessage(this.time, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableReportSystemStartTimeMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableReportSystemStartTimeMessage
        && equalTo(0, (ImmutableReportSystemStartTimeMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableReportSystemStartTimeMessage another) {
    return time == another.time
        && secondsPerHour == another.secondsPerHour;
  }

  /**
   * Computes a hash code from attributes: {@code time}, {@code secondsPerHour}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Longs.hashCode(time);
    h += (h << 5) + Longs.hashCode(secondsPerHour);
    return h;
  }

  /**
   * Prints the immutable value {@code ReportSystemStartTimeMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ReportSystemStartTimeMessage")
        .omitNullValues()
        .add("time", time)
        .add("secondsPerHour", secondsPerHour)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ReportSystemStartTimeMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ReportSystemStartTimeMessage {
    long time;
    boolean timeIsSet;
    long secondsPerHour;
    boolean secondsPerHourIsSet;
    @JsonProperty("time")
    public void setTime(long time) {
      this.time = time;
      this.timeIsSet = true;
    }
    @JsonProperty("secondsPerHour")
    public void setSecondsPerHour(long secondsPerHour) {
      this.secondsPerHour = secondsPerHour;
      this.secondsPerHourIsSet = true;
    }
    @Override
    public long getTime() { throw new UnsupportedOperationException(); }
    @Override
    public long getSecondsPerHour() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableReportSystemStartTimeMessage fromJson(Json json) {
    ImmutableReportSystemStartTimeMessage.Builder builder = ImmutableReportSystemStartTimeMessage.builder();
    if (json.timeIsSet) {
      builder.time(json.time);
    }
    if (json.secondsPerHourIsSet) {
      builder.secondsPerHour(json.secondsPerHour);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ReportSystemStartTimeMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ReportSystemStartTimeMessage instance
   */
  public static ImmutableReportSystemStartTimeMessage copyOf(ReportSystemStartTimeMessage instance) {
    if (instance instanceof ImmutableReportSystemStartTimeMessage) {
      return (ImmutableReportSystemStartTimeMessage) instance;
    }
    return ImmutableReportSystemStartTimeMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableReportSystemStartTimeMessage ImmutableReportSystemStartTimeMessage}.
   * <pre>
   * ImmutableReportSystemStartTimeMessage.builder()
   *    .time(long) // required {@link ReportSystemStartTimeMessage#getTime() time}
   *    .secondsPerHour(long) // required {@link ReportSystemStartTimeMessage#getSecondsPerHour() secondsPerHour}
   *    .build();
   * </pre>
   * @return A new ImmutableReportSystemStartTimeMessage builder
   */
  public static ImmutableReportSystemStartTimeMessage.Builder builder() {
    return new ImmutableReportSystemStartTimeMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableReportSystemStartTimeMessage ImmutableReportSystemStartTimeMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ReportSystemStartTimeMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_TIME = 0x1L;
    private static final long INIT_BIT_SECONDS_PER_HOUR = 0x2L;
    private long initBits = 0x3L;

    private long time;
    private long secondsPerHour;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ReportSystemStartTimeMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ReportSystemStartTimeMessage instance) {
      Objects.requireNonNull(instance, "instance");
      time(instance.getTime());
      secondsPerHour(instance.getSecondsPerHour());
      return this;
    }

    /**
     * Initializes the value for the {@link ReportSystemStartTimeMessage#getTime() time} attribute.
     * @param time The value for time 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("time")
    public final Builder time(long time) {
      this.time = time;
      initBits &= ~INIT_BIT_TIME;
      return this;
    }

    /**
     * Initializes the value for the {@link ReportSystemStartTimeMessage#getSecondsPerHour() secondsPerHour} attribute.
     * @param secondsPerHour The value for secondsPerHour 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("secondsPerHour")
    public final Builder secondsPerHour(long secondsPerHour) {
      this.secondsPerHour = secondsPerHour;
      initBits &= ~INIT_BIT_SECONDS_PER_HOUR;
      return this;
    }

    /**
     * Builds a new {@link ImmutableReportSystemStartTimeMessage ImmutableReportSystemStartTimeMessage}.
     * @return An immutable instance of ReportSystemStartTimeMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableReportSystemStartTimeMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableReportSystemStartTimeMessage(time, secondsPerHour);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_TIME) != 0) attributes.add("time");
      if ((initBits & INIT_BIT_SECONDS_PER_HOUR) != 0) attributes.add("secondsPerHour");
      return "Cannot build ReportSystemStartTimeMessage, some of required attributes are not set " + attributes;
    }
  }
}
