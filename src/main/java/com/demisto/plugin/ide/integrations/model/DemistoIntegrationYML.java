package com.demisto.plugin.ide.integrations.model;

import com.demisto.plugin.ide.DemistoYML;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.demisto.plugin.ide.DemistoUtils.*;

/**
 * @author Shachar Hirshberg
 * @since  December 30, 2018
 * <p>
 * This is the object for an Integration YML
 */
public class DemistoIntegrationYML extends DemistoYML {
    private String display;
    private String description;
    private String detaileddescription;
    private String category;
    private ArrayList<DemistoParameter> configuration;
    private IntegrationScript script;
    private String defaultclassifier;
    private String defaultmapperin;
    private String defaultmapperout;
    private Boolean beta;

    private Map unsupportedFields = new HashMap();
    private String[] supportedFieldsArray = new String[]{
            "name",
            "commonfields",
            "display",
            "description",
            "detaileddescription",
            "category",
            "configuration",
            "script",
            "beta"
    };

    public DemistoIntegrationYML(Map integrationYML) {
        LOG.info("Creating new integration YML. Input YML is: " + integrationYML.toString());
        setName(String.valueOf(integrationYML.get("name")));
        setDisplay(String.valueOf(integrationYML.get("display")));
        setCategory(String.valueOf(integrationYML.get("category")));
        setDescription(String.valueOf(integrationYML.get("description")));
        setDetaileddescription(
                String.valueOf(integrationYML.get("detaileddescription")));
        setDefaultclassifier(
                getValueOfStringOrNull(integrationYML.get("defaultclassifier")));
        setDefaultmapperin(
                getValueOfStringOrNull(integrationYML.get("defaultmapperin")));
        setDefaultmapperout(
                getValueOfStringOrNull(integrationYML.get("defaultmapperout")));
        setBeta(Boolean.valueOf(getValueOfStringOrNull(integrationYML.get("beta"))));
        // common fields
        LinkedHashMap commonFieldsMap = (LinkedHashMap) integrationYML.get("commonfields");
        DemistoCommonFields commonFields;
        if (commonFieldsMap != null && !commonFieldsMap.isEmpty()) {
            String id = String.valueOf(commonFieldsMap.get("id"));
            int version;
            if (stringIsNotEmptyOrNull(String.valueOf(commonFieldsMap.get("version")))) {
                version = Integer.valueOf(String.valueOf(commonFieldsMap.get("version")));
            } else {
                version = -1;
            }
            commonFields = new DemistoCommonFields(id, version);
        } else { // brand new integration, setting random uuid
            commonFields = new DemistoCommonFields(getRandomID(), -1);
        }
        setCommonfields(commonFields);

        // configuration fields
        ArrayList<DemistoParameter> paramsList = new ArrayList();
        if (stringIsNotEmptyOrNull(String.valueOf(integrationYML.get("configuration")))) {
            ArrayList<LinkedHashMap> originalParamsList = (ArrayList<LinkedHashMap>) integrationYML.get("configuration");
            originalParamsList.forEach(param -> {
                int type;
                if (stringIsNotEmptyOrNull(String.valueOf(param.get("type")))) {
                    type = Integer.valueOf(String.valueOf(param.get("type")));
                } else {
                    type = 0; // short text
                }
                DemistoParameter currentParam = new DemistoParameter(
                        String.valueOf(param.get("display")),
                        String.valueOf(param.get("name")),
                        String.valueOf(param.get("defaultvalue")),
                        type,
                        (Boolean) param.get("required"),
                        (ArrayList) param.get("options"),
                        String.valueOf(param.get("additionalinfo")),
                        (Boolean) param.get("hidden"));
                paramsList.add(currentParam);
            });
        }
        setConfiguration(paramsList);

        // script fields
        LinkedHashMap integrationScriptMap = (LinkedHashMap) integrationYML.get("script");

        IntegrationScript integrationScript = new IntegrationScript();
        if (integrationScriptMap != null && integrationScriptMap.size() > 0) {
            integrationScript.setScript(String.valueOf(integrationScriptMap.get("script")));
            integrationScript.setType(String.valueOf(integrationScriptMap.get("type")));
            integrationScript.setRunonce((Boolean) integrationScriptMap.get("runonce"));
            integrationScript.setDockerimage(String.valueOf(integrationScriptMap.get("dockerimage")));
            integrationScript.setIsfetch((Boolean) integrationScriptMap.get("isfetch"));
            integrationScript.setLongRunning((Boolean) integrationScriptMap.get("longRunning"));
            integrationScript.setFeed((Boolean) integrationScriptMap.get("feed"));
            integrationScript.setLongRunningPort((Boolean) integrationScriptMap.get("longRunningPort"));
            integrationScript.setSubtype(String.valueOf(integrationScriptMap.get("subtype")));
            integrationScript.setIsFetchSamples((Boolean) integrationScriptMap.get("isFetchSamples"));
            integrationScript.setIsmappable((Boolean) integrationScriptMap.get("ismappable"));
            integrationScript.setIsremotesyncin((Boolean) integrationScriptMap.get("isremotesyncin"));
            integrationScript.setIsremotesyncout((Boolean) integrationScriptMap.get("isremotesyncout"));
        }

        ArrayList<DemistoCommand> commands = new ArrayList();
        if (integrationScriptMap != null && stringIsNotEmptyOrNull(String.valueOf(integrationScriptMap.get("commands")))) {
            ArrayList<LinkedHashMap> originalcommandsList = (ArrayList<LinkedHashMap>) integrationScriptMap.get("commands");
            originalcommandsList.forEach(command -> {
                DemistoCommand currentCommand = new DemistoCommand();
                currentCommand.setName(String.valueOf(command.get("name")));
                currentCommand.setDescription(String.valueOf(command.get("description")));
                currentCommand.setExecution((Boolean) (command.get("execution")));
                currentCommand.setDeprecated((Boolean) (command.get("deprecated")));
                currentCommand.setHidden((Boolean) (command.get("hidden")));

                // get arguments list
                ArrayList<DemistoArgument> args = getArgumentsListFromYML((ArrayList<LinkedHashMap>) command.get("arguments"));
                currentCommand.setArguments(args);
                // get outputs list
                ArrayList outputs = getOutputsListFromYML((ArrayList<LinkedHashMap>) command.get("outputs"));
                currentCommand.setOutputs(outputs);
                commands.add(currentCommand);
            });
        }
        integrationScript.setCommands(commands);

        setScript(integrationScript);

        integrationYML.forEach((k, v) -> {
            if (!ArrayUtils.contains(supportedFieldsArray, k)) {
                unsupportedFields.put(k, v);
            }
        });
    }

