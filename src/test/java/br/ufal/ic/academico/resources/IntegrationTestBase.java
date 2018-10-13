package br.ufal.ic.academico.resources;

import br.ufal.ic.academico.AcademicoApp;
import br.ufal.ic.academico.ConfigApp;
import ch.qos.logback.classic.Level;
import com.github.javafaker.Faker;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
abstract class IntegrationTestBase {
    static {
        BootstrapLogging.bootstrap(Level.DEBUG);
    }

    DropwizardAppExtension<ConfigApp> RULE = new DropwizardAppExtension(AcademicoApp.class,
            ResourceHelpers.resourceFilePath("config-test.yml"));

    String url;
    Faker faker = new Faker();
    BasicBackground background;

    @BeforeEach
    void setup() {
        this.url = "http://localhost:" + RULE.getLocalPort() + "/academicotest/";
        background = new BasicBackground(url);
    }

}
