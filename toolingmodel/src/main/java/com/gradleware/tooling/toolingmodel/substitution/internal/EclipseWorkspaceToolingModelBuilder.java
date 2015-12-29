package com.gradleware.tooling.toolingmodel.substitution.internal;

import com.gradleware.tooling.toolingmodel.substitution.EclipseWorkspace;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

public class EclipseWorkspaceToolingModelBuilder implements ToolingModelBuilder {

    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(EclipseWorkspace.class.getName());
    }

    @Override
    public Object buildAll(String modelName, Project project) {
        // call off to EclipseModelBuilder
        return null;
    }
}
