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
 * Immutable implementation of {@link SetNumericValueMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableSetNumericValueMessage.builder()}.
 */
@Generated(from = "SetNumericValueMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableSetNumericValueMessage
    implements SetNumericValueMessage {
  private final String type;
  private final double data;
  private final String agentName;

  private ImmutableSetNumericValueMessage(String type, double data, String agentName) {
    this.type = type;
    this.data = data;
    this.agentName = agentName;
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
   * @return The value of the {@code agentName} attribute
   */
  @JsonProperty("agentName")
  @Override
  public String getAgentName() {
    return agentName;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SetNumericValueMessage#getType() type} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSetNumericValueMessage withType(String value) {
    String newValue = Objects.requireNonNull(value, "type");
    if (this.type.equals(newValue)) return this;
    return new ImmutableSetNumericValueMessage(newValue, this.data, this.agentName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SetNumericValueMessage#getData() data} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSetNumericValueMessage withData(double value) {
    if (Double.doubleToLongBits(this.data) == Double.doubleToLongBits(value)) return this;
    return new ImmutableSetNumericValueMessage(this.type, value, this.agentName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link SetNumericValueMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableSetNumericValueMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableSetNumericValueMessage(this.type, this.data, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableSetNumericValueMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableSetNumericValueMessage
        && equalTo(0, (ImmutableSetNumericValueMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableSetNumericValueMessage another) {
    return type.equals(another.type)
        && Double.doubleToLongBits(data) == Double.doubleToLongBits(another.data)
        && agentName.equals(another.agentName);
  }

  /**
   * Computes a hash code from attributes: {@code type}, {@code data}, {@code agentName}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + type.hashCode();
    h += (h << 5) + Doubles.hashCode(data);
    h += (h << 5) + agentName.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code SetNumericValueMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("SetNumericValueMessage")
        .omitNullValues()
        .add("type", type)
        .add("data", data)
        .add("agentName", agentName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "SetNumericValueMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements SetNumericValueMessage {
    @Nullable String type;
    double data;
    boolean dataIsSet;
    @Nullable String agentName;
    @JsonProperty("type")
    public void setType(String type) {
      this.type = type;
    }
    @JsonProperty("data")
    public void setData(double data) {
      this.data = data;
      this.dataIsSet = true;
    }
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @Override
    public String getType() { throw new UnsupportedOperationException(); }
    @Override
    public double getData() { throw new UnsupportedOperationException(); }
    @Override
    public String getAgentName() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableSetNumericValueMessage fromJson(Json json) {
    ImmutableSetNumericValueMessage.Builder builder = ImmutableSetNumericValueMessage.builder();
    if (json.type != null) {
      builder.type(json.type);
    }
    if (json.dataIsSet) {
      builder.data(json.data);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link SetNumericValueMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable SetNumericValueMessage instance
   */
  public static ImmutableSetNumericValueMessage copyOf(SetNumericValueMessage instance) {
    if (instance instanceof ImmutableSetNumericValueMessage) {
      return (ImmutableSetNumericValueMessage) instance;
    }
    return ImmutableSetNumericValueMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableSetNumericValueMessage ImmutableSetNumericValueMessage}.
   * <pre>
   * ImmutableSetNumericValueMessage.builder()
   *    .type(String) // required {@link SetNumericValueMessage#getType() type}
   *    .data(double) // required {@link SetNumericValueMessage#getData() data}
   *    .agentName(String) // required {@link SetNumericValueMessage#getAgentName() agentName}
   *    .build();
   * </pre>
   * @return A new ImmutableSetNumericValueMessage builder
   */
  public static ImmutableSetNumericValueMessage.Builder builder() {
    return new ImmutableSetNumericValueMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableSetNumericValueMessage ImmutableSetNumericValueMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "SetNumericValueMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_TYPE = 0x1L;
    private static final long INIT_BIT_DATA = 0x2L;
    private static final long INIT_BIT_AGENT_NAME = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String type;
    private double data;
    private @Nullable String agentName;

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
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.SetNumericValueMessage} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(SetNumericValueMessage instance) {
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
      if (object instanceof SetNumericValueMessage) {
        SetNumericValueMessage instance = (SetNumericValueMessage) object;
        data(instance.getData());
        if ((bits & 0x1L) == 0) {
          type(instance.getType());
          bits |= 0x1L;
        }
        agentName(instance.getAgentName());
      }
    }

    /**
     * Initializes the value for the {@link SetNumericValueMessage#getType() type} attribute.
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
     * Initializes the value for the {@link SetNumericValueMessage#getData() data} attribute.
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
     * Initializes the value for the {@link SetNumericValueMessage#getAgentName() agentName} attribute.
     * @param agentName The value for agentName 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("agentName")
    public final Builder agentName(String agentName) {
      this.agentName = Objects.requireNonNull(agentName, "agentName");
      initBits &= ~INIT_BIT_AGENT_NAME;
      return this;
    }

    /**
     * Builds a new {@link ImmutableSetNumericValueMessage ImmutableSetNumericValueMessage}.
     * @return An immutable instance of SetNumericValueMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableSetNumericValueMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableSetNumericValueMessage(type, data, agentName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      return "Cannot build SetNumericValueMessage, some of required attributes are not set " + attributes;
    }
  }
}
