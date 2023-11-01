package org.greencloud.gui.messages;

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
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link UpdateSingleValueMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableUpdateSingleValueMessage.builder()}.
 */
@Generated(from = "UpdateSingleValueMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableUpdateSingleValueMessage
    implements UpdateSingleValueMessage {
  private final String type;
  private final double data;

  private ImmutableUpdateSingleValueMessage(String type, double data) {
    this.type = type;
    this.data = data;
  }

  /**
   * @return The value of the {@code type} attribute
   */
  @JsonProperty("type")
  @Override
  public String getType() {
    return type;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public double getData() {
    return data;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateSingleValueMessage#getType() type} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateSingleValueMessage withType(String value) {
    String newValue = Objects.requireNonNull(value, "type");
    if (this.type.equals(newValue)) return this;
    return new ImmutableUpdateSingleValueMessage(newValue, this.data);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateSingleValueMessage#getData() data} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateSingleValueMessage withData(double value) {
    if (Double.doubleToLongBits(this.data) == Double.doubleToLongBits(value)) return this;
    return new ImmutableUpdateSingleValueMessage(this.type, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableUpdateSingleValueMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableUpdateSingleValueMessage
        && equalTo(0, (ImmutableUpdateSingleValueMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableUpdateSingleValueMessage another) {
    return type.equals(another.type)
        && Double.doubleToLongBits(data) == Double.doubleToLongBits(another.data);
  }

  /**
   * Computes a hash code from attributes: {@code type}, {@code data}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + type.hashCode();
    h += (h << 5) + Doubles.hashCode(data);
    return h;
  }

  /**
   * Prints the immutable value {@code UpdateSingleValueMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("UpdateSingleValueMessage")
        .omitNullValues()
        .add("type", type)
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "UpdateSingleValueMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements UpdateSingleValueMessage {
    @Nullable String type;
    double data;
    boolean dataIsSet;
    @JsonProperty("type")
    public void setType(String type) {
      this.type = type;
    }
    @JsonProperty("data")
    public void setData(double data) {
      this.data = data;
      this.dataIsSet = true;
    }
    @Override
    public String getType() { throw new UnsupportedOperationException(); }
    @Override
    public double getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableUpdateSingleValueMessage fromJson(Json json) {
    ImmutableUpdateSingleValueMessage.Builder builder = ImmutableUpdateSingleValueMessage.builder();
    if (json.type != null) {
      builder.type(json.type);
    }
    if (json.dataIsSet) {
      builder.data(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link UpdateSingleValueMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable UpdateSingleValueMessage instance
   */
  public static ImmutableUpdateSingleValueMessage copyOf(UpdateSingleValueMessage instance) {
    if (instance instanceof ImmutableUpdateSingleValueMessage) {
      return (ImmutableUpdateSingleValueMessage) instance;
    }
    return ImmutableUpdateSingleValueMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableUpdateSingleValueMessage ImmutableUpdateSingleValueMessage}.
   * <pre>
   * ImmutableUpdateSingleValueMessage.builder()
   *    .type(String) // required {@link UpdateSingleValueMessage#getType() type}
   *    .data(double) // required {@link UpdateSingleValueMessage#getData() data}
   *    .build();
   * </pre>
   * @return A new ImmutableUpdateSingleValueMessage builder
   */
  public static ImmutableUpdateSingleValueMessage.Builder builder() {
    return new ImmutableUpdateSingleValueMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableUpdateSingleValueMessage ImmutableUpdateSingleValueMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "UpdateSingleValueMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_TYPE = 0x1L;
    private static final long INIT_BIT_DATA = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String type;
    private double data;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.domain.Message} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(Message instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.UpdateSingleValueMessage} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(UpdateSingleValueMessage instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
    }

    private void from(Object object) {
      @Var long bits = 0;
      if (object instanceof Message) {
        Message instance = (Message) object;
        if ((bits & 0x1L) == 0) {
          type(instance.getType());
          bits |= 0x1L;
        }
      }
      if (object instanceof UpdateSingleValueMessage) {
        UpdateSingleValueMessage instance = (UpdateSingleValueMessage) object;
        data(instance.getData());
        if ((bits & 0x1L) == 0) {
          type(instance.getType());
          bits |= 0x1L;
        }
      }
    }

    /**
     * Initializes the value for the {@link UpdateSingleValueMessage#getType() type} attribute.
     * @param type The value for type 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("type")
    public final Builder type(String type) {
      this.type = Objects.requireNonNull(type, "type");
      initBits &= ~INIT_BIT_TYPE;
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateSingleValueMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(double data) {
      this.data = data;
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Builds a new {@link ImmutableUpdateSingleValueMessage ImmutableUpdateSingleValueMessage}.
     * @return An immutable instance of UpdateSingleValueMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableUpdateSingleValueMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableUpdateSingleValueMessage(type, data);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      return "Cannot build UpdateSingleValueMessage, some of required attributes are not set " + attributes;
    }
  }
}
