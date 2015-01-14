package com.gradleware.tooling.toolingmodel;

import com.google.common.base.Suppliers;
import com.google.common.reflect.TypeToken;
import com.gradleware.tooling.toolingmodel.generic.DefaultModelField;
import com.gradleware.tooling.toolingmodel.generic.ModelField;

import java.io.File;

/**
 * Enumerates the detailed information available on a Eclipse project.
 *
 * @see org.gradle.tooling.model.eclipse.EclipseProject
 */
public final class EclipseProjectFields {

    /**
     * The name of this project. Note that the name is not a unique identifier for the project.
     */
    public static final ModelField<String, EclipseProjectFields> NAME =
            new DefaultModelField<String, EclipseProjectFields>(TypeToken.of(String.class), TypeToken.of(EclipseProjectFields.class));

    /**
     * The description of this project, or {@code null} if it has no description.
     */
    public static final ModelField<String, EclipseProjectFields> DESCRIPTION =
            new DefaultModelField<String, EclipseProjectFields>(TypeToken.of(String.class), TypeToken.of(EclipseProjectFields.class));

    /**
     * The path of this project. The path can be used as a unique identifier for the project within a given build.
     */
    public static final ModelField<String, EclipseProjectFields> PATH =
            new DefaultModelField<String, EclipseProjectFields>(TypeToken.of(String.class), TypeToken.of(EclipseProjectFields.class));

    /**
     * The project directory of this project.
     */
    public static final ModelField<File, EclipseProjectFields> PROJECT_DIRECTORY =
            new DefaultModelField<File, EclipseProjectFields>(TypeToken.of(File.class), TypeToken.of(EclipseProjectFields.class), Suppliers.<File>ofInstance(null));

    private EclipseProjectFields() {
    }

}
