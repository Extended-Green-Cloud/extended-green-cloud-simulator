package weather.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value.Immutable;
import org.jetbrains.annotations.Nullable;

@JsonSerialize(as = ImmutableSys.class)
@JsonDeserialize(as = ImmutableSys.class)
@JsonInclude(Include.NON_NULL)
@Immutable
public interface Sys {

    @Nullable
    String getCountry();

    @Nullable
    String getType();

    @Nullable
    String getId();

    String getSunrise();

    String getSunset();
}