    public ArrayList<DemistoArgument> getArgumentsListFromYML(ArrayList<LinkedHashMap> arguments) {
        ArrayList<DemistoArgument> argumentsList = new ArrayList();
        if (arguments != null) {
            arguments.forEach(arg -> {
                DemistoArgument cur = new DemistoArgument(String.valueOf(arg.get("name")), String.valueOf(arg.get("description")),
                        Boolean.valueOf(String.valueOf(arg.get("required"))), Boolean.valueOf(String.valueOf(arg.get("default"))),
                        Boolean.valueOf(String.valueOf(arg.get("secret"))), Boolean.valueOf(String.valueOf(arg.get("isArray"))),
                        String.valueOf(arg.get("defaultValue")), (ArrayList) arg.get("predefined"), String.valueOf(arg.get("auto")));
                argumentsList.add(cur);
            });
        }
        return argumentsList;
    }

    public ArrayList getOutputsListFromYML(ArrayList<LinkedHashMap> outputs) {
        ArrayList<DemistoOutput> outputsList = new ArrayList();
        if (outputs != null) {
            outputs.forEach(output -> {
                DemistoOutput cur = new DemistoOutput(String.valueOf(output.get("contextPath")), String.valueOf(output.get("description")), String.valueOf(output.get("type")));
                outputsList.add(cur);
            });
        }
        return outputsList;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = validateStringParameter(category);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = validateStringParameter(description);

    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = validateStringParameter(display);
    }

