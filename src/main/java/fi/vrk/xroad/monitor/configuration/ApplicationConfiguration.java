/**
 * The MIT License
 * Copyright (c) 2017, Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fi.vrk.xroad.monitor.configuration;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import fi.vrk.xroad.monitor.extensions.SpringExtension;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Spring configuration for application
 */
@Configuration
@Lazy
@EnableElasticsearchRepositories(basePackages = "fi.vrk.xroad.monitor.repository")
@ComponentScan(basePackages = {
        "fi.vrk.xroad.monitor"})
public class ApplicationConfiguration {

    // The application context is needed to initialize the Akka Spring
    // Extension
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringExtension springExtension;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    public ActorSystem actorSystem() {
        ActorSystem system = ActorSystem.create("AkkaTaskProcessing", akkaConfiguration());
        return system;
    }

    /**
     * Read configuration from application.conf file
     */
    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }


    /**
     *
     * @return client
     */
    @Bean
    public Client client() {
        try {
            final Path tmpDir = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")),
                "elasticsearch_data");

            final Settings.Builder elasticsearchSettings =
                Settings.settingsBuilder().put("http.enabled", "false")
                    .put("path.data", tmpDir.toAbsolutePath().toString())
                    .put("path.home", "/home/ilkka");

            return new NodeBuilder()
                .local(true)
                .settings(elasticsearchSettings)
                .node()
                .client();

            // @formatter:on
        } catch (final IOException ioex) {
            throw new RuntimeException();
        }
    }

    /**
     *
     * @return operations
     */
    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(client());
    }
}
