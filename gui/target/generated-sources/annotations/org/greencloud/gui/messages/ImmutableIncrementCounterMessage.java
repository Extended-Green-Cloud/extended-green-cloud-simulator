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
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link IncrementCounterMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableIncrementCounterMessage.builder()}.
 */
@Generated(from = "IncrementCounterMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableIncrementCounterMessage
    implements IncrementCounterMessage {
  private final String type;

  private ImmutableIncrementCounterMessage(String type) {
    this.type = type;
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
   * Copy the current immutable object by setting a value for the {@link IncrementCounterMessage#getType() type} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableIncrementCounterMessage withType(String value) {
    String newValue = Objects.requireNonNull(value, "type");
    if (this.type.equals(newValue)) return this;
    return new ImmutableIncrementCounterMessage(newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableIncrementCounterMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableIncrementCounterMessage
        && equalTo(0, (ImmutableIncrementCounterMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableIncrementCounterMessage another) {
    return type.equals(another.type);
  }

  /**
   * Computes a hash code from attributes: {@code type}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + type.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code IncrementCounterMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("IncrementCounterMessage")
        .omitNullValues()
        .add("type", type)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "IncrementCounterMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements IncrementCounterMessage {
    @Nullable String type;
    @JsonProperty("type")
    public void setType(String type) {
      this.type = type;
    }
    @Override
    public String getType() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableIncrementCounterMessage fromJson(Json json) {
    ImmutableIncrementCounterMessage.Builder builder = ImmutableIncrementCounterMessage.builder();
    if (json.type != null) {
      builder.type(json.type);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link IncrementCounterMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable IncrementCounterMessage instance
   */
  public static ImmutableIncrementCounterMessage copyOf(IncrementCounterMessage instance) {
    if (instance instanceof ImmutableIncrementCounterMessage) {
      return (ImmutableIncrementCounterMessage) instance;
    }
    return ImmutableIncrementCounterMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableIncrementCounterMessage ImmutableIncrementCounterMessage}.
   * <pre>
   * ImmutableIncrementCounterMessage.builder()
   *    .type(String) // required {@link IncrementCounterMessage#getType() type}
   *    .build();
   * </pre>
   * @return A new ImmutableIncrementCounterMessage builder
   */
  public static ImmutableIncrementCounterMessage.Builder builder() {
    return new ImmutableIncrementCounterMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableIncrementCounterMessage ImmutableIncrementCounterMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "IncrementCounterMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_TYPE = 0x1L;
    private long initBits = 0x1L;

    private @Nullable String type;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.IncrementCounterMessage} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(IncrementCounterMessage instance) {
      Objects.requireNonNull(instance, "instance");
      from((Object) instance);
      return this;
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

    private void from(Object object) {
      @Var long bits = 0;
      if (object instanceof IncrementCounterMessage) {
        IncrementCounterMessage instance = (IncrementCounterMessage) object;
        if ((bits & 0x1L) == 0) {
          type(instance.getType());
          bits |= 0x1L;
        }
      }
      if (object instanceof Message) {
        Message instance = (Message) object;
        if ((bits & 0x1L) == 0) {
          type(instance.getType());
          bits |= 0x1L;
        }
      }
    }

    /**
     * Initializes the value for the {@link IncrementCounterMessage#getType() type} attribute.
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
     * Builds a new {@link ImmutableIncrementCounterMessage ImmutableIncrementCounterMessage}.
     * @return An immutable instance of IncrementCounterMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableIncrementCounterMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableIncrementCounterMessage(type);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      return "Cannot build IncrementCounterMessage, some of required attributes are not set " + attributes;
    }
  }
}
