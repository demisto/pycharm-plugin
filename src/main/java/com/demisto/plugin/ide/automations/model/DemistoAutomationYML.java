package com.demisto.plugin.ide.automations.model;

import com.demisto.plugin.ide.DemistoYML;
import org.apache.commons.lang.ArrayUtils;

import java.util.*;

import static com.demisto.plugin.ide.DemistoUtils.*;

/**
 *
 * @author  Shachar Hirshberg
 * @since November 22, 2018
 *
 * This is the object for Automation YML
 */
public class DemistoAutomationYML extends DemistoYML {
    private String type;
    private ArrayList tags;
    private String comment;
    private Boolean enabled;
    private Boolean system;
    private String timeout;
    private Integer scripttarget;
    private String script;
    private String subtype;

    private ArrayList<DemistoArgument> args;
    private ArrayList<DemistoOutput> outputs;

    private Map unsupportedFields = new HashMap();
    private String [] supportedFieldsArray = new String[] {"name", "comment", "tags", "type", "enabled", "system", "scripttarget",
                                                      "timeout", "script", "commonfields", "args", "outputs"};

    public DemistoAutomationYML(Map automationYML) {
        setName(String.valueOf(automationYML.get("name")));
        setComment(String.valueOf(automationYML.get("comment")));
        setTags((ArrayList) automationYML.get("tags"));
        setType(String.valueOf(automationYML.get("type")));
        setSubtype(String.valueOf(automationYML.get("subtype")));
        setEnabled(Boolean.valueOf(String.valueOf(automationYML.get("enabled"))));
        setSystem(Boolean.valueOf(String.valueOf(automationYML.get("system"))));
        setScripttarget(String.valueOf(automationYML.get("scripttarget")));
        setTimeout(String.valueOf(automationYML.get("timeout")));
        setScript(String.valueOf(automationYML.get("script")));

        // nested fields
        LinkedHashMap commonFieldsMap = (LinkedHashMap) automationYML.get("commonfields");
        DemistoCommonFields commonFields;
        if (commonFieldsMap != null && !commonFieldsMap.isEmpty()){
            String id = String.valueOf(commonFieldsMap.get("id"));
            int version;
            if (stringIsNotEmptyOrNull(String.valueOf(commonFieldsMap.get("version")))){
                version = Integer.valueOf(String.valueOf(commonFieldsMap.get("version")));
            } else {
                version = -1;
            }
            commonFields = new DemistoCommonFields(id,version);
        } else {
            commonFields = new DemistoCommonFields(UUID.randomUUID().toString(),-1);
        }
        setCommonfields(commonFields);

        ArrayList<DemistoArgument> argumentsList = new ArrayList();
        if (stringIsNotEmptyOrNull(String.valueOf(automationYML.get("args")))){
            ArrayList<LinkedHashMap> originalArgumentsList = (ArrayList<LinkedHashMap>) automationYML.get("args");
            originalArgumentsList.forEach(arg ->{
                DemistoArgument cur = new DemistoArgument(String.valueOf(arg.get("name")),String.valueOf(arg.get("description")),
                        Boolean.valueOf(String.valueOf(arg.get("required"))), Boolean.valueOf(String.valueOf(arg.get("default"))),
                        Boolean.valueOf(String.valueOf(arg.get("secret"))), Boolean.valueOf(String.valueOf(arg.get("isArray"))),
                        String.valueOf(arg.get("defaultValue")), (ArrayList) arg.get("predefined"), String.valueOf(arg.get("auto")));
                argumentsList.add(cur);
            });
        }
        setArgs(argumentsList);

        ArrayList<DemistoOutput> outputsList = new ArrayList();
        if (stringIsNotEmptyOrNull(String.valueOf(automationYML.get("outputs")))){
            ArrayList<LinkedHashMap> originalArgumentsList = (ArrayList<LinkedHashMap>) automationYML.get("outputs");
            originalArgumentsList.forEach(output ->{
                DemistoOutput cur = new DemistoOutput(String.valueOf(output.get("contextPath")),String.valueOf(output.get("description")), String.valueOf(output.get("type")));
                outputsList.add(cur);
            });
        }
        setOutputs(outputsList);
        automationYML.forEach((k,v) -> {
            if (!ArrayUtils.contains(supportedFieldsArray, k)){
                unsupportedFields.put(k,v);
            }
        });
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

    public ArrayList getTags() {
        return tags;
    }

    public void setTags(ArrayList tags) {
        if (tags != null){
            this.tags = tags;
        } else {
            this.tags = new ArrayList();
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        if (stringIsNotEmptyOrNull(comment)){
            this.comment = comment;
        } else {
            this.comment = "";
        }
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        if (enabled != null){
            this.enabled = enabled;
        } else {
            this.enabled = true;
        }
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        if (system != null){
            this.system = system;
        } else {
            this.system = false;
        }
    }
    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = validateStringParameter(script);
    }

    public ArrayList<DemistoArgument> getArgs() {
        return this.args;
    }

    public void setArgs(ArrayList<DemistoArgument> args) {
        this.args = args;
    }

    public DemistoArgument addEmptyArg() {
        DemistoArgument arg = new DemistoArgument();
        this.getArgs().add(arg);
        return arg;
    }

    public DemistoOutput addEmptyOutput() {
        DemistoOutput output = new DemistoOutput();
        this.outputs.add(output);
        return output;
    }

    public void removeArgument(int index) {
        if (index != -1) {
            this.getArgs().remove(index);
        }
    }

    public int findArgumentInArray(String name){
        for (int i=0; i<this.args.size(); i++){
            if (this.getArgs().get(i).getName().equals(name)) {
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

    public int findOutputInArray(String contextPath){
        for (int i=0; i<this.getOutputs().size(); i++){
            if (this.getOutputs().get(i).getContextPath().equals(contextPath)) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<DemistoOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(ArrayList<DemistoOutput> outputs) {
        this.outputs = outputs;
    }

    public Integer getScripttarget() {
        return scripttarget;
    }

    public void setScripttarget(String scripttarget) {
        if (stringIsNotEmptyOrNull(scripttarget)){
            this.scripttarget = Integer.valueOf(scripttarget);
        } else {
            this.scripttarget = 0;
        }
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        if (stringIsNotEmptyOrNull(timeout)){
            this.timeout = timeout;
        } else {
            this.timeout = "0";
        }
    }

    public void validateArguments() {
        // missing a name
        this.args.removeIf(arg -> !stringIsNotEmptyOrNull(arg.getName()));
    }

    public void validateOutputs() {
        // missing a context path
        this.outputs.removeIf(output -> !stringIsNotEmptyOrNull(output.getContextPath()));
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

    public Map getDemistoYMLMapWithUnsupportedFields(DemistoAutomationYML demistoObject) {
        // this function gets a Demisto Automation object and returns a string representation of its yml including the fields the plugin doesn't support

        // build a map of current automationyml
        Map demistoMap = writeDemistoObjectToMap(demistoObject);
        // add unsupported fields to the map
        demistoObject.getUnsupportedFields().forEach((k,v) -> demistoMap.put(k,v));

        // remove redundant keys
        demistoMap.remove("unsupportedFields");
        demistoMap.remove("supportedFieldsArray");

        return demistoMap;
    }
}
