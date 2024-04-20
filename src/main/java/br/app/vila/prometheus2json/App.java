package br.app.vila.prometheus2json;

import br.app.vila.prometheus2json.constants.Constants;
import br.app.vila.prometheus2json.core.JobTask;
import br.app.vila.prometheus2json.helpers.Job;
import br.app.vila.prometheus2json.helpers.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;

/**
 * Main app class.
 *
 * @author fvilarin
 */
public class App {
    private static final Logger logger = LogManager.getLogger(App.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private Settings settings = null;

    /**
     * Default constructor.
     */
    public App() {
        super();

        logger.info("Starting...");
    }

    /**
     * Load the default settings.
     */
    private void loadSettings() {
        this.loadSettings(null);
    }

    /**
     * Load the settings.
     *
     * @param settingsFilename String that contains the settings filename.
     */
    private void loadSettings(String settingsFilename) {
        logger.info("Loading settings...");

        try {
            if(settingsFilename == null || settingsFilename.isEmpty())
                this.settings = mapper.readValue(getClass().getClassLoader().getResourceAsStream(Constants.DEFAULT_SETTINGS_FILENAME), Settings.class);
            else
                this.settings = mapper.readValue(new FileInputStream(settingsFilename), Settings.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Starts the app.
     */
    public void run() {
        if (this.settings != null) {
            logger.info("Scheduling jobs...");

            List<br.app.vila.prometheus2json.helpers.Job> jobs = this.settings.getJobs();

            if (jobs != null && !jobs.isEmpty()) {
                Timer timer = new Timer();

                for (Job job : jobs) {
                    timer.scheduleAtFixedRate(new JobTask(job), 0, job.getInterval() * 1000L);

                    logger.info("Job {} scheduled in an interval of {} seconds!", job.getId(), job.getInterval());
                }
            } else
                logger.info("No jobs to schedule!");
        }
        else
            logger.info("No settings found!");
    }

    /**
     * Run the app.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        App app = new App();

        if(args.length > 0)
            app.loadSettings(args[0]);
        else
            app.loadSettings();

        app.run();
    }
}