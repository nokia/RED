package org.robotframework.ide.eclipse.main.plugin.project.library;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "keywordspec")
public class LibrarySpecification {

    private String name;
    private String scope;
    private String format;
    private String version;
    private String documentation;

    private List<KeywordSpecification> keywords;
    private boolean isRemote;
    private boolean isReferenced;
    private String additionalInfo = "";

    public String getName() {
        return name;
    }

    @XmlAttribute
    public void setName(final String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    @XmlElement
    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getFormat() {
        return format;
    }

    @XmlAttribute
    public void setFormat(final String format) {
        this.format = format;
    }

    public String getVersion() {
        return version;
    }

    @XmlElement
    public void setVersion(final String version) {
        this.version = version;
    }

    public String getDocumentation() {
        return documentation;
    }

    @XmlElement(name = "doc")
    public void setDocumentation(final String documentation) {
        this.documentation = documentation;
    }

    public List<KeywordSpecification> getKeywords() {
        return keywords;
    }

    @XmlElement(name = "kw")
    public void setKeywords(final List<KeywordSpecification> keywords) {
        this.keywords = keywords;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote() {
        isRemote = true;
    }
    
    public boolean isReferenced() {
        return isReferenced;
    }

    public void setReferenced() {
        isReferenced = true;
    }

    public void propagateFormat() {
        for (final KeywordSpecification kwSpec : keywords) {
            kwSpec.setFormat(format);
        }
    }

    public String getAdditionalInformation() {
        return additionalInfo;
    }

    public void setAdditionalInformation(final String info) {
        this.additionalInfo = info;
    }

    public boolean isAccessibleWithoutImport() {
        return Arrays.asList("BuiltIn", "Easter", "Reserved").contains(name);
    }

    public boolean canBeConvertedToHtml() {
        return "ROBOT".equals(format);
    }

    public String getDocumentationAsHtml() {
        if ("ROBOT".equals(format)) {
            return new RobotToHtmlConverter().convert(documentation);
        }
        throw new IllegalArgumentException("Only ROBOT format can be converted to HTML");
    }

    public KeywordSpecification getKeywordSpecification(final String keywordName) {
        if (keywords == null) {
            return null;
        }
        for (final KeywordSpecification keywordSpec : keywords) {
            if (keywordSpec.getName().equals(keywordName)) {
                return keywordSpec;
            }
        }
        return null;
    }
}
