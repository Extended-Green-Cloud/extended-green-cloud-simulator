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
import org.greencloud.gui.messages.domain.ServerMaintenanceData;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link ServerMaintenanceMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableServerMaintenanceMessage.builder()}.
 */
@Generated(from = "ServerMaintenanceMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableServerMaintenanceMessage
    implements ServerMaintenanceMessage {
  private final String type;
  private final String agentName;
  private final ServerMaintenanceData data;

  private ImmutableServerMaintenanceMessage(
      String type,
      String agentName,
      ServerMaintenanceData data) {
    this.type = type;
    this.agentName = agentName;
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
   * @return The value of the {@code agentName} attribute
   */
  @JsonProperty("agentName")
  @Override
  public String getAgentName() {
    return agentName;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public ServerMaintenanceData getData() {
    return data;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ServerMaintenanceMessage#getType() type} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableServerMaintenanceMessage withType(String value) {
    String newValue = Objects.requireNonNull(value, "type");
    if (this.type.equals(newValue)) return this;
    return new ImmutableServerMaintenanceMessage(newValue, this.agentName, this.data);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ServerMaintenanceMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableServerMaintenanceMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableServerMaintenanceMessage(this.type, newValue, this.data);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ServerMaintenanceMessage#getData() data} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableServerMaintenanceMessage withData(ServerMaintenanceData value) {
    if (this.data == value) return this;
    ServerMaintenanceData newValue = Objects.requireNonNull(value, "data");
    return new ImmutableServerMaintenanceMessage(this.type, this.agentName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableServerMaintenanceMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableServerMaintenanceMessage
        && equalTo(0, (ImmutableServerMaintenanceMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableServerMaintenanceMessage another) {
    return type.equals(another.type)
        && agentName.equals(another.agentName)
        && data.equals(another.data);
  }

  /**
   * Computes a hash code from attributes: {@code type}, {@code agentName}, {@code data}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + type.hashCode();
    h += (h << 5) + agentName.hashCode();
    h += (h << 5) + data.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ServerMaintenanceMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ServerMaintenanceMessage")
        .omitNullValues()
        .add("type", type)
        .add("agentName", agentName)
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ServerMaintenanceMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ServerMaintenanceMessage {
    @Nullable String type;
    @Nullable String agentName;
    @Nullable ServerMaintenanceData data;
    @JsonProperty("type")
    public void setType(String type) {
      this.type = type;
    }
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @JsonProperty("data")
    public void setData(ServerMaintenanceData data) {
      this.data = data;
    }
    @Override
    public String getType() { throw new UnsupportedOperationException(); }
    @Override
    public String getAgentName() { throw new UnsupportedOperationException(); }
    @Override
    public ServerMaintenanceData getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableServerMaintenanceMessage fromJson(Json json) {
    ImmutableServerMaintenanceMessage.Builder builder = ImmutableServerMaintenanceMessage.builder();
    if (json.type != null) {
      builder.type(json.type);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    if (json.data != null) {
      builder.data(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ServerMaintenanceMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ServerMaintenanceMessage instance
   */
  public static ImmutableServerMaintenanceMessage copyOf(ServerMaintenanceMessage instance) {
    if (instance instanceof ImmutableServerMaintenanceMessage) {
      return (ImmutableServerMaintenanceMessage) instance;
    }
    return ImmutableServerMaintenanceMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableServerMaintenanceMessage ImmutableServerMaintenanceMessage}.
   * <pre>
   * ImmutableServerMaintenanceMessage.builder()
   *    .type(String) // required {@link ServerMaintenanceMessage#getType() type}
   *    .agentName(String) // required {@link ServerMaintenanceMessage#getAgentName() agentName}
   *    .data(org.greencloud.gui.messages.domain.ServerMaintenanceData) // required {@link ServerMaintenanceMessage#getData() data}
   *    .build();
   * </pre>
   * @return A new ImmutableServerMaintenanceMessage builder
   */
  public static ImmutableServerMaintenanceMessage.Builder builder() {
    return new ImmutableServerMaintenanceMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableServerMaintenanceMessage ImmutableServerMaintenanceMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ServerMaintenanceMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_TYPE = 0x1L;
    private static final long INIT_BIT_AGENT_NAME = 0x2L;
    private static final long INIT_BIT_DATA = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String type;
    private @Nullable String agentName;
    private @Nullable ServerMaintenanceData data;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.ServerMaintenanceMessage} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ServerMaintenanceMessage instance) {
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
      if (object instanceof ServerMaintenanceMessage) {
        ServerMaintenanceMessage instance = (ServerMaintenanceMessage) object;
        data(instance.getData());
        if ((bits & 0x1L) == 0) {
          type(instance.getType());
          bits |= 0x1L;
        }
        agentName(instance.getAgentName());
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
     * Initializes the value for the {@link ServerMaintenanceMessage#getType() type} attribute.
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
     * Initializes the value for the {@link ServerMaintenanceMessage#getAgentName() agentName} attribute.
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
     * Initializes the value for the {@link ServerMaintenanceMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(ServerMaintenanceData data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Builds a new {@link ImmutableServerMaintenanceMessage ImmutableServerMaintenanceMessage}.
     * @return An immutable instance of ServerMaintenanceMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableServerMaintenanceMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableServerMaintenanceMessage(type, agentName, data);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      return "Cannot build ServerMaintenanceMessage, some of required attributes are not set " + attributes;
    }
  }
}
