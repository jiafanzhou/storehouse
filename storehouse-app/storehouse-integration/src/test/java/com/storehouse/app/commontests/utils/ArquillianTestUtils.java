package com.storehouse.app.commontests.utils;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;

@Ignore
public class ArquillianTestUtils {
    /**
     * ShrinkWrap is used to create the WebArchive file.
     *
     * beans.xml file (an empty marker file) is required for CDI to work.
     *
     * @return
     */
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addPackages(true, "com.storehouse.app")
                .addAsResource("persistence-integration.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("jboss-web.xml")
                .setWebXML(new File("src/test/resources/web.xml"))
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve("com.google.code.gson:gson",
                        "org.mockito:mockito-core").withTransitivity().asFile());
    }
}
