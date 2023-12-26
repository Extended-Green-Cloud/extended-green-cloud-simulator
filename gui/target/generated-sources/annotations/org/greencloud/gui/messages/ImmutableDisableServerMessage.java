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
 * Immutable implementation of {@link DisableServerMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableDisableServerMessage.builder()}.
 */
@Generated(from = "DisableServerMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableDisableServerMessage implements DisableServerMessage {
  private final String server;
  private final String rma;
  private final double cpu;

  private ImmutableDisableServerMessage(String server, String rma, double cpu) {
    this.server = server;
    this.rma = rma;
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
   * @return The value of the {@code rma} attribute
   */
  @JsonProperty("rma")
  @Override
  public String getRma() {
    return rma;
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
   * Copy the current immutable object by setting a value for the {@link DisableServerMessage#getServer() server} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for server
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableDisableServerMessage withServer(String value) {
    String newValue = Objects.requireNonNull(value, "server");
    if (this.server.equals(newValue)) return this;
    return new ImmutableDisableServerMessage(newValue, this.rma, this.cpu);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link DisableServerMessage#getRma() rma} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for rma
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableDisableServerMessage withRma(String value) {
    String newValue = Objects.requireNonNull(value, "rma");
    if (this.rma.equals(newValue)) return this;
    return new ImmutableDisableServerMessage(this.server, newValue, this.cpu);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link DisableServerMessage#getCpu() cpu} attribute.
   * A value strict bits equality used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for cpu
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableDisableServerMessage withCpu(double value) {
    if (Double.doubleToLongBits(this.cpu) == Double.doubleToLongBits(value)) return this;
    return new ImmutableDisableServerMessage(this.server, this.rma, value);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableDisableServerMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableDisableServerMessage
        && equalTo(0, (ImmutableDisableServerMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableDisableServerMessage another) {
    return server.equals(another.server)
        && rma.equals(another.rma)
        && Double.doubleToLongBits(cpu) == Double.doubleToLongBits(another.cpu);
  }

  /**
   * Computes a hash code from attributes: {@code server}, {@code rma}, {@code cpu}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + server.hashCode();
    h += (h << 5) + rma.hashCode();
    h += (h << 5) + Doubles.hashCode(cpu);
    return h;
  }

  /**
   * Prints the immutable value {@code DisableServerMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("DisableServerMessage")
        .omitNullValues()
        .add("server", server)
        .add("rma", rma)
        .add("cpu", cpu)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "DisableServerMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements DisableServerMessage {
    @Nullable String server;
    @Nullable String rma;
    double cpu;
    boolean cpuIsSet;
    @JsonProperty("server")
    public void setServer(String server) {
      this.server = server;
    }
    @JsonProperty("rma")
    public void setRma(String rma) {
      this.rma = rma;
    }
    @JsonProperty("cpu")
    public void setCpu(double cpu) {
      this.cpu = cpu;
      this.cpuIsSet = true;
    }
    @Override
    public String getServer() { throw new UnsupportedOperationException(); }
    @Override
    public String getRma() { throw new UnsupportedOperationException(); }
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
  static ImmutableDisableServerMessage fromJson(Json json) {
    ImmutableDisableServerMessage.Builder builder = ImmutableDisableServerMessage.builder();
    if (json.server != null) {
      builder.server(json.server);
    }
    if (json.rma != null) {
      builder.rma(json.rma);
    }
    if (json.cpuIsSet) {
      builder.cpu(json.cpu);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link DisableServerMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable DisableServerMessage instance
   */
  public static ImmutableDisableServerMessage copyOf(DisableServerMessage instance) {
    if (instance instanceof ImmutableDisableServerMessage) {
      return (ImmutableDisableServerMessage) instance;
    }
    return ImmutableDisableServerMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableDisableServerMessage ImmutableDisableServerMessage}.
   * <pre>
   * ImmutableDisableServerMessage.builder()
   *    .server(String) // required {@link DisableServerMessage#getServer() server}
   *    .rma(String) // required {@link DisableServerMessage#getRma() rma}
   *    .cpu(double) // required {@link DisableServerMessage#getCpu() cpu}
   *    .build();
   * </pre>
   * @return A new ImmutableDisableServerMessage builder
   */
  public static ImmutableDisableServerMessage.Builder builder() {
    return new ImmutableDisableServerMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableDisableServerMessage ImmutableDisableServerMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "DisableServerMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_SERVER = 0x1L;
    private static final long INIT_BIT_RMA = 0x2L;
    private static final long INIT_BIT_CPU = 0x4L;
    private long initBits = 0x7L;

    private @Nullable String server;
    private @Nullable String rma;
    private double cpu;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code DisableServerMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(DisableServerMessage instance) {
      Objects.requireNonNull(instance, "instance");
      server(instance.getServer());
      rma(instance.getRma());
      cpu(instance.getCpu());
      return this;
    }

    /**
     * Initializes the value for the {@link DisableServerMessage#getServer() server} attribute.
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
     * Initializes the value for the {@link DisableServerMessage#getRma() rma} attribute.
     * @param rma The value for rma 
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("rma")
    public final Builder rma(String rma) {
      this.rma = Objects.requireNonNull(rma, "rma");
      initBits &= ~INIT_BIT_RMA;
      return this;
    }

    /**
     * Initializes the value for the {@link DisableServerMessage#getCpu() cpu} attribute.
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
     * Builds a new {@link ImmutableDisableServerMessage ImmutableDisableServerMessage}.
     * @return An immutable instance of DisableServerMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableDisableServerMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableDisableServerMessage(server, rma, cpu);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_SERVER) != 0) attributes.add("server");
      if ((initBits & INIT_BIT_RMA) != 0) attributes.add("rma");
      if ((initBits & INIT_BIT_CPU) != 0) attributes.add("cpu");
      return "Cannot build DisableServerMessage, some of required attributes are not set " + attributes;
    }
  }
}
