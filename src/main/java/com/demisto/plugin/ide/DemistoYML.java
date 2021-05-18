package com.demisto.plugin.ide;

import java.util.ArrayList;
import java.util.UUID;

import static com.demisto.plugin.ide.DemistoUtils.*;

/**
 * This is the base object of Demisto YML
 */
public class DemistoYML {
    protected DemistoCommonFields commonfields;
    protected String name;

    public DemistoCommonFields getCommonfields() {
        return commonfields;
    }

    public void setCommonfields(DemistoCommonFields commonfields) {
        this.commonfields = commonfields;
    }

    public static String getRandomID() {
        return UUID.randomUUID().toString();
    }


    public static class DemistoCommonFields {
        public DemistoCommonFields(String id, Integer version) {
            setId(id);
            setVersion(version);
        }

        protected String id;
        protected Integer version;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = -1;
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (stringIsNotEmptyOrNull(name)) {
            this.name = name;
        } else {
            this.name = "";
        }
    }


    public static class DemistoArgument {
        private String name;
        private String description;
        private Boolean required;
        private Boolean Default;
        private Boolean secret;
        private Boolean isArray;
        private String defaultValue;
        private ArrayList predefined;
        private String auto;

        public DemistoArgument(String name, String description, Boolean required, Boolean isDefault, Boolean secret,
                               Boolean isArray, String defaultValue, ArrayList predefined, String auto) {
            setName(name);
            setDescription(description);
            setRequired(required);
            setDefault(isDefault);
            setSecret(secret);
            setIsArray(isArray);
            setDefaultValue(defaultValue);
            setPredefined(predefined);
            setAuto(auto);
        }

        public DemistoArgument() {
            setName("");
            setDescription("");
            setRequired(false);
            setDefault(false);
            setSecret(false);
            setIsArray(false);
            setDefaultValue("");
            setPredefined(new ArrayList());
            setAuto("");
        }

        public String getAuto() {
            return auto;
        }

        public void setAuto(String auto) {
            this.auto = validateStringParameter(auto);
        }

        public Boolean getSecret() {
            return secret;
        }

        public void setSecret(Boolean secret) {
            if (booleanIsNotEmptyOrNull(secret)) {
                this.secret = secret;
            } else {
                this.secret = false;
            }
        }

        public Boolean getDefault() {
            return Default;
        }

        public void setDefault(Boolean isDefault) {
            if (booleanIsNotEmptyOrNull(isDefault)) {
                this.Default = isDefault;
            } else {
                this.Default = false;
            }
        }

        public Boolean getIsArray() {
            return isArray;
        }

        public void setIsArray(Boolean isArray) {
            if (booleanIsNotEmptyOrNull(isArray)) {
                this.isArray = isArray;
            } else {
                this.isArray = false;
            }
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            if (stringIsNotEmptyOrNull(defaultValue)) {
                this.defaultValue = defaultValue;
            } else {
                this.defaultValue = "";
            }
        }

        public ArrayList getPredefined() {
            return predefined;
        }

        public void setPredefined(ArrayList predefined) {
            if (predefined != null) {
                this.predefined = predefined;
            } else {
                this.predefined = new ArrayList();
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            if (stringIsNotEmptyOrNull(name)) {
                this.name = name;
            } else {
                this.name = "";
            }
        }

        public Boolean getRequired() {
            return required;
        }

        public void setRequired(Boolean required) {
            if (booleanIsNotEmptyOrNull(required)) {
                this.required = required;
            } else {
                this.required = false;
            }
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            if (stringIsNotEmptyOrNull(description)) {
                this.description = description;
            } else {
                this.description = "";
            }
        }
    }

    public class DemistoOutput {
        private String contextPath;
        private String description;
        private String type;

        public DemistoOutput(String contextPath, String description, String type) {
            setContextPath(contextPath);
            setDescription(description);
            setType(type);
        }

        public DemistoOutput() {
            setContextPath("");
            setDescription("");
            setType("");
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            if (stringIsNotEmptyOrNull(type)) {
                this.type = type;
            } else {
                this.type = "Unknown";
            }
        }

        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            if (stringIsNotEmptyOrNull(contextPath)) {
                this.contextPath = contextPath;
            } else {
                this.contextPath = "";
            }
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            if (stringIsNotEmptyOrNull(description)) {
                this.description = description;
            } else {
                this.description = "";
            }
        }
    }
}
