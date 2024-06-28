package org.greencloud.dataanalysisapi.api;

import static java.lang.String.join;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static org.greencloud.commons.constants.resource.ResourceTypesConstants.ID;
import static org.greencloud.dataanalysisapi.mapper.ClusteringMapper.mapToClusteringConfig;
import static org.greencloud.dataanalysisapi.mapper.ClusteringMapper.mapToClusteringRequest;
import static org.jrba.utils.mapper.JsonMapper.getMapper;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.greencloud.commons.exception.IncorrectMessageContentException;
import org.greencloud.commons.exception.InvalidPropertiesException;
import org.greencloud.dataanalysisapi.domain.ClusteringConfiguration;
import org.greencloud.dataanalysisapi.domain.ClusteringEncodingResponse;
import org.greencloud.dataanalysisapi.domain.ClusteringFeatures;
import org.greencloud.dataanalysisapi.domain.ClusteringMethod;
import org.greencloud.dataanalysisapi.domain.ClusteringParameters;
import org.greencloud.dataanalysisapi.domain.ClusteringRequest;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Service used to communicate with external Data Clustering API
 */
public class DataClusteringApi {

	private static final Logger logger = getLogger(DataClusteringApi.class);

	private static final String CLUSTERING_ENCODED_JOBS_ENDPOINT = "clustering/encoded_jobs";
	private static final String CLUSTERING_JOBS_ENDPOINT = "clustering/jobs";

	private final OkHttpClient client;
	private final String address;

	/**
	 * Default constructor that establishes the communication with API
	 */
	public DataClusteringApi() {
		final Properties properties = loadProperties();

		this.address = properties.getProperty("dataanalysis.api.url");
		this.client = new OkHttpClient();
	}

	/**
	 * Method performs clustering of given resources and returns map matching identifiers with their encoded clusters.
	 *
	 * @param resources  resources to be clustered
	 * @param features   features used in clustering
	 * @param methods    methods used in clustering
	 * @param parameters parameters used in clustering
	 * @return map of identifiers and encoded vectors
	 */
	public ClusteringEncodingResponse getCodeForResources(final List<Map<String, Object>> resources,
			final ClusteringFeatures features, final ClusteringMethod methods, final ClusteringParameters parameters) {
		validateForIdentifier(resources);
		final Request request = prepareDataRequest(resources, features, methods, parameters,
				CLUSTERING_ENCODED_JOBS_ENDPOINT);

		try (final Response response = client.newCall(request).execute()) {
			final ResponseBody responseBody =
					ofNullable(response.body()).orElseThrow(IncorrectMessageContentException::new);
			return getMapper().readValue(responseBody.string(), ClusteringEncodingResponse.class);
		} catch (final IOException | NullPointerException | IncorrectMessageContentException e) {
			logger.error("Error while communicating with data analysis server.", e);
			throw new IncorrectMessageContentException();
		}
	}

	/**
	 * Method performs clustering of given resources.
	 *
	 * @param resources  resources to be clustered
	 * @param features   features used in clustering
	 * @param methods    methods used in clustering
	 * @param parameters parameters used in clustering
	 * @return map of identifiers and encoded vectors
	 */
	public Map<String, List<Map<String, Object>>> getClusteredResources(final List<Map<String, Object>> resources,
			final ClusteringFeatures features, final ClusteringMethod methods, final ClusteringParameters parameters) {
		validateForIdentifier(resources);
		final Request request = prepareDataRequest(resources, features, methods, parameters, CLUSTERING_JOBS_ENDPOINT);

		try (final Response response = client.newCall(request).execute()) {
			final ResponseBody responseBody =
					ofNullable(response.body()).orElseThrow(IncorrectMessageContentException::new);
			final TypeReference<Map<String, List<Map<String, Object>>>> requiredType = new TypeReference<>() {
			};
			return getMapper().readValue(responseBody.string(), requiredType);
		} catch (final IOException | NullPointerException | IncorrectMessageContentException e) {
			logger.error("Error while communicating with data analysis server.", e);
			throw new IncorrectMessageContentException();
		}
	}

	private Request prepareDataRequest(final List<Map<String, Object>> resources,
			final ClusteringFeatures features, final ClusteringMethod methods, final ClusteringParameters parameters,
			final String endpoint) {
		final ClusteringConfiguration configuration = mapToClusteringConfig(features, methods, parameters);
		final ClusteringRequest clusteringRequest = mapToClusteringRequest(resources, configuration);

		final String url = join("/", List.of(address, endpoint));
		final RequestBody requestBody = constructRequestBodyForConfiguration(clusteringRequest);

		return new Request.Builder().url(url).post(requireNonNull(requestBody)).build();
	}

	private void validateForIdentifier(final List<Map<String, Object>> resources) {
		if (resources.stream().anyMatch(resource -> !resource.containsKey(ID))) {
			logger.warn("All resources must contain their individual identifiers!");
			throw new InvalidPropertiesException("Missing identifiers");
		}
	}

	private RequestBody constructRequestBodyForConfiguration(final ClusteringRequest clusteringRequest) {
		try {
			final String body = getMapper().writeValueAsString(clusteringRequest);
			return RequestBody.create(body, MediaType.get("application/json"));
		} catch (final JsonProcessingException e) {
			logger.error("Could not map to request body", e);
		}
		return null;
	}

	private Properties loadProperties() {
		final Properties properties = new Properties();

		try (final InputStream res = getClass().getClassLoader().getResourceAsStream("api.properties")) {
			properties.load(res);
		} catch (FileNotFoundException fileNotFoundException) {
			logger.error("Could not find the properties file", fileNotFoundException);
		} catch (Exception exception) {
			logger.error("Could not load properties file {}", exception.toString());
		}

		return properties;
	}
}
