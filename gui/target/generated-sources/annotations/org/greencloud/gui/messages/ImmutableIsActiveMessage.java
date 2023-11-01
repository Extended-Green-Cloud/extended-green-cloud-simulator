package org.greencloud.gui.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.primitives.Booleans;
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
 * Immutable implementation of {@link IsActiveMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableIsActiveMessage.builder()}.
 */
@Generated(from = "IsActiveMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableIsActiveMessage implements IsActiveMessage {
  private final boolean data;
  private final String agentName;

  private ImmutableIsActiveMessage(boolean data, String agentName) {
    this.data = data;
    this.agentName = agentName;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public boolean getData() {
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
   * Copy the current immutable object by setting a value for the {@link IsActiveMessage#getData() data} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableIsActiveMessage withData(boolean value) {
    if (this.data == value) return this;
    return new ImmutableIsActiveMessage(value, this.agentName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link IsActiveMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableIsActiveMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableIsActiveMessage(this.data, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableIsActiveMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableIsActiveMessage
        && equalTo(0, (ImmutableIsActiveMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableIsActiveMessage another) {
    return data == another.data
        && agentName.equals(another.agentName);
  }

  /**
   * Computes a hash code from attributes: {@code data}, {@code agentName}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Booleans.hashCode(data);
    h += (h << 5) + agentName.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code IsActiveMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("IsActiveMessage")
        .omitNullValues()
        .add("data", data)
        .add("agentName", agentName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "IsActiveMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements IsActiveMessage {
    boolean data;
    boolean dataIsSet;
    @Nullable String agentName;
    @JsonProperty("data")
    public void setData(boolean data) {
      this.data = data;
      this.dataIsSet = true;
    }
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @Override
    public boolean getData() { throw new UnsupportedOperationException(); }
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
  static ImmutableIsActiveMessage fromJson(Json json) {
    ImmutableIsActiveMessage.Builder builder = ImmutableIsActiveMessage.builder();
    if (json.dataIsSet) {
      builder.data(json.data);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link IsActiveMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable IsActiveMessage instance
   */
  public static ImmutableIsActiveMessage copyOf(IsActiveMessage instance) {
    if (instance instanceof ImmutableIsActiveMessage) {
      return (ImmutableIsActiveMessage) instance;
    }
    return ImmutableIsActiveMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableIsActiveMessage ImmutableIsActiveMessage}.
   * <pre>
   * ImmutableIsActiveMessage.builder()
   *    .data(boolean) // required {@link IsActiveMessage#getData() data}
   *    .agentName(String) // required {@link IsActiveMessage#getAgentName() agentName}
   *    .build();
   * </pre>
   * @return A new ImmutableIsActiveMessage builder
   */
  public static ImmutableIsActiveMessage.Builder builder() {
    return new ImmutableIsActiveMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableIsActiveMessage ImmutableIsActiveMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "IsActiveMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_DATA = 0x1L;
    private static final long INIT_BIT_AGENT_NAME = 0x2L;
    private long initBits = 0x3L;

    private boolean data;
    private @Nullable String agentName;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code IsActiveMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(IsActiveMessage instance) {
      Objects.requireNonNull(instance, "instance");
      data(instance.getData());
      agentName(instance.getAgentName());
      return this;
    }

    /**
     * Initializes the value for the {@link IsActiveMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(boolean data) {
      this.data = data;
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Initializes the value for the {@link IsActiveMessage#getAgentName() agentName} attribute.
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
     * Builds a new {@link ImmutableIsActiveMessage ImmutableIsActiveMessage}.
     * @return An immutable instance of IsActiveMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableIsActiveMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableIsActiveMessage(data, agentName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      return "Cannot build IsActiveMessage, some of required attributes are not set " + attributes;
    }
  }
}
