package com.tycho.mss.layout;

import javafx.stage.FileChooser;

import java.nio.file.Files;
import java.nio.file.Path;

public class JarInputField extends FileInputLayout {

    public JarInputField(){
        super();
        setValidator(new PathValidator() {
            @Override
            protected boolean isPathValid(Path path, StringBuilder invalidReason) {
                if (!Files.exists(path) || Files.isDirectory(path)) {
                    if (invalidReason != null) invalidReason.append("File does not exist!");
                    return false;
                }

                if (!path.getFileName().toString().toLowerCase().endsWith("jar")) {
                    if (invalidReason != null) invalidReason.append("Not a valid JAR file!");
                    return false;
                }
                return true;
            }
        });
        addExtensionFilter(new FileChooser.ExtensionFilter("JAR file", "*.jar"));
    }
}
