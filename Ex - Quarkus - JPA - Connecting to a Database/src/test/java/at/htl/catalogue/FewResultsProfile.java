package at.htl.catalogue;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class FewResultsProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("catalogue.max-results", "2");
    }
}
