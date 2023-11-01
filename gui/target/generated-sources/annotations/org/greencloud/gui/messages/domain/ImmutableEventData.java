package org.greencloud.gui.messages.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
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
 * Immutable implementation of {@link EventData}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableEventData.builder()}.
 */
@Generated(from = "EventData", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableEventData implements EventData {
  private final Instant occurrenceTime;
  private final @Nullable Boolean isFinished;

  private ImmutableEventData(Instant occurrenceTime, @Nullable Boolean isFinished) {
    this.occurrenceTime = occurrenceTime;
    this.isFinished = isFinished;
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
   * Copy the current immutable object by setting a value for the {@link EventData#getOccurrenceTime() occurrenceTime} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for occurrenceTime
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableEventData withOccurrenceTime(Instant value) {
    if (this.occurrenceTime == value) return this;
    Instant newValue = Objects.requireNonNull(value, "occurrenceTime");
    return new ImmutableEventData(newValue, this.isFinished);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link EventData#isFinished() isFinished} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for isFinished (can be {@code null})
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableEventData withIsFinished(@Nullable Boolean value) {
    if (Objects.equals(this.isFinished, value)) return this;
    return new ImmutableEventData(this.occurrenceTime, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableEventData} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableEventData
        && equalTo(0, (ImmutableEventData) another);
  }

  private boolean equalTo(int synthetic, ImmutableEventData another) {
    return occurrenceTime.equals(another.occurrenceTime)
        && Objects.equals(isFinished, another.isFinished);
  }

  /**
   * Computes a hash code from attributes: {@code occurrenceTime}, {@code isFinished}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + occurrenceTime.hashCode();
    h += (h << 5) + Objects.hashCode(isFinished);
    return h;
  }

  /**
   * Prints the immutable value {@code EventData} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("EventData")
        .omitNullValues()
        .add("occurrenceTime", occurrenceTime)
        .add("isFinished", isFinished)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "EventData", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements EventData {
    @Nullable Instant occurrenceTime;
    @Nullable Boolean isFinished;
    @JsonProperty("occurrenceTime")
    public void setOccurrenceTime(Instant occurrenceTime) {
      this.occurrenceTime = occurrenceTime;
    }
    @JsonProperty("isFinished")
    public void setIsFinished(@Nullable Boolean isFinished) {
      this.isFinished = isFinished;
    }
    @Override
    public Instant getOccurrenceTime() { throw new UnsupportedOperationException(); }
    @Override
    public Boolean isFinished() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableEventData fromJson(Json json) {
    ImmutableEventData.Builder builder = ImmutableEventData.builder();
    if (json.occurrenceTime != null) {
      builder.occurrenceTime(json.occurrenceTime);
    }
    if (json.isFinished != null) {
      builder.isFinished(json.isFinished);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link EventData} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable EventData instance
   */
  public static ImmutableEventData copyOf(EventData instance) {
    if (instance instanceof ImmutableEventData) {
      return (ImmutableEventData) instance;
    }
    return ImmutableEventData.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableEventData ImmutableEventData}.
   * <pre>
   * ImmutableEventData.builder()
   *    .occurrenceTime(java.time.Instant) // required {@link EventData#getOccurrenceTime() occurrenceTime}
   *    .isFinished(Boolean | null) // nullable {@link EventData#isFinished() isFinished}
   *    .build();
   * </pre>
   * @return A new ImmutableEventData builder
   */
  public static ImmutableEventData.Builder builder() {
    return new ImmutableEventData.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableEventData ImmutableEventData}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "EventData", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_OCCURRENCE_TIME = 0x1L;
    private long initBits = 0x1L;

    private @Nullable Instant occurrenceTime;
    private @Nullable Boolean isFinished;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code EventData} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(EventData instance) {
      Objects.requireNonNull(instance, "instance");
      occurrenceTime(instance.getOccurrenceTime());
      @Nullable Boolean isFinishedValue = instance.isFinished();
      if (isFinishedValue != null) {
        isFinished(isFinishedValue);
      }
      return this;
    }

    /**
     * Initializes the value for the {@link EventData#getOccurrenceTime() occurrenceTime} attribute.
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
     * Initializes the value for the {@link EventData#isFinished() isFinished} attribute.
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
     * Builds a new {@link ImmutableEventData ImmutableEventData}.
     * @return An immutable instance of EventData
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableEventData build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableEventData(occurrenceTime, isFinished);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_OCCURRENCE_TIME) != 0) attributes.add("occurrenceTime");
      return "Cannot build EventData, some of required attributes are not set " + attributes;
    }
  }
}
