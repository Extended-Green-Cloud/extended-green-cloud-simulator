package org.greencloud.gui.messages.domain;

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
 * Immutable implementation of {@link ServerConnection}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableServerConnection.builder()}.
 */
@Generated(from = "ServerConnection", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableServerConnection implements ServerConnection {
  private final boolean isConnected;
  private final String serverName;

  private ImmutableServerConnection(boolean isConnected, String serverName) {
    this.isConnected = isConnected;
    this.serverName = serverName;
  }

  /**
   * @return flag indicating if the server should be connected or disconnected
   */
  @JsonProperty("isConnected")
  @Override
  public boolean isConnected() {
    return isConnected;
  }

  /**
   * @return name of the server to connect/disconnect to given green source
   */
  @JsonProperty("serverName")
  @Override
  public String getServerName() {
    return serverName;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ServerConnection#isConnected() isConnected} attribute.
   * A value equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for isConnected
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableServerConnection withIsConnected(boolean value) {
    if (this.isConnected == value) return this;
    return new ImmutableServerConnection(value, this.serverName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link ServerConnection#getServerName() serverName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for serverName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableServerConnection withServerName(String value) {
    String newValue = Objects.requireNonNull(value, "serverName");
    if (this.serverName.equals(newValue)) return this;
    return new ImmutableServerConnection(this.isConnected, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableServerConnection} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableServerConnection
        && equalTo(0, (ImmutableServerConnection) another);
  }

  private boolean equalTo(int synthetic, ImmutableServerConnection another) {
    return isConnected == another.isConnected
        && serverName.equals(another.serverName);
  }

  /**
   * Computes a hash code from attributes: {@code isConnected}, {@code serverName}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + Booleans.hashCode(isConnected);
    h += (h << 5) + serverName.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code ServerConnection} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("ServerConnection")
        .omitNullValues()
        .add("isConnected", isConnected)
        .add("serverName", serverName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "ServerConnection", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements ServerConnection {
    boolean isConnected;
    boolean isConnectedIsSet;
    @Nullable String serverName;
    @JsonProperty("isConnected")
    public void setIsConnected(boolean isConnected) {
      this.isConnected = isConnected;
      this.isConnectedIsSet = true;
    }
    @JsonProperty("serverName")
    public void setServerName(String serverName) {
      this.serverName = serverName;
    }
    @Override
    public boolean isConnected() { throw new UnsupportedOperationException(); }
    @Override
    public String getServerName() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableServerConnection fromJson(Json json) {
    ImmutableServerConnection.Builder builder = ImmutableServerConnection.builder();
    if (json.isConnectedIsSet) {
      builder.isConnected(json.isConnected);
    }
    if (json.serverName != null) {
      builder.serverName(json.serverName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link ServerConnection} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable ServerConnection instance
   */
  public static ImmutableServerConnection copyOf(ServerConnection instance) {
    if (instance instanceof ImmutableServerConnection) {
      return (ImmutableServerConnection) instance;
    }
    return ImmutableServerConnection.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableServerConnection ImmutableServerConnection}.
   * <pre>
   * ImmutableServerConnection.builder()
   *    .isConnected(boolean) // required {@link ServerConnection#isConnected() isConnected}
   *    .serverName(String) // required {@link ServerConnection#getServerName() serverName}
   *    .build();
   * </pre>
   * @return A new ImmutableServerConnection builder
   */
  public static ImmutableServerConnection.Builder builder() {
    return new ImmutableServerConnection.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableServerConnection ImmutableServerConnection}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "ServerConnection", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_IS_CONNECTED = 0x1L;
    private static final long INIT_BIT_SERVER_NAME = 0x2L;
    private long initBits = 0x3L;

    private boolean isConnected;
    private @Nullable String serverName;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code ServerConnection} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(ServerConnection instance) {
      Objects.requireNonNull(instance, "instance");
      isConnected(instance.isConnected());
      serverName(instance.getServerName());
      return this;
    }

    /**
     * Initializes the value for the {@link ServerConnection#isConnected() isConnected} attribute.
     * @param isConnected The value for isConnected 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("isConnected")
    public final Builder isConnected(boolean isConnected) {
      this.isConnected = isConnected;
      initBits &= ~INIT_BIT_IS_CONNECTED;
      return this;
    }

    /**
     * Initializes the value for the {@link ServerConnection#getServerName() serverName} attribute.
     * @param serverName The value for serverName 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("serverName")
    public final Builder serverName(String serverName) {
      this.serverName = Objects.requireNonNull(serverName, "serverName");
      initBits &= ~INIT_BIT_SERVER_NAME;
      return this;
    }

    /**
     * Builds a new {@link ImmutableServerConnection ImmutableServerConnection}.
     * @return An immutable instance of ServerConnection
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableServerConnection build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableServerConnection(isConnected, serverName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_IS_CONNECTED) != 0) attributes.add("isConnected");
      if ((initBits & INIT_BIT_SERVER_NAME) != 0) attributes.add("serverName");
      return "Cannot build ServerConnection, some of required attributes are not set " + attributes;
    }
  }
}