    public String getDetaileddescription() {
        return detaileddescription;
    }

    public void setDetaileddescription(String detaileddescription) {
        this.detaileddescription = validateStringParameter(detaileddescription);
    }

    public ArrayList<DemistoParameter> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ArrayList<DemistoParameter> configuration) {
        this.configuration = configuration;
    }

    public IntegrationScript getScript() {
        return script;
    }

    public void setScript(IntegrationScript script) {
        this.script = script;
    }

    public String getDefaultclassifier() { return defaultclassifier; }

    public void setDefaultclassifier(@Nullable String defaultclassifier) { this.defaultclassifier = defaultclassifier; }

    public String getDefaultmapperin() { return defaultmapperin; }

    public void setDefaultmapperin(@Nullable String defaultmapperin) {
        this.defaultmapperin = defaultmapperin;
    }

    public String getDefaultmapperout() { return defaultmapperout; }

    public void setDefaultmapperout(@Nullable String defaultmapperout) { this.defaultmapperout = defaultmapperout; }

    public DemistoParameter addEmptyParameter() {
        DemistoParameter param = new DemistoParameter();
        this.getConfiguration().add(param);
        return param;
    }

    public int findParamInArray(String name) {
        for (int i = 0; i < this.getConfiguration().size(); i++) {
            if (this.getConfiguration().get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void removeParam(int index) {
        if (index != -1) {
            this.getConfiguration().remove(index);
        }
    }

    public void validateParams() {
        // missing a name
        this.getConfiguration().removeIf(param -> !stringIsNotEmptyOrNull(param.getName()));
    }

    public Map getUnsupportedFields() {
        return unsupportedFields;
    }

    public void setUnsupportedFields(Map unsupportedFields) {
        this.unsupportedFields = unsupportedFields;
    }

    public String[] getSupportedFieldsArray() {
        return supportedFieldsArray;
    }

    public void setSupportedFieldsArray(String[] supportedFieldsArray) {
        this.supportedFieldsArray = supportedFieldsArray;
    }

    public void validateYMLCommands() {
        // missing a name
        this.getScript().getCommands().forEach(command -> {
            command.validateArguments();
            command.validateOutputs();
        });
    }

    public Map getDemistoYMLMapWithUnsupportedFields(DemistoIntegrationYML demistoObject) {
        // this function gets a Demisto Automation object and returns a string representation of its yml including the fields the plugin doesn't support

        // build a map of current automationyml
        Map demistoMap = writeDemistoObjectToMap(demistoObject);
        // add unsupported fields to the map
        demistoObject.getUnsupportedFields().forEach((k, v) -> demistoMap.put(k, v));

        // remove redundant keys
        demistoMap.remove("unsupportedFields");
        demistoMap.remove("supportedFieldsArray");

        return demistoMap;
    }

    public Boolean getBeta() {
        return beta;
    }

    public void setBeta(Boolean beta) {
        this.beta = beta;
    }


    public class DemistoParameter {
        private String display;
        private String name;
        private String defaultvalue;
        private int type;
        private Boolean required;
        private ArrayList options;
        private String additionalinfo;
        private Boolean hidden;

        public DemistoParameter(String display, String name, String defaultvalue, int type, Boolean required, ArrayList options, String additionalinfo, Boolean hidden) {
            setDisplay(display);
            setName(name);
            setDefaultvalue(defaultvalue);
            setType(type);
            setRequired(required);
            setOptions(options);
            setadditionalinfo(additionalinfo);
            setHidden(hidden);
        }

        public DemistoParameter() {
            setDisplay("");
            setName("");
            setDefaultvalue("");
            setType(0);
            setRequired(false);
            setOptions(null);
            setadditionalinfo("");
            setHidden(false);
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            // type 0 is short text
            if (type != 0) {
                this.type = type;
            } else {
                this.type = 0;
            }
        }

        public ArrayList getOptions() {
            return options;
        }

        public void setOptions(ArrayList options) {
            if (options != null) {
                this.options = options;
            } else {
                this.options = new ArrayList();
            }
        }

        public String getadditionalinfo() {
            return this.additionalinfo;
        }

        public void setadditionalinfo(String tooltip) {
            this.additionalinfo = validateStringParameter(tooltip);
        }

        public Boolean getHidden() {
            return this.hidden;
        }

        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = validateStringParameter(display);
        }

        public String getDefaultvalue() {
            return defaultvalue;
        }

        public void setDefaultvalue(String defaultvalue) {
            this.defaultvalue = validateStringParameter(defaultvalue);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = validateStringParameter(name);
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            this.required = validateBooleanParameter(required);
        }
    }

    public class IntegrationScript {
        private String type;
        private String script;
        private ArrayList<DemistoCommand> commands;
        private Boolean runonce;
        private String dockerimage;
        private Boolean isfetch;
        private Boolean longRunning;
        private Boolean longRunningPort;
        private Boolean feed;
        private String subtype;
        private Boolean isFetchSamples;
        private Boolean ismappable;
        private Boolean isremotesyncin;
        private Boolean isremotesyncout;

        public IntegrationScript() {
            setType("");
            setScript("");
            setCommands(new ArrayList<>());
            setRunonce(false);
            setDockerimage("");
            setIsfetch(false);
            setLongRunning(false);
            setFeed(false);
            setSubtype("python3");
        }

        public IntegrationScript(String type, String script, ArrayList<DemistoCommand> commands, Boolean runonce, String dockerimage, Boolean isfetch) {
            setType(type);
            setScript(script);
            setCommands(commands);
            setRunonce(runonce);
            setDockerimage(dockerimage);
            setIsfetch(isfetch);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            if (stringIsNotEmptyOrNull(type)) {
                this.type = type;
            } else {
                this.type = "python";
            }
        }

        public String getSubtype() {
            return this.subtype;
        }

        public void setSubtype(String subtype) {
            if (stringIsNotEmptyOrNull(subtype)) {
                this.subtype = subtype;
            } else {
                this.subtype = "python3";
            }
        }

        public String getScript() {
            return script;
        }

        public void setScript(String script) {
            this.script = validateStringParameter(script);
        }

        public Boolean getRunonce() {
            return runonce;
        }

        public void setRunonce(Boolean runonce) {
            this.runonce = validateBooleanParameter(runonce);
        }

        public String getDockerimage() {
            return dockerimage;
        }

        public void setDockerimage(String dockerimage) {
            this.dockerimage = validateStringParameter(dockerimage);
        }

        public Boolean getIsfetch() {
            return isfetch;
        }

        public void setIsfetch(Boolean isfetch) {
            this.isfetch = validateBooleanParameter(isfetch);
        }

        public Boolean getLongRunning() {
            return longRunning;
        }

        public void setLongRunning(Boolean longRunning) {
            this.longRunning = validateBooleanParameter(longRunning);
        }

        public Boolean getLongRunningPort() {
            return longRunningPort;
        }


        public void setLongRunningPort(Boolean longRunningPort) {
            this.longRunningPort = validateBooleanParameter(longRunningPort);
        }

        public Boolean getFeed() { return feed; }

        public void setFeed(Boolean feed) {
            this.feed = validateBooleanParameter(feed);
        }

        public Boolean getIsFetchSamples() { return isFetchSamples; }

        public void setIsFetchSamples(Boolean isFetchSamples) { this.isFetchSamples = isFetchSamples; }

        public ArrayList<DemistoCommand> getCommands() {
            return commands;
        }

        public void setCommands(ArrayList<DemistoCommand> commands) {
            this.commands = commands;
        }

        public DemistoCommand addEmptyCommand() {
            DemistoCommand command = new DemistoCommand();
            this.getCommands().add(command);
            return command;
        }

        public int findCommandInArray(String name) {
            for (int i = 0; i < this.getCommands().size(); i++) {
                if (this.getCommands().get(i).getName().equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        public void removeCommand(int index) {
            if (index != -1) {
                this.getCommands().remove(index);
            }
        }

        public Boolean getIsmappable() {
            return ismappable;
        }

        public void setIsmappable(Boolean ismappable) {
            this.ismappable = ismappable;
        }

        public Boolean getIsremotesyncin() {
            return isremotesyncin;
        }

        public void setIsremotesyncin(Boolean isremotesyncin) {
            this.isremotesyncin = isremotesyncin;
        }

        public Boolean getIsremotesyncout() {
            return isremotesyncout;
        }

        public void setIsremotesyncout(Boolean isremotesyncout) {
            this.isremotesyncout = isremotesyncout;
        }
    }

    public class DemistoCommand {
        private String name;
        private String description;
        private ArrayList<DemistoArgument> arguments;
        private ArrayList<DemistoOutput> outputs;
        private Boolean execution;
        private Boolean deprecated;
        private Boolean hidden;

        public DemistoCommand() {
            setName("");
            setDescription("");
            setArguments(new ArrayList<>());
            setOutputs(new ArrayList<>());
            setExecution(false);
            setDeprecated(false);
            setHidden(false);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = validateStringParameter(name);
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = validateStringParameter(description);
        }

        public ArrayList<DemistoArgument> getArguments() {
            return arguments;
        }

        public void setArguments(ArrayList<DemistoArgument> arguments) {
            this.arguments = arguments;
        }

        public ArrayList<DemistoOutput> getOutputs() {
            return outputs;
        }

        public void setOutputs(ArrayList<DemistoOutput> outputs) {
            this.outputs = outputs;
        }

        public Boolean getExecution() {
            return execution;
        }

        public void setExecution(Boolean execution) {
            this.execution = validateBooleanParameter(execution);
        }

        public Boolean getDeprecated() {
            return deprecated;
        }

        public void setDeprecated(Boolean deprecated) {
            this.deprecated = validateBooleanParameter(deprecated);
        }

        public DemistoArgument addEmptyArg() {
            DemistoArgument arg = new DemistoArgument();
            this.getArguments().add(arg);
            return arg;
        }

        public DemistoOutput addEmptyOutput() {
            DemistoOutput output = new DemistoOutput();
            this.outputs.add(output);
            return output;
        }

        public void removeArgument(int index) {
            if (index != -1) {
                this.getArguments().remove(index);
            }
        }

        public int findArgumentInArray(String name) {
            for (int i = 0; i < this.arguments.size(); i++) {
                if (this.getArguments().get(i).getName().equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        public void removeOutput(int index) {
            if (index != -1) {
                this.getOutputs().remove(index);
            }
        }

        public int findOutputInArray(String contextPath) {
            for (int i = 0; i < this.getOutputs().size(); i++) {
                if (this.getOutputs().get(i).getContextPath().equals(contextPath)) {
                    return i;
                }
            }
            return -1;
        }

        public void validateArguments() {
            // missing a name
            this.arguments.removeIf(arg -> !stringIsNotEmptyOrNull(arg.getName()));
        }

        public void validateOutputs() {
            // missing a context path
            this.outputs.removeIf(output -> !stringIsNotEmptyOrNull(output.getContextPath()));
        }

        public Boolean getHidden() {
            return hidden;
        }

        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }
    }
}