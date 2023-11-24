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
import org.greencloud.gui.messages.domain.JobCreator;
import org.greencloud.gui.messages.domain.Message;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link CreateClientMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableCreateClientMessage.builder()}.
 */
@Generated(from = "CreateClientMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableCreateClientMessage implements CreateClientMessage {
  private final String type;
  private final String clientName;
  private final JobCreator data;

  private ImmutableCreateClientMessage(
      String type,
      String clientName,
      JobCreator data) {
    this.type = type;
    this.clientName = clientName;
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
   * @return The value of the {@code clientName} attribute
   */
  @JsonProperty("clientName")
  @Override
  public String getClientName() {
    return clientName;
  }

  /**
   * @return The value of the {@code data} attribute
   */
  @JsonProperty("data")
  @Override
  public JobCreator getData() {
    return data;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link CreateClientMessage#getType() type} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for type
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableCreateClientMessage withType(String value) {
    String newValue = Objects.requireNonNull(value, "type");
    if (this.type.equals(newValue)) return this;
    return new ImmutableCreateClientMessage(newValue, this.clientName, this.data);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link CreateClientMessage#getClientName() clientName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for clientName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableCreateClientMessage withClientName(String value) {
    String newValue = Objects.requireNonNull(value, "clientName");
    if (this.clientName.equals(newValue)) return this;
    return new ImmutableCreateClientMessage(this.type, newValue, this.data);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link CreateClientMessage#getData() data} attribute.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for data
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableCreateClientMessage withData(JobCreator value) {
    if (this.data == value) return this;
    JobCreator newValue = Objects.requireNonNull(value, "data");
    return new ImmutableCreateClientMessage(this.type, this.clientName, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableCreateClientMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableCreateClientMessage
        && equalTo(0, (ImmutableCreateClientMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableCreateClientMessage another) {
    return type.equals(another.type)
        && clientName.equals(another.clientName)
        && data.equals(another.data);
  }

  /**
   * Computes a hash code from attributes: {@code type}, {@code clientName}, {@code data}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + type.hashCode();
    h += (h << 5) + clientName.hashCode();
    h += (h << 5) + data.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code CreateClientMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("CreateClientMessage")
        .omitNullValues()
        .add("type", type)
        .add("clientName", clientName)
        .add("data", data)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "CreateClientMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements CreateClientMessage {
    @Nullable String type;
    @Nullable String clientName;
    @Nullable JobCreator data;
    @JsonProperty("type")
    public void setType(String type) {
      this.type = type;
    }
    @JsonProperty("clientName")
    public void setClientName(String clientName) {
      this.clientName = clientName;
    }
    @JsonProperty("data")
    public void setData(JobCreator data) {
      this.data = data;
    }
    @Override
    public String getType() { throw new UnsupportedOperationException(); }
    @Override
    public String getClientName() { throw new UnsupportedOperationException(); }
    @Override
    public JobCreator getData() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableCreateClientMessage fromJson(Json json) {
    ImmutableCreateClientMessage.Builder builder = ImmutableCreateClientMessage.builder();
    if (json.type != null) {
      builder.type(json.type);
    }
    if (json.clientName != null) {
      builder.clientName(json.clientName);
    }
    if (json.data != null) {
      builder.data(json.data);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link CreateClientMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable CreateClientMessage instance
   */
  public static ImmutableCreateClientMessage copyOf(CreateClientMessage instance) {
    if (instance instanceof ImmutableCreateClientMessage) {
      return (ImmutableCreateClientMessage) instance;
    }
    return ImmutableCreateClientMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableCreateClientMessage ImmutableCreateClientMessage}.
   * <pre>
   * ImmutableCreateClientMessage.builder()
   *    .type(String) // required {@link CreateClientMessage#getType() type}
   *    .clientName(String) // required {@link CreateClientMessage#getClientName() clientName}
   *    .data(org.greencloud.gui.messages.domain.JobCreator) // required {@link CreateClientMessage#getData() data}
   *    .build();
   * </pre>
   * @return A new ImmutableCreateClientMessage builder
   */
  public static ImmutableCreateClientMessage.Builder builder() {
    return new ImmutableCreateClientMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableCreateClientMessage ImmutableCreateClientMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "CreateClientMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_TYPE = 0x1L;
    private static final long INIT_BIT_CLIENT_NAME = 0x2L;
    private static final long INIT_BIT_DATA = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String type;
    private @Nullable String clientName;
    private @Nullable JobCreator data;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code org.greencloud.gui.messages.CreateClientMessage} instance.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(CreateClientMessage instance) {
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
      if (object instanceof CreateClientMessage) {
        CreateClientMessage instance = (CreateClientMessage) object;
        data(instance.getData());
        if ((bits & 0x1L) == 0) {
          type(instance.getType());
          bits |= 0x1L;
        }
        clientName(instance.getClientName());
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
     * Initializes the value for the {@link CreateClientMessage#getType() type} attribute.
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
     * Initializes the value for the {@link CreateClientMessage#getClientName() clientName} attribute.
     * @param clientName The value for clientName 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("clientName")
    public final Builder clientName(String clientName) {
      this.clientName = Objects.requireNonNull(clientName, "clientName");
      initBits &= ~INIT_BIT_CLIENT_NAME;
      return this;
    }

    /**
     * Initializes the value for the {@link CreateClientMessage#getData() data} attribute.
     * @param data The value for data 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("data")
    public final Builder data(JobCreator data) {
      this.data = Objects.requireNonNull(data, "data");
      initBits &= ~INIT_BIT_DATA;
      return this;
    }

    /**
     * Builds a new {@link ImmutableCreateClientMessage ImmutableCreateClientMessage}.
     * @return An immutable instance of CreateClientMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableCreateClientMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableCreateClientMessage(type, clientName, data);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_TYPE) != 0) attributes.add("type");
      if ((initBits & INIT_BIT_CLIENT_NAME) != 0) attributes.add("clientName");
      if ((initBits & INIT_BIT_DATA) != 0) attributes.add("data");
      return "Cannot build CreateClientMessage, some of required attributes are not set " + attributes;
    }
  }
}
