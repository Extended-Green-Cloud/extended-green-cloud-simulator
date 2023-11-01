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
 * Immutable implementation of {@link JobTimeFrame}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableJobTimeFrame.builder()}.
 */
@Generated(from = "JobTimeFrame", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableJobTimeFrame implements JobTimeFrame {
  private final Instant start;
  private final Instant end;

  private ImmutableJobTimeFrame(Instant start, Instant end) {
    this.start = start;
    this.end = end;
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
   * Copy the current immutable object by setting a value for the {@link JobTimeFrame#getStart() start} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for start
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableJobTimeFrame withStart(Instant value) {
    if (this.start == value) return this;
    Instant newValue = Objects.requireNonNull(value, "start");
    return new ImmutableJobTimeFrame(newValue, this.end);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link JobTimeFrame#getEnd() end} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for end
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableJobTimeFrame withEnd(Instant value) {
    if (this.end == value) return this;
    Instant newValue = Objects.requireNonNull(value, "end");
    return new ImmutableJobTimeFrame(this.start, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableJobTimeFrame} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableJobTimeFrame
        && equalTo(0, (ImmutableJobTimeFrame) another);
  }

  private boolean equalTo(int synthetic, ImmutableJobTimeFrame another) {
    return start.equals(another.start)
        && end.equals(another.end);
  }

  /**
   * Computes a hash code from attributes: {@code start}, {@code end}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + start.hashCode();
    h += (h << 5) + end.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code JobTimeFrame} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("JobTimeFrame")
        .omitNullValues()
        .add("start", start)
        .add("end", end)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "JobTimeFrame", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements JobTimeFrame {
    @Nullable Instant start;
    @Nullable Instant end;
    @JsonProperty("start")
    public void setStart(Instant start) {
      this.start = start;
    }
    @JsonProperty("end")
    public void setEnd(Instant end) {
      this.end = end;
    }
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
  static ImmutableJobTimeFrame fromJson(Json json) {
    ImmutableJobTimeFrame.Builder builder = ImmutableJobTimeFrame.builder();
    if (json.start != null) {
      builder.start(json.start);
    }
    if (json.end != null) {
      builder.end(json.end);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link JobTimeFrame} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable JobTimeFrame instance
   */
  public static ImmutableJobTimeFrame copyOf(JobTimeFrame instance) {
    if (instance instanceof ImmutableJobTimeFrame) {
      return (ImmutableJobTimeFrame) instance;
    }
    return ImmutableJobTimeFrame.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableJobTimeFrame ImmutableJobTimeFrame}.
   * <pre>
   * ImmutableJobTimeFrame.builder()
   *    .start(java.time.Instant) // required {@link JobTimeFrame#getStart() start}
   *    .end(java.time.Instant) // required {@link JobTimeFrame#getEnd() end}
   *    .build();
   * </pre>
   * @return A new ImmutableJobTimeFrame builder
   */
  public static ImmutableJobTimeFrame.Builder builder() {
    return new ImmutableJobTimeFrame.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableJobTimeFrame ImmutableJobTimeFrame}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "JobTimeFrame", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_START = 0x1L;
    private static final long INIT_BIT_END = 0x2L;
    private long initBits = 0x3L;

    private @Nullable Instant start;
    private @Nullable Instant end;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code JobTimeFrame} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(JobTimeFrame instance) {
      Objects.requireNonNull(instance, "instance");
      start(instance.getStart());
      end(instance.getEnd());
      return this;
    }

    /**
     * Initializes the value for the {@link JobTimeFrame#getStart() start} attribute.
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
     * Initializes the value for the {@link JobTimeFrame#getEnd() end} attribute.
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
     * Builds a new {@link ImmutableJobTimeFrame ImmutableJobTimeFrame}.
     * @return An immutable instance of JobTimeFrame
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableJobTimeFrame build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableJobTimeFrame(start, end);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_START) != 0) attributes.add("start");
      if ((initBits & INIT_BIT_END) != 0) attributes.add("end");
      return "Cannot build JobTimeFrame, some of required attributes are not set " + attributes;
    }
  }
}
