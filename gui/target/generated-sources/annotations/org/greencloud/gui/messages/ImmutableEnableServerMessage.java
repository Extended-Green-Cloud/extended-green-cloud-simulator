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
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link EnableServerMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableEnableServerMessage.builder()}.
 */
@Generated(from = "EnableServerMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableEnableServerMessage implements EnableServerMessage {
  private final String server;
  private final String cna;
  private final double cpu;

  private ImmutableEnableServerMessage(String server, String cna, double cpu) {
    this.server = server;
    this.cna = cna;
    this.cpu = cpu;
  }

  /**
   * @return The value of the {@code server} attribute
   */
  @JsonProperty("server")
  @Override
  public String getServer() {
    return server;
  }

  /**
   * @return The value of the {@code cna} attribute
   */
  @JsonProperty("cna")
  @Override
  public String getCna() {
    return cna;
  }

  /**
   * @return The value of the {@code cpu} attribute
   */
  @JsonProperty("cpu")
  @Override
  public double getCpu() {
    return cpu;
  }

  /**
   * Copy the current immutable object by setting a value for the {@link EnableServerMessage#getServer() server} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for server
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableEnableServerMessage withServer(String value) {
    String newValue = Objects.requireNonNull(value, "server");
    if (this.server.equals(newValue)) return this;
    return new ImmutableEnableServerMessage(newValue, this.cna, this.cpu);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link EnableServerMessage#getCna() cna} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for cna
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableEnableServerMessage withCna(String value) {
    String newValue = Objects.requireNonNull(value, "cna");
    if (this.cna.equals(newValue)) return this;
    return new ImmutableEnableServerMessage(this.server, newValue, this.cpu);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link EnableServerMessage#getCpu() cpu} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for cpu
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableEnableServerMessage withCpu(double value) {
    if (Double.doubleToLongBits(this.cpu) == Double.doubleToLongBits(value)) return this;
    return new ImmutableEnableServerMessage(this.server, this.cna, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableEnableServerMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableEnableServerMessage
        && equalTo(0, (ImmutableEnableServerMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableEnableServerMessage another) {
    return server.equals(another.server)
        && cna.equals(another.cna)
        && Double.doubleToLongBits(cpu) == Double.doubleToLongBits(another.cpu);
  }

  /**
   * Computes a hash code from attributes: {@code server}, {@code cna}, {@code cpu}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + server.hashCode();
    h += (h << 5) + cna.hashCode();
    h += (h << 5) + Doubles.hashCode(cpu);
    return h;
  }

  /**
   * Prints the immutable value {@code EnableServerMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("EnableServerMessage")
        .omitNullValues()
        .add("server", server)
        .add("cna", cna)
        .add("cpu", cpu)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "EnableServerMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements EnableServerMessage {
    @Nullable String server;
    @Nullable String cna;
    double cpu;
    boolean cpuIsSet;
    @JsonProperty("server")
    public void setServer(String server) {
      this.server = server;
    }
    @JsonProperty("cna")
    public void setCna(String cna) {
      this.cna = cna;
    }
    @JsonProperty("cpu")
    public void setCpu(double cpu) {
      this.cpu = cpu;
      this.cpuIsSet = true;
    }
    @Override
    public String getServer() { throw new UnsupportedOperationException(); }
    @Override
    public String getCna() { throw new UnsupportedOperationException(); }
    @Override
    public double getCpu() { throw new UnsupportedOperationException(); }
  }

  /**
   * @param json A JSON-bindable data structure
   * @return An immutable value type
   * @deprecated Do not use this method directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Deprecated
  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  static ImmutableEnableServerMessage fromJson(Json json) {
    ImmutableEnableServerMessage.Builder builder = ImmutableEnableServerMessage.builder();
    if (json.server != null) {
      builder.server(json.server);
    }
    if (json.cna != null) {
      builder.cna(json.cna);
    }
    if (json.cpuIsSet) {
      builder.cpu(json.cpu);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link EnableServerMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable EnableServerMessage instance
   */
  public static ImmutableEnableServerMessage copyOf(EnableServerMessage instance) {
    if (instance instanceof ImmutableEnableServerMessage) {
      return (ImmutableEnableServerMessage) instance;
    }
    return ImmutableEnableServerMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableEnableServerMessage ImmutableEnableServerMessage}.
   * <pre>
   * ImmutableEnableServerMessage.builder()
   *    .server(String) // required {@link EnableServerMessage#getServer() server}
   *    .cna(String) // required {@link EnableServerMessage#getCna() cna}
   *    .cpu(double) // required {@link EnableServerMessage#getCpu() cpu}
   *    .build();
   * </pre>
   * @return A new ImmutableEnableServerMessage builder
   */
  public static ImmutableEnableServerMessage.Builder builder() {
    return new ImmutableEnableServerMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableEnableServerMessage ImmutableEnableServerMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "EnableServerMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_SERVER = 0x1L;
    private static final long INIT_BIT_CNA = 0x2L;
    private static final long INIT_BIT_CPU = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String server;
    private @Nullable String cna;
    private double cpu;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code EnableServerMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(EnableServerMessage instance) {
      Objects.requireNonNull(instance, "instance");
      server(instance.getServer());
      cna(instance.getCna());
      cpu(instance.getCpu());
      return this;
    }

    /**
     * Initializes the value for the {@link EnableServerMessage#getServer() server} attribute.
     * @param server The value for server 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("server")
    public final Builder server(String server) {
      this.server = Objects.requireNonNull(server, "server");
      initBits &= ~INIT_BIT_SERVER;
      return this;
    }

    /**
     * Initializes the value for the {@link EnableServerMessage#getCna() cna} attribute.
     * @param cna The value for cna 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("cna")
    public final Builder cna(String cna) {
      this.cna = Objects.requireNonNull(cna, "cna");
      initBits &= ~INIT_BIT_CNA;
      return this;
    }

    /**
     * Initializes the value for the {@link EnableServerMessage#getCpu() cpu} attribute.
     * @param cpu The value for cpu 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("cpu")
    public final Builder cpu(double cpu) {
      this.cpu = cpu;
      initBits &= ~INIT_BIT_CPU;
      return this;
    }

    /**
     * Builds a new {@link ImmutableEnableServerMessage ImmutableEnableServerMessage}.
     * @return An immutable instance of EnableServerMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableEnableServerMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableEnableServerMessage(server, cna, cpu);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_SERVER) != 0) attributes.add("server");
      if ((initBits & INIT_BIT_CNA) != 0) attributes.add("cna");
      if ((initBits & INIT_BIT_CPU) != 0) attributes.add("cpu");
      return "Cannot build EnableServerMessage, some of required attributes are not set " + attributes;
    }
  }
}
