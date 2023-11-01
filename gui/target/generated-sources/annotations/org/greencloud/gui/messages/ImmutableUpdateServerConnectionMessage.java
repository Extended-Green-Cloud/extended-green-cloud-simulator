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
import org.greencloud.gui.messages.domain.ServerConnection;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link UpdateServerConnectionMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableUpdateServerConnectionMessage.builder()}.
 */
@Generated(from = "UpdateServerConnectionMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableUpdateServerConnectionMessage
    implements UpdateServerConnectionMessage {
  private final String agentName;
  private final ServerConnection data;

  private ImmutableUpdateServerConnectionMessage(String agentName, ServerConnection data) {
    this.agentName = agentName;
    this.data = data;
  }

  /**
   * @return name of the green source agent
   */
  @JsonProperty("agentName")
  @Override
  public String getAgentName() {
    return agentName;
  }

  /**
   * @return message connection details
   */
  @JsonProperty("data")
  @Override
  public ServerConnection getData() {
    return data;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateServerConnectionMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateServerConnectionMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableUpdateServerConnectionMessage(newValue, this.data);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateServerConnectionMessage#getData() data} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateServerConnectionMessage withData(ServerConnection value) {
    if (this.data == value) return this;
    ServerConnection newValue = Objects.requireNonNull(value, "data");
    return new ImmutableUpdateServerConnectionMessage(this.agentName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableUpdateServerConnectionMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableUpdateServerConnectionMessage
        && equalTo(0, (ImmutableUpdateServerConnectionMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableUpdateServerConnectionMessage another) {
    return agentName.equals(another.agentName)
        && data.equals(another.data);
  }

  /**
   * Computes a hash code from attributes: {@code agentName}, {@code data}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + agentName.hashCode();
    h += (h << 5) + data.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code UpdateServerConnectionMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("UpdateServerConnectionMessage")
        .omitNullValues()
        .add("agentName", agentName)
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "UpdateServerConnectionMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements UpdateServerConnectionMessage {
    @Nullable String agentName;
    @Nullable ServerConnection data;
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @JsonProperty("data")
    public void setData(ServerConnection data) {
      this.data = data;
    }
    @Override
    public String getAgentName() { throw new UnsupportedOperationException(); }
    @Override
    public ServerConnection getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableUpdateServerConnectionMessage fromJson(Json json) {
    ImmutableUpdateServerConnectionMessage.Builder builder = ImmutableUpdateServerConnectionMessage.builder();
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    if (json.data != null) {
      builder.data(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link UpdateServerConnectionMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable UpdateServerConnectionMessage instance
   */
  public static ImmutableUpdateServerConnectionMessage copyOf(UpdateServerConnectionMessage instance) {
    if (instance instanceof ImmutableUpdateServerConnectionMessage) {
      return (ImmutableUpdateServerConnectionMessage) instance;
    }
    return ImmutableUpdateServerConnectionMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableUpdateServerConnectionMessage ImmutableUpdateServerConnectionMessage}.
   * <pre>
   * ImmutableUpdateServerConnectionMessage.builder()
   *    .agentName(String) // required {@link UpdateServerConnectionMessage#getAgentName() agentName}
   *    .data(org.greencloud.gui.messages.domain.ServerConnection) // required {@link UpdateServerConnectionMessage#getData() data}
   *    .build();
   * </pre>
   * @return A new ImmutableUpdateServerConnectionMessage builder
   */
  public static ImmutableUpdateServerConnectionMessage.Builder builder() {
    return new ImmutableUpdateServerConnectionMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableUpdateServerConnectionMessage ImmutableUpdateServerConnectionMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "UpdateServerConnectionMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_AGENT_NAME = 0x1L;
    private static final long INIT_BIT_DATA = 0x2L;
    private long initBits = 0x3L;

    private @Nullable String agentName;
    private @Nullable ServerConnection data;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code UpdateServerConnectionMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(UpdateServerConnectionMessage instance) {
      Objects.requireNonNull(instance, "instance");
      agentName(instance.getAgentName());
      data(instance.getData());
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateServerConnectionMessage#getAgentName() agentName} attribute.
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
     * Initializes the value for the {@link UpdateServerConnectionMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(ServerConnection data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Builds a new {@link ImmutableUpdateServerConnectionMessage ImmutableUpdateServerConnectionMessage}.
     * @return An immutable instance of UpdateServerConnectionMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableUpdateServerConnectionMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableUpdateServerConnectionMessage(agentName, data);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      return "Cannot build UpdateServerConnectionMessage, some of required attributes are not set " + attributes;
    }
  }
}
