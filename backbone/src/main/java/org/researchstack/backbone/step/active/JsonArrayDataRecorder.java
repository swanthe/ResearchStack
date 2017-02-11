package org.researchstack.backbone.step.active;

import com.google.gson.JsonObject;

import org.researchstack.backbone.result.FileResult;
import org.researchstack.backbone.result.logger.DataLogger;
import org.researchstack.backbone.step.Step;

import java.io.File;

/**
 * Created by TheMDP on 2/7/17.
 *
 * The JsonArrayDataRecorder class is set up to be able to save a JsonArray to a DataLogger file
 * It coordinates the file header and footer of "[" and "]" and injects a separator ","
 * in between individual json object writes, so that the format of the file is correct
 */

abstract class JsonArrayDataRecorder extends Recorder {

    public static final String JSON_MIME_CONTENT_TYPE = "application/json";
    public static final String JSON_FILE_SUFFIX = ".json";
    public static final String JSON_OBJECT_SEPARATOR = ",";

    protected boolean isFirstJsonObject;

    protected DataLogger dataLogger;
    protected File dataLoggerFile;

    /** Default constructor for serialization/deserialization */
    JsonArrayDataRecorder() {
        super();
    }

    JsonArrayDataRecorder(String identifier, Step step, File outputDirectory) {
        super(identifier, step, outputDirectory);
    }

    protected void startJsonDataLogging(double frequency) {
        if (dataLoggerFile == null) {
            dataLoggerFile = new File(getOutputDirectory(), uniqueFilename + JSON_FILE_SUFFIX);
            dataLogger = new DataLogger(dataLoggerFile, new DataLogger.DataWriteListener() {
                @Override
                public void onWriteError(Throwable throwable) {
                    getRecorderListener().onFail(JsonArrayDataRecorder.this, throwable);
                }

                @Override
                public void onWriteComplete(File file) {
                    FileResult fileResult = new FileResult(getIdentifier(), dataLoggerFile, JSON_MIME_CONTENT_TYPE);
                    getRecorderListener().onComplete(JsonArrayDataRecorder.this, fileResult);
                }
            });
        }

        setRecording(true);

        // Since we are writing a JsonArray, have the header and footer be
        dataLogger.start("[", "]", frequency);
        isFirstJsonObject = true; // will avoid comma separator on write object write
    }

    protected void stopJsonDataLogging() {
        dataLogger.stop();
        setRecording(false);
    }

    protected void writeJsonObjectToFile(JsonObject jsonObject) {
        // append optional comma for array separation
        String jsonString = (!isFirstJsonObject ? JSON_OBJECT_SEPARATOR : "") + jsonObject.toString();
        dataLogger.appendData(jsonString);
        isFirstJsonObject = false;
    }
}
