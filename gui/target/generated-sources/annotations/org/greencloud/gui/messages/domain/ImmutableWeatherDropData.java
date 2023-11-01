package org.greencloud.gui.messages.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.primitives.Longs;
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
 * Immutable implementation of {@link WeatherDropData}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableWeatherDropData.builder()}.
 */
@Generated(from = "WeatherDropData", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableWeatherDropData implements WeatherDropData {
  private final Instant occurrenceTime;
  private final @Nullable Boolean isFinished;
  private final long duration;

  private ImmutableWeatherDropData(
      Instant occurrenceTime,
      @Nullable Boolean isFinished,
      long duration) {
    this.occurrenceTime = occurrenceTime;
    this.isFinished = isFinished;
    this.duration = duration;
  }

  /**
   * @return The value of the {@code occurrenceTime} attribute
   */
  @JsonProperty("occurrenceTime")
  @Override
  public Instant getOccurrenceTime() {
    return occurrenceTime;
  }

  /**
   * @return The value of the {@code isFinished} attribute
   */
  @JsonProperty("isFinished")
  @Override
  public @Nullable Boolean isFinished() {
    return isFinished;
  }

  /**
   * @return The value of the {@code duration} attribute
   */
  @JsonProperty("duration")
  @Override
  public long getDuration() {
    return duration;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link WeatherDropData#getOccurrenceTime() occurrenceTime} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for occurrenceTime
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableWeatherDropData withOccurrenceTime(Instant value) {
    if (this.occurrenceTime == value) return this;
    Instant newValue = Objects.requireNonNull(value, "occurrenceTime");
    return new ImmutableWeatherDropData(newValue, this.isFinished, this.duration);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link WeatherDropData#isFinished() isFinished} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for isFinished (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableWeatherDropData withIsFinished(@Nullable Boolean value) {
    if (Objects.equals(this.isFinished, value)) return this;
    return new ImmutableWeatherDropData(this.occurrenceTime, value, this.duration);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link WeatherDropData#getDuration() duration} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for duration
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableWeatherDropData withDuration(long value) {
    if (this.duration == value) return this;
    return new ImmutableWeatherDropData(this.occurrenceTime, this.isFinished, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableWeatherDropData} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableWeatherDropData
        && equalTo(0, (ImmutableWeatherDropData) another);
  }

  private boolean equalTo(int synthetic, ImmutableWeatherDropData another) {
    return occurrenceTime.equals(another.occurrenceTime)
        && Objects.equals(isFinished, another.isFinished)
        && duration == another.duration;
  }

  /**
   * Computes a hash code from attributes: {@code occurrenceTime}, {@code isFinished}, {@code duration}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + occurrenceTime.hashCode();
    h += (h << 5) + Objects.hashCode(isFinished);
    h += (h << 5) + Longs.hashCode(duration);
    return h;
  }

  /**
   * Prints the immutable value {@code WeatherDropData} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("WeatherDropData")
        .omitNullValues()
        .add("occurrenceTime", occurrenceTime)
        .add("isFinished", isFinished)
        .add("duration", duration)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "WeatherDropData", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements WeatherDropData {
    @Nullable Instant occurrenceTime;
    @Nullable Boolean isFinished;
    long duration;
    boolean durationIsSet;
    @JsonProperty("occurrenceTime")
    public void setOccurrenceTime(Instant occurrenceTime) {
      this.occurrenceTime = occurrenceTime;
    }
    @JsonProperty("isFinished")
    public void setIsFinished(@Nullable Boolean isFinished) {
      this.isFinished = isFinished;
    }
    @JsonProperty("duration")
    public void setDuration(long duration) {
      this.duration = duration;
      this.durationIsSet = true;
    }
    @Override
    public Instant getOccurrenceTime() { throw new UnsupportedOperationException(); }
    @Override
    public Boolean isFinished() { throw new UnsupportedOperationException(); }
    @Override
    public long getDuration() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableWeatherDropData fromJson(Json json) {
    ImmutableWeatherDropData.Builder builder = ImmutableWeatherDropData.builder();
    if (json.occurrenceTime != null) {
      builder.occurrenceTime(json.occurrenceTime);
    }
    if (json.isFinished != null) {
      builder.isFinished(json.isFinished);
    }
    if (json.durationIsSet) {
      builder.duration(json.duration);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link WeatherDropData} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable WeatherDropData instance
   */
  public static ImmutableWeatherDropData copyOf(WeatherDropData instance) {
    if (instance instanceof ImmutableWeatherDropData) {
      return (ImmutableWeatherDropData) instance;
    }
    return ImmutableWeatherDropData.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableWeatherDropData ImmutableWeatherDropData}.
   * <pre>
   * ImmutableWeatherDropData.builder()
   *    .occurrenceTime(java.time.Instant) // required {@link WeatherDropData#getOccurrenceTime() occurrenceTime}
   *    .isFinished(Boolean | null) // nullable {@link WeatherDropData#isFinished() isFinished}
   *    .duration(long) // required {@link WeatherDropData#getDuration() duration}
   *    .build();
   * </pre>
   * @return A new ImmutableWeatherDropData builder
   */
  public static ImmutableWeatherDropData.Builder builder() {
    return new ImmutableWeatherDropData.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableWeatherDropData ImmutableWeatherDropData}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "WeatherDropData", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_OCCURRENCE_TIME = 0x1L;
    private static final long INIT_BIT_DURATION = 0x2L;
    private long initBits = 0x3L;

    private @Nullable Instant occurrenceTime;
    private @Nullable Boolean isFinished;
    private long duration;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.domain.WeatherDropData} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(WeatherDropData instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.domain.EventData} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(EventData instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      @Var long bits = 0;
      if (object instanceof WeatherDropData) {
        WeatherDropData instance = (WeatherDropData) object;
        duration(instance.getDuration());
        if ((bits & 0x2L) == 0) {
          @Nullable Boolean isFinishedValue = instance.isFinished();
          if (isFinishedValue != null) {
            isFinished(isFinishedValue);
          }
          bits |= 0x2L;
        }
        if ((bits & 0x1L) == 0) {
          occurrenceTime(instance.getOccurrenceTime());
          bits |= 0x1L;
        }
      }
      if (object instanceof EventData) {
        EventData instance = (EventData) object;
        if ((bits & 0x2L) == 0) {
          @Nullable Boolean isFinishedValue = instance.isFinished();
          if (isFinishedValue != null) {
            isFinished(isFinishedValue);
          }
          bits |= 0x2L;
        }
        if ((bits & 0x1L) == 0) {
          occurrenceTime(instance.getOccurrenceTime());
          bits |= 0x1L;
        }
      }
    }

    /**
     * Initializes the value for the {@link WeatherDropData#getOccurrenceTime() occurrenceTime} attribute.
     * @param occurrenceTime The value for occurrenceTime 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("occurrenceTime")
    public final Builder occurrenceTime(Instant occurrenceTime) {
      this.occurrenceTime = Objects.requireNonNull(occurrenceTime, "occurrenceTime");
      initBits &= ~INIT_BIT_OCCURRENCE_TIME;
      return this;
    }

    /**
     * Initializes the value for the {@link WeatherDropData#isFinished() isFinished} attribute.
     * @param isFinished The value for isFinished (can be {@code null})
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("isFinished")
    public final Builder isFinished(@Nullable Boolean isFinished) {
      this.isFinished = isFinished;
      return this;
    }

    /**
     * Initializes the value for the {@link WeatherDropData#getDuration() duration} attribute.
     * @param duration The value for duration 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("duration")
    public final Builder duration(long duration) {
      this.duration = duration;
      initBits &= ~INIT_BIT_DURATION;
      return this;
    }

    /**
     * Builds a new {@link ImmutableWeatherDropData ImmutableWeatherDropData}.
     * @return An immutable instance of WeatherDropData
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableWeatherDropData build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableWeatherDropData(occurrenceTime, isFinished, duration);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_OCCURRENCE_TIME) != 0) attributes.add("occurrenceTime");
      if ((initBits & INIT_BIT_DURATION) != 0) attributes.add("duration");
      return "Cannot build WeatherDropData, some of required attributes are not set " + attributes;
    }
  }
}
