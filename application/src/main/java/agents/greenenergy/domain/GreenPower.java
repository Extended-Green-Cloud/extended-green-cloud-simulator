package agents.greenenergy.domain;

import static agents.greenenergy.domain.GreenEnergyAgentConstants.CUT_ON_WIND_SPEED;
import static agents.greenenergy.domain.GreenEnergyAgentConstants.RATED_WIND_SPEED;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.time.ZoneOffset.UTC;
import static java.util.Comparator.comparingLong;

import agents.greenenergy.GreenEnergyAgent;
import domain.MonitoringData;
import domain.WeatherData;
import domain.location.Location;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import org.shredzone.commons.suncalc.SunTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenPower {

    private static final Logger logger = LoggerFactory.getLogger(GreenPower.class);

    private GreenEnergyAgent greenEnergyAgent;
    private int maximumCapacity;

    public GreenPower() {
    }

    public GreenPower(int maximumCapacity, GreenEnergyAgent greenEnergyAgent) {
        this.maximumCapacity = maximumCapacity;
        this.greenEnergyAgent = greenEnergyAgent;
    }

    public void setMaximumCapacity(int maximumCapacity) {
        this.maximumCapacity = maximumCapacity;
    }

    public double getAvailablePower(WeatherData weather, ZonedDateTime dateTime, Location location) {
        return switch (greenEnergyAgent.getEnergyType()) {
            case SOLAR -> getSolarPower(weather, dateTime, location);
            case WIND -> getWindPower(weather);
        };
    }

    public double getAvailablePower(MonitoringData monitoringData, Instant dateTime, Location location) {
        var weather = monitoringData.getDataForTimestamp(dateTime)
            .orElse(getNearestWeather(monitoringData, dateTime));
        return getAvailablePower(weather, dateTime.atZone(UTC), location);
    }

    public double getAvailablePower(WeatherData weather, Instant instant, Location location) {
        return getAvailablePower(weather, instant.atZone(UTC), location);
    }

    /**
     * Returns available solar power in regard to the sunset and sunrise times and cloudiness.
     *
     * @param weather  current weather used in calculations
     * @param dateTime datetime used in sun times calculations
     * @param location location used in sun times calculations
     * @return available solar power
     */
    private double getSolarPower(WeatherData weather, ZonedDateTime dateTime, Location location) {
        var sunTimes = getSunTimes(dateTime, location);

        if (dateTime.isBefore(sunTimes.getRise()) || dateTime.isAfter(sunTimes.getSet())) {
            logger.debug("SOLAR farm is shutdown at {}, sunrise at {} & sunset at {}", dateTime, sunTimes.getRise(),
                sunTimes.getSet());
            return 0;
        }

        return maximumCapacity * min(weather.getCloudCover() / 100 + 0.1, 1);
    }

    /**
     * Returns available wind power in regard to wind power
     *
     * @param weather provides wind speed needed for calculations
     * @return available wind speed
     */
    private double getWindPower(WeatherData weather) {
        //TODO get proper wind speed, for now +5 m/s to get wind at some height above ground level
        return maximumCapacity * pow(
            (weather.getWindSpeed() + 5 - CUT_ON_WIND_SPEED) / (RATED_WIND_SPEED - CUT_ON_WIND_SPEED), 2);
    }

    private SunTimes getSunTimes(ZonedDateTime dateTime, Location location) {
        return SunTimes.compute().on(dateTime).at(location.getLatitude(), location.getLongitude()).execute();
    }

    private WeatherData getNearestWeather(MonitoringData monitoringData, Instant timestamp) {
        var timestamps = monitoringData.getWeatherData().stream().map(WeatherData::getTime).toList();
        var nearestTimestamp = timestamps.stream()
            .min(comparingLong(i -> Math.abs(i.getEpochSecond() - timestamp.getEpochSecond())))
            .orElseThrow(() -> new NoSuchElementException("No value present"));
        return monitoringData.getWeatherData().stream()
            .filter(weather -> weather.getTime().equals(nearestTimestamp))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No value present"));
    }
}
