package org.greencloud.gui.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
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
import org.greencloud.gui.messages.domain.AdaptationAction;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link UpdateAdaptationActionMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableUpdateAdaptationActionMessage.builder()}.
 */
@Generated(from = "UpdateAdaptationActionMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableUpdateAdaptationActionMessage
    implements UpdateAdaptationActionMessage {
  private final AdaptationAction data;

  private ImmutableUpdateAdaptationActionMessage(AdaptationAction data) {
    this.data = data;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public AdaptationAction getData() {
    return data;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateAdaptationActionMessage#getData() data} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateAdaptationActionMessage withData(AdaptationAction value) {
    if (this.data == value) return this;
    AdaptationAction newValue = Objects.requireNonNull(value, "data");
    return new ImmutableUpdateAdaptationActionMessage(newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableUpdateAdaptationActionMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableUpdateAdaptationActionMessage
        && equalTo(0, (ImmutableUpdateAdaptationActionMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableUpdateAdaptationActionMessage another) {
    return data.equals(another.data);
  }

  /**
   * Computes a hash code from attributes: {@code data}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + data.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code UpdateAdaptationActionMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("UpdateAdaptationActionMessage")
        .omitNullValues()
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "UpdateAdaptationActionMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements UpdateAdaptationActionMessage {
    @Nullable AdaptationAction data;
    @JsonProperty("data")
    public void setData(AdaptationAction data) {
      this.data = data;
    }
    @Override
    public AdaptationAction getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableUpdateAdaptationActionMessage fromJson(Json json) {
    ImmutableUpdateAdaptationActionMessage.Builder builder = ImmutableUpdateAdaptationActionMessage.builder();
    if (json.data != null) {
      builder.data(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link UpdateAdaptationActionMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable UpdateAdaptationActionMessage instance
   */
  public static ImmutableUpdateAdaptationActionMessage copyOf(UpdateAdaptationActionMessage instance) {
    if (instance instanceof ImmutableUpdateAdaptationActionMessage) {
      return (ImmutableUpdateAdaptationActionMessage) instance;
    }
    return ImmutableUpdateAdaptationActionMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableUpdateAdaptationActionMessage ImmutableUpdateAdaptationActionMessage}.
   * <pre>
   * ImmutableUpdateAdaptationActionMessage.builder()
   *    .data(org.greencloud.gui.messages.domain.AdaptationAction) // required {@link UpdateAdaptationActionMessage#getData() data}
   *    .build();
   * </pre>
   * @return A new ImmutableUpdateAdaptationActionMessage builder
   */
  public static ImmutableUpdateAdaptationActionMessage.Builder builder() {
    return new ImmutableUpdateAdaptationActionMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableUpdateAdaptationActionMessage ImmutableUpdateAdaptationActionMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "UpdateAdaptationActionMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DATA = 0x1L;
    private long initBits = 0x1L;

    private @Nullable AdaptationAction data;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code UpdateAdaptationActionMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(UpdateAdaptationActionMessage instance) {
      Objects.requireNonNull(instance, "instance");
      data(instance.getData());
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateAdaptationActionMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(AdaptationAction data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Builds a new {@link ImmutableUpdateAdaptationActionMessage ImmutableUpdateAdaptationActionMessage}.
     * @return An immutable instance of UpdateAdaptationActionMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableUpdateAdaptationActionMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableUpdateAdaptationActionMessage(data);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      return "Cannot build UpdateAdaptationActionMessage, some of required attributes are not set " + attributes;
    }
  }
}
