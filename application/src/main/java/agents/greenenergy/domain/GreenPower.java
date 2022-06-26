package agents.greenenergy.domain;

import static agents.greenenergy.domain.GreenEnergyAgentConstants.CUT_ON_WIND_SPEED;
import static agents.greenenergy.domain.GreenEnergyAgentConstants.RATED_WIND_SPEED;
import static java.lang.Math.min;
import static java.lang.Math.pow;

import agents.greenenergy.GreenEnergyAgent;
import domain.MonitoringData;
import domain.location.Location;
import java.time.ZonedDateTime;
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

    public double getAvailablePower(MonitoringData weather, ZonedDateTime dateTime, Location location) {
        return switch (greenEnergyAgent.getEnergyType()) {
            case SOLAR -> getSolarPower(weather, dateTime, location);
            case WIND -> getWindPower(weather);
        };
    }

    /**
     * Returns available solar power in regard to the sunset and sunrise times and cloudiness.
     *
     * @param weather  current weather used in calculations
     * @param dateTime datetime used in sun times calculations
     * @param location location used in sun times calculations
     * @return available solar power
     */
    private double getSolarPower(MonitoringData weather, ZonedDateTime dateTime, Location location) {
        var sunTimes = getSunTimes(dateTime, location);

        //TODO ADJUST TIME or LOCATION
        if (dateTime.isBefore(sunTimes.getRise()) || dateTime.isAfter(sunTimes.getSet())) {
            logger.info("SOLAR farm at {} is shutdown, sunrise {}; sunset {}", dateTime, sunTimes.getRise(),
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
    private double getWindPower(MonitoringData weather) {
        //TODO get proper wind speed, for now +5 m/s to get wind at some height above ground level
        return maximumCapacity * pow(
            (weather.getWindSpeed() + 5 - CUT_ON_WIND_SPEED) / (RATED_WIND_SPEED - CUT_ON_WIND_SPEED), 2);
    }

    private SunTimes getSunTimes(ZonedDateTime dateTime, Location location) {
        return SunTimes.compute().on(dateTime).at(location.getLatitude(), location.getLongitude()).execute();
    }
}
