package org.google.web.common;

import static org.jbehave.core.io.CodeLocations.codeLocationFromClass;
import static org.jbehave.core.reporters.Format.CONSOLE;
import static org.jbehave.core.reporters.Format.HTML;

import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.SilentStepMonitor;
import org.jbehave.core.steps.spring.SpringApplicationContextFactory;
import org.jbehave.core.steps.spring.SpringStepsFactory;
import org.jbehave.web.selenium.ContextView;
import org.jbehave.web.selenium.LocalFrameContextView;
import org.jbehave.web.selenium.SeleniumConfiguration;
import org.jbehave.web.selenium.SeleniumContext;
import org.jbehave.web.selenium.SeleniumContextOutput;
import org.jbehave.web.selenium.SeleniumStepMonitor;
import org.jbehave.web.selenium.WebDriverProvider;
import org.springframework.context.ApplicationContext;

public class GoogleWebStories extends JUnitStories {

    private WebDriverProvider driverProvider = new SeleniumWebDriverProvider();
    private Configuration configuration;
    private static ContextView contextView = new LocalFrameContextView().sized(640,120);
    private static SeleniumContext seleniumContext = new SeleniumContext();
	
    public GoogleWebStories() {
        Embedder embedder = configuredEmbedder();
        embedder.embedderControls().doGenerateViewAfterStories(true).doIgnoreFailureInStories(true)
                .doIgnoreFailureInView(true).doVerboseFiltering(true).useThreads(1).useStoryTimeoutInSecs(1);
    }
    
    

    @Override
    public Configuration configuration() {
        configuration = makeConfiguration(this.getClass(), driverProvider);
        return configuration;
    }

    public static Configuration makeConfiguration(Class<?> embeddableClass, WebDriverProvider driverProvider) {

        return new SeleniumConfiguration()
            .useWebDriverProvider(driverProvider)
            .useSeleniumContext(seleniumContext)
            .useFailureStrategy(new FailingUponPendingStep())
            .useStepMonitor(new SeleniumStepMonitor(contextView, new SeleniumContext(), new SilentStepMonitor()))
            .useStoryLoader(new LoadFromClasspath(embeddableClass.getClassLoader()))
            .useStoryReporterBuilder(
                new StoryReporterBuilder()
                    .withCodeLocation(CodeLocations.codeLocationFromClass(embeddableClass))
                    .withDefaultFormats()
                    .withFormats(new SeleniumContextOutput(seleniumContext), CONSOLE, HTML));
    }

	@Override
	public InjectableStepsFactory stepsFactory() {
		ApplicationContext context = new SpringApplicationContextFactory(
				"org/google/web/google-webacceptancetest.xml")
				.createApplicationContext();
		return new SpringStepsFactory(configuration(), context);
	}

	@Override
	protected List<String> storyPaths() {
		return new StoryFinder().findPaths(
				codeLocationFromClass(this.getClass()), "**/*.story",
				"**/excluded*.story");
	}
}