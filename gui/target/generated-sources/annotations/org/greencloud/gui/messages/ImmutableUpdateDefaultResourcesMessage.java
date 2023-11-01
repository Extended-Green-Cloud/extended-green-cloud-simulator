package org.greencloud.gui.messages;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.greencloud.commons.domain.resources.Resource;
import org.immutables.value.Generated;

/**
 * Immutable implementation of {@link UpdateDefaultResourcesMessage}.
 * <p>
 * Use the builder to create immutable instances:
 * {@code ImmutableUpdateDefaultResourcesMessage.builder()}.
 */
@Generated(from = "UpdateDefaultResourcesMessage", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
@Immutable
@CheckReturnValue
public final class ImmutableUpdateDefaultResourcesMessage
    implements UpdateDefaultResourcesMessage {
  private final ImmutableMap<String, Resource> resources;
  private final String agentName;

  private ImmutableUpdateDefaultResourcesMessage(
      ImmutableMap<String, Resource> resources,
      String agentName) {
    this.resources = resources;
    this.agentName = agentName;
  }

  /**
   * @return The value of the {@code resources} attribute
   */
  @JsonProperty("resources")
  @Override
  public ImmutableMap<String, Resource> getResources() {
    return resources;
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
   * Copy the current immutable object by replacing the {@link UpdateDefaultResourcesMessage#getResources() resources} map with the specified map.
   * Nulls are not permitted as keys or values.
   * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
   * @param entries The entries to be added to the resources map
   * @return A modified copy of {@code this} object
   */
  public final ImmutableUpdateDefaultResourcesMessage withResources(Map<String, ? extends Resource> entries) {
    if (this.resources == entries) return this;
    ImmutableMap<String, Resource> newValue = ImmutableMap.copyOf(entries);
    return new ImmutableUpdateDefaultResourcesMessage(newValue, this.agentName);
  }

  /**
   * Copy the current immutable object by setting a value for the {@link UpdateDefaultResourcesMessage#getAgentName() agentName} attribute.
   * An equals check used to prevent copying of the same value by returning {@code this}.
   * @param value A new value for agentName
   * @return A modified copy of the {@code this} object
   */
  public final ImmutableUpdateDefaultResourcesMessage withAgentName(String value) {
    String newValue = Objects.requireNonNull(value, "agentName");
    if (this.agentName.equals(newValue)) return this;
    return new ImmutableUpdateDefaultResourcesMessage(this.resources, newValue);
  }

  /**
   * This instance is equal to all instances of {@code ImmutableUpdateDefaultResourcesMessage} that have equal attribute values.
   * @return {@code true} if {@code this} is equal to {@code another} instance
   */
  @Override
  public boolean equals(@Nullable Object another) {
    if (this == another) return true;
    return another instanceof ImmutableUpdateDefaultResourcesMessage
        && equalTo(0, (ImmutableUpdateDefaultResourcesMessage) another);
  }

  private boolean equalTo(int synthetic, ImmutableUpdateDefaultResourcesMessage another) {
    return resources.equals(another.resources)
        && agentName.equals(another.agentName);
  }

  /**
   * Computes a hash code from attributes: {@code resources}, {@code agentName}.
   * @return hashCode value
   */
  @Override
  public int hashCode() {
    @Var int h = 5381;
    h += (h << 5) + resources.hashCode();
    h += (h << 5) + agentName.hashCode();
    return h;
  }

  /**
   * Prints the immutable value {@code UpdateDefaultResourcesMessage} with attribute values.
   * @return A string representation of the value
   */
  @Override
  public String toString() {
    return MoreObjects.toStringHelper("UpdateDefaultResourcesMessage")
        .omitNullValues()
        .add("resources", resources)
        .add("agentName", agentName)
        .toString();
  }

  /**
   * Utility type used to correctly read immutable object from JSON representation.
   * @deprecated Do not use this type directly, it exists only for the <em>Jackson</em>-binding infrastructure
   */
  @Generated(from = "UpdateDefaultResourcesMessage", generator = "Immutables")
  @Deprecated
  @SuppressWarnings("Immutable")
  @JsonDeserialize
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
  static final class Json implements UpdateDefaultResourcesMessage {
    @Nullable Map<String, Resource> resources = ImmutableMap.of();
    @Nullable String agentName;
    @JsonProperty("resources")
    public void setResources(Map<String, Resource> resources) {
      this.resources = resources;
    }
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
      this.agentName = agentName;
    }
    @Override
    public Map<String, Resource> getResources() { throw new UnsupportedOperationException(); }
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
  static ImmutableUpdateDefaultResourcesMessage fromJson(Json json) {
    ImmutableUpdateDefaultResourcesMessage.Builder builder = ImmutableUpdateDefaultResourcesMessage.builder();
    if (json.resources != null) {
      builder.putAllResources(json.resources);
    }
    if (json.agentName != null) {
      builder.agentName(json.agentName);
    }
    return builder.build();
  }

  /**
   * Creates an immutable copy of a {@link UpdateDefaultResourcesMessage} value.
   * Uses accessors to get values to initialize the new immutable instance.
   * If an instance is already immutable, it is returned as is.
   * @param instance The instance to copy
   * @return A copied immutable UpdateDefaultResourcesMessage instance
   */
  public static ImmutableUpdateDefaultResourcesMessage copyOf(UpdateDefaultResourcesMessage instance) {
    if (instance instanceof ImmutableUpdateDefaultResourcesMessage) {
      return (ImmutableUpdateDefaultResourcesMessage) instance;
    }
    return ImmutableUpdateDefaultResourcesMessage.builder()
        .from(instance)
        .build();
  }

  /**
   * Creates a builder for {@link ImmutableUpdateDefaultResourcesMessage ImmutableUpdateDefaultResourcesMessage}.
   * <pre>
   * ImmutableUpdateDefaultResourcesMessage.builder()
   *    .putResources|putAllResources(String =&gt; org.greencloud.commons.domain.resources.Resource) // {@link UpdateDefaultResourcesMessage#getResources() resources} mappings
   *    .agentName(String) // required {@link UpdateDefaultResourcesMessage#getAgentName() agentName}
   *    .build();
   * </pre>
   * @return A new ImmutableUpdateDefaultResourcesMessage builder
   */
  public static ImmutableUpdateDefaultResourcesMessage.Builder builder() {
    return new ImmutableUpdateDefaultResourcesMessage.Builder();
  }

  /**
   * Builds instances of type {@link ImmutableUpdateDefaultResourcesMessage ImmutableUpdateDefaultResourcesMessage}.
   * Initialize attributes and then invoke the {@link #build()} method to create an
   * immutable instance.
   * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
   * but instead used immediately to create instances.</em>
   */
  @Generated(from = "UpdateDefaultResourcesMessage", generator = "Immutables")
  @NotThreadSafe
  public static final class Builder {
    private static final long INIT_BIT_AGENT_NAME = 0x1L;
    private long initBits = 0x1L;

    private ImmutableMap.Builder<String, Resource> resources = ImmutableMap.builder();
    private @Nullable String agentName;

    private Builder() {
    }

    /**
     * Fill a builder with attribute values from the provided {@code UpdateDefaultResourcesMessage} instance.
     * Regular attribute values will be replaced with those from the given instance.
     * Absent optional values will not replace present values.
     * Collection elements and entries will be added, not replaced.
     * @param instance The instance from which to copy values
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder from(UpdateDefaultResourcesMessage instance) {
      Objects.requireNonNull(instance, "instance");
      putAllResources(instance.getResources());
      agentName(instance.getAgentName());
      return this;
    }

    /**
     * Put one entry to the {@link UpdateDefaultResourcesMessage#getResources() resources} map.
     * @param key The key in the resources map
     * @param value The associated value in the resources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putResources(String key, Resource value) {
      this.resources.put(key, value);
      return this;
    }

    /**
     * Put one entry to the {@link UpdateDefaultResourcesMessage#getResources() resources} map. Nulls are not permitted
     * @param entry The key and value entry
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putResources(Map.Entry<String, ? extends Resource> entry) {
      this.resources.put(entry);
      return this;
    }

    /**
     * Sets or replaces all mappings from the specified map as entries for the {@link UpdateDefaultResourcesMessage#getResources() resources} map. Nulls are not permitted
     * @param entries The entries that will be added to the resources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    @JsonProperty("resources")
    public final Builder resources(Map<String, ? extends Resource> entries) {
      this.resources = ImmutableMap.builder();
      return putAllResources(entries);
    }

    /**
     * Put all mappings from the specified map as entries to {@link UpdateDefaultResourcesMessage#getResources() resources} map. Nulls are not permitted
     * @param entries The entries that will be added to the resources map
     * @return {@code this} builder for use in a chained invocation
     */
    @CanIgnoreReturnValue 
    public final Builder putAllResources(Map<String, ? extends Resource> entries) {
      this.resources.putAll(entries);
      return this;
    }

    /**
     * Initializes the value for the {@link UpdateDefaultResourcesMessage#getAgentName() agentName} attribute.
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
     * Builds a new {@link ImmutableUpdateDefaultResourcesMessage ImmutableUpdateDefaultResourcesMessage}.
     * @return An immutable instance of UpdateDefaultResourcesMessage
     * @throws java.lang.IllegalStateException if any required attributes are missing
     */
    public ImmutableUpdateDefaultResourcesMessage build() {
      if (initBits != 0) {
        throw new IllegalStateException(formatRequiredAttributesMessage());
      }
      return new ImmutableUpdateDefaultResourcesMessage(resources.build(), agentName);
    }

    private String formatRequiredAttributesMessage() {
      List<String> attributes = new ArrayList<>();
      if ((initBits & INIT_BIT_AGENT_NAME) != 0) attributes.add("agentName");
      return "Cannot build UpdateDefaultResourcesMessage, some of required attributes are not set " + attributes;
    }
  }
}
